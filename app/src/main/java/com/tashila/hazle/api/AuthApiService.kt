package com.tashila.hazle.api

import com.tashila.hazle.features.auth.RefreshTokenRequest
import com.tashila.hazle.features.auth.ResendEmailRequest
import com.tashila.hazle.features.auth.SupabaseSignInRequest
import com.tashila.hazle.features.auth.SupabaseSignUpRequest
import io.ktor.client.statement.HttpResponse

interface AuthApiService {
    suspend fun signUp(request: SupabaseSignUpRequest): HttpResponse
    suspend fun signIn(request: SupabaseSignInRequest): HttpResponse
    suspend fun refreshToken(request: RefreshTokenRequest): HttpResponse
    suspend fun resendEmail(resendEmailRequest: ResendEmailRequest): HttpResponse
}