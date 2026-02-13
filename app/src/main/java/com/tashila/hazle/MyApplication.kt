package com.tashila.hazle

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.tashila.hazle.di.appModule
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions
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
        initializeSentry()
        initializeRevenueCat()
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
            description = "Notifications for chat responses."
            enableLights(true)
            enableVibration(true)
        }

        val serviceChannel = NotificationChannel(
            FOREGROUND_NOTIFICATION_CHANNEL_ID,
            "Background Operations",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notifications when processing requests in the background."
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(chatChannel)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun initializeSentry() {
        if (BuildConfig.DEBUG) return

        SentryAndroid.init(this) { options: SentryAndroidOptions ->
            options.dsn = "https://d5f49007f580761e93cb6132149c7aeb@o4509654455418880.ingest.de.sentry.io/4509654457253968"
            options.isEnableUserInteractionBreadcrumbs = true
            options.isEnableUserInteractionTracing = true
            options.isEnableScreenTracking = false
            options.tracesSampleRate = 0.25
            options.environment = "production"
        }
    }

    private fun initializeRevenueCat() {
        Purchases.logLevel = if (BuildConfig.DEBUG) LogLevel.VERBOSE else LogLevel.ERROR
        Purchases.configure(
            PurchasesConfiguration.Builder(
                this, getString(R.string.revenuecat_api_key)
            ).build()
        )
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