package com.tashila.hazle.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import com.tashila.hazle.features.chat.ChatViewModel
import com.tashila.hazle.features.notifications.NotificationService
import com.tashila.hazle.ui.screens.ChatScreen
import com.tashila.hazle.ui.theme.HazleTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class BubbleChatActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModel()
    private var passedThreadId by mutableLongStateOf(-1L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            HazleTheme {
                ChatScreen(
                    onCloseChat = { finish() },
                    viewModel = viewModel,
                    initialThreadId = passedThreadId
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return
        setIntent(intent)
        val threadIdFromExtras = intent.getLongExtra(NotificationService.EXTRA_MESSAGE_THREAD_ID, -1L)
        if (threadIdFromExtras != -1L) {
            passedThreadId = threadIdFromExtras
            return
        }

        val threadIdFromData = intent.data?.lastPathSegment?.toLongOrNull() ?: -1L
        if(threadIdFromData != -1L) {
            passedThreadId = threadIdFromData
        }
    }
}
