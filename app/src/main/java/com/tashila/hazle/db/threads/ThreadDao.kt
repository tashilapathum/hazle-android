package com.tashila.hazle.db.threads

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreadDao {
    @Insert
    suspend fun insertThread(thread: ThreadEntity): Long // Returns the new thread's ID

    @Update
    suspend fun updateThread(thread: ThreadEntity)

    @Query("SELECT * FROM threads ORDER BY isPinned DESC, createdAt DESC")
    fun getAllThreads(): Flow<List<ThreadEntity>>

    @Query("SELECT * FROM threads WHERE id = :localThreadId")
    suspend fun getThreadById(localThreadId: Long): ThreadEntity?

    @Query("DELETE FROM threads WHERE id = :localThreadId")
    suspend fun deleteThread(localThreadId: Long)
}