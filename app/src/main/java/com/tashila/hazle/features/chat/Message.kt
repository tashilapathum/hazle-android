package com.tashila.hazle.features.chat

import com.tashila.hazle.utils.InstantStringSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Clock.System
import kotlin.time.Instant

@Serializable
data class Message(
    val id: Long = System.now().toEpochMilliseconds(),
    val text: String,
    val isFromMe: Boolean,
    @Serializable(with = InstantStringSerializer::class)
    val timestamp: Instant = System.now()
)