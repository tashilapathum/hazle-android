package com.tashila.hazle.di

import com.tashila.hazle.api.ApiService
import com.tashila.hazle.api.ApiServiceImpl
import com.tashila.hazle.api.AuthApiService
import com.tashila.hazle.api.AuthApiServiceImpl
import com.tashila.hazle.features.auth.AuthRepository
import com.tashila.hazle.features.auth.AuthRepositoryImpl
import com.tashila.hazle.features.auth.AuthViewModel
import com.tashila.hazle.features.auth.TokenRepository
import com.tashila.hazle.features.auth.TokenRepositoryImpl
import com.tashila.hazle.features.chat.ChatRepository
import com.tashila.hazle.features.chat.ChatRepositoryImpl
import com.tashila.hazle.features.chat.ChatViewModel
import com.tashila.hazle.features.settings.SettingsRepository
import com.tashila.hazle.features.settings.SettingsRepositoryImpl
import com.tashila.hazle.features.settings.SettingsViewModel
import com.tashila.hazle.features.thread.ThreadRepository
import com.tashila.hazle.features.thread.ThreadRepositoryImpl
import com.tashila.hazle.features.thread.ThreadsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    // --- HttpClient Providers ---
    single { provideJsonDecoder() }
    single(named("AuthHttpClient")) { provideHttpClient() } // Dedicated HttpClient for Auth calls (NO Auth plugin)
    single { provideAuthenticatedHttpClient(get(), get()) } // Main HttpClient for other API calls (WITH Auth plugin)

    // --- API Service Providers ---
    single<AuthApiService> {
        AuthApiServiceImpl(get(), get(named("AuthHttpClient")))
    }
    single<ApiService> {
        ApiServiceImpl(get(), get()) // Resolves the non-named HttpClient
    }

    // --- Repository Providers ---
    single<TokenRepository> { TokenRepositoryImpl(androidApplication()) }
    single<AuthRepository> { AuthRepositoryImpl(
        authApiService = get(),
        tokenRepository = get(),
        settingsRepository = get(),
        jsonDecoder = get()
    ) }
    single<ChatRepository> { ChatRepositoryImpl(
        apiService = get(),
        messageDao = get(),
        threadDao = get(),
        jsonDecoder = get()
    ) }
    single<ThreadRepository> { ThreadRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(androidContext()) }

    // --- Room Database ---
    single { provideDatabase(get()) }
    single { provideMessageDao(get()) }
    single { provideThreadDao(get()) }

    // --- View Models ---
    viewModel { AuthViewModel(get()) }
    viewModel { ThreadsViewModel(get()) }
    viewModel { ChatViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
}