package com.technitedminds.wallet.presentation.navigation

import androidx.compose.runtime.Composable
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
import com.technitedminds.wallet.presentation.screens.settings.SettingsScreen
import com.technitedminds.wallet.presentation.constants.AppConstants

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
                    navController.navigateToDetail(NavigationDestinations.CardDetail.createRoute(card.id))
                },
                onAddCardClick = {
                    navController.navigateToDetail(NavigationDestinations.AddCard.route)
                }
            )
        }
        
        // Add card screen
        composable(NavigationDestinations.AddCard.route) { _ ->
            // Check for camera capture results
            val frontImagePath = navController.getNavigationResult<String>(NavigationResultKeys.FRONT_IMAGE_PATH)
            val backImagePath = navController.getNavigationResult<String>(NavigationResultKeys.BACK_IMAGE_PATH)
            val extractedData = navController.getNavigationResult<Map<String, String>>(NavigationResultKeys.EXTRACTED_DATA)
            
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
                    navController.navigateToDetail(NavigationDestinations.Camera.createRoute(cardType))
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
            val cardTypeArg = backStackEntry.arguments?.getString(NavigationParams.CARD_TYPE) ?: AppConstants.Defaults.DEFAULT_CARD_TYPE
            val cardType = parseCardTypeFromString(cardTypeArg)
            
            CameraScreen(
                cardType = cardType,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onImagesConfirmed = { frontImagePath, backImagePath, extractedData ->
                    // Navigate back to add card with captured data
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set(NavigationResultKeys.FRONT_IMAGE_PATH, frontImagePath)
                        set(NavigationResultKeys.BACK_IMAGE_PATH, backImagePath)
                        set(NavigationResultKeys.EXTRACTED_DATA, extractedData)
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
            val cardId = backStackEntry.arguments?.getString(NavigationParams.CARD_ID) ?: ""
            
            CardDetailScreen(
                cardId = cardId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { editCardId ->
                    navController.navigateToEdit(NavigationDestinations.EditCard.createRoute(editCardId))
                }
            )
        }
        
        // Edit card screen (reuses AddCardScreen)
        composable(
            route = NavigationDestinations.EditCard.route,
            arguments = NavigationDestinations.EditCard.arguments
        ) { backStackEntry ->
            val editCardId = backStackEntry.arguments?.getString(NavigationParams.CARD_ID) ?: ""
            
            AddCardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCardSaved = { card ->
                    // Navigate back to card detail after editing
                    navController.navigate(NavigationDestinations.CardDetail.createRoute(card.id)) {
                        popUpTo(NavigationDestinations.CardDetail.createRoute(editCardId)) {
                            inclusive = true
                        }
                    }
                },
                onCameraCapture = { cardType ->
                    navController.navigateToDetail(NavigationDestinations.Camera.createRoute(cardType))
                }
            )
        }
        
        // Categories screen
        composable(NavigationDestinations.Categories.route) {
            CategoriesScreen()
        }
        
        // Settings screen
        composable(NavigationDestinations.Settings.route) {
            SettingsScreen()
        }
    }
}

/**
 * Parse CardType from string representation
 */
private fun parseCardTypeFromString(cardTypeString: String): CardType {
    return CardType.fromSerializableName(cardTypeString)
}

