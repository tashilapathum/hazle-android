package com.tashila.hazle.features.auth

interface AuthRepository {
    suspend fun signIn(request: SupabaseSignInRequest): Result<SupabaseAuthResponse>
    suspend fun signUp(request: SupabaseSignUpRequest): Result<SupabaseAuthResponse>
    suspend fun isAuthenticated(): Boolean
    fun clearAuthData() // For sign out
}