package com.tashila.hazle.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

object AppDestinations {

    /*----------------------------- Auth -----------------------------*/

    const val HOME_ROUTE = "home"
    const val SIGNUP_ROUTE = "signup"
    const val LOGIN_ROUTE = "login"
    const val VERIFY_ROUTE = "verify"

    /*----------------------------- Main -----------------------------*/

    const val THREADS_ROUTE = "threads"
    const val SETTINGS_ROUTE = "settings"
    const val ONBOARDING_ROUTE = "onboarding"
    const val UPGRADE_ROUTE = "upgrade"

    /*----------------------------- Chats -----------------------------*/

    const val CHAT_VIEW_BASE_ROUTE = "chat"
    // The argument name must match the one used in navArgument()
    const val CHAT_VIEW_ROUTE_WITH_ARGS = "$CHAT_VIEW_BASE_ROUTE/{localThreadId}"
    fun chatDetailRoute(localThreadId: Long? = null): String {
        return "$CHAT_VIEW_BASE_ROUTE/${localThreadId ?: -1L}" // Use -1L as a placeholder for null/new chat
    }

    // Define navigation arguments for the composable
    val CHAT_DETAIL_ARGS = listOf(
        navArgument("localThreadId") {
            type = NavType.LongType
            defaultValue = -1L // Default value if argument is not provided (e.g., direct deep link without ID)
            nullable = false // Argument type is Long, which cannot be null, so use a default value
        }
    )
}