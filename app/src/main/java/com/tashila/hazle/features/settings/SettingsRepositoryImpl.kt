package com.tashila.hazle.features.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tashila.hazle.features.auth.SupabaseAuthResponse
import com.tashila.hazle.features.auth.UserInfo
import com.tashila.hazle.utils.SERVER_URL
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.util.Locale

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Implementation of the SettingsRepository using Jetpack DataStore for persistence.
 * This version uses DataStore for API URL and in-memory MutableStateFlow for user info simulation.
 */
class SettingsRepositoryImpl(private val context: Context, ) : SettingsRepository {

    // Preference key for storing the API URL
    private object PreferencesKeys {
        val API_URL = stringPreferencesKey("api_url")
        val USER_INFO = stringPreferencesKey("user_info")
        val ONBOARD_DONE = booleanPreferencesKey("onboard_done")
        val LOCALE_TAG = stringPreferencesKey("locale_tag")
    }

    /**
     * Saves user info to DataStore.
     */
    override suspend fun saveUserInfo(supabaseAuthResponse: SupabaseAuthResponse) {
        val id = supabaseAuthResponse.user.id
        val email = supabaseAuthResponse.user.email
        val userInfo = UserInfo(
            id = id,
            email = email,
        )
        val userInfoJson = Json.encodeToString(userInfo)

        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_INFO] = userInfoJson
        }
    }

    /**
     * Retrieves the user information from the simulated storage.
     */
    override fun getUserInfo(): Flow<UserInfo> { // Changed return type to UserInfo? to handle null
        val defaultId = "Hazle User"
        val defaultEmail = "user@email.com"
        return context.dataStore.data
            .map { preferences ->
                val userInfoJson = preferences[PreferencesKeys.USER_INFO]
                userInfoJson?.let {
                    Json.decodeFromString<UserInfo>(it)
                } ?: UserInfo(
                    id = defaultId,
                    email = defaultEmail,
                )
            }
    }

    /**
     * Saves the API URL to DataStore.
     */
    override suspend fun saveApiUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.API_URL] = url
        }
    }

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

    override suspend fun saveOnboardState(isDone: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARD_DONE] = isDone
        }
    }

    override fun isOnboarded(): Boolean {
        return runBlocking {
            context.dataStore.data
                .map { preferences ->
                    preferences[PreferencesKeys.ONBOARD_DONE] ?: false
                }.first()
        }
    }

    /**
     * Saves the selected locale tag to DataStore.
     */
    override suspend fun saveLocale(localeTag: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCALE_TAG] = localeTag
        }
    }

    /**
     * Retrieves the locale tag from DataStore as a Flow.
     */
    override fun getLocale(): Flow<String> {
        return context.dataStore.data
            .map { preferences ->
                // Default to the device's current language if no locale is saved
                preferences[PreferencesKeys.LOCALE_TAG] ?: Locale.getDefault().language
            }
    }

    override fun getLanguage(): String {
        return runBlocking { getLocale().first() }
    }

    /**
     * Simulates a user logout operation.
     * In a real app, this would involve clearing user tokens, navigating to login, etc.
     */
    override suspend fun logout() {
        context.dataStore.edit { it.clear() }
    }
}
