package com.tashila.hazle.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.lifecycleScope
import com.tashila.hazle.features.notifications.ForegroundApiService
import com.tashila.hazle.features.thread.ThreadRepository
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TextReceiverActivity : ComponentActivity(), KoinComponent {
    private val threadRepository: ThreadRepository by inject()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processRequest()
    }

    private fun processRequest() {
        Toast.makeText(this, "Sent to Hazle. You'll receive the reply via a notification.", Toast.LENGTH_LONG).show()
        val messageContent: String? = when (intent.action) {
            Intent.ACTION_PROCESS_TEXT -> {
                // For "Process text" from text selection menu
                intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
            }
            Intent.ACTION_SEND -> {
                // For "Share" from general share sheet
                intent.getStringExtra(Intent.EXTRA_TEXT)
            }
            else -> null
        }

        if (messageContent.isNullOrBlank().not()) {
            lifecycleScope.launch {
                // Always create a new thread for new received text
                val localThreadId = threadRepository.createThread(messageContent)
                ForegroundApiService.Companion.startService(this@TextReceiverActivity, localThreadId, messageContent)
            }
        }
        finish()
    }
}