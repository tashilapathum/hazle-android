package com.tashila.hazle.features.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput

class NotificationReplyReceiver : BroadcastReceiver() {

    companion object {
        const val KEY_TEXT_REPLY = "key_text_reply" // Key for the remote input
        const val ACTION_REPLY = "com.tashila.hazle.ACTION_NOTIFICATION_REPLY"
        const val EXTRA_THREAD_ID = "extra_thread_id" // To identify which conversation to reply to
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_REPLY) {
            val replyText = getMessageText(intent)
            val localThreadId = intent.getLongExtra(EXTRA_THREAD_ID, -1L)

            if (replyText != null && replyText.isNotBlank()) {
                ForegroundApiService.startService(context, localThreadId, replyText.toString())
            }
        }
    }

    // Helper to get text from RemoteInput
    private fun getMessageText(intent: Intent): CharSequence? {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        return remoteInput?.getCharSequence(KEY_TEXT_REPLY)
    }
}