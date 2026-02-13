package com.tashila.hazle.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tashila.hazle.features.auth.AuthRepository
import com.tashila.hazle.features.auth.UserInfo
import com.tashila.hazle.features.paywall.RevenueCatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * ViewModel for the Settings screen.
 * Manages UI-related data and logic, interacting with the SettingsRepository.
 */
class SettingsViewModel(
    // Koin will inject this dependency automatically
    private val repository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val revenueCatRepository: RevenueCatRepository
) : ViewModel() {

    // State for user information
    private val _userInfo = MutableStateFlow(UserInfo("", ""))
    val userInfo: StateFlow<UserInfo> = _userInfo.asStateFlow()

    // State for showing a loading indicator or message
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State for showing user messages
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // State locale tag
    private val _selectedLocale = MutableStateFlow(Locale.getDefault().language)
    val selectedLocale: StateFlow<String> = _selectedLocale.asStateFlow()

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed.asStateFlow()

    private val _currentPlan = MutableStateFlow<String?>(null)
    val currentPlan: StateFlow<String?> = _currentPlan.asStateFlow()

    init {
        // Collect user info from repository
        viewModelScope.launch {
            repository.getUserInfo().collect { _userInfo.value = it }
        }
        // Collect Locale
        viewModelScope.launch {
            repository.getLocale().collect { _selectedLocale.value = it }
        }
        // Collect subscription status
        viewModelScope.launch {
            revenueCatRepository.customerInfo.collect { customerInfo ->
                val isPro = revenueCatRepository.hasProAccess(customerInfo)
                val isVip = revenueCatRepository.hasVipAccess(customerInfo)
                _isSubscribed.value = isPro || isVip
                _currentPlan.value = when {
                    isVip -> "VIP Plan"
                    isPro -> "Pro Plan"
                    else -> null
                }
            }
        }
    }

    /**
     * Handles the save language action.
     */
    fun onLanguageClicked(locale: String) {
        _selectedLocale.value = locale
        viewModelScope.launch {
            _message.value = null // Clear previous messages
            try {
                repository.saveLocale(_selectedLocale.value)
                _message.value = "Language changed. Restart the app to apply changes."
            } catch (e: Exception) {
                _message.value = "Failed to change languageL: ${e.localizedMessage}"
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
