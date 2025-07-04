package com.tashila.hazle.api

import com.tashila.hazle.SERVER_URL
import com.tashila.hazle.features.auth.RefreshTokenRequest
import com.tashila.hazle.features.auth.SupabaseSignInRequest
import com.tashila.hazle.features.auth.SupabaseSignUpRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApiServiceImpl(private val httpClient: HttpClient) : AuthApiService {
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
}