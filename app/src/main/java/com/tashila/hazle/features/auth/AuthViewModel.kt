package com.tashila.hazle.features.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    var resendTimer by mutableIntStateOf(0)
    var isResending by mutableStateOf(false)

    // UI State for Login/Signup Inputs
    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword = _confirmPassword.asStateFlow() // Only for SignupScreen

    // UI State for Loading and Errors
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // One-time events for navigation (e.g., navigate to Home, show Toast)
    private val _authEvent = MutableSharedFlow<AuthEvent>()
    val authEvent = _authEvent.asSharedFlow()

    // --- Input Change Handlers ---
    fun onEmailChanged(newEmail: String) {
        _email.value = newEmail
        _errorMessage.value = null // Clear error when input changes
    }

    fun onPasswordChanged(newPassword: String) {
        _password.value = newPassword
        _errorMessage.value = null // Clear error when input changes
    }

    fun onConfirmPasswordChanged(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
        _errorMessage.value = null // Clear error when input changes
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun onLoginClick() {
        _errorMessage.value = null

        val emailError = validateEmail(_email.value)
        val passwordError = validatePassword(_password.value)

        // If any validation error exists, display it and stop
        if (emailError != null) {
            _errorMessage.value = emailError
            return
        }
        if (passwordError != null) {
            _errorMessage.value = passwordError
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val request = SupabaseSignInRequest(
                    email = _email.value,
                    password = _password.value
                )
                authRepository.signIn(request)
                    .onSuccess { authResponse ->
                        _authEvent.emit(AuthEvent.LoginSuccess)
                        Log.i(TAG, "Login successful")
                    }
                    .onFailure { exception ->
                        // Display specific error message from the repository or a generic one
                        _errorMessage.value = exception.message ?: "Login failed. Please try again."
                        Log.i(TAG, "Login failed: ${exception.message}")
                    }
            } catch (e: Exception) {
                // Catch unexpected network or other exceptions
                _errorMessage.value = "An unexpected error occurred: ${e.message}"
                Log.i(TAG, "Login exception: ${e.message}")
            } finally {
                _isLoading.value = false // Hide loading spinner
            }
        }
    }

    fun onSignupClick() {
        _errorMessage.value = null

        val emailError = validateEmail(_email.value)
        val passwordError = validatePassword(_password.value)
        val confirmPasswordError: String? = when {
            _confirmPassword.value.isBlank() -> "Confirm password cannot be empty."
            _password.value != _confirmPassword.value -> "Passwords do not match."
            else -> null
        }

        // If any validation error exists, display it and stop
        if (emailError != null) {
            _errorMessage.value = emailError
            return
        }
        if (passwordError != null) {
            _errorMessage.value = passwordError
            return
        }
        if (confirmPasswordError != null) {
            _errorMessage.value = confirmPasswordError
            return
        }

        _isLoading.value = true // Show loading spinner
        viewModelScope.launch {
            try {
                val request = SupabaseSignUpRequest(
                    email = _email.value,
                    password = _password.value
                )
                authRepository.signUp(request)
                    .onSuccess { _ ->
                        _authEvent.emit(AuthEvent.SignupSuccess)
                    }
                    .onFailure { exception ->
                        when (exception) {
                            is AuthException.UserAlreadyExists -> {
                                _errorMessage.value = exception.message // "User with this email already exists"
                            }
                            is AuthException.InvalidCredentials -> {
                                _errorMessage.value = exception.message
                            }
                            else -> {
                                _errorMessage.value = exception.message ?: "Signup failed. Please try again."
                            }
                        }
                    }
            } catch (e: Exception) {
                // This catch block handles exceptions that occur *before* the repository call returns a Result,
                // or if the Result.failure() wasn't handled by the specific AuthException types.
                _errorMessage.value = "An unexpected error occurred during signup: ${e.message}"
                Log.e(TAG, "Signup exception in ViewModel catch block: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyEmail(accessToken: String?, refreshToken: String?) {
        viewModelScope.launch {
            if (refreshToken != null && accessToken != null) {
                authRepository.confirm(refreshToken, accessToken)
                _authEvent.emit(AuthEvent.VerifySuccess)
            }
            else
                _errorMessage.value = "An unexpected error occurred"
        }
    }

    fun resendEmail(email: String) {
        viewModelScope.launch {
            isResending = true
            authRepository.resend(email)
            startTimer()
            isResending = false
        }
    }

    private fun validateEmail(email: String): String? {
        if (email.isBlank()) {
            return "Email cannot be empty."
        }
        // Use the official Android framework regex directly
        // To avoid using `Patterns` from Android framework
        val emailRegex = Regex(
            ("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+")
        )
        if (!emailRegex.matches(email)) {
            return "Please enter a valid email address."
        }
        return null
    }


    private fun validatePassword(password: String): String? {
        if (password.isBlank()) {
            return "Password cannot be empty."
        }
        if (password.length < 8) {
            return "Password must be at least 8 characters long."
        }
        return null
    }

    fun startTimer() {
        resendTimer = 120
        viewModelScope.launch {
            while (resendTimer > 0) {
                delay(1000)
                resendTimer--
            }
        }
    }

    // --- Auth Event for Navigation ---
    sealed class AuthEvent {
        object LoginSuccess : AuthEvent()
        object SignupSuccess : AuthEvent()
        object VerifySuccess : AuthEvent()
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}