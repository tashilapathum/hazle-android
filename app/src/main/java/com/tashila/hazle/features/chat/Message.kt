package com.tashila.hazle.features.chat

import com.tashila.hazle.utils.InstantStringSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Message(
    val id: Long,
    val text: String,
    val isFromMe: Boolean,
    val aiThreadId: String? = null,
    @Serializable(with = InstantStringSerializer::class)
    val timestamp: Instant
)