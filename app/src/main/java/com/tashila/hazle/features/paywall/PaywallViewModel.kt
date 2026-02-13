package com.tashila.hazle.features.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PaywallState(
    val isLoading: Boolean = false,
    val monthly: Package? = null,
    val annual: Package? = null,
    val lifetime: Package? = null,
    val error: String? = null,
    val purchaseResult: PurchaseResult? = null
)

class PaywallViewModel(
    private val revenueCatRepository: RevenueCatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaywallState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        loadOfferings()
    }

    private fun loadOfferings() {
        viewModelScope.launch {
            revenueCatRepository.offerings.collect { offerings ->
                val current = offerings.current
                if (current != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            monthly = runCatching { current.getPackage($$"$rc_monthly") }.getOrNull(),
                            annual = runCatching { current.getPackage($$"$rc_annual") }.getOrNull(),
                            lifetime = runCatching { current.getPackage($$"$rc_lifetime") }.getOrNull(),
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No offerings found"
                        )
                    }
                }
            }
        }
    }

    fun purchasePackage(activity: Activity, aPackage: Package) {
        viewModelScope.launch {
            val purchaseResult = revenueCatRepository.purchasePackage(activity, aPackage)
            _uiState.update {
                it.copy(purchaseResult = purchaseResult)
            }
        }
    }
}