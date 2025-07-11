package com.tashila.hazle.features.chat

import com.tashila.hazle.api.ApiService
import com.tashila.hazle.db.messages.MessageDao
import com.tashila.hazle.db.messages.toDomain
import com.tashila.hazle.db.messages.toEntity
import com.tashila.hazle.db.threads.ThreadDao
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ChatRepositoryImpl(
    private val apiService: ApiService,
    private val messageDao: MessageDao,
    private val threadDao: ThreadDao,
    private val jsonDecoder: Json
) : ChatRepository {

    override suspend fun sendUserMessage(localThreadId: Long, aiThreadId: String?, message: String): String {
        // check if there's a stored one because it updates after receiving the first response
        val finalAiThreadId = aiThreadId ?: threadDao.getThreadById(localThreadId)?.aiThreadId

        val newMessage = Message(
            id = System.now().toEpochMilliseconds(),
            text = message.trim(),
            isFromMe = true,
            timestamp = System.now(),
            aiThreadId = finalAiThreadId
        )

        messageDao.insertMessage(newMessage.toEntity(localThreadId))
        updateThread(newMessage, localThreadId)
        val response = apiService.sendMessage(newMessage)
        val responseBody = response.bodyAsText()
        val message = jsonDecoder.decodeFromString<Message>(responseBody)
        storeAiMessage(localThreadId, message)
        return message.text
    }

    override suspend fun storeAiMessage(localThreadId: Long, message: Message) {
        val newMessage = message.copy(
            timestamp = System.now()
        )
        updateThread(newMessage, localThreadId)
        messageDao.insertMessage(newMessage.toEntity(localThreadId))
    }

    private suspend fun updateThread(message: Message, localThreadId: Long) {
        val thread = threadDao.getThreadById(localThreadId)
        if (thread != null)
            threadDao.updateThread(thread.copy(
                lastMessageText = message.text,
                lastMessageTime = message.timestamp,
                aiThreadId = message.aiThreadId
            ))
    }

    override fun getChatMessages(localThreadId: Long): Flow<List<Message>> {
        return messageDao.getMessagesByThreadId(localThreadId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun deleteAllMessages(localThreadId: Long) {
        messageDao.deleteAllMessagesByThreadId(localThreadId)
    }

    companion object {
        const val TAG = "ChatRepositoryImpl"
    }
}