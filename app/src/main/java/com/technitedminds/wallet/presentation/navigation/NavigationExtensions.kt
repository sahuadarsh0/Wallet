package com.technitedminds.wallet.presentation.navigation

import androidx.navigation.NavController

/**
 * Navigation extensions for common navigation patterns
 */

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