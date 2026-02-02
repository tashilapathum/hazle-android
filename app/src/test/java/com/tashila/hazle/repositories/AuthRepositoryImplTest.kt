package com.tashila.hazle.repositories

import com.tashila.hazle.api.AuthApiServiceImpl
import com.tashila.hazle.features.auth.AuthException
import com.tashila.hazle.features.auth.AuthRepositoryImpl
import com.tashila.hazle.features.auth.SupabaseAuthResponse
import com.tashila.hazle.features.auth.SupabaseSignInRequest
import com.tashila.hazle.features.auth.SupabaseSignUpRequest
import com.tashila.hazle.features.auth.TokenRepository
import com.tashila.hazle.features.settings.SettingsRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.ConcurrentLinkedQueue

@ExperimentalCoroutinesApi
class AuthRepositoryImplTest {

    private lateinit var authRepository: AuthRepositoryImpl
    private lateinit var mockEngine: MockEngine

    private val tokenRepository: TokenRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val json = Json { ignoreUnknownKeys = true }

    // A queue to hold the mocked responses for each test case
    private val responseQueue = ConcurrentLinkedQueue<suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData>()

    @Before
    fun setUp() {
        // Clear the response queue before each test for isolation
        responseQueue.clear()

        // Configure the MockEngine to pull responses from the queue
        mockEngine = MockEngine { request ->
            responseQueue.poll()?.invoke(this, request)
                ?: respondError(HttpStatusCode.NotFound, "No mocked response in queue for this request: ${request.url}")
        }

        // Create a real HttpClient, but powered by our MockEngine
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        // The ApiService is now a real instance using the mock http client
        val authApiService = AuthApiServiceImpl(settingsRepository, httpClient)

        // The repository uses the real ApiService
        authRepository = AuthRepositoryImpl(authApiService, tokenRepository, settingsRepository, json)

        // Mock settings repository dependency
        coEvery { settingsRepository.getBaseUrl() } returns "http://localhost/"
    }

    @Test
    fun `signIn success should save tokens and user info`() = runTest {
        // Given
        val request = SupabaseSignInRequest("test@example.com", "password")
        val responseJson = """{
            "access_token": "fake-access-token",
            "refresh_token": "fake-refresh-token",
            "expires_in": 3600,
            "expires_at": 9999999999,
            "token_type": "bearer",
            "user": {
                "id": "user-id-123",
                "aud": "authenticated",
                "role": "authenticated",
                "email": "test@example.com",
                "phone": "",
                "created_at": "2023-01-01T00:00:00Z",
                "updated_at": "2023-01-01T00:00:00Z",
                "email_confirmed_at": "2023-01-01T00:00:00Z",
                "phone_confirmed_at": null,
                "last_sign_in_at": "2023-01-01T00:00:00Z",
                "app_metadata": {},
                "user_metadata": {}
            }
        }"""

        // Add the expected successful response to the queue
        responseQueue.add {
            respond(
                content = responseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        // When
        val result = authRepository.signIn(request)

        // Then
        assertTrue(result.isSuccess)
        coVerify { tokenRepository.saveTokens("fake-access-token", "fake-refresh-token") }
        val userInfoSlot = slot<SupabaseAuthResponse>()
        coVerify { settingsRepository.saveUserInfo(capture(userInfoSlot)) }
        val capturedEmail = userInfoSlot.captured.user.email
        Assert.assertEquals("test@example.com", capturedEmail)
    }

    @Test
    fun `signIn with invalid credentials should return InvalidCredentials failure`() = runTest {
        // Given
        val request = SupabaseSignInRequest("test@example.com", "wrong-password")
        val errorJson = "{ \"message\": \"Invalid credentials\" }"

        responseQueue.add {
            respond(
                content = errorJson,
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        // When
        val result = authRepository.signIn(request)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AuthException.InvalidCredentials)
    }

    @Test
    fun `signUp with existing user should return UserAlreadyExists failure`() = runTest {
        // Given
        val request = SupabaseSignUpRequest("test@example.com", "password")
        val errorJson = "{ \"message\": \"User already exists\" }"

        responseQueue.add {
            respond(
                content = errorJson,
                status = HttpStatusCode.Conflict,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        // When
        val result = authRepository.signUp(request)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AuthException.UserAlreadyExists)
    }

/*    @Test
    fun `isAuthenticated with expired token should trigger refresh`() = runTest {
        // Given an expired access token and a valid refresh token
        val expiredAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjEwMDAwMDAwMDB9.i-o-JofT_lF2kfV_PaB2-43Ssr_Pz1i_S3A-aFvJ5-U"
        val refreshToken = "valid-refresh-token"
        coEvery { tokenRepository.getAccessToken() } returns expiredAccessToken
        coEvery { tokenRepository.getRefreshToken() } returns refreshToken

        // And a successful refresh response queued up
        val refreshResponseJson = """{
            "access_token": "fake-access-token",
            "refresh_token": "fake-refresh-token",
            "expires_in": 3600,
            "expires_at": 9999999999,
            "token_type": "bearer",
            "user": {
                "id": "user-id-123",
                "aud": "authenticated",
                "role": "authenticated",
                "email": "test@example.com",
                "phone": "",
                "created_at": "2023-01-01T00:00:00Z",
                "updated_at": "2023-01-01T00:00:00Z",
                "email_confirmed_at": "2023-01-01T00:00:00Z",
                "phone_confirmed_at": null,
                "last_sign_in_at": "2023-01-01T00:00:00Z",
                "app_metadata": {},
                "user_metadata": {}
            }
        }"""
        responseQueue.add {
            respond(
                content = refreshResponseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        // When
        val isAuthenticated = authRepository.isAuthenticated()

        // Then
        assertTrue(isAuthenticated)
        coVerify { tokenRepository.saveTokens("new-access-token", "new-refresh-token") }
    }*/

    @Test
    fun `isAuthenticated with expired token and failed refresh should return false`() = runTest {
        // Given an expired access token and a valid refresh token
        val expiredAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjEwMDAwMDAwMDB9.i-o-JofT_lF2kfV_PaB2-43Ssr_Pz1i_S3A-aFvJ5-U"
        val refreshToken = "valid-refresh-token"
        coEvery { tokenRepository.getAccessToken() } returns expiredAccessToken
        coEvery { tokenRepository.getRefreshToken() } returns refreshToken

        // And a failed refresh response queued up
        val errorJson = "{ \"message\": \"Token expired\" }"
        responseQueue.add {
            respond(
                content = errorJson,
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        // When
        val isAuthenticated = authRepository.isAuthenticated()

        // Then
        assertFalse(isAuthenticated)
        coVerify { tokenRepository.clearTokens() }
    }

    @Test
    fun `refresh failure should clear tokens and return false`() = runTest {
        // Given a refresh token
        val refreshToken = "invalid-refresh-token"
        val errorJson = "{ \"message\": \"Token invalid\" }"

        // And a failed refresh response
        responseQueue.add {
            respond(
                content = errorJson,
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        // When
        val result = authRepository.refresh(refreshToken)

        // Then
        assertFalse(result)
        coVerify { tokenRepository.clearTokens() }
    }
}
