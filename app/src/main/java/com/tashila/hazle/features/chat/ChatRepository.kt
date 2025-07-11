package com.tashila.hazle.features.chat

import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendUserMessage(localThreadId: Long, aiThreadId: String? = null, message: String): String
    suspend fun storeAiMessage(localThreadId: Long, message: Message)
    fun getChatMessages(localThreadId: Long): Flow<List<Message>>
    suspend fun deleteAllMessages(localThreadId: Long)
}