package com.tashila.hazle.features.paywall

import android.app.Activity
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.PurchaseResult
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesTransactionException
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementation of [RevenueCatRepository] using the RevenueCat SDK.
 * This class handles all interactions with RevenueCat for subscriptions.
 */
class RevenueCatRepositoryImpl : RevenueCatRepository {

    /**
     * A flow that emits the latest [CustomerInfo] for the user.
     */
    override val customerInfo: Flow<CustomerInfo> = flow { emit(Purchases.sharedInstance.awaitCustomerInfo()) }

    /**
     * A flow that emits the available [Offerings] from RevenueCat.
     */
    override val offerings: Flow<Offerings> = flow { emit(Purchases.sharedInstance.awaitOfferings()) }

    /**
     * Initiates a purchase for a specific [Package].
     * @param activity The current activity.
     * @param packageToPurchase The package to purchase.
     * @return The result of the purchase, or null if it fails.
     */
    override suspend fun purchasePackage(activity: Activity, packageToPurchase: Package): PurchaseResult? {
        val purchaseParams = PurchaseParams.Builder(activity, packageToPurchase).build()
        return try {
            Purchases.sharedInstance.awaitPurchase(purchaseParams)
        } catch (e: PurchasesTransactionException) {
            null
        }
    }

    /**
     * Checks if the user has "pro" access based on their entitlements.
     * @param customerInfo The user's customer info.
     * @return True if the user has "pro" access, false otherwise.
     */
    override fun hasProAccess(customerInfo: CustomerInfo): Boolean {
        return customerInfo.entitlements[ENTITLEMENT_PRO]?.isActive == true
    }

    /**
     * Checks if the user has "vip" access based on their entitlements.
     * @param customerInfo The user's customer info.
     * @return True if the user has "vip" access, false otherwise.
     */
    override fun hasVipAccess(customerInfo: CustomerInfo): Boolean {
        return customerInfo.entitlements[ENTITLEMENT_VIP]?.isActive == true
    }

    companion object {
        private const val ENTITLEMENT_PRO = "Hazle Pro"
        private const val ENTITLEMENT_VIP = "Hazle VIP"
    }
}
