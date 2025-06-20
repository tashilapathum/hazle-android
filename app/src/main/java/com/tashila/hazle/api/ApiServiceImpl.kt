package com.tashila.hazle.api

import android.util.Log
import com.tashila.hazle.BuildConfig
import com.tashila.hazle.features.auth.RefreshTokenRequest
import com.tashila.hazle.features.auth.SupabaseSignInRequest
import com.tashila.hazle.features.auth.SupabaseSignUpRequest
import com.tashila.hazle.features.chat.Message
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ApiServiceImpl(private val httpClient: HttpClient) : ApiService {

    override suspend fun sendMessage(request: Message): HttpResponse {
        Log.i(TAG, "sendMessage: ${request.text}")

        return try {
            httpClient.post("${SERVER_URL}chat") {
                contentType(ContentType.Application.Json) // Tell the server you're sending JSON
                setBody(request) // Set the body to your MessageRequest object
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendMessage: ERROR sending request: ${e.message}", e)
            throw e // Re-throw to propagate the error if needed
        }
    }

    override suspend fun signUp(request: SupabaseSignUpRequest): HttpResponse {
        return httpClient.post("${SERVER_URL}auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun signIn(request: SupabaseSignInRequest): HttpResponse {
        return httpClient.post("${SERVER_URL}auth/signin") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): HttpResponse {
        return httpClient.post("${SERVER_URL}auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    companion object {
        const val TAG = "ApiServiceImpl"
        val SERVER_URL = if (BuildConfig.DEBUG)
            "http://192.168.100.80:8080/"
        else
            "https://api.hazle.tashila.me/"

        // "http://10.0.2.2:8080/" // For Android Emulator
        // "http://192.168.100.80:8080/" // Hutch wifi
        // "http://192.168.0.101:8080/" // Dialog wifi
        // "https://hazle.onrender.com/" // Render deploy
        // "https://api.hazle.tashila.me/" // Live deploy
    }
}