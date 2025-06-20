package com.tashila.hazle.features.chat

import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendUserMessage(threadId: Long, message: String): HttpResponse
    suspend fun storeAiMessage(threadId: Long, message: String)
    fun getChatMessages(threadId: Long): Flow<List<Message>>
    suspend fun deleteAllMessages(threadId: Long)
}