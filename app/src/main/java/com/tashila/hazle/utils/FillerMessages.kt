package com.tashila.hazle.utils

fun getRandomMessagePrompt(): String {
    val prompts = listOf(
        "What's up?",
        "Followup?",
        "What's on your mind?",
        "Got a thought to share?",
        "Feeling chatty?"
    )
    return prompts.random()
}

fun getRandomNotificationTitle(): String {
    val titles = listOf(
        "Hazle is on it!",
        "Working on it...",
        "Generating response...",
        "Crafting your reply",
        "Processing your message"
    )
    return titles.random()
}

fun getRandomNotificationText(): String {
    val texts = listOf(
        "Just a sec, magic takes time!",
        "Formulating a thoughtful response...",
        "Assembling the perfect reply...",
        "This won’t take long",
        "Generating something clever..."
    )
    return texts.random()
}
