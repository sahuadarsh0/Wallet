package com.technitedminds.wallet.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.technitedminds.wallet.presentation.screens.home.HomeViewModel

/**
 * Main app scaffold with bottom navigation
 */
@Composable
fun WalletAppScaffold(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Get category count from HomeViewModel
    val homeUiState by homeViewModel.uiState.collectAsState()
    val categoryCount = homeUiState.categories.size
    
    Scaffold(
        bottomBar = {
            if (shouldShowBottomNavigation(currentRoute)) {
                WalletBottomNavigation(
                    navController = navController,
                    categoryCount = categoryCount
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        WalletNavigation(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}