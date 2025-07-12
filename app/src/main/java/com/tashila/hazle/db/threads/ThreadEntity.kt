package com.tashila.hazle.db.threads

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Entity(tableName = "threads")
@OptIn(ExperimentalTime::class)
data class ThreadEntity(
    @PrimaryKey val id: Long = System.currentTimeMillis(),
    val name: String,
    val isPinned: Boolean,
    val createdAt: Instant = Clock.System.now(),
    val aiThreadId: String? = null,
    val lastMessageText: String? = null,
    val lastMessageTime: Instant? = null
)