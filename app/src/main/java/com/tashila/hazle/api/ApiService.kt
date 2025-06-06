package com.tashila.hazle.api

import io.ktor.client.statement.HttpResponse
import com.tashila.hazle.features.chat.Message

interface ApiService {
    suspend fun getGreeting(): HttpResponse
    suspend fun sendMessage(request: Message): HttpResponse
}