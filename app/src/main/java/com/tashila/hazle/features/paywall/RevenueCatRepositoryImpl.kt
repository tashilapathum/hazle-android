package com.tashila.hazle.features.paywall

import android.app.Activity
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.PurchaseResult
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RevenueCatRepositoryImpl : RevenueCatRepository {

    override val customerInfo: Flow<CustomerInfo> = flow { emit(Purchases.sharedInstance.awaitCustomerInfo()) }
    override val offerings: Flow<Offerings> = flow { emit(Purchases.sharedInstance.awaitOfferings()) }

    override suspend fun purchasePackage(activity: Activity, packageToPurchase: Package): PurchaseResult {
        val purchaseParams = PurchaseParams.Builder(activity, packageToPurchase).build()
        return Purchases.sharedInstance.awaitPurchase(purchaseParams)
    }

    override fun hasProAccess(customerInfo: CustomerInfo): Boolean {
        return customerInfo.entitlements["pro_access"]?.isActive == true
    }

    override fun hasVipAccess(customerInfo: CustomerInfo): Boolean {
        return customerInfo.entitlements["vip_access"]?.isActive == true
    }
}
