package com.tashila.hazle.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tashila.hazle.R
import com.tashila.hazle.db.threads.ThreadEntity
import com.tashila.hazle.features.thread.ThreadsViewModel
import com.tashila.hazle.ui.components.dialogs.ConfirmationDialog
import com.tashila.hazle.ui.components.thread.ThreadItem
import com.tashila.hazle.ui.theme.WhisperFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadsScreen(
    onClickSettings: () -> Unit,
    onCreateNewChat: () -> Unit,
    onThreadSelected: (Long) -> Unit,
    threadsViewModel: ThreadsViewModel = viewModel(),
) {
    val allThreads by threadsViewModel.allThreads.collectAsState()
    val selectedThreadId by threadsViewModel.selectedThreadId.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf<ThreadEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Hazle",
                        fontFamily = WhisperFontFamily,
                        style = MaterialTheme.typography.displayMedium,
                        textAlign = TextAlign.Center
                    )
                },
                actions = {
                    IconButton(onClick = { onClickSettings.invoke() }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onCreateNewChat.invoke() }) {
                Icon(Icons.Filled.Add, "New Chat")
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (allThreads.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Chat,
                        contentDescription = stringResource(id = R.string.no_chats_content_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.no_chats_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.no_chats_description),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp, start = 32.dp, end = 32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(allThreads, key = { it.id }) { thread ->
                        val isSelected = thread.id == selectedThreadId
                        ThreadItem(
                            thread = thread,
                            isSelected = isSelected,
                            onThreadClick = {
                                threadsViewModel.selectThread(it.id)
                                onThreadSelected(it.id) // Notify parent about selection
                            },
                            onDeleteClick = { showDeleteConfirmation = it },
                            onRenameClick = { threadsViewModel.updateThread(it) },
                            onTogglePinClick = { threadsViewModel.toggleThreadPin(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        if (showDeleteConfirmation != null) {
            ConfirmationDialog(
                onConfirm = {
                    showDeleteConfirmation?.let { threadsViewModel.deleteThread(it.id) }
                },
                onDismiss = { showDeleteConfirmation = null },
                title = stringResource(id = R.string.delete_thread_confirmation_title),
                message = stringResource(id = R.string.delete_thread_confirmation_message)
            )
        }
    }
}