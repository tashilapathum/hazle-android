package com.tashila.hazle.api

import com.tashila.hazle.features.auth.RefreshTokenRequest
import com.tashila.hazle.features.auth.ResendEmailRequest
import com.tashila.hazle.features.auth.SupabaseSignInRequest
import com.tashila.hazle.features.auth.SupabaseSignUpRequest
import com.tashila.hazle.features.settings.SettingsRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApiServiceImpl(
    settingsRepository: SettingsRepository,
    private val httpClient: HttpClient
) : AuthApiService {
    private val baseUrl = settingsRepository.getBaseUrl()

    override suspend fun signUp(request: SupabaseSignUpRequest): HttpResponse {
        return httpClient.post("${baseUrl}auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun signIn(request: SupabaseSignInRequest): HttpResponse {
        return httpClient.post("${baseUrl}auth/signin") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): HttpResponse {
        return httpClient.post("${baseUrl}auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun resendEmail(resendEmailRequest: ResendEmailRequest): HttpResponse {
        return httpClient.post("${baseUrl}auth/resend") {
            contentType(ContentType.Application.Json)
            setBody(resendEmailRequest)
        }
    }
}