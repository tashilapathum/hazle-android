package com.tashila.hazle.features.notifications

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tashila.hazle.MyApplication
import com.tashila.hazle.MyApplication.Companion.FOREGROUND_NOTIFICATION_CHANNEL_ID
import com.tashila.hazle.R
import com.tashila.hazle.features.chat.ChatRepository
import com.tashila.hazle.utils.getRandomNotificationText
import com.tashila.hazle.utils.getRandomNotificationTitle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class ForegroundApiService : Service(), KoinComponent {

    private val chatRepository: ChatRepository by inject()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var notificationManager: NotificationManagerCompat

    // A counter for active requests
    private var activeRequestCount = 0
    private val stopServiceSignal = Channel<Unit>(Channel.CONFLATED) // Use a channel to signal completion

    companion object {
        const val TAG = "ForegroundApiService"
        const val FOREGROUND_NOTIFICATION_ID = 100
        const val EXTRA_THREAD_ID = "extra_thread_id"
        const val EXTRA_MESSAGE_CONTENT = "extra_message_content"

        fun startService(context: Context, localThreadId: Long, messageContent: String) {
            val intent = Intent(context, ForegroundApiService::class.java).apply {
                putExtra(EXTRA_THREAD_ID, localThreadId)
                putExtra(EXTRA_MESSAGE_CONTENT, messageContent)
            }
            context.startForegroundService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = NotificationManagerCompat.from(this)
        // Start a coroutine that waits for the signal to stop the service
        serviceScope.launch {
            stopServiceSignal.receiveAsFlow().collect {
                // This block will be executed when Unit is sent to the channel
                // We'll update the notification and then stop the service.
                notificationManager.cancel(FOREGROUND_NOTIFICATION_ID)
                stopSelf() // Stop the foreground service
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val messageContent = intent?.getStringExtra(EXTRA_MESSAGE_CONTENT)
        val localThreadId = intent?.getLongExtra(EXTRA_THREAD_ID, -1L) ?: -1L

        val initialNotification = NotificationCompat.Builder(this, FOREGROUND_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getRandomNotificationTitle())
            .setContentText(getRandomNotificationText())
            .setSmallIcon(R.drawable.ic_logo)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(FOREGROUND_NOTIFICATION_ID, initialNotification)

        if (messageContent.isNullOrBlank().not()) {
            activeRequestCount++ // Increment before launching the coroutine
            serviceScope.launch {
                var title: String
                var message: String
                try {
                    title = messageContent
                    message = chatRepository.sendUserMessage(
                        localThreadId = localThreadId,
                        message = messageContent
                    )
                } catch (e: Exception) {
                    title = "Message Failed!"
                    message = "Error: ${e.localizedMessage ?: "Unknown error"}"
                } finally {
                    activeRequestCount-- // Decrement when a request finishes
                    if (activeRequestCount == 0) {
                        // All tasks are done, signal the service to stop
                        stopServiceSignal.trySend(Unit).isSuccess
                    }
                }

                if (MyApplication.isAppInForeground().not())
                    NotificationService.startService(
                        applicationContext,
                        title, message, localThreadId
                    )
            }
        } else {
            // If no message content provided, and no other active jobs, stop immediately
            if (activeRequestCount == 0) { // Check active count
                stopServiceSignal.trySend(Unit).isSuccess
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel() // Cancel coroutine scope to clean up resources
        stopServiceSignal.close() // Close the channel
        notificationManager.cancel(FOREGROUND_NOTIFICATION_ID) // Ensure notification is cleared
    }
}