package com.technitedminds.wallet.presentation.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.ui.theme.Glass
import com.technitedminds.wallet.ui.theme.GlassSurface
import com.technitedminds.wallet.ui.theme.WalletSpring

/**
 * Floating pill-shaped bottom navigation bar with spring-animated
 * selection indicator, glass morphism background, and haptic feedback.
 */
@Composable
fun WalletBottomNavigation(
    navController: NavController,
    modifier: Modifier = Modifier,
    categoryCount: Int = 0,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val haptic = LocalHapticFeedback.current

    // Entrance spring — pill bounces in from below
    var appeared by remember { mutableStateOf(false) }
    val entranceTranslateY by animateFloatAsState(
        targetValue = if (appeared) 0f else 120f,
        animationSpec = WalletSpring.bouncy(),
        label = "pill_entrance",
    )
    val entranceAlpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = WalletSpring.gentle(),
        label = "pill_alpha",
    )
    LaunchedEffect(Unit) { appeared = true }

    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = entranceTranslateY
                alpha = entranceAlpha
            }
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        GlassSurface(
            shape = RoundedCornerShape(Glass.PillCornerRadius),
            useElevated = true,
            modifier = Modifier
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(Glass.PillCornerRadius),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                ),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = currentRoute == item.route

                    PillNavItem(
                        icon = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        label = item.label,
                        isSelected = isSelected,
                        badgeCount = if (item.route == NavigationDestinations.Categories.route) categoryCount else 0,
                        onClick = {
                            if (currentRoute != item.route) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                navController.navigate(item.route) {
                                    popUpTo(NavigationDestinations.Home.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PillNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit,
) {
    // Spring-animated selection scale
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = WalletSpring.bouncy(),
        label = "nav_icon_scale",
    )

    // Press dip
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = WalletSpring.bouncy(),
        label = "nav_press_scale",
    )

    // Selection indicator size
    val indicatorSize by animateDpAsState(
        targetValue = if (isSelected) 48.dp else 0.dp,
        animationSpec = WalletSpring.snappy(),
        label = "nav_indicator",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(56.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                isPressed = true
                onClick()
            }
            .graphicsLayer {
                scaleX = iconScale * pressScale
                scaleY = iconScale * pressScale
            },
    ) {
        // Selected indicator background
        if (indicatorSize > 0.dp) {
            Box(
                modifier = Modifier
                    .size(indicatorSize)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    ),
            )
        }

        // Icon
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(24.dp),
        )

        // Badge
        if (badgeCount > 0) {
            val badgeScale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = WalletSpring.bouncy(),
                label = "badge_scale",
            )
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .graphicsLayer {
                        scaleX = badgeScale
                        scaleY = badgeScale
                    },
            ) {
                Text(
                    text = badgeCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }

    // Reset press state
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

// --- Data ---

private data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

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
        },
    )
}

/**
 * Check if current route should show bottom navigation
 */
fun shouldShowBottomNavigation(currentRoute: String?): Boolean {
    return currentRoute in bottomNavItems.map { it.route }
}
