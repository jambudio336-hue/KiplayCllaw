package com.k1play.agentpro.agent

import javax.inject.Inject
import javax.inject.Singleton

enum class PermissionProfile(val displayName: String, val description: String) {
    FULL_AUTO(
        "Otomatis untuk operasi aman",
        "Operasi baca yang terdaftar berjalan otomatis; perubahan, pengiriman, dan eksekusi perintah meminta persetujuan."
    ),
    BALANCED(
        "Seimbang",
        "Operasi baca berjalan otomatis; tindakan lain meminta persetujuan."
    ),
    CAUTIOUS(
        "Hati-hati",
        "Hanya pembacaan dasar yang berjalan otomatis; tindakan lain meminta persetujuan."
    )
}

@Singleton
class AutonomyPolicy @Inject constructor() {

    private val policies = mutableMapOf<String, ToolApprovalPolicy>()

    private val allTools = listOf(
        "sms.read", "contacts.search", "calendar.query", "location.get",
        "clipboard.read", "file.read", "file.list", "settings.get",
        "sensor.read", "phone.log", "notification.send", "notification.listen",
        "browser.search", "browser.open", "screen.read", "screen.capture",
        "vision.analyze", "messaging.read",
        "app.launch", "app.automate", "messaging.open",
        "sms.send", "phone.call", "contacts.add", "calendar.create",
        "camera.snap", "camera.record", "clipboard.write", "file.write",
        "script.exec", "termux.exec", "email.send", "messaging.reply", "schedule.manage",
        "heartbeat.context", "memory.manage"
    )

    private val readOps = setOf(
        "sms.read", "contacts.search", "calendar.query", "location.get",
        "clipboard.read", "file.read", "file.list", "settings.get",
        "sensor.read", "phone.log", "notification.listen",
        "browser.search", "browser.open", "screen.read", "screen.capture",
        "vision.analyze", "messaging.read", "heartbeat.context"
    )

    private val basicReads = setOf(
        "contacts.search", "calendar.query", "clipboard.read",
        "file.read", "file.list", "settings.get", "sensor.read",
        "browser.search", "browser.open"
    )

    init {
        applyProfile(PermissionProfile.FULL_AUTO)
    }

    fun applyProfile(profile: PermissionProfile) {
        policies.clear()
        for (tool in allTools) {
            policies[tool] = when (profile) {
                PermissionProfile.FULL_AUTO ->
                    if (tool in readOps && tool != "memory.manage") ToolApprovalPolicy.AUTO
                    else ToolApprovalPolicy.ASK
                PermissionProfile.BALANCED ->
                    if (tool in readOps && tool != "memory.manage") ToolApprovalPolicy.AUTO
                    else ToolApprovalPolicy.ASK
                PermissionProfile.CAUTIOUS ->
                    if (tool in basicReads) ToolApprovalPolicy.AUTO
                    else ToolApprovalPolicy.ASK
            }
        }
    }

    /** Unknown or newly added tools must require approval until explicitly classified. */
    fun getPolicy(toolName: String): ToolApprovalPolicy =
        policies[toolName] ?: ToolApprovalPolicy.ASK

    fun setPolicy(toolName: String, policy: ToolApprovalPolicy) {
        policies[toolName] = policy
    }

    fun allPolicies(): Map<String, ToolApprovalPolicy> = policies.toMap()
}

enum class ToolApprovalPolicy {
    AUTO,
    ASK,
    DENY
}
