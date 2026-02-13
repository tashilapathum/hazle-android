package com.tashila.hazle.features.paywall

import android.app.Activity
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseResult
import kotlinx.coroutines.flow.Flow

interface RevenueCatRepository {
    val customerInfo: Flow<CustomerInfo>
    val offerings: Flow<Offerings>

    suspend fun purchasePackage(activity: Activity, packageToPurchase: Package): PurchaseResult

    fun hasProAccess(customerInfo: CustomerInfo): Boolean
    fun hasVipAccess(customerInfo: CustomerInfo): Boolean
}
