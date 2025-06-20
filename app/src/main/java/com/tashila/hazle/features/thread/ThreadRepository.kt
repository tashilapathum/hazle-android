package com.tashila.hazle.features.thread

import com.tashila.hazle.db.threads.ThreadEntity
import kotlinx.coroutines.flow.Flow

interface ThreadRepository {
    suspend fun createThread(name: String, isPinned: Boolean = false): Long
    fun getAllThreads(): Flow<List<ThreadEntity>>
    suspend fun deleteThread(threadId: Long)
    suspend fun getThreadById(threadId: Long): ThreadEntity?
    suspend fun updateThread(thread: ThreadEntity)
}