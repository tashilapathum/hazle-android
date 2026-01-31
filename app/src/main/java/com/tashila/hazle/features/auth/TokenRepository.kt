package com.tashila.hazle.features.auth

interface TokenRepository {
    suspend fun saveTokens(accessToken: String?, refreshToken: String?)
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun clearTokens()
}