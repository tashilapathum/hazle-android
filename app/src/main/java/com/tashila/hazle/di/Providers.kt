package com.tashila.hazle.di

import android.app.Application
import androidx.room.Room
import com.tashila.hazle.db.MainDatabase
import com.tashila.hazle.db.messages.MessageDao
import com.tashila.hazle.db.threads.ThreadDao
import com.tashila.hazle.features.auth.AuthRepository
import com.tashila.hazle.features.auth.TokenRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun provideJsonDecoder(): Json {
    return Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }
}

// Dedicated HttpClient for Auth calls (without Auth plugin to avoid circular dependencies)
fun provideHttpClient(): HttpClient {
    return HttpClient(Android) {
        install(ContentNegotiation) {
            json(provideJsonDecoder())
        }
        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.ALL
        }
    }
}

// Main HttpClient for authenticated API calls (with Auth plugin to handle refresh token headers)
fun provideAuthenticatedHttpClient(
    tokenRepository: TokenRepository,
    authRepository: AuthRepository
): HttpClient {
    return HttpClient(Android) {
        install(ContentNegotiation) {
            json(provideJsonDecoder())
        }
        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.ALL
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val accessToken = tokenRepository.getAccessToken()
                    val refreshToken = tokenRepository.getRefreshToken()
                    if (accessToken != null && refreshToken != null) {
                        BearerTokens(accessToken, refreshToken)
                    } else {
                        null
                    }
                }
                refreshTokens {
                    val currentRefreshToken = tokenRepository.getRefreshToken()

                    if (currentRefreshToken != null) {
                        val success = try {
                            authRepository.refresh(currentRefreshToken)
                        } catch (e: Exception) {
                            false
                        }

                        if (success) {
                            val newAccessToken = tokenRepository.getAccessToken()
                            val newRefreshToken = tokenRepository.getRefreshToken()

                            if (newAccessToken != null && newRefreshToken != null) {
                                BearerTokens(newAccessToken, newRefreshToken)
                            } else {
                                null
                            }
                        } else {
                            tokenRepository.clearTokens()
                            null
                        }
                    } else {
                        null
                    }
                }

            }
        }
    }
}

fun provideDatabase(application: Application): MainDatabase {
    return Room.databaseBuilder(
        application,
        MainDatabase::class.java,
        "main_db"
    )
        .fallbackToDestructiveMigration(dropAllTables = true) //todo
        .build()
}

fun provideMessageDao(database: MainDatabase): MessageDao {
    return database.messageDao()
}

fun provideThreadDao(database: MainDatabase): ThreadDao {
    return database.threadDao()
}

