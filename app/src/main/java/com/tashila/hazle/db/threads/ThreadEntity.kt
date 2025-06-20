package com.tashila.hazle.db.threads

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant

@Entity(tableName = "threads")
data class ThreadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isPinned: Boolean,
    val createdAt: Instant = System.now(),
    val lastMessageText: String? = null,
    val lastMessageTime: Instant? = null
)