package com.tashila.hazle.features.notifications

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tashila.hazle.MyApplication.Companion.FOREGROUND_NOTIFICATION_CHANNEL_ID
import com.tashila.hazle.R
import com.tashila.hazle.features.chat.ChatRepository
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ForegroundApiService : Service(), KoinComponent {

    private val chatRepository: ChatRepository by inject()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        const val FOREGROUND_NOTIFICATION_ID = 100 // Unique ID for the foreground notification
        const val EXTRA_MESSAGE_CONTENT = "extra_message_content"

        // Helper to start this service
        fun startService(context: Context, messageContent: String) {
            val intent = Intent(context, ForegroundApiService::class.java).apply {
                putExtra(EXTRA_MESSAGE_CONTENT, messageContent)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val messageContent = intent?.getStringExtra(EXTRA_MESSAGE_CONTENT)

        // Show initial "working" notification to start as a foreground service
        val notification = NotificationCompat.Builder(this, FOREGROUND_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Processing Request")
            .setContentText("Your message is being sent...")
            .setSmallIcon(R.drawable.ic_logo) // Use a relevant icon from your `res/drawable`
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // Makes it an ongoing notification
            .build()

        startForeground(FOREGROUND_NOTIFICATION_ID, notification)

        if (messageContent.isNullOrBlank().not()) {
            serviceScope.launch {
                try {
                    val response = chatRepository.sendUserMessage(messageContent)
                    val title = messageContent
                    val msg = if (response.status.isSuccess()) {
                        chatRepository.storeAiMessage(response.bodyAsText())
                        response.bodyAsText()
                    } else {
                        "Server error: ${response.status.value}"
                    }

                    // Use your existing NotificationService to show the final result
                    NotificationService.startService(
                        applicationContext, // Pass context to your existing NotificationService
                        title, msg
                    )
                } catch (e: Exception) {
                    NotificationService.startService(
                        applicationContext,
                        "Message Failed!",
                        "Error: ${e.localizedMessage ?: "Unknown error"}"
                    )
                } finally {
                    stopSelf() // Stop the foreground service when done
                }
            }
        } else {
            stopSelf() // Stop if no message content provided
        }

        return START_NOT_STICKY // Service won't restart if killed by system
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null // Not a bound service
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel() // Cancel coroutine scope to clean up resources
    }
}