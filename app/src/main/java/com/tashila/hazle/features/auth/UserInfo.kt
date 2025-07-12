package com.tashila.hazle.features.auth

import kotlinx.serialization.Serializable

/**
 * Data class representing user information.
 */
@Serializable
data class UserInfo(
    val id: String,
    val email: String,
)