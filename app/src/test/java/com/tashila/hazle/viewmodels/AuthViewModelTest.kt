package com.tashila.hazle.viewmodels

import app.cash.turbine.test
import com.tashila.hazle.features.auth.AuthException
import com.tashila.hazle.features.auth.AuthRepository
import com.tashila.hazle.features.auth.AuthViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AuthViewModelTest {
    private lateinit var viewModel: AuthViewModel
    private val mockAuthRepository: AuthRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(mockAuthRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onLoginClick with valid credentials should emit LoginSuccess`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        coEvery { mockAuthRepository.signIn(any()) } returns Result.success(mockk())

        // When
        viewModel.authEvent.test {
            viewModel.onLoginClick()
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            Assert.assertEquals(AuthViewModel.AuthEvent.LoginSuccess, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        Assert.assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `onLoginClick with invalid email should set error message`() = runTest {
        // Given
        val email = "invalid-email"
        val password = "password123"
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)

        // When
        viewModel.onLoginClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        Assert.assertEquals("Please enter a valid email address.", viewModel.errorMessage.value)
    }

    @Test
    fun `onLoginClick with invalid password should set error message`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "123"
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)

        // When
        viewModel.onLoginClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        Assert.assertEquals(
            "Password must be at least 8 characters long.",
            viewModel.errorMessage.value
        )
    }

    @Test
    fun `onLoginClick with wrong credentials should set error message`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        val errorMessage = "Invalid login credentials"
        coEvery { mockAuthRepository.signIn(any()) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.onLoginClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        Assert.assertEquals(errorMessage, viewModel.errorMessage.value)
    }

    @Test
    fun `onLoginClick when repository throws exception should set error message`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        val errorMessage = "Network error"
        coEvery { mockAuthRepository.signIn(any()) } throws RuntimeException(errorMessage)

        // When
        viewModel.onLoginClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        Assert.assertEquals(
            "An unexpected error occurred: $errorMessage",
            viewModel.errorMessage.value
        )
    }

    @Test
    fun `onSignupClick with valid credentials should emit SignupSuccess`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        viewModel.onConfirmPasswordChanged(password)
        coEvery { mockAuthRepository.signUp(any()) } returns Result.success(mockk())

        // Then
        viewModel.authEvent.test { // <-- Step 1: Start listening to the authEvent Channel/Flow
            // When
            viewModel.onSignupClick() // <-- Step 2: Trigger the action that sends the event
            testDispatcher.scheduler.advanceUntilIdle() // <-- Step 3: Let coroutines finish their work

            // Assert
            Assert.assertEquals(AuthViewModel.AuthEvent.SignupSuccess, awaitItem())

            // Cleanup
            cancelAndIgnoreRemainingEvents()
        }

        // You can still assert other states after the test block
        Assert.assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `onSignupClick with invalid email should set error message`() = runTest {
        // Given
        val email = "invalid-email"
        val password = "password123"
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        viewModel.onConfirmPasswordChanged(password)

        // When
        viewModel.onSignupClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        Assert.assertEquals("Please enter a valid email address.", viewModel.errorMessage.value)
    }

    @Test
    fun `onSignupClick with invalid password should set error message`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "123"
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        viewModel.onConfirmPasswordChanged(password)

        // When
        viewModel.onSignupClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        Assert.assertEquals(
            "Password must be at least 8 characters long.",
            viewModel.errorMessage.value
        )
    }

    @Test
    fun `onSignupClick with mismatched passwords should set error message`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val confirmPassword = "password456"
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        viewModel.onConfirmPasswordChanged(confirmPassword)

        // When
        viewModel.onSignupClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        Assert.assertEquals("Passwords do not match.", viewModel.errorMessage.value)
    }

    @Test
    fun `onSignupClick with existing user should set error message`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        viewModel.onConfirmPasswordChanged(password)
        val errorMessage = "User with this email already exists"
        coEvery { mockAuthRepository.signUp(any()) } returns Result.failure(
            AuthException.UserAlreadyExists(
                errorMessage
            )
        )

        // When
        viewModel.onSignupClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        Assert.assertEquals(errorMessage, viewModel.errorMessage.value)
    }

    @Test
    fun `onSignupClick when repository throws exception should set error message`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        viewModel.onConfirmPasswordChanged(password)
        val errorMessage = "Network error"
        coEvery { mockAuthRepository.signUp(any()) } throws RuntimeException(errorMessage)

        // When
        viewModel.onSignupClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        Assert.assertEquals(
            "An unexpected error occurred during signup: $errorMessage",
            viewModel.errorMessage.value
        )
    }
}