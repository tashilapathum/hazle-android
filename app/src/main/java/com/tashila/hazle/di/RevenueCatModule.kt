package com.tashila.hazle.di

import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.tashila.hazle.BuildConfig
import org.koin.dsl.module

val revenueCatModule = module {
    single {
        Purchases.configure(PurchasesConfiguration.Builder(
            get(), BuildConfig.REVENUECAT_API_KEY).build()
        )
        Purchases.sharedInstance
    }
}
