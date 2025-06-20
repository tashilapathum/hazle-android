package com.tashila.hazle

import android.app.Application
import androidx.room.Room
import com.tashila.hazle.api.ApiService
import com.tashila.hazle.api.ApiServiceImpl
import com.tashila.hazle.db.MainDatabase
import com.tashila.hazle.db.messages.MessageDao
import com.tashila.hazle.db.threads.ThreadDao
import com.tashila.hazle.features.auth.AuthRepository
import com.tashila.hazle.features.auth.AuthRepositoryImpl
import com.tashila.hazle.features.auth.AuthViewModel
import com.tashila.hazle.features.auth.TokenStorage
import com.tashila.hazle.features.auth.TokenStorageImpl
import com.tashila.hazle.features.chat.ChatRepository
import com.tashila.hazle.features.chat.ChatRepositoryImpl
import com.tashila.hazle.features.chat.ChatViewModel
import com.tashila.hazle.features.thread.ThreadRepository
import com.tashila.hazle.features.thread.ThreadRepositoryImpl
import com.tashila.hazle.features.thread.ThreadsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Single instance of HttpClient
    single { provideHttpClient() }

    single<ApiService> { ApiServiceImpl(get()) } // 'get()' resolves HttpClient from Koin
    single<TokenStorage> { TokenStorageImpl(androidApplication()) }
    single<ChatRepository> { ChatRepositoryImpl(get(), get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<ThreadRepository> { ThreadRepositoryImpl(get()) }

    // Room Database
    single { provideDatabase(get()) }
    single { provideMessageDao(get()) }
    single { provideThreadDao(get()) }

    // View Models
    viewModel { AuthViewModel(get()) }
    viewModel { ThreadsViewModel(get()) }
    viewModel { ChatViewModel(get(), get()) }
}

fun provideHttpClient(): HttpClient {
    return HttpClient(Android) { // Use Android engine
        install(ContentNegotiation) { // For JSON serialization/deserialization
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.ALL // TODO
        }
    }
}

fun provideDatabase(application: Application): MainDatabase {
    return Room.databaseBuilder(
        application,
        MainDatabase::class.java,
        "main_db"
    )
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
}

fun provideMessageDao(database: MainDatabase): MessageDao {
    return database.messageDao()
}

fun provideThreadDao(database: MainDatabase): ThreadDao {
    return database.threadDao()
}