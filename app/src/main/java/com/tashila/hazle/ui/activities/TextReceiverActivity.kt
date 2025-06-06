package com.tashila.hazle.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import com.tashila.hazle.features.notifications.ForegroundApiService

class TextReceiverActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processRequest()
    }

    private fun processRequest() {
        Toast.makeText(this, "Sending to Hazle...", Toast.LENGTH_LONG).show()
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
            ForegroundApiService.Companion.startService(this, messageContent)
        }
        finish() // Finish the activity instantly
    }
}