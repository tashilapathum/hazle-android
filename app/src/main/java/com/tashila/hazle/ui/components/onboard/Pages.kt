package com.tashila.hazle.ui.components.onboard

import androidx.compose.ui.graphics.Color
import com.tashila.hazle.R

val pages = listOf(
    Page(
        id = 1,
        content = PageContent(
            titleResId = R.string.onboarding_page1_title,
            imageUrl = "https://imgur.com/FqUiVGN.png",
            textResId = R.string.onboarding_page1_text
        ),
        color = Color(0xFFB39DDB) // Light Purple
    ),
    Page(
        id = 2,
        content = PageContent(
            titleResId = R.string.onboarding_page2_title,
            imageUrl = "https://imgur.com/JP42HIV.png",
            textResId = R.string.onboarding_page2_text
        ),
        color = Color(0xFFA5D6A7) // Light Green
    ),
    Page(
        id = 3,
        content = PageContent(
            titleResId = R.string.onboarding_page3_title,
            imageUrl = "https://imgur.com/i7Osmrm.png",
            textResId = R.string.onboarding_page3_text
        ),
        color = Color(0xFFFFF9C4) // Soft Yellow
    ),
    Page(
        id = 4,
        content = PageContent(
            titleResId = R.string.onboarding_page4_title,
            imageUrl = "https://imgur.com/8clzlvY.png",
            textResId = R.string.onboarding_page4_text
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
    val titleResId: Int,
    val imageUrl: String,
    val textResId: Int
)

