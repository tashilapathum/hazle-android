package com.tashila.hazle

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(appModule)
        }
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chatChannel = NotificationChannel(
                CHAT_NOTIFICATIONS_CHANNEL_ID,
                "Chat Responses",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for chat responses and API call results."
                enableLights(true)
                enableVibration(true)
            }

            val serviceChannel = NotificationChannel(
                FOREGROUND_NOTIFICATION_CHANNEL_ID,
                "Background API Operations",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for ongoing background API calls."
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(chatChannel)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val CHAT_NOTIFICATIONS_CHANNEL_ID = "chat_notifications"
        const val FOREGROUND_NOTIFICATION_CHANNEL_ID = "foreground_api_channel"
    }
}