package com.technitedminds.wallet.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Navigation extensions for common navigation patterns
 */

/**
 * Navigate to a destination with proper back stack management for bottom navigation
 */
fun NavController.navigateToBottomNavDestination(route: String) {
    navigate(route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }
}

/**
 * Navigate to a detail screen with proper back stack management
 */
fun NavController.navigateToDetail(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}

/**
 * Navigate to an edit screen with proper back stack management
 */
fun NavController.navigateToEdit(route: String, popUpToRoute: String? = null) {
    navigate(route) {
        launchSingleTop = true
        popUpToRoute?.let { popUpTo ->
            popUpTo(popUpTo) {
                inclusive = false
            }
        }
    }
}

/**
 * Navigate back with result handling
 */
fun NavController.navigateBackWithResult(key: String, result: Any) {
    previousBackStackEntry?.savedStateHandle?.set(key, result)
    popBackStack()
}

/**
 * Get result from navigation
 */
inline fun <reified T> NavController.getNavigationResult(key: String): T? {
    return currentBackStackEntry?.savedStateHandle?.get<T>(key)
}

/**
 * Clear navigation result
 */
fun NavController.clearNavigationResult(key: String) {
    currentBackStackEntry?.savedStateHandle?.remove<Any>(key)
}