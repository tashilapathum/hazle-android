package com.tashila.hazle.features.auth

sealed class AuthException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    class UserAlreadyExists(message: String? = "User with this email already exists") : AuthException(message)
    class InvalidCredentials(message: String? = "Invalid email or password") : AuthException(message)
    class InvalidInput(message: String? = "Invalid input provided") : AuthException(message)
    class NetworkError(message: String? = "Network error. Please check your connection.", cause: Throwable? = null) : AuthException(message, cause)
    class ServerError(message: String? = "Server error. Please try again later.", cause: Throwable? = null) : AuthException(message, cause)
    class UnknownError(message: String? = "An unexpected error occurred.", cause: Throwable? = null) : AuthException(message, cause)
}