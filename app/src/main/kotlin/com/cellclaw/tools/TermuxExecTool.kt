package com.cellclaw.tools

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.*
import java.util.UUID
import javax.inject.Inject

/**
 * Executes an explicitly approved command inside the user's Termux environment.
 * Requires Termux >= 0.109, allow-external-apps=true, and the RUN_COMMAND permission
 * granted to this app in Android settings.
 */
class TermuxExecTool @Inject constructor(
    @ApplicationContext private val context: Context
) : Tool {
    override val name = "termux.exec"
    override val description = "Run an approved coding/build/test command inside Termux. Requires Termux setup and explicit user approval."
    override val parameters = ToolParameters(
        properties = mapOf(
            "command" to ParameterProperty("string", "Shell command to run inside Termux; keep it focused and explain destructive actions."),
            "workdir" to ParameterProperty("string", "Working directory inside Termux, e.g. ~/my-project (defaults to ~/)."),
            "timeout_seconds" to ParameterProperty("integer", "Timeout in seconds, 1-120; defaults to 60.")
        ),
        required = listOf("command")
    )
    override val requiresApproval = true

    override suspend fun execute(params: JsonObject): ToolResult {
        val command = params["command"]?.jsonPrimitive?.content
            ?: return ToolResult.error("Missing command")
        if (command.isBlank() || command.length > MAX_COMMAND_LENGTH) {
            return ToolResult.error("Command must be non-empty and no longer than $MAX_COMMAND_LENGTH characters")
        }
        val workdir = params["workdir"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() } ?: "~/"
        if (workdir.length > 500 || workdir.contains("\u0000")) {
            return ToolResult.error("Invalid workdir")
        }
        val timeoutSeconds = (params["timeout_seconds"]?.jsonPrimitive?.intOrNull ?: 60).coerceIn(1, 120)
        val id = UUID.randomUUID().toString()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val callbackIntent = Intent(context, TermuxCommandResultReceiver::class.java)
            .putExtra(TermuxCommandResultReceiver.EXTRA_EXECUTION_ID, id)
        val pendingFlags = PendingIntent.FLAG_ONE_SHOT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        val callback = PendingIntent.getBroadcast(context, id.hashCode(), callbackIntent, pendingFlags)

        val intent = Intent(ACTION_RUN_COMMAND).apply {
            setClassName(TERMUX_PACKAGE, TERMUX_SERVICE)
            putExtra(EXTRA_COMMAND_PATH, "$PREFIX/bin/bash")
            putExtra(EXTRA_ARGUMENTS, arrayOf("-lc", command))
            putExtra(EXTRA_WORKDIR, workdir)
            putExtra(EXTRA_BACKGROUND, true)
            putExtra(EXTRA_PENDING_INTENT, callback)
            putExtra(EXTRA_COMMAND_LABEL, "K1pL4y Agent command")
            putExtra(EXTRA_COMMAND_DESCRIPTION, "Command requested by the user through K1pL4y_Agent.pro")
        }

        return try {
            context.startService(intent)
            val result = withTimeout(timeoutSeconds * 1000L) {
                while (true) {
                    val raw = prefs.getString(RESULT_PREFIX + id, null)
                    if (raw != null) {
                        prefs.edit { remove(RESULT_PREFIX + id) }
                        return@withTimeout JSONObjectResult.parse(raw)
                    }
                    delay(250)
                }
                @Suppress("UNREACHABLE_CODE")
                ToolResult.error("No result")
            }
            result
        } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
            ToolResult.error("Termux command timed out after $timeoutSeconds seconds. Check the Termux session; the process may still be running.")
        } catch (e: Exception) {
            ToolResult.error("Could not start Termux. Install/update Termux, enable allow-external-apps, and grant this app the 'Run commands in Termux environment' permission. Details: ${e.javaClass.simpleName}")
        }
    }

    private object JSONObjectResult {
        fun parse(raw: String): ToolResult {
            return try {
                val obj = org.json.JSONObject(raw)
                val exitCode = obj.optInt("exit_code", -1)
                val stdout = obj.optString("stdout", "").take(MAX_OUTPUT_LENGTH)
                val stderr = obj.optString("stderr", "").take(MAX_OUTPUT_LENGTH)
                val internalError = obj.optString("error", "")
                if (internalError.isNotBlank()) ToolResult.error("Termux error: $internalError")
                else ToolResult.success(buildJsonObject {
                    put("exit_code", exitCode)
                    put("success", exitCode == 0)
                    put("stdout", stdout)
                    put("stderr", stderr)
                })
            } catch (_: Exception) {
                ToolResult.error("Termux returned an unreadable result")
            }
        }
    }

    companion object {
        const val PREFS_NAME = "termux_command_results"
        const val RESULT_PREFIX = "result_"
        const val MAX_COMMAND_LENGTH = 100_000
        const val MAX_OUTPUT_LENGTH = 10_000
        private const val ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"
        private const val TERMUX_PACKAGE = "com.termux"
        private const val TERMUX_SERVICE = "com.termux.app.RunCommandService"
        private const val PREFIX = "\$PREFIX"
        private const val EXTRA_COMMAND_PATH = "com.termux.RUN_COMMAND_PATH"
        private const val EXTRA_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS"
        private const val EXTRA_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR"
        private const val EXTRA_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND"
        private const val EXTRA_PENDING_INTENT = "com.termux.RUN_COMMAND_PENDING_INTENT"
        private const val EXTRA_COMMAND_LABEL = "com.termux.RUN_COMMAND_COMMAND_LABEL"
        private const val EXTRA_COMMAND_DESCRIPTION = "com.termux.RUN_COMMAND_COMMAND_DESCRIPTION"
    }
}
