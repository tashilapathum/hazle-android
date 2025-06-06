package com.tashila.hazle.api

import android.util.Log
import com.tashila.hazle.features.chat.Message
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ApiServiceImpl(private val httpClient: HttpClient) : ApiService {
    override suspend fun getGreeting(): HttpResponse {
        return httpClient.get(SERVER_URL)
    }

    override suspend fun sendMessage(request: Message): HttpResponse {
        Log.i(TAG, "sendMessage: ${request.text}")

        return try {
            httpClient.post("$SERVER_URL/chat") {
                contentType(ContentType.Application.Json) // Tell the server you're sending JSON
                setBody(request) // Set the body to your MessageRequest object
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendMessage: ERROR sending request: ${e.message}", e)
            throw e // Re-throw to propagate the error if needed
        }
    }

    companion object {
        const val TAG = "ApiServiceImpl"
        //const val SERVER_URL = "http://10.0.2.2:8080/" // For Android Emulator
        const val SERVER_URL = "http://192.168.100.80:8080/" // Hutch wifi
        //const val SERVER_URL = "http://192.168.0.101:8080/" // Dialog wifi
    }
}