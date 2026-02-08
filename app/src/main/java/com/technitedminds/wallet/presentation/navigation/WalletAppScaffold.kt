package com.technitedminds.wallet.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.technitedminds.wallet.presentation.screens.home.HomeViewModel
import com.technitedminds.wallet.ui.theme.WalletSpring

/**
 * Main app scaffold with floating pill bottom navigation overlay.
 *
 * Uses a Box layout instead of Scaffold bottomBar so the pill
 * floats above content rather than pushing it up.
 */
@Composable
fun WalletAppScaffold(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val homeUiState by homeViewModel.uiState.collectAsState()
    val categoryCount = homeUiState.categories.size
    val showBottomNav = shouldShowBottomNavigation(currentRoute)

    Box(modifier = modifier.fillMaxSize()) {
        // Main content fills entire screen
        WalletNavigation(
            navController = navController,
            modifier = Modifier.fillMaxSize(),
        )

        // Floating pill nav overlaid on top
        AnimatedVisibility(
            visible = showBottomNav,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = WalletSpring.bouncy(),
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = WalletSpring.gentle(),
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            WalletBottomNavigation(
                navController = navController,
                categoryCount = categoryCount,
            )
        }
    }
}
