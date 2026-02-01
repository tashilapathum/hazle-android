package com.tashila.hazle.features.chat

import android.util.Log
import com.tashila.hazle.api.ApiService
import com.tashila.hazle.db.messages.MessageDao
import com.tashila.hazle.db.messages.toDomain
import com.tashila.hazle.db.messages.toEntity
import com.tashila.hazle.db.threads.ThreadDao
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ChatRepositoryImpl(
    private val apiService: ApiService,
    private val messageDao: MessageDao,
    private val threadDao: ThreadDao,
    private val jsonDecoder: Json,
    private val clock: Clock
) : ChatRepository {

    override suspend fun sendUserMessage(localThreadId: Long, aiThreadId: String?, message: String): String {
        // check if there's a stored one because it updates after receiving the first response
        val finalAiThreadId = aiThreadId ?: threadDao.getThreadById(localThreadId)?.aiThreadId

        val now = clock.now()
        val newMessage = Message(
            id = now.toEpochMilliseconds(),
            text = message.trim(),
            isFromMe = true,
            timestamp = now,
            aiThreadId = finalAiThreadId
        )

        messageDao.insertMessage(newMessage.toEntity(localThreadId))
        updateThread(newMessage, localThreadId)
        val response = apiService.sendMessage(newMessage)
        val responseBody = response.bodyAsText()
        val receivedMessage = try {
            jsonDecoder.decodeFromString<Message>(responseBody)
        } catch (e: Exception) {
            Log.e(TAG, "sendUserMessage: Failed to decode JSON", e)
            val errorTime = clock.now()
            Message(
                id = errorTime.toEpochMilliseconds(),
                timestamp = errorTime,
                text = "Something went wrong",
                isFromMe = false
            )
        }
        storeAiMessage(localThreadId, receivedMessage)
        return receivedMessage.text
    }

    override suspend fun storeAiMessage(localThreadId: Long, message: Message) {
        updateThread(message, localThreadId)
        messageDao.insertMessage(message.toEntity(localThreadId))
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