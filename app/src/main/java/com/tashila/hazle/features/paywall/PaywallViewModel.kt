package com.tashila.hazle.features.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.models.StoreTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class PurchaseState {
    object NotStarted : PurchaseState()
    object InProgress : PurchaseState()
    data class Success(val storeTransaction: StoreTransaction) : PurchaseState()
    data class Failure(val message: String) : PurchaseState()
    object Cancelled : PurchaseState()
}

/**
 * Represents the state of the paywall screen.
 */
data class PaywallState(
    val isLoading: Boolean = false,
    val monthly: Package? = null,
    val annual: Package? = null,
    val lifetime: Package? = null,
    val error: String? = null,
    val purchaseState: PurchaseState = PurchaseState.NotStarted
)

/**
 * ViewModel for the paywall screen.
 * It is responsible for loading subscription offerings and handling the purchase flow.
 */
class PaywallViewModel(
    private val revenueCatRepository: RevenueCatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaywallState(isLoading = true))

    /**
     * The UI state for the paywall screen.
     */
    val uiState = _uiState.asStateFlow()

    init {
        loadOfferings()
    }

    /**
     * Loads the available subscription packages from RevenueCat.
     */
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

    /**
     * Initiates the purchase of a subscription package.
     * @param activity The current activity.
     * @param aPackage The package to purchase.
     */
    fun purchasePackage(activity: Activity, aPackage: Package) {
        viewModelScope.launch {
            _uiState.update { it.copy(purchaseState = PurchaseState.InProgress) }
            val purchaseResult = revenueCatRepository.purchasePackage(activity, aPackage)
            _uiState.update {
                val newPurchaseState = when {
                    purchaseResult == null -> PurchaseState.Failure("Purchase failed")
                    purchaseResult.customerInfo.entitlements.active.isNotEmpty() -> PurchaseState.Success(purchaseResult.storeTransaction)
                    else -> PurchaseState.Failure("Purchase failed")
                }
                it.copy(purchaseState = newPurchaseState)
            }
        }
    }

    fun resetPurchaseState() {
        _uiState.update { it.copy(purchaseState = PurchaseState.NotStarted) }
    }
}
