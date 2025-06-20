package com.tashila.hazle.features.auth

import android.util.Log
import com.auth0.android.jwt.JWT
import com.tashila.hazle.api.ApiService
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import java.util.Date

class AuthRepositoryImpl(
    private val authApiService: ApiService,
    private val tokenStorage: TokenStorage
) : AuthRepository {
    private val jsonDecoder = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun signIn(request: SupabaseSignInRequest): Result<SupabaseAuthResponse> {
        return try {
            val response: HttpResponse = authApiService.signIn(request)
            val responseBody = response.bodyAsText()

            if (response.status.isSuccess()) {
                val parsedResponse = jsonDecoder.decodeFromString<SupabaseAuthResponse>(responseBody)
                tokenStorage.saveTokens(parsedResponse.accessToken, parsedResponse.refreshToken)
                Result.success(parsedResponse)
            } else {
                try {
                    val backendError = jsonDecoder.decodeFromString<BackendErrorMessage>(responseBody)
                    when (response.status) {
                        HttpStatusCode.BadRequest -> Result.failure(AuthException.InvalidCredentials(backendError.message))
                        HttpStatusCode.Unauthorized -> Result.failure(AuthException.InvalidCredentials(backendError.message))
                        HttpStatusCode.InternalServerError -> Result.failure(AuthException.ServerError(backendError.message))
                        else -> Result.failure(AuthException.UnknownError("An unexpected backend error occurred: ${backendError.message}"))
                    }
                } catch (jsonParseE: Exception) {
                    Log.e("AuthRepoImpl", "Failed to parse backend error response for sign-in. Raw: $responseBody. Error: ${jsonParseE.message}", jsonParseE)
                    Result.failure(AuthException.ServerError("Sign-in failed: Unexpected error from server."))
                }
            }
        } catch (e: Exception) {
            Result.failure(AuthException.NetworkError("Network error during sign-in: ${e.message}", e))
        }
    }

    override suspend fun signUp(request: SupabaseSignUpRequest): Result<SupabaseAuthResponse> {
        return try {
            val response: HttpResponse = authApiService.signUp(request)
            val responseBody = response.bodyAsText()
            if (response.status.isSuccess()) {
                val parsedResponse = jsonDecoder.decodeFromString<SupabaseAuthResponse>(responseBody)
                tokenStorage.saveTokens(parsedResponse.accessToken, parsedResponse.refreshToken)
                Result.success(parsedResponse)
            } else { // If the response indicates an error (non-2xx status code)
                try {
                    val backendError = jsonDecoder.decodeFromString<BackendErrorMessage>(responseBody)
                    when (response.status) {
                        HttpStatusCode.Conflict -> Result.failure(AuthException.UserAlreadyExists(backendError.message))
                        HttpStatusCode.BadRequest -> Result.failure(AuthException.InvalidInput(backendError.message))
                        HttpStatusCode.Unauthorized -> Result.failure(AuthException.InvalidCredentials(backendError.message))
                        HttpStatusCode.InternalServerError -> Result.failure(AuthException.ServerError(backendError.message))
                        else -> Result.failure(AuthException.UnknownError("An unexpected backend error occurred: ${backendError.message}"))
                    }
                } catch (jsonParseE: Exception) {
                    Log.e("AuthRepoImpl", "Failed to parse backend error response. Raw: $responseBody. Error: ${jsonParseE.message}", jsonParseE)
                    Result.failure(AuthException.ServerError("Signup failed: Unexpected error from server."))
                }
            }
        } catch (e: Exception) {
            Result.failure(AuthException.NetworkError("Network error during signup: ${e.message}", e))
        }
    }

    override suspend fun isAuthenticated(): Boolean {
        val accessToken = tokenStorage.getAccessToken()
        val refreshToken = tokenStorage.getRefreshToken()

        if (accessToken == null || refreshToken == null) {
            return false
        }

        try {
            val jwt = JWT(accessToken)
            val expiresAt = jwt.expiresAt

            if (expiresAt == null || expiresAt.before(Date())) {
                try {
                    val response: HttpResponse = authApiService.refreshToken(RefreshTokenRequest(refreshToken))
                    val responseBody = response.bodyAsText()

                    if (response.status.isSuccess()) {
                        val parsedResponse = jsonDecoder.decodeFromString<SupabaseAuthResponse>(responseBody)
                        tokenStorage.saveTokens(parsedResponse.accessToken, parsedResponse.refreshToken)
                        return true
                    } else {
                        try {
                            val backendError = jsonDecoder.decodeFromString<BackendErrorMessage>(responseBody)
                            Log.e("AuthRepoImpl", "Token refresh failed: ${response.status.value}: ${backendError.message}. Clearing auth data.")
                            tokenStorage.clearTokens()
                            return false
                        } catch (jsonParseE: Exception) {
                            Log.e("AuthRepoImpl", "Failed to parse backend error response for token refresh. Raw: $responseBody. Error: ${jsonParseE.message}", jsonParseE)
                            tokenStorage.clearTokens()
                            return false
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "isAuthenticated", e)
                    tokenStorage.clearTokens()
                    return false
                }
            } else {
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "isAuthenticated", e)
            tokenStorage.clearTokens()
            return false
        }
    }

    override fun clearAuthData() {
        tokenStorage.clearTokens()
    }

    companion object {
        const val TAG = "AuthRepositoryImpl"
    }
}