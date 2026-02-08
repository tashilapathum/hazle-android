package com.tashila.hazle.features.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.getOfferingsWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaywallViewModel(private val purchases: Purchases) : ViewModel() {

    private val _offering = MutableStateFlow<Offering?>(null)
    val offering = _offering.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                purchases.getOfferingsWith { offerings ->
                    _offering.value = offerings.current
                }
            } catch (e: PurchasesException) {
                _error.value = e.message
            }
        }
    }
}