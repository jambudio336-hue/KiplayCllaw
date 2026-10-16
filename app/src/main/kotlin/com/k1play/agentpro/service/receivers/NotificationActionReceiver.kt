package com.k1play.agentpro.service.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.k1play.agentpro.K1pL4yAgentProApp
import com.k1play.agentpro.R
import com.k1play.agentpro.agent.AgentLoop
import com.k1play.agentpro.approval.ApprovalQueue
import com.k1play.agentpro.approval.ApprovalResult
import com.k1play.agentpro.tools.ScreenCaptureTool
import com.k1play.agentpro.tools.VisionAnalyzeTool
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class NotificationActionReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReceiverEntryPoint {
        fun agentLoop(): AgentLoop
        fun approvalQueue(): ApprovalQueue
        fun screenCaptureTool(): ScreenCaptureTool
        fun visionAnalyzeTool(): VisionAnalyzeTool
    }

    override fun onReceive(context: Context, intent: Intent) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext, ReceiverEntryPoint::class.java
        )

        when (intent.action) {
            ACTION_REPLY -> handleReply(context, intent, entryPoint)
            ACTION_SCREENSHOT_EXPLAIN -> handleScreenshotExplain(context, entryPoint)
            ACTION_APPROVE_ALL -> {
                entryPoint.approvalQueue().respondAll(ApprovalResult.APPROVED)
                Log.d(TAG, "Approved all pending requests")
            }
            ACTION_DENY_ALL -> {
                entryPoint.approvalQueue().respondAll(ApprovalResult.DENIED)
                Log.d(TAG, "Denied all pending requests")
            }
        }
    }

    private fun handleReply(context: Context, intent: Intent, entryPoint: ReceiverEntryPoint) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        val text = remoteInput?.getCharSequence(KEY_REPLY)?.toString()
        if (text.isNullOrBlank()) return

        Log.d(TAG, "Reply received: $text")
        entryPoint.agentLoop().submitMessage(text)
    }

    private fun handleScreenshotExplain(context: Context, entryPoint: ReceiverEntryPoint) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val screenCapture = entryPoint.screenCaptureTool()
                val visionAnalyze = entryPoint.visionAnalyzeTool()

                val captureResult = screenCapture.execute(
                    buildJsonObject { put("include_base64", false) }
                )
                if (!captureResult.success) {
                    postResultNotification(context, "Screenshot failed: ${captureResult.error}")
                    return@launch
                }

                val filePath = captureResult.data?.let { data ->
                    (data as? kotlinx.serialization.json.JsonObject)?.get("file_path")
                        ?.let { (it as? kotlinx.serialization.json.JsonPrimitive)?.content }
                }
                if (filePath == null) {
                    postResultNotification(context, "Screenshot failed: no file path")
                    return@launch
                }

                val analyzeResult = visionAnalyze.execute(buildJsonObject {
                    put("file_path", filePath)
                    put("question", "Describe what you see on the screen. What app is open? What is the user looking at?")
                })

                if (analyzeResult.success) {
                    val analysis = analyzeResult.data?.let { data ->
                        (data as? kotlinx.serialization.json.JsonObject)?.get("analysis")
                            ?.let { (it as? kotlinx.serialization.json.JsonPrimitive)?.content }
                    } ?: "Analysis complete"
                    postResultNotification(context, analysis)
                } else {
                    postResultNotification(context, "Analysis failed: ${analyzeResult.error}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Screenshot explain failed: ${e.message}")
                postResultNotification(context, "Error: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun postResultNotification(context: Context, text: String) {
        val notification = NotificationCompat.Builder(context, K1pL4yAgentProApp.CHANNEL_ALERTS)
            .setContentTitle("Screen Analysis")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(EXPLAIN_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "NotifActionReceiver"
        const val ACTION_REPLY = "com.k1play.agentpro.NOTIFICATION_REPLY"
        const val ACTION_SCREENSHOT_EXPLAIN = "com.k1play.agentpro.SCREENSHOT_EXPLAIN"
        const val ACTION_APPROVE_ALL = "com.k1play.agentpro.APPROVE_ALL"
        const val ACTION_DENY_ALL = "com.k1play.agentpro.DENY_ALL"
        const val KEY_REPLY = "key_reply_text"
        const val EXPLAIN_NOTIFICATION_ID = 100
    }
}
