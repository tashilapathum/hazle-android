package com.tashila.hazle.features.settings

import com.tashila.hazle.features.auth.SupabaseAuthResponse
import com.tashila.hazle.features.auth.UserInfo
import kotlinx.coroutines.flow.Flow

/**
 * Interface for the Settings Repository.
 * Defines the contract for managing settings data, such as API URL and user information.
 */
interface SettingsRepository {
    /**
     * Retrieves the API URL as a Flow, allowing for reactive updates.
     * @return A Flow emitting the current API URL string.
     */
    fun getApiUrl(): Flow<String>

    /**
     * Retrieves the stored API URL.
     * @return API URL string.
     */
    fun getBaseUrl(): String

    /**
     * Saves the provided API URL.
     * @param url The API URL string to save.
     */
    suspend fun saveApiUrl(url: String)

    /**
     * Converts `SupabaseAuthResponse` into `UserInfo` and stores it.
     * */
    suspend fun saveUserInfo(supabaseAuthResponse: SupabaseAuthResponse)

    /**
     * Retrieves the current user information as a Flow.
     * @return A Flow emitting the UserInfo object.
     */
    fun getUserInfo(): Flow<UserInfo>

    /**
     * Simulates a user logout operation.
     * In a real application, this would clear session data, invalidate tokens, etc.
     */
    suspend fun logout()
}
