package com.tashila.hazle.features.subscription

import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubscriptionManager(private val purchases: Purchases) {

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed = _isSubscribed.asStateFlow()

    init {
        Purchases.sharedInstance.getCustomerInfoWith(
            onError = { _ ->
                _isSubscribed.value = false
            },
            onSuccess = { customerInfo ->
                _isSubscribed.value = customerInfo.entitlements["Hazle Pro"]?.isActive == true
            }
        )
    }

    fun getCustomerInfo(callback: ReceiveCustomerInfoCallback) {
        purchases.getCustomerInfo(callback)
    }
}