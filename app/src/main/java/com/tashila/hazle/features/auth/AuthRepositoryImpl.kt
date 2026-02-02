package com.tashila.hazle.features.auth

import android.util.Log
import com.auth0.android.jwt.JWT
import com.tashila.hazle.api.AuthApiService
import com.tashila.hazle.features.settings.SettingsRepository
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import java.util.Date

class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val tokenRepository: TokenRepository,
    private val settingsRepository: SettingsRepository,
    private val jsonDecoder: Json
) : AuthRepository {

    override suspend fun signIn(request: SupabaseSignInRequest): Result<SupabaseAuthResponse> {
        return try {
            val response: HttpResponse = authApiService.signIn(request)
            val responseBody = response.bodyAsText()

            if (response.status.isSuccess()) {
                val parsedResponse = jsonDecoder.decodeFromString<SupabaseAuthResponse>(responseBody)
                tokenRepository.saveTokens(parsedResponse.accessToken, parsedResponse.refreshToken)
                settingsRepository.saveUserInfo(parsedResponse)
                Result.success(parsedResponse)
            } else {
                try {
                    val backendError = jsonDecoder.decodeFromString<BackendErrorMessage>(responseBody)
                    when (response.status) {
                        HttpStatusCode.BadRequest -> Result.failure(AuthException.InvalidCredentials())
                        HttpStatusCode.Unauthorized -> Result.failure(AuthException.InvalidCredentials())
                        HttpStatusCode.TooManyRequests -> Result.failure(AuthException.TooManyRequests())
                        HttpStatusCode.InternalServerError -> Result.failure(AuthException.ServerError(backendError.message))
                        else -> Result.failure(AuthException.UnknownError("An unexpected backend error occurred: ${backendError.message}"))
                    }
                } catch (jsonParseE: Exception) {
                    Log.e("AuthRepoImpl", "Failed to parse backend error response for sign-in. Raw: $responseBody. Error: ${jsonParseE.message}", jsonParseE)
                    Result.failure(AuthException.ServerError("Sign-in failed: Unexpected error from server."))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error during sign-in", e)
            Result.failure(AuthException.NetworkError("Network error. Please check your connection and try again."))
        }
    }

    override suspend fun signUp(request: SupabaseSignUpRequest): Result<SupabaseVerifyResponse> {
        return try {
            val response: HttpResponse = authApiService.signUp(request)
            val responseBody = response.bodyAsText()
            if (response.status.isSuccess()) {
                val parsedResponse = jsonDecoder.decodeFromString<SupabaseVerifyResponse>(responseBody)
                Result.success(parsedResponse)
            } else { // If the response indicates an error (non-2xx status code)
                try {
                    val backendError = jsonDecoder.decodeFromString<BackendErrorMessage>(responseBody)
                    when (response.status) {
                        HttpStatusCode.Conflict -> Result.failure(AuthException.UserAlreadyExists())
                        HttpStatusCode.BadRequest -> Result.failure(AuthException.InvalidInput())
                        HttpStatusCode.TooManyRequests -> Result.failure(AuthException.TooManyRequests())
                        HttpStatusCode.Unauthorized -> Result.failure(AuthException.InvalidCredentials())
                        HttpStatusCode.InternalServerError -> Result.failure(AuthException.ServerError(backendError.message))
                        else -> Result.failure(AuthException.UnknownError("An unexpected backend error occurred: ${backendError.message}"))
                    }
                } catch (jsonParseE: Exception) {
                    Log.e("AuthRepoImpl", "Failed to parse backend error response. Raw: $responseBody. Error: ${jsonParseE.message}", jsonParseE)
                    Result.failure(AuthException.ServerError("Signup failed: Unexpected error from server."))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error during sign-up", e)
            Result.failure(AuthException.NetworkError("Network error. Please check your connection and try again."))
        }
    }

    override suspend fun isAuthenticated(): Boolean {
        val accessToken = tokenRepository.getAccessToken()
        val refreshToken = tokenRepository.getRefreshToken()

        if (accessToken == null || refreshToken == null) {
            return false
        }

        try {
            val jwt = JWT(accessToken)
            val expiresAt: Date? = jwt.expiresAt
            val needsRefresh = expiresAt?.before(Date()) ?: true // Refresh if expired or no expiry info

            return if (needsRefresh) {
                refresh(refreshToken)
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while processing JWT. Clearing tokens.", e)
            tokenRepository.clearTokens()
            return false
        }
    }

    override suspend fun refresh(refreshToken: String): Boolean {
        try {
            val response: HttpResponse = authApiService.refreshToken(RefreshTokenRequest(refreshToken))
            val responseBody = response.bodyAsText()

            if (response.status.isSuccess()) {
                val parsedResponse = jsonDecoder.decodeFromString<SupabaseAuthResponse>(responseBody)
                tokenRepository.saveTokens(parsedResponse.accessToken, parsedResponse.refreshToken)
                settingsRepository.saveUserInfo(parsedResponse)
                return true
            } else {
                val errorMessage = try {
                    jsonDecoder.decodeFromString<BackendErrorMessage>(responseBody).message
                } catch (e: Exception) {
                    "Failed to parse error response. Raw body: $responseBody"
                }
                Log.e(TAG, "Token refresh failed: ${response.status.value}: $errorMessage. Clearing auth data.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Token refresh request failed.", e)
        }

        tokenRepository.clearTokens()
        return false
    }

    override suspend fun resend(email: String): Result<Unit> {
        return try {
            val request = ResendEmailRequest(email)
            val response: HttpResponse = authApiService.resendEmail(request)
            val responseBody = response.bodyAsText()

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                try {
                    val backendError = jsonDecoder.decodeFromString<BackendErrorMessage>(responseBody)
                    when (response.status) {
                        HttpStatusCode.BadRequest -> Result.failure(AuthException.InvalidInput())
                        HttpStatusCode.InternalServerError -> Result.failure(AuthException.ServerError(backendError.message))
                        else -> Result.failure(AuthException.UnknownError("Unexpected error: ${backendError.message}"))
                    }
                } catch (jsonParseE: Exception) {
                    Log.e(TAG, "Failed to parse error. Raw: $responseBody", jsonParseE)
                    Result.failure(AuthException.ServerError("Resend failed: Unexpected server error."))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error during resend", e)
            Result.failure(AuthException.NetworkError("Network error. Please check your connection and try again."))
        }
    }

    override suspend fun confirm(refreshToken: String?, accessToken: String?) {
        tokenRepository.saveTokens(accessToken, refreshToken)
    }

    override suspend fun clearAuthData() {
        tokenRepository.clearTokens()
    }

    companion object {
        const val TAG = "AuthRepositoryImpl"
    }
}