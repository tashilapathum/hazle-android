package com.tashila.hazle

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tashila.hazle.features.auth.TokenRepository
import com.tashila.hazle.features.auth.TokenRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class TokenRepositoryImplTest {

    private lateinit var tokenRepository: TokenRepository
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        tokenRepository = TokenRepositoryImpl(context)
    }

    @After
    fun tearDown() = runTest {
        tokenRepository.clearTokens()
    }

    @Test
    fun saveAndRetrieveAccessToken() = runTest {
        // Given
        val accessToken = "my-access-token"

        // When
        tokenRepository.saveTokens(accessToken, null)

        // Then
        assertEquals(accessToken, tokenRepository.getAccessToken())
    }

    @Test
    fun retrieveNonExistentTokenReturnsNull() = runTest {
        // When
        val accessToken = tokenRepository.getAccessToken()

        // Then
        assertNull(accessToken)
    }

    @Test
    fun saveAndRetrieveRefreshToken() = runTest {
        // Given
        val refreshToken = "my-refresh-token"

        // When
        tokenRepository.saveTokens(null, refreshToken)

        // Then
        assertEquals(refreshToken, tokenRepository.getRefreshToken())
    }

    @Test
    fun updateAccessToken() = runTest {
        // Given
        val oldToken = "old-access-token"
        val newToken = "new-access-token"

        // When
        tokenRepository.saveTokens(oldToken, null)
        tokenRepository.saveTokens(newToken, null)

        // Then
        assertEquals(newToken, tokenRepository.getAccessToken())
    }

    @Test
    fun clearTokens() = runTest {
        // Given
        val accessToken = "my-access-token"
        val refreshToken = "my-refresh-token"

        // When
        tokenRepository.saveTokens(accessToken, refreshToken)
        tokenRepository.clearTokens()

        // Then
        assertNull(tokenRepository.getAccessToken())
        assertNull(tokenRepository.getRefreshToken())
    }
}
