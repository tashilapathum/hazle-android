package com.tashila.hazle.api

import com.tashila.hazle.features.chat.Message
import io.ktor.client.statement.HttpResponse

interface ApiService {
    suspend fun sendMessage(request: Message): HttpResponse
}