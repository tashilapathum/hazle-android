package com.tashila.hazle.features.auth

interface AuthRepository {
    suspend fun signIn(request: SupabaseSignInRequest): Result<SupabaseAuthResponse>
    suspend fun signUp(request: SupabaseSignUpRequest): Result<SupabaseAuthResponse>
    suspend fun refresh(refreshToken: String): Boolean
    suspend fun isAuthenticated(): Boolean
    suspend fun clearAuthData() // For sign out
}