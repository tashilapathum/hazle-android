package com.tashila.hazle.ui.components.thread

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tashila.hazle.db.threads.ThreadEntity
import com.tashila.hazle.utils.toTimeString
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThreadItem(
    thread: ThreadEntity,
    isSelected: Boolean,
    onThreadClick: (ThreadEntity) -> Unit,
    onDeleteClick: (ThreadEntity) -> Unit,
    onRenameClick: (ThreadEntity) -> Unit,
    onTogglePinClick: (ThreadEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    var showContextMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    // Define a range of base icons for variety
    val baseIcons = remember { getIconsList() }
    // Determine the icon and its circle color based on thread ID for consistency
    val randomGenerator = remember(thread.id) { Random(thread.id) }
    val randomColor = Color(randomGenerator.nextFloat(), randomGenerator.nextFloat(), randomGenerator.nextFloat())
    val avatarIcon = remember(thread.id) { baseIcons[randomGenerator.nextInt(baseIcons.size)] }
    val avatarCircleColor = remember(thread.id) { randomColor.copy(alpha = 0.1f) }
    val avatarIconTint = remember(avatarCircleColor) { randomColor.copy(alpha = 0.7f) }

    val textColor = if (isSelected)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = { onThreadClick(thread) },
                onLongClick = { showContextMenu = true }
            ),
        // No explicit background color for Card; let its default colors handle it
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Avatar (Icon in a Circle)
            Box(
                modifier = Modifier
                    .size(48.dp) // Slightly larger circle for the icon
                    .clip(CircleShape) // Clip to a circle
                    .background(avatarCircleColor) // Random background color for the circle
                    .padding(8.dp), // Padding for the icon inside the circle
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = avatarIcon, // Use the randomly selected base icon
                    contentDescription = "Avatar",
                    tint = avatarIconTint, // Tint the icon
                    modifier = Modifier.size(32.dp) // Smaller icon inside the circle
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            // Main Content (Name and Last Message)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = thread.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = thread.lastMessageTime?.toTimeString() ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.Top)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = thread.lastMessageText ?: "No messages yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(if (thread.isPinned) "Unpin Chat" else "Pin Chat") },
                onClick = {
                    onTogglePinClick(thread)
                    showContextMenu = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (thread.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Pin/Unpin"
                    )
                }
            )
            DropdownMenuItem(
                text = { Text("Rename Chat") },
                onClick = {
                    showRenameDialog = true
                    showContextMenu = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.DriveFileRenameOutline,
                        contentDescription = "Rename"
                    )
                }
            )
            DropdownMenuItem(
                text = { Text("Delete Chat", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    onDeleteClick(thread)
                    showContextMenu = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }

    if (showRenameDialog) {
        RenameThreadDialog(
            currentName = thread.name,
            onDismiss = { showRenameDialog = false },
            onRename = { name ->
                onRenameClick.invoke(thread.copy(name = name))
                showRenameDialog = false
            }
        )
    }
}