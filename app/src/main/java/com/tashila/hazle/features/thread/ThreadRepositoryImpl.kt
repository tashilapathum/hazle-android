package com.tashila.hazle.features.thread

import com.tashila.hazle.db.threads.ThreadDao
import com.tashila.hazle.db.threads.ThreadEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock.System

class ThreadRepositoryImpl(
    private val threadDao: ThreadDao
) : ThreadRepository {

    override suspend fun createThread(name: String, isPinned: Boolean): Long {
        val thread = ThreadEntity(
            name = name.take(64),
            isPinned = isPinned,
            createdAt = System.now()
        )
        return threadDao.insertThread(thread)
    }

    override fun getAllThreads(): Flow<List<ThreadEntity>> {
        return threadDao.getAllThreads()
    }

    override suspend fun deleteThread(threadId: Long) {
        threadDao.deleteThread(threadId)
    }

    override suspend fun getThreadById(threadId: Long): ThreadEntity? {
        return threadDao.getThreadById(threadId)
    }

    override suspend fun updateThread(thread: ThreadEntity) {
        threadDao.updateThread(thread)
    }

    companion object {
        const val TAG = "ThreadRepositoryImpl"
    }
}
