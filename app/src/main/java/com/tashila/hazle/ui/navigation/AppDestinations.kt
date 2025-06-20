package com.tashila.hazle.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

object AppDestinations {
    const val THREADS_ROUTE = "threads_list"
    const val CHAT_VIEW_BASE_ROUTE = "chat_view"
    // The argument name must match the one used in navArgument()
    const val CHAT_VIEW_ROUTE_WITH_ARGS = "$CHAT_VIEW_BASE_ROUTE/{threadId}"
    fun chatDetailRoute(threadId: Long? = null): String {
        return "$CHAT_VIEW_BASE_ROUTE/${threadId ?: -1L}" // Use -1L as a placeholder for null/new chat
    }

    // Define navigation arguments for the composable
    val CHAT_DETAIL_ARGS = listOf(
        navArgument("threadId") {
            type = NavType.LongType
            defaultValue = -1L // Default value if argument is not provided (e.g., direct deep link without ID)
            nullable = false // Argument type is Long, which cannot be null, so use a default value
        }
    )
}