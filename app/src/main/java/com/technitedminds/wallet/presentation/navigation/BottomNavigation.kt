package com.technitedminds.wallet.presentation.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.technitedminds.wallet.presentation.constants.AppConstants

/**
 * Bottom navigation bar for main app screens
 */
@Composable
fun WalletBottomNavigation(
    navController: NavController,
    modifier: Modifier = Modifier,
    categoryCount: Int = 0
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route
            
            NavigationBarItem(
                icon = {
                    BadgedBox(
                        badge = {
                            if (item.route == NavigationDestinations.Categories.route && categoryCount > 0) {
                                Badge {
                                    Text(
                                        text = categoryCount.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_LARGE)
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(NavigationDestinations.Home.route) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

/**
 * Bottom navigation item data class
 */
private data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * List of bottom navigation items - using the predefined destinations
 */
private val bottomNavItems = NavigationDestinations.getBottomNavDestinations().map { destination ->
    BottomNavItem(
        route = destination.route,
        label = when (destination) {
            NavigationDestinations.Home -> AppConstants.NavigationLabels.HOME
            NavigationDestinations.Categories -> AppConstants.NavigationLabels.CATEGORIES
            NavigationDestinations.Settings -> AppConstants.NavigationLabels.SETTINGS
            else -> destination.route.replaceFirstChar { it.uppercase() }
        },
        selectedIcon = when (destination) {
            NavigationDestinations.Home -> Icons.Filled.Home
            NavigationDestinations.Categories -> Icons.Filled.Category
            NavigationDestinations.Settings -> Icons.Filled.Settings
            else -> Icons.Filled.Home
        },
        unselectedIcon = when (destination) {
            NavigationDestinations.Home -> Icons.Outlined.Home
            NavigationDestinations.Categories -> Icons.Outlined.Category
            NavigationDestinations.Settings -> Icons.Outlined.Settings
            else -> Icons.Outlined.Home
        }
    )
}

/**
 * Check if current route should show bottom navigation
 */
fun shouldShowBottomNavigation(currentRoute: String?): Boolean {
    return currentRoute in bottomNavItems.map { it.route }
}