package com.tashila.hazle.features.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tashila.hazle.utils.SERVER_URL
import com.tashila.hazle.features.auth.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Implementation of the SettingsRepository using Jetpack DataStore for persistence.
 * This version uses DataStore for API URL and in-memory MutableStateFlow for user info simulation.
 */
class SettingsRepositoryImpl(private val context: Context, ) : SettingsRepository {

    // Preference key for storing the API URL
    private object PreferencesKeys {
        val API_URL = stringPreferencesKey("api_url")
    }

    // Simulate storage for user information (would come from auth service in real app)
    private val _userInfo = MutableStateFlow(
        UserInfo(
            username = "AI Chat User",
            email = "user@example.com",
            profileImageUrl = "https://placehold.co/100x100/A020F0/ffffff?text=User" // Placeholder image
        )
    )

    /**
     * Retrieves the API URL from DataStore as a Flow.
     */
    override fun getApiUrl(): Flow<String> {
        return context.dataStore.data
            .map { preferences ->
                preferences[PreferencesKeys.API_URL] ?: SERVER_URL
            }
    }

    override fun getBaseUrl(): String {
        return runBlocking { getApiUrl().first() }
    }

    /**
     * Saves the API URL to DataStore.
     */
    override suspend fun saveApiUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.API_URL] = url
        }
        println("API URL saved to DataStore: $url") // For debugging
    }

    /**
     * Retrieves the user information from the simulated storage.
     */
    override fun getUserInfo(): Flow<UserInfo> {
        return _userInfo.asStateFlow()
    }

    /**
     * Simulates a user logout operation.
     * In a real app, this would involve clearing user tokens, navigating to login, etc.
     */
    override suspend fun logout() {
        context.dataStore.edit { it.clear() }
    }
}
