package com.tashila.hazle.features.auth

/**
 * Data class representing user information.
 */
data class UserInfo(
    val username: String,
    val email: String,
    val profileImageUrl: String? = null
)