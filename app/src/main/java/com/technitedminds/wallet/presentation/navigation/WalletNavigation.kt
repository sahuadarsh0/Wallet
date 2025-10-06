package com.technitedminds.wallet.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.presentation.screens.addcard.AddCardScreen
import com.technitedminds.wallet.presentation.screens.camera.CameraScreen
import com.technitedminds.wallet.presentation.screens.carddetail.CardDetailScreen
import com.technitedminds.wallet.presentation.screens.categories.CategoriesScreen
import com.technitedminds.wallet.presentation.screens.home.HomeScreen

/**
 * Main navigation component for the CardVault app
 */
@Composable
fun WalletNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavigationDestinations.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Home screen
        composable(NavigationDestinations.Home.route) {
            HomeScreen(
                onCardClick = { card ->
                    navController.navigate(NavigationDestinations.CardDetail.createRoute(card.id))
                },
                onAddCardClick = {
                    navController.navigate(NavigationDestinations.AddCard.route)
                }
            )
        }
        
        // Add card screen
        composable(NavigationDestinations.AddCard.route) { backStackEntry ->
            // Check for camera capture results
            val frontImagePath = navController.currentBackStackEntry?.savedStateHandle?.get<String>("frontImagePath")
            val backImagePath = navController.currentBackStackEntry?.savedStateHandle?.get<String>("backImagePath")
            val extractedData = navController.currentBackStackEntry?.savedStateHandle?.get<Map<String, String>>("extractedData")
            
            AddCardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCardSaved = { card ->
                    // Navigate to card detail after saving
                    navController.navigate(NavigationDestinations.CardDetail.createRoute(card.id)) {
                        popUpTo(NavigationDestinations.Home.route)
                    }
                },
                onCameraCapture = { cardType ->
                    navController.navigate(NavigationDestinations.Camera.createRoute(cardType))
                },
                capturedFrontImagePath = frontImagePath,
                capturedBackImagePath = backImagePath,
                capturedExtractedData = extractedData ?: emptyMap()
            )
        }
        
        // Camera screen
        composable(
            route = NavigationDestinations.Camera.route,
            arguments = NavigationDestinations.Camera.arguments
        ) { backStackEntry ->
            val cardTypeArg = backStackEntry.arguments?.getString("cardType") ?: "Credit"
            val cardType = parseCardTypeFromString(cardTypeArg)
            
            CameraScreen(
                cardType = cardType,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onImagesConfirmed = { frontImagePath, backImagePath, extractedData ->
                    // Navigate back to add card with captured data
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("frontImagePath", frontImagePath)
                        set("backImagePath", backImagePath)
                        set("extractedData", extractedData)
                    }
                    navController.popBackStack()
                }
            )
        }
        
        // Card detail screen
        composable(
            route = NavigationDestinations.CardDetail.route,
            arguments = NavigationDestinations.CardDetail.arguments
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId") ?: ""
            
            CardDetailScreen(
                cardId = cardId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { cardId ->
                    navController.navigate(NavigationDestinations.EditCard.createRoute(cardId))
                }
            )
        }
        
        // Edit card screen (reuses AddCardScreen)
        composable(
            route = NavigationDestinations.EditCard.route,
            arguments = NavigationDestinations.EditCard.arguments
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId") ?: ""
            
            AddCardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCardSaved = { card ->
                    // Navigate back to card detail after editing
                    navController.navigate(NavigationDestinations.CardDetail.createRoute(card.id)) {
                        popUpTo(NavigationDestinations.CardDetail.createRoute(cardId)) {
                            inclusive = true
                        }
                    }
                },
                onCameraCapture = { cardType ->
                    navController.navigate(NavigationDestinations.Camera.createRoute(cardType))
                }
            )
        }
        
        // Categories screen
        composable(NavigationDestinations.Categories.route) {
            CategoriesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Settings screen (placeholder)
        composable(NavigationDestinations.Settings.route) {
            // TODO: Implement SettingsScreen
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Settings Screen - Coming Soon")
            }
        }
    }
}

/**
 * Parse CardType from string representation
 */
private fun parseCardTypeFromString(cardTypeString: String): CardType {
    return CardType.fromSerializableName(cardTypeString)
}

