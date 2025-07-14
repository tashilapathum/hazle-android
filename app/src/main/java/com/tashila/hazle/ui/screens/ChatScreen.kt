package com.tashila.hazle.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.tashila.hazle.features.chat.ChatViewModel
import com.tashila.hazle.ui.components.chat.ChatInputBar
import com.tashila.hazle.ui.components.chat.ChatMessageBubble
import com.tashila.hazle.ui.components.chat.ChatTopBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    onCloseChat: () -> Unit,
    viewModel: ChatViewModel,
    initialThreadId: Long?
) {
    val messages by viewModel.messages.collectAsState()
    val currentMessage by viewModel.currentMessage.collectAsState()
    val chatTitle by viewModel.chatTitle.collectAsState()
    val chatSubtitle by viewModel.chatSubtitle.collectAsState()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var showInfoDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(initialThreadId) {
        if (initialThreadId != null && initialThreadId != -1L) {
            viewModel.setActiveThread(initialThreadId)
        } else { // for new chat
            viewModel.setActiveThread(null)
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(messages.size) {
        // Automatically scroll to the bottom when new messages arrive
        listState.animateScrollToItem(messages.size)

        // Update the subtitle only after receiving a message from AI
        if (messages.isNotEmpty() && messages.last().isFromMe.not())
            viewModel.updateSubtitle()
    }

    LaunchedEffect(Unit) {
        viewModel.closeChat.collect {
            onCloseChat.invoke()
        }
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                chatTitle = chatTitle,
                chatSubtitle = chatSubtitle,
                onBackClick = { viewModel.closeChat() },
                onInfoClick = { showInfoDialog = true },
            )
        },
        bottomBar = {
            ChatInputBar(
                modifier =  Modifier.focusRequester(focusRequester),
                text = currentMessage,
                onTextChange = { viewModel.onMessageChange(it) },
                onSendClick = {
                    viewModel.sendMessage()
                    keyboardController?.hide() // Hide keyboard on send
                }
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = messages,
                key = { message -> message.id }
            ) { message ->
                ChatMessageBubble(message = message)
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("About Hazle") },
            text = { Text("Hazle can make mistakes. Information for reference only.") },
            confirmButton = {
                TextButton (onClick = { showInfoDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}