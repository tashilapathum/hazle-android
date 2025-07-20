package com.tashila.hazle.features.onboard

import androidx.compose.ui.graphics.Color

val pages = listOf(
    Page(
        id = 1,
        content = PageContent(
            title = "Chat with AI anytime, anywhere",
            image = "https://imgur.com/FqUiVGN.png",
            text = "Just select some text or the whole page and \"Hazle it\" to start a conversation."
        ),
        color = Color(0xFFB39DDB) // Light Purple
    ),
    Page(
        id = 2,
        content = PageContent(
            title = "Select and share",
            image = "https://imgur.com/JP42HIV.png",
            text = "When app doesn't include Hazle in the context menu, share using the share sheet."
        ),
        color = Color(0xFFA5D6A7) // Light Green
    ),
    Page(
        id = 3,
        content = PageContent(
            title = "No more waiting for responses",
            image = "https://imgur.com/i7Osmrm.png",
            text = "Hazle will send you a notification when processed. Just reply to continue the conversation."
        ),
        color = Color(0xFFFFF9C4) // Soft Yellow
    ),
    Page(
        id = 4,
        content = PageContent(
            title = "Muti-task with ease!",
            image = "https://imgur.com/8clzlvY.png",
            text = "Keep up with multiple chats at the same time, in your notification panel."
        ),
        color = Color(0xFF90CAF9) // Light Blue
    ),
)

data class Page(
    val id: Int,
    val content: PageContent,
    val color: Color
)

data class PageContent(
    val title: String,
    val image: String,
    val text: String
)

