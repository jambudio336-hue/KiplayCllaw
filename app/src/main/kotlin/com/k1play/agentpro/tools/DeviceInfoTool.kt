package com.k1play.agentpro.tools

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import com.k1play.agentpro.BuildConfig
import com.k1play.agentpro.service.K1pL4yAgentProAccessibility
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.*
import javax.inject.Inject

/**
 * Reports non-unique device and Android capability information so the agent can
 * adapt instructions to the user's handset. Does not collect IMEI, serial number,
 * account identifiers, passwords, OTPs, or other persistent device identifiers.
 */
class DeviceInfoTool @Inject constructor(
    @ApplicationContext private val context: Context
) : Tool {
    override val name = "device.info"
    override val description = """Inspect the current handset and Android environment: manufacturer, brand, model, Android version/API, security patch, supported ABIs, memory, battery, network, and whether K1pL4y accessibility is enabled. Optionally include installed app labels/package names to plan user-requested app workflows. Never claims that an app can be controlled if its UI/API does not permit it."""
    override val parameters = ToolParameters(
        properties = mapOf(
            "include_installed_apps" to ParameterProperty(
                "boolean",
                "Whether to include a list of installed app names and package IDs (default false)"
            )
        )
    )
    override val requiresApproval = false

    override suspend fun execute(params: JsonObject): ToolResult {
        return try {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            val batteryPct = if (level >= 0 && scale > 0) (level * 100) / scale else -1

            val memoryManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memory = ActivityManager.MemoryInfo()
            memoryManager.getMemoryInfo(memory)

            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ).orEmpty()
            val ourService = "${context.packageName}/${K1pL4yAgentProAccessibility::class.java.name}"
            val accessibilityEnabled = enabledServices.split(':').any {
                it.equals(ourService, ignoreCase = true) ||
                    it.endsWith("/.service.K1pL4yAgentProAccessibility", ignoreCase = true)
            }

            val result = buildJsonObject {
                putJsonObject("device") {
                    put("manufacturer", Build.MANUFACTURER.orEmpty())
                    put("brand", Build.BRAND.orEmpty())
                    put("model", Build.MODEL.orEmpty())
                    put("device_codename", Build.DEVICE.orEmpty())
                    put("product", Build.PRODUCT.orEmpty())
                    put("android_release", Build.VERSION.RELEASE.orEmpty())
                    put("android_api_level", Build.VERSION.SDK_INT)
                    put("security_patch", Build.VERSION.SECURITY_PATCH.orEmpty())
                    putJsonArray("supported_abis") {
                        Build.SUPPORTED_ABIS.forEach { add(JsonPrimitive(it)) }
                    }
                    put("app_version", BuildConfig.VERSION_NAME)
                    put("app_version_code", BuildConfig.VERSION_CODE)
                }
                putJsonObject("runtime") {
                    put("total_ram_mb", memory.totalMem / (1024 * 1024))
                    put("available_ram_mb", memory.availMem / (1024 * 1024))
                    put("low_memory", memory.lowMemory)
                    put("battery_percent", batteryPct)
                    put("accessibility_service_enabled", accessibilityEnabled)
                    put("accessibility_service_connected", com.k1play.agentpro.service.AccessibilityBridge.isServiceConnected)
                }

                if (params["include_installed_apps"]?.jsonPrimitive?.booleanOrNull == true) {
                    val pm = context.packageManager
                    @Suppress("DEPRECATION")
                    val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                        .map { app ->
                            val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                            Triple(
                                pm.getApplicationLabel(app).toString(),
                                app.packageName,
                                launchIntent != null
                            )
                        }
                        .distinctBy { it.second }
                        .sortedBy { it.first.lowercase() }
                        .take(250)
                    put("installed_app_count_returned", apps.size)
                    put("installed_app_list_truncated", apps.size >= 250)
                    putJsonArray("installed_apps") {
                        apps.forEach { (label, packageName, launchable) ->
                            add(buildJsonObject {
                                put("name", label)
                                put("package", packageName)
                                put("launchable", launchable)
                            })
                        }
                    }
                }
            }
            ToolResult.success(result)
        } catch (e: Exception) {
            ToolResult.error("Device inspection failed: ${e.message ?: e.javaClass.simpleName}")
        }
    }
}
