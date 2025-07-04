package com.tashila.hazle.api

import com.tashila.hazle.SERVER_URL
import com.tashila.hazle.features.chat.Message
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ApiServiceImpl(private val httpClient: HttpClient) : ApiService {

    override suspend fun sendMessage(request: Message): HttpResponse {
        return httpClient.post("${SERVER_URL}chat") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}