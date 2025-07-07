package com.tashila.hazle.api

import com.tashila.hazle.features.chat.Message
import com.tashila.hazle.features.settings.SettingsRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ApiServiceImpl(
    settingsRepository: SettingsRepository,
    private val httpClient: HttpClient
) : ApiService {
    private val baseUrl = settingsRepository.getBaseUrl()

    override suspend fun sendMessage(request: Message): HttpResponse {
        return httpClient.post("${baseUrl}chat") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}