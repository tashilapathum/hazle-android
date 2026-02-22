package com.tashila.hazle.di

import android.content.Context
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.tashila.hazle.R
import org.koin.dsl.module

val revenueCatModule = module {
    single {
        val context = get<Context>()
        val apiKey = context.getString(R.string.revenuecat_api_key)

        Purchases.configure(
            PurchasesConfiguration.Builder(context, apiKey).build()
        )
        Purchases.sharedInstance
    }
}
