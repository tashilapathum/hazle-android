package com.tashila.hazle.db.messages

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tashila.hazle.db.threads.ThreadEntity
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Entity(
    tableName = "messages",
    // Define a foreign key constraint: messages.threadId must exist in threads.id
    foreignKeys = [ForeignKey(
        entity = ThreadEntity::class,
        parentColumns = ["id"],
        childColumns = ["threadId"],
        onDelete = ForeignKey.CASCADE // If a thread is deleted, its messages are also deleted
    )],
    // Add an index for faster lookups by threadId
    indices = [Index(value = ["threadId"])]
)
@OptIn(ExperimentalTime::class)
data class MessageEntity(
    @PrimaryKey val id: Long, // Message ID (e.g., timestamp from API)
    val threadId: Long, // Foreign key linking to ThreadEntity
    val text: String,
    val isFromMe: Boolean,
    val timestamp: Instant // Room can store Instant directly with a TypeConverter
)