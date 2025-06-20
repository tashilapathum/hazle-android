package com.tashila.hazle.ui.components.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp


/**
 * @param chatTitle The name of the AI (e.g., "Gemini", "ChatGPT", "Assistant").
 * @param onBackClick Lambda to execute when the back button is clicked.
 * @param onNewChatClick Lambda to execute when the "New Chat" or "Reset" button is clicked.
 * @param onInfoClick Lambda to execute when the "Info" or "Settings" button is clicked.
 * @param scrollBehavior Optional: Provides a scroll behavior for dynamic elevation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    chatTitle: String,
    chatSubtitle: String,
    onBackClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onInfoClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = chatTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
                AnimatedContent(
                    targetState = chatSubtitle,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> -height } + fadeOut())
                    }
                ) { targetChatSubtitle ->
                    Text(
                        text = targetChatSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to previous screen",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            // Reset Conversation button
            IconButton(onClick = onNewChatClick) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Start new chat",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            // AI Info button
            IconButton(onClick = onInfoClick) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        scrollBehavior = scrollBehavior
    )
}