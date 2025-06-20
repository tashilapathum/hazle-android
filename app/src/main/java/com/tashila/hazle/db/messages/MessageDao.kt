package com.tashila.hazle.db.messages

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getMessagesByThreadId(threadId: Long): Flow<List<MessageEntity>>

    @Query("DELETE FROM messages WHERE threadId = :threadId")
    suspend fun deleteMessagesByThreadId(threadId: Long)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    @Query("DELETE FROM messages WHERE threadId = :threadId")
    suspend fun deleteAllMessagesByThreadId(threadId: Long)
}