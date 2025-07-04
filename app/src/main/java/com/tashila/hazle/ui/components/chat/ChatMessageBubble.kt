package com.tashila.hazle.ui.components.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tashila.hazle.features.chat.Message
import com.tashila.hazle.utils.toDateTimeString

@Composable
fun ChatMessageBubble(
    message: Message,
    modifier: Modifier = Modifier
) {
    val horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start
    val alignment = if (message.isFromMe) Alignment.TopEnd else Alignment.TopStart
    val backgroundColor = if (message.isFromMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (message.isFromMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    val bubbleShape = if (message.isFromMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    var isExpanded by remember { mutableStateOf(false) }

    val lineCount = message.text.count { it == '\n' } + 1
    val showMoreButton = message.text.length > 300 || lineCount > 10

    // Build the AnnotatedString dynamically
    val annotatedMessageText = remember(message.text, isExpanded, showMoreButton, textColor) {
        buildAnnotatedString {
            if (showMoreButton && !isExpanded) {
                // If text needs truncation and is not expanded
                val truncatedByLines = if (lineCount > 10) {
                    message.text.lines().take(10).joinToString("\n")
                } else {
                    message.text
                }
                val finalTruncatedText = if (truncatedByLines.length > 300) {
                    truncatedByLines.take(300)
                } else {
                    truncatedByLines
                }
                append(finalTruncatedText)
                if (finalTruncatedText.length < message.text.length) { // Only add "..." if actual truncation happened
                    append("...")
                }
                append(" ") // Space before "More"
                pushStringAnnotation(tag = "TOGGLE_EXPAND", annotation = "toggle")
                withStyle(style = SpanStyle(
                    color = textColor.copy(alpha = 0.7f),
                )) {
                    append("More")
                }
                pop()
            } else {
                // Full text when expanded or no truncation needed
                append(message.text)
                // If it was expanded and a button was shown, add "Less" button
                if (showMoreButton && isExpanded) {
                    append(" ")
                    pushStringAnnotation(tag = "TOGGLE_EXPAND", annotation = "toggle")
                    withStyle(style = SpanStyle(
                        color = textColor.copy(alpha = 0.7f),
                    )) {
                        append("Less")
                    }
                    pop()
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = if (message.isFromMe) 64.dp else 0.dp,
                end = if (message.isFromMe) 0.dp else 64.dp
            ),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .clip(bubbleShape)
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .animateContentSize(animationSpec = tween(durationMillis = 300)),
            horizontalAlignment = horizontalAlignment
        ) {
            SelectionContainer {
                ClickableText( // Use ClickableText here
                    text = annotatedMessageText,
                    onClick = { offset ->
                        // Check if the click was on the "TOGGLE_EXPAND" annotation
                        annotatedMessageText.getStringAnnotations(tag = "TOGGLE_EXPAND", start = offset, end = offset)
                            .firstOrNull()?.let {
                                isExpanded = !isExpanded
                            }
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = textColor,
                        fontSize = 16.sp
                    )
                )
            }

            Text(
                text = message.timestamp.toDateTimeString(),
                color = textColor.copy(alpha = 0.6f),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}