package com.tashila.hazle.viewmodels

import com.tashila.hazle.features.auth.AuthRepository
import com.tashila.hazle.features.auth.UserInfo
import com.tashila.hazle.features.settings.SettingsRepository
import com.tashila.hazle.features.settings.SettingsViewModel
import com.tashila.hazle.features.subscription.SubscriptionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.util.Locale

@ExperimentalCoroutinesApi
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel
    private val mockSettingsRepository: SettingsRepository = mockk(relaxed = true)
    private val mockAuthRepository: AuthRepository = mockk(relaxed = true)
    private val mockSubscriptionManager: SubscriptionManager = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Mock repository calls before initializing the ViewModel
        coEvery { mockSettingsRepository.getUserInfo() } returns flowOf(UserInfo("user", "user@example.com"))
        coEvery { mockSettingsRepository.getLocale() } returns flowOf(Locale.ENGLISH.language)

        viewModel = SettingsViewModel(mockSettingsRepository, mockAuthRepository, mockSubscriptionManager)
        testDispatcher.scheduler.advanceUntilIdle() // Run init block
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onLogoutClicked should clear auth data and show success message`() = runTest {
        // When
        viewModel.onLogoutClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { mockSettingsRepository.logout() }
        coVerify { mockAuthRepository.clearAuthData() }
        assertEquals("Logged out successfully!", viewModel.message.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `onLogoutClicked with repository failure should show error message`() = runTest {
        // Given
        val errorMessage = "Logout failed"
        coEvery { mockSettingsRepository.logout() } throws RuntimeException(errorMessage)

        // When
        viewModel.onLogoutClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Logout failed: $errorMessage", viewModel.message.value)
        assertFalse(viewModel.isLoading.value)
    }
}