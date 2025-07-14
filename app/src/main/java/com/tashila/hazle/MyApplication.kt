package com.tashila.hazle

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.tashila.hazle.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application(), LifecycleObserver {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(appModule)
        }
        addLifeCycleObserver()
        createNotificationChannel()
    }

    private fun addLifeCycleObserver() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                isAppInForeground = true
            }

            override fun onStop(owner: LifecycleOwner) {
                isAppInForeground = false
            }
        })
    }

    private fun createNotificationChannel() {
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

    companion object {
        const val CHAT_NOTIFICATIONS_CHANNEL_ID = "chat_notifications"
        const val FOREGROUND_NOTIFICATION_CHANNEL_ID = "foreground_api_channel"

        private var isAppInForeground = false
        fun isAppInForeground(): Boolean {
            return isAppInForeground
        }
    }
}