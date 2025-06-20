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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
    LaunchedEffect(initialThreadId) {
        if (initialThreadId != null && initialThreadId != -1L) {
            viewModel.setActiveThread(initialThreadId)
        } else {
            viewModel.setActiveThread(null)
        }
    }
    val messages by viewModel.messages.collectAsState()
    val currentMessage by viewModel.currentMessage.collectAsState()
    val chatTitle by viewModel.chatTitle.collectAsState()
    val chatSubtitle by viewModel.chatSubtitle.collectAsState()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var showInfoDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Automatically scroll to the bottom when new messages arrive
    LaunchedEffect(messages.size) {
        listState.animateScrollToItem(messages.size)
    }

    LaunchedEffect(Unit) {
        viewModel.closeChat.collect {
            onCloseChat.invoke()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ChatTopBar(
                chatTitle = chatTitle,
                chatSubtitle = chatSubtitle,
                onBackClick = { viewModel.closeChat() },
                onNewChatClick = { showResetDialog = true },
                onInfoClick = { showInfoDialog = true },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            ChatInputBar(
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

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Confirm") },
            text = { Text("Are you sure you want to reset the chat? All messages will be deleted. This action cannot be undone.") },
            confirmButton = {
                TextButton (onClick = {
                    showResetDialog = false
                    viewModel.resetChat()
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showResetDialog = false
                }) {
                    Text("No")
                }
            }
        )
    }
}