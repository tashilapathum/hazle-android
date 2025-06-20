package com.tashila.hazle.features.chat

import com.tashila.hazle.api.ApiService
import com.tashila.hazle.db.messages.MessageDao
import com.tashila.hazle.db.messages.toDomain
import com.tashila.hazle.db.messages.toEntity
import com.tashila.hazle.db.threads.ThreadDao
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock.System

class ChatRepositoryImpl(
    private val apiService: ApiService,
    private val messageDao: MessageDao,
    private val threadDao: ThreadDao
) : ChatRepository {

    override suspend fun getGreetingMessage(): HttpResponse {
        return apiService.getGreeting()
    }

    override suspend fun sendUserMessage(threadId: Long, newMessage: String): HttpResponse {
        val message = Message(
            id = System.now().toEpochMilliseconds(),
            text = newMessage.trim(),
            isFromMe = true,
            timestamp = System.now()
        )

        messageDao.insertMessage(message.toEntity(threadId))
        updateThread(message, threadId)
        return apiService.sendMessage(message)
    }

    override suspend fun storeAiMessage(threadId: Long, message: String) {
        val messageToStore = Message(
            id = System.now().toEpochMilliseconds(),
            text = message.trim(),
            isFromMe = false,
            timestamp = System.now()
        )
        updateThread(messageToStore, threadId)
        messageDao.insertMessage(messageToStore.toEntity(threadId))
    }

    private suspend fun updateThread(message: Message, threadId: Long) {
        val thread = threadDao.getThreadById(threadId)
        if (thread != null)
            threadDao.updateThread(thread.copy(
                lastMessageText = message.text,
                lastMessageTime = message.timestamp
            ))
    }

    override fun getChatMessages(threadId: Long): Flow<List<Message>> {
        return messageDao.getMessagesByThreadId(threadId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun deleteAllMessages(threadId: Long) {
        messageDao.deleteAllMessagesByThreadId(threadId) // You'll need a new DAO method for this.
    }

    companion object {
        const val TAG = "ChatRepositoryImpl"
    }
}