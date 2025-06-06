package com.tashila.hazle.features.chat

import com.tashila.hazle.api.ApiService
import com.tashila.hazle.db.MainDatabase
import com.tashila.hazle.db.toDomain
import com.tashila.hazle.db.toEntity
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class ChatRepositoryImpl(
    private val apiService: ApiService,
    db: MainDatabase
): ChatRepository {
    private val messageDao = db.messageDao()

    override suspend fun getGreetingMessage(): HttpResponse {
        return apiService.getGreeting()
    }

    override suspend fun sendUserMessage(newMessage: String): HttpResponse {
        // Retrieve the last message before storing the new one
        val lastMessage = getChatMessages().first().lastOrNull()?.text ?: ""

        // Store the NEW message in the database immediately
        val messageToStore = Message(
            id = Clock.System.now().toEpochMilliseconds(),
            text = newMessage.trim(), // Store the original user input
            isFromMe = true,
            timestamp = Clock.System.now()
        )
        messageDao.insertMessage(messageToStore.toEntity())

        // Prepare last and new messages to send to the API
        val messageToSendToApi = Message(
            id = messageToStore.id, // Can reuse ID or generate new
            text = "\"$lastMessage\"\n$newMessage".trim(), // Combine for API
            isFromMe = true,
            timestamp = messageToStore.timestamp
        )

        // Send the COMBINED message to the API
        return apiService.sendMessage(messageToSendToApi)
    }

    override suspend fun storeAiMessage(message: String) {
        val messageToStore = Message(
            id = Clock.System.now().toEpochMilliseconds(),
            text = message.trim(),
            isFromMe = false,
            timestamp = Clock.System.now()
        )

        messageDao.insertMessage(messageToStore.toEntity())
    }

    override fun getChatMessages(): Flow<List<Message>> {
        return messageDao.getAllMessages().map { entities ->
            // Map list of MessageEntity to list of domain Message
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getMessageTexts(): List<String> {
        return messageDao.getAllMessages().first().map {
            it.text
        }
    }

    override suspend fun deleteAllMessages() {
        messageDao.deleteAllMessages()
    }

    companion object {
        const val TAG = "ChatRepositoryImpl"
    }
}