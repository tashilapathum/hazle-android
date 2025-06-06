package com.tashila.hazle.features.chat

import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Long = System.now().toEpochMilliseconds(),
    val text: String,
    val isFromMe: Boolean,
    val timestamp: Instant = System.now()
)