package com.tashila.hazle.features.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SupabaseSignUpRequest(
    val email: String,
    val password: String,
)

@Serializable
data class SupabaseSignInRequest(
    val email: String,
    val password: String
)

@Serializable
data class SupabaseAuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_at") val expiresAt: Long,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("token_type") val tokenType: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("user") val user: SupabaseUser
)

@Serializable
data class SupabaseVerifyResponse(
    val message: String,
    val email: String
)

@Serializable
data class BackendErrorMessage(
    val message: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class ResendEmailRequest(
    val email: String
)

@Serializable
data class SupabaseUser(
    val id: String,
    val email: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("email_confirmed_at") val emailConfirmedAt: String? = null,
    @SerialName("phone_confirmed_at") val phoneConfirmedAt: String? = null,
    @SerialName("aud") val aud: String? = null,
    @SerialName("role") val role: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("last_sign_in_at") val lastSignInAt: String? = null,
    @SerialName("app_metadata") val appMetadata: JsonElement? = null,
    @SerialName("user_metadata") val userMetadata: JsonElement? = null
)