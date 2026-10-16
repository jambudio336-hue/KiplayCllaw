package com.k1play.agentpro.approval

import android.content.Context
import android.content.SharedPreferences
import com.k1play.agentpro.agent.AutonomyPolicy
import com.k1play.agentpro.agent.ToolApprovalPolicy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists explicit per-tool "Always allow" choices. This is intentionally
 * scoped to the exact tool the user approved, never a global bypass for every
 * action. Unknown tools still require approval.
 */
@Singleton
class ApprovalPolicyManager @Inject constructor(
    private val autonomyPolicy: AutonomyPolicy,
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("k1play_approval_policy", Context.MODE_PRIVATE)

    init {
        restoreSavedPolicies()
    }

    @Synchronized
    fun restoreSavedPolicies() {
        val allowed = prefs.getStringSet(KEY_ALWAYS_ALLOWED_TOOLS, emptySet()).orEmpty().toSet()
        allowed.forEach { toolName ->
            if (toolName.isNotBlank()) autonomyPolicy.setPolicy(toolName, ToolApprovalPolicy.AUTO)
        }
    }

    @Synchronized
    fun handleResult(toolName: String, result: ApprovalResult) {
        when (result) {
            ApprovalResult.ALWAYS_ALLOW -> {
                autonomyPolicy.setPolicy(toolName, ToolApprovalPolicy.AUTO)
                val allowed = prefs.getStringSet(KEY_ALWAYS_ALLOWED_TOOLS, emptySet())
                    .orEmpty().toMutableSet()
                allowed.add(toolName)
                prefs.edit().putStringSet(KEY_ALWAYS_ALLOWED_TOOLS, allowed).apply()
            }
            ApprovalResult.APPROVED, ApprovalResult.DENIED -> Unit
        }
    }

    companion object {
        private const val KEY_ALWAYS_ALLOWED_TOOLS = "always_allowed_tools"
    }
}
