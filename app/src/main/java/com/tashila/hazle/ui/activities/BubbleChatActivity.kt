package com.tashila.hazle.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tashila.hazle.features.chat.ChatViewModel
import com.tashila.hazle.features.notifications.NotificationService
import com.tashila.hazle.ui.screens.ChatScreen
import com.tashila.hazle.ui.theme.HazleTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class BubbleChatActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val threadId = intent.getLongExtra(NotificationService.EXTRA_MESSAGE_THREAD_ID, -1L)

        setContent {
            HazleTheme {
                ChatScreen(
                    onCloseChat = { finish() },
                    viewModel = viewModel,
                    initialThreadId = threadId
                )
            }
        }
    }
}
