package com.tashila.hazle.features.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.models.StoreTransaction
import com.tashila.hazle.utils.ENTITLEMENT_PRO
import com.tashila.hazle.utils.ENTITLEMENT_VIP
import com.tashila.hazle.utils.PACKAGE_ANNUAL
import com.tashila.hazle.utils.PACKAGE_LIFETIME
import com.tashila.hazle.utils.PACKAGE_MONTHLY
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
    object Restored : PurchaseState()
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
    private val _isSubscribed = MutableStateFlow(false)
    private val _currentPlan = MutableStateFlow<String?>(null)

    /**
     * The UI state for the paywall screen.
     */
    val uiState = _uiState.asStateFlow()
    val isSubscribed = _isSubscribed.asStateFlow()
    val currentPlan = _currentPlan.asStateFlow()

    init {
        loadOfferings()
        viewModelScope.launch {
            revenueCatRepository.customerInfo.collect { customerInfo ->
                val isPro = customerInfo.entitlements.all[ENTITLEMENT_PRO]?.isActive == true
                val isLifetime = customerInfo.entitlements.all[ENTITLEMENT_VIP]?.isActive == true

                _isSubscribed.value = isPro || isLifetime

                _currentPlan.value = when {
                    isPro -> "pro"
                    isLifetime -> "lifetime"
                    else -> null
                }
            }
        }
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
                            monthly = runCatching { current.getPackage(PACKAGE_MONTHLY) }.getOrNull(),
                            annual = runCatching { current.getPackage(PACKAGE_ANNUAL) }.getOrNull(),
                            lifetime = runCatching { current.getPackage(PACKAGE_LIFETIME) }.getOrNull(),
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

    fun restorePurchases() {
        viewModelScope.launch {
            _uiState.update { it.copy(purchaseState = PurchaseState.InProgress) }
            try {
                val customerInfo = revenueCatRepository.restorePurchases()
                _uiState.update {
                    if (customerInfo.entitlements.active.isNotEmpty()) {
                        it.copy(purchaseState = PurchaseState.Restored)
                    } else {
                        it.copy(purchaseState = PurchaseState.Failure("No active subscriptions found"))
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(purchaseState = PurchaseState.Failure(e.message ?: "An error occurred")) }
            }
        }
    }

    fun resetPurchaseState() {
        _uiState.update { it.copy(purchaseState = PurchaseState.NotStarted) }
    }
}
