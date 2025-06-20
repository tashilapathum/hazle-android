package com.tashila.hazle.api

import com.tashila.hazle.features.auth.RefreshTokenRequest
import com.tashila.hazle.features.auth.SupabaseSignInRequest
import com.tashila.hazle.features.auth.SupabaseSignUpRequest
import com.tashila.hazle.features.chat.Message
import io.ktor.client.statement.HttpResponse

interface ApiService {
    suspend fun sendMessage(request: Message): HttpResponse
    suspend fun signUp(request: SupabaseSignUpRequest): HttpResponse
    suspend fun signIn(request: SupabaseSignInRequest): HttpResponse
    suspend fun refreshToken(request: RefreshTokenRequest): HttpResponse
}