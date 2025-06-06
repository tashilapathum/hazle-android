package com.tashila.hazle.features.chat

import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun getGreetingMessage(): HttpResponse
    suspend fun sendUserMessage(message: String): HttpResponse
    suspend fun storeAiMessage(message: String)
    fun getChatMessages(): Flow<List<Message>>
    suspend fun getMessageTexts(): List<String>
    suspend fun deleteAllMessages()
}