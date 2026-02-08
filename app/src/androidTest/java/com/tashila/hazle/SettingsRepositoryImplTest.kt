package com.tashila.hazle

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tashila.hazle.features.auth.SupabaseAuthResponse
import com.tashila.hazle.features.auth.SupabaseUser
import com.tashila.hazle.features.settings.SettingsRepository
import com.tashila.hazle.features.settings.SettingsRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Locale

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class SettingsRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var testDataStore: DataStore<Preferences>
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setup() {
        testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { context.preferencesDataStoreFile("test_settings") }
        )
        settingsRepository = SettingsRepositoryImpl(testDataStore)
    }

    @After
    fun tearDown() {
        File(context.filesDir, "datastore/test_settings.preferences_pb").delete()
    }

    @Test
    fun saveUserInfo_and_getUserInfo() = testScope.runTest {
        // Given
        val user = SupabaseUser(
            id = "test_id",
            email = "test@example.com",
            createdAt = "",
            updatedAt = ""
        )
        val authResponse = SupabaseAuthResponse(
            accessToken = "",
            expiresAt = 0,
            expiresIn = 0,
            tokenType = "",
            refreshToken = "",
            user = user
        )

        // When
        settingsRepository.saveUserInfo(authResponse)

        // Then
        val userInfo = settingsRepository.getUserInfo().first()
        assertEquals(authResponse.user.id, userInfo.id)
        assertEquals(authResponse.user.email, userInfo.email)
    }

    @Test
    fun saveOnboardState_and_isOnboarded() = testScope.runTest {
        // When
        settingsRepository.saveOnboardState(true)

        // Then
        assertTrue(settingsRepository.isOnboarded())
    }

    @Test
    fun isOnboarded_returnsFalse_whenNotSet() = testScope.runTest {
        // When
        val onboarded = settingsRepository.isOnboarded()

        // Then
        assertFalse(onboarded)
    }

    @Test
    fun saveLocale_and_getLocale() = testScope.runTest {
        // Given
        val localeTag = "fr"

        // When
        settingsRepository.saveLocale(localeTag)

        // Then
        val savedLocale = settingsRepository.getLocale().first()
        assertEquals(localeTag, savedLocale)
    }

    @Test
    fun getLocale_returnsDefaultLocale_whenNotSet() = testScope.runTest {
        // When
        val savedLocale = settingsRepository.getLocale().first()

        // Then
        assertEquals(Locale.getDefault().language, savedLocale)
    }

    @Test
    fun logout_clearsData() = testScope.runTest {
        // Given
        settingsRepository.saveOnboardState(true)

        // When
        settingsRepository.logout()

        // Then
        val onboarded = settingsRepository.isOnboarded()
        assertFalse(onboarded)
    }
}