package com.tashila.hazle.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tashila.hazle.features.auth.AuthRepository
import com.tashila.hazle.features.auth.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen.
 * Manages UI-related data and logic, interacting with the SettingsRepository.
 */
class SettingsViewModel(
    // Koin will inject this dependency automatically
    private val repository: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // State for the API URL text field
    private val _apiUrlInput = MutableStateFlow("")
    val apiUrlInput: StateFlow<String> = _apiUrlInput.asStateFlow()

    // State for the currently saved API URL (might be different from input until saved)
    private val _savedApiUrl = MutableStateFlow("")
    val savedApiUrl: StateFlow<String> = _savedApiUrl.asStateFlow()

    // State for user information
    private val _userInfo = MutableStateFlow(UserInfo("", ""))
    val userInfo: StateFlow<UserInfo> = _userInfo.asStateFlow()

    // State for showing a loading indicator or message
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State for showing user messages (e.g., "Saved successfully!")
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        // Collect API URL from repository when ViewModel is initialized
        viewModelScope.launch {
            repository.getApiUrl().collect { url ->
                _apiUrlInput.value = url // Update input field with saved value
                _savedApiUrl.value = url // Keep track of the last saved URL
            }
        }
        // Collect user info from repository
        viewModelScope.launch {
            repository.getUserInfo().collect { info ->
                _userInfo.value = info
            }
        }
    }

    /**
     * Updates the API URL input field value.
     * @param url The new URL string from the text field.
     */
    fun onApiUrlInputChanged(url: String) {
        _apiUrlInput.value = url
    }

    /**
     * Handles the save API URL action.
     * Calls the repository to persist the new API URL.
     */
    fun onSaveApiUrlClicked() {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = null // Clear previous messages
            try {
                repository.saveApiUrl(_apiUrlInput.value)
                _savedApiUrl.value = _apiUrlInput.value // Update saved URL state
                _message.value = "API URL saved"
            } catch (e: Exception) {
                _message.value = "Failed to save API URL: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Handles the logout action.
     * Calls the repository to perform logout.
     */
    fun onLogoutClicked() {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = null // Clear previous messages
            try {
                repository.logout()
                authRepository.clearAuthData()
                _message.value = "Logged out successfully!"
                // In a real app, you'd typically navigate to the login screen here
            } catch (e: Exception) {
                _message.value = "Logout failed: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clears any displayed messages.
     */
    fun clearMessage() {
        _message.value = null
    }
}
