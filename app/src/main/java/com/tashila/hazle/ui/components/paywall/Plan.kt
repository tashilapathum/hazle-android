package com.tashila.hazle.ui.components.paywall

import com.revenuecat.purchases.Package

data class Plan(
    val title: Int,
    val price: String?,
    val features: List<Int>,
    val ctaText: Int,
    val subtitle: Int? = null,
    val isRecommended: Boolean = false,
    val revenueCatPackage: Package? = null,
) {
    val isCtaEnabled: Boolean
        get() = revenueCatPackage != null
}
