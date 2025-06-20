package com.tashila.hazle.features.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.graphics.drawable.IconCompat
import com.tashila.hazle.MyApplication.Companion.CHAT_NOTIFICATIONS_CHANNEL_ID
import com.tashila.hazle.R
import com.tashila.hazle.features.chat.ChatRepository
import com.tashila.hazle.features.chat.Message
import com.tashila.hazle.ui.activities.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NotificationService : Service(), KoinComponent {
    private val chatRepository: ChatRepository by inject()

    // Create a CoroutineScope for the service's lifecycle
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        const val TAG = "NotificationService"

        const val EXTRA_NOTIFICATION_TITLE = "notification_title"
        const val EXTRA_NOTIFICATION_MESSAGE = "notification_message"
        const val EXTRA_MESSAGE_THREAD_ID = "message_thread_id"

        // Helper to start this service
        fun startService(context: Context, title: String, message: String, threadId: Long) {
            val intent = Intent(context, NotificationService::class.java).apply {
                action = "SHOW_CHAT_NOTIFICATION" // Custom action for clarity
                putExtra(EXTRA_NOTIFICATION_TITLE, title)
                putExtra(EXTRA_NOTIFICATION_MESSAGE, message)
                putExtra(EXTRA_MESSAGE_THREAD_ID, threadId)
            }
            context.startService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "SHOW_CHAT_NOTIFICATION") {
            val title = intent.getStringExtra(EXTRA_NOTIFICATION_TITLE) ?: "Notification"
            val message = intent.getStringExtra(EXTRA_NOTIFICATION_MESSAGE) ?: "No message"
            val threadId = intent.getLongExtra(EXTRA_MESSAGE_THREAD_ID, -1L)

            serviceScope.launch {
                showNotification(title, threadId)
            }
        }
        stopSelf(startId) // Stop the service once the notification is shown
        return START_NOT_STICKY
    }

    private suspend fun showNotification(title: String, threadId: Long) {
        // Create an Intent to open MainActivity when the notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_MESSAGE_THREAD_ID, threadId)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val allMessages = chatRepository.getChatMessages(threadId).first().map { it }

        // Create Person objects for the participants
        val user = Person.Builder()
            .setName("You")
            .setKey("user_key")
            .setIcon(IconCompat.createWithResource(this, R.drawable.ic_avatar))
            .build()

        val assistant = Person.Builder()
            .setName("Hazle")
            .setKey("assistant_key")
            .setIcon(IconCompat.createWithResource(this, R.drawable.ic_logo))
            .build()

        fun getPerson(message: Message): Person {
            return if (message.isFromMe == true)
                user
            else
                assistant
        }

        // Retrieve old messages and add to conversation
        val conversationMessages = mutableListOf<NotificationCompat.MessagingStyle.Message>()
        for (oldMessage in allMessages) {
            conversationMessages.add(
                NotificationCompat.MessagingStyle.Message(
                    oldMessage.text,
                    System.currentTimeMillis(),
                    getPerson(oldMessage)
                )
            )
        }

        // Create a NEW MessagingStyle instance each time
        val messagingStyle = NotificationCompat.MessagingStyle(user)
            .setConversationTitle(title)
            .setGroupConversation(false)

        // Add ALL messages from your stored list to the new MessagingStyle
        for (message in conversationMessages) {
            messagingStyle.addMessage(message)
        }

        // --- Start of Reply Button Code ---

        // Create an Intent for the BroadcastReceiver
        val replyIntent = Intent(this, NotificationReplyReceiver::class.java).apply {
            action = NotificationReplyReceiver.ACTION_REPLY
            putExtra(NotificationReplyReceiver.EXTRA_THREAD_ID, threadId)
        }

        // Create a PendingIntent for the reply action
        val replyPendingIntent = PendingIntent.getBroadcast(
            applicationContext, // Use applicationContext for BroadcastReceiver
            threadId.toInt(), // Use a unique request code if you have multiple reply notifications
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        // Create RemoteInput for the text reply
        val remoteInput = RemoteInput.Builder(NotificationReplyReceiver.KEY_TEXT_REPLY)
            .setLabel("Ask Hazle")
            .build()

        // Create the NotificationCompat.Action (the reply button itself)
        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.ic_reply, // Icon for the reply button
            "Reply", // Text for the reply button
            replyPendingIntent // PendingIntent to trigger when button is pressed
        ).addRemoteInput(remoteInput).build()

        // --- End of Reply Button Code ---

        // Build the notification
        val notification = NotificationCompat.Builder(this, CHAT_NOTIFICATIONS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setStyle(messagingStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Set the intent to launch when tapped
            .setAutoCancel(true) // Notification disappears when tapped
            .addAction(replyAction)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(threadId.toInt(), notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}