package com.k1play.agentpro.service.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.k1play.agentpro.service.K1pL4yAgentProService
import com.k1play.agentpro.wakeword.WakeWordService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.d(TAG, "Device booted, checking auto-start preference")

        val prefs = context.getSharedPreferences("k1play_agent_config", Context.MODE_PRIVATE)
        val autoStart = prefs.getBoolean("auto_start_boot", false)

        if (autoStart) {
            Log.d(TAG, "Auto-starting K1pL4yAgentPro service")
            val serviceIntent = Intent(context, K1pL4yAgentProService::class.java).apply {
                action = K1pL4yAgentProService.ACTION_START
            }
            context.startForegroundService(serviceIntent)
        }

        val wakeWordEnabled = prefs.getBoolean("wake_word_enabled", false)
        if (wakeWordEnabled) {
            Log.d(TAG, "Auto-starting WakeWord service")
            val wakeWordIntent = Intent(context, WakeWordService::class.java).apply {
                action = WakeWordService.ACTION_START
            }
            context.startForegroundService(wakeWordIntent)
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
