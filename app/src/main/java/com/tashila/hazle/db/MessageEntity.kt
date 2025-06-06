package com.tashila.hazle.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: Long,
    val text: String,
    val isFromMe: Boolean,
    val timestamp: Instant // Room can store Instant directly with a TypeConverter
)