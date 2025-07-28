package com.tashila.hazle.features.settings

import com.tashila.hazle.features.auth.SupabaseAuthResponse
import com.tashila.hazle.features.auth.UserInfo
import kotlinx.coroutines.flow.Flow

/**
 * Interface for the Settings Repository.
 * Defines the contract for managing settings data, such as API URL and user information.
 */
interface SettingsRepository {

    suspend fun saveUserInfo(supabaseAuthResponse: SupabaseAuthResponse)
    fun getUserInfo(): Flow<UserInfo>

    suspend fun saveApiUrl(url: String)
    fun getApiUrl(): Flow<String>
    fun getBaseUrl(): String

    suspend fun saveOnboardState(isDone: Boolean)
    fun isOnboarded(): Boolean

    suspend fun saveLocale(localeTag: String)
    fun getLocale(): Flow<String>
    fun getLanguage(): String

    suspend fun logout()
}
