package com.k1play.agentpro.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.k1play.agentpro.K1pL4yAgentProApp
import com.k1play.agentpro.R
import com.k1play.agentpro.agent.AgentLoop
import com.k1play.agentpro.agent.AgentState
import com.k1play.agentpro.agent.HeartbeatManager
import com.k1play.agentpro.agent.HeartbeatState
import com.k1play.agentpro.approval.ApprovalQueue
import com.k1play.agentpro.service.receivers.NotificationActionReceiver
import com.k1play.agentpro.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@AndroidEntryPoint
class K1pL4yAgentProService : Service() {

    @Inject lateinit var agentLoop: AgentLoop
    @Inject lateinit var approvalQueue: ApprovalQueue
    @Inject lateinit var heartbeatManager: HeartbeatManager

    private var wakeLock: PowerManager.WakeLock? = null
    private var serviceScope: CoroutineScope? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            // Android may restart a sticky service with a null intent after process reclaim.
            ACTION_START, null -> {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification("K1pL4yAgentPro is running", AgentState.IDLE, false, HeartbeatState.STOPPED, null)
                )
                acquireWakeLock()
                agentLoop.loadHistory()
                observeState()
                heartbeatManager.start(wakeLock)
            }
            ACTION_STOP -> {
                heartbeatManager.stop()
                agentLoop.stop()
                serviceScope?.cancel()
                serviceScope = null
                releaseWakeLock()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_PAUSE -> agentLoop.pause()
            ACTION_RESUME -> agentLoop.resume()
            else -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        heartbeatManager.stop()
        serviceScope?.cancel()
        serviceScope = null
        releaseWakeLock()
        super.onDestroy()
    }

    private fun observeState() {
        serviceScope?.cancel()
        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        serviceScope?.launch {
            combine(
                agentLoop.state,
                approvalQueue.requests,
                heartbeatManager.state,
                heartbeatManager.activeTaskContext
            ) { agentState, requests, heartbeatState, taskContext ->
                StateSnapshot(agentState, requests, heartbeatState, taskContext)
            }.collect { snapshot ->
                val hasPending = snapshot.requests.isNotEmpty()
                val text = when (snapshot.agentState) {
                    AgentState.IDLE -> {
                        when (snapshot.heartbeatState) {
                            HeartbeatState.ACTIVE -> {
                                val ctx = snapshot.taskContext
                                if (ctx != null) "Monitoring: $ctx" else "Idle"
                            }
                            HeartbeatState.POLLING -> "Checking..."
                            HeartbeatState.STOPPED -> "Idle"
                        }
                    }
                    AgentState.THINKING -> "Thinking..."
                    AgentState.EXECUTING_TOOLS -> "Executing tools..."
                    AgentState.WAITING_APPROVAL -> "Waiting for approval (${snapshot.requests.size} pending)"
                    AgentState.PAUSED -> "Paused"
                    AgentState.ERROR -> "Error occurred"
                }
                val notification = buildNotification(text, snapshot.agentState, hasPending, snapshot.heartbeatState, snapshot.taskContext)
                val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
                manager.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private data class StateSnapshot(
        val agentState: AgentState,
        val requests: List<Any>,
        val heartbeatState: HeartbeatState,
        val taskContext: String?
    )

    private fun buildNotification(text: String, state: AgentState, hasPendingApprovals: Boolean, heartbeatState: HeartbeatState, taskContext: String?): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, K1pL4yAgentProService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )

        // Pause/Resume toggle
        val isPaused = state == AgentState.PAUSED
        val toggleAction = if (isPaused) {
            PendingIntent.getService(
                this, 2,
                Intent(this, K1pL4yAgentProService::class.java).apply { action = ACTION_RESUME },
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                this, 2,
                Intent(this, K1pL4yAgentProService::class.java).apply { action = ACTION_PAUSE },
                PendingIntent.FLAG_IMMUTABLE
            )
        }

        // Reply action via RemoteInput (shows as text field, not a button)
        val replyIntent = PendingIntent.getBroadcast(
            this, 3,
            Intent(this, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_REPLY
            },
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val remoteInput = RemoteInput.Builder(NotificationActionReceiver.KEY_REPLY)
            .setLabel("Ask K1pL4yAgentPro...")
            .build()
        val replyAction = NotificationCompat.Action.Builder(0, "Reply", replyIntent)
            .addRemoteInput(remoteInput)
            .build()

        // Explain Screen action
        val explainIntent = PendingIntent.getBroadcast(
            this, 4,
            Intent(this, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_SCREENSHOT_EXPLAIN
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, K1pL4yAgentProApp.CHANNEL_SERVICE)
            .setContentTitle("K1pL4yAgentPro")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setSilent(true)

        // Android shows max 3 action buttons — prioritize by state
        if (hasPendingApprovals) {
            val approveIntent = PendingIntent.getBroadcast(
                this, 5,
                Intent(this, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_APPROVE_ALL
                },
                PendingIntent.FLAG_IMMUTABLE
            )
            val denyIntent = PendingIntent.getBroadcast(
                this, 6,
                Intent(this, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_DENY_ALL
                },
                PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Approve All", approveIntent)
            builder.addAction(0, "Deny All", denyIntent)
            builder.addAction(0, if (isPaused) "Resume" else "Pause", toggleAction)
        } else {
            builder.addAction(0, "Stop", stopIntent)
            builder.addAction(0, if (isPaused) "Resume" else "Pause", toggleAction)
            builder.addAction(0, "Explain Screen", explainIntent)
        }
        builder.addAction(replyAction)

        return builder.build()
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "K1pL4yAgentPro::AgentWakeLock"
        ).apply {
            acquire(HeartbeatManager.WAKE_LOCK_DURATION_MS) // 5 min, renewable by heartbeat
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    companion object {
        const val ACTION_START = "com.k1play.agentpro.START"
        const val ACTION_STOP = "com.k1play.agentpro.STOP"
        const val ACTION_PAUSE = "com.k1play.agentpro.PAUSE"
        const val ACTION_RESUME = "com.k1play.agentpro.RESUME"
        const val NOTIFICATION_ID = 1
    }
}
