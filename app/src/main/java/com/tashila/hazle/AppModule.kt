package com.tashila.hazle

import android.app.Application
import androidx.room.Room
import com.tashila.hazle.api.ApiService
import com.tashila.hazle.api.ApiServiceImpl
import com.tashila.hazle.api.AuthApiService
import com.tashila.hazle.api.AuthApiServiceImpl
import com.tashila.hazle.db.MainDatabase
import com.tashila.hazle.db.messages.MessageDao
import com.tashila.hazle.db.threads.ThreadDao
import com.tashila.hazle.features.auth.AuthRepository
import com.tashila.hazle.features.auth.AuthRepositoryImpl
import com.tashila.hazle.features.auth.AuthViewModel
import com.tashila.hazle.features.auth.TokenRepository
import com.tashila.hazle.features.auth.TokenRepositoryImpl
import com.tashila.hazle.features.chat.ChatRepository
import com.tashila.hazle.features.chat.ChatRepositoryImpl
import com.tashila.hazle.features.chat.ChatViewModel
import com.tashila.hazle.features.thread.ThreadRepository
import com.tashila.hazle.features.thread.ThreadRepositoryImpl
import com.tashila.hazle.features.thread.ThreadsViewModel
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
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    // --- HttpClient Providers ---
    single(named("AuthHttpClient")) { provideHttpClient() } // Dedicated HttpClient for Auth calls (NO Auth plugin)
    single { provideAuthenticatedHttpClient(get(), get()) } // Main HttpClient for other API calls (WITH Auth plugin)

    // --- API Service Providers ---
    single<AuthApiService> { AuthApiServiceImpl(get(named("AuthHttpClient"))) }
    single<ApiService> { ApiServiceImpl(get()) } // 'get()' resolves the non-named HttpClient

    // --- Repository Providers ---
    single<TokenRepository> { TokenRepositoryImpl(androidApplication()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<ChatRepository> { ChatRepositoryImpl(get(), get(), get()) }
    single<ThreadRepository> { ThreadRepositoryImpl(get()) }

    // --- Room Database ---
    single { provideDatabase(get()) }
    single { provideMessageDao(get()) }
    single { provideThreadDao(get()) }

    // --- View Models ---
    viewModel { AuthViewModel(get()) }
    viewModel { ThreadsViewModel(get()) }
    viewModel { ChatViewModel(get(), get()) }
}

// Dedicated HttpClient for Auth calls (without Auth plugin to avoid circular dependencies)
fun provideHttpClient(): HttpClient {
    return HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
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
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
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

private const val TAG = "AppModule"