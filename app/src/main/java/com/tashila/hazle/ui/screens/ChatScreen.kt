package com.tashila.hazle.ui.screens

import android.app.Activity
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tashila.hazle.features.chat.ChatViewModel
import com.tashila.hazle.ui.components.ChatInputBar
import com.tashila.hazle.ui.components.ChatMessageBubble
import com.tashila.hazle.ui.components.ChatTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val messages by viewModel.messages.collectAsState()
    val currentMessage by viewModel.currentMessage.collectAsState()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val activity = LocalLifecycleOwner.current as Activity
    var showInfoDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Automatically scroll to the bottom when new messages arrive
    LaunchedEffect(messages.size) {
        listState.animateScrollToItem(messages.size)
    }

    LaunchedEffect(Unit) {
        viewModel.closeChat.collect {
            activity.finish()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ChatTopBar(
                chatName = "Hazle",
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
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
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