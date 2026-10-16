package com.k1play.agentpro.agent

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A small, on-device learning journal. It stores only tool names, outcome categories,
 * and counters—not user prompts, tool arguments, credentials, or raw error messages.
 *
 * This improves future planning by surfacing patterns; it does not modify executable
 * code or grant the agent new permissions.
 */
@Singleton
class SelfImprovementMemory @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Synchronized
    fun recordToolOutcome(toolName: String, success: Boolean, error: String? = null) {
        val safeName = toolName.take(64).ifBlank { "unknown_tool" }
        val category = if (success) "success" else categorize(error.orEmpty())
        val current = try {
            JSONArray(prefs.getString(KEY_EVENTS, "[]") ?: "[]")
        } catch (_: Exception) {
            JSONArray()
        }

        current.put(JSONObject().apply {
            put("tool", safeName)
            put("category", category)
            put("ok", success)
        })

        val trimmed = JSONArray()
        val start = (current.length() - MAX_EVENTS).coerceAtLeast(0)
        for (index in start until current.length()) {
            trimmed.put(current.getJSONObject(index))
        }

        val key = "tool_${safeName}_${if (success) "ok" else "fail"}"
        val nextCount = prefs.getInt(key, 0) + 1
        prefs.edit {
            putString(KEY_EVENTS, trimmed.toString())
            putInt(key, nextCount)
        }
    }

    @Synchronized
    fun systemPromptAddition(): String {
        val events = try {
            JSONArray(prefs.getString(KEY_EVENTS, "[]") ?: "[]")
        } catch (_: Exception) {
            JSONArray()
        }
        if (events.length() == 0) return ""

        val counts = linkedMapOf<String, Int>()
        val failures = linkedMapOf<String, Int>()
        for (index in 0 until events.length()) {
            val event = events.optJSONObject(index) ?: continue
            val tool = event.optString("tool", "unknown_tool")
            val category = event.optString("category", "other")
            counts[tool] = (counts[tool] ?: 0) + 1
            if (category != "success") failures["$tool:$category"] = (failures["$tool:$category"] ?: 0) + 1
        }

        val summary = buildString {
            appendLine("Local improvement journal (observed outcomes, not guaranteed facts):")
            appendLine("Recent tool usage: " + counts.entries.joinToString { "${it.key}=${it.value}" })
            if (failures.isNotEmpty()) {
                appendLine("Recent failure patterns: " + failures.entries.joinToString { "${it.key}=${it.value}" })
            }
            append("Use these patterns to plan more carefully, verify results, and choose a safe fallback after failures. Do not claim a task succeeded unless the tool result confirms it.")
        }
        return summary
    }

    @Synchronized
    fun clear() {
        prefs.edit { clear() }
    }

    private fun categorize(error: String): String {
        val value = error.lowercase()
        return when {
            "permission" in value || "denied" in value || "not allowed" in value -> "permission"
            "timeout" in value || "timed out" in value -> "timeout"
            "not found" in value || "no such file" in value -> "not_found"
            "network" in value || "connect" in value || "socket" in value -> "network"
            "invalid" in value || "malformed" in value || "argument" in value -> "invalid_input"
            else -> "other_error"
        }
    }

    companion object {
        private const val PREFS_NAME = "k1play_self_improvement"
        private const val KEY_EVENTS = "recent_tool_outcomes"
        private const val MAX_EVENTS = 40
    }
}
