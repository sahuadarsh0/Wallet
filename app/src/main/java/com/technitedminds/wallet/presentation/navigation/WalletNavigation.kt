package com.technitedminds.wallet.presentation.navigation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.presentation.screens.addcard.AddCardScreen
import com.technitedminds.wallet.presentation.screens.camera.CameraScreen
import com.technitedminds.wallet.presentation.screens.carddetail.CardDetailScreen
import com.technitedminds.wallet.presentation.screens.categories.CategoriesScreen
import com.technitedminds.wallet.presentation.screens.home.EnhancedHomeScreen
import com.technitedminds.wallet.presentation.screens.settings.SettingsScreen
import com.technitedminds.wallet.presentation.constants.AppConstants

// Smooth easing curves for jerk-free screen transitions
private val EaseOut = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
private val EaseIn = CubicBezierEasing(0.6f, 0f, 0.84f, 0f)
private const val ENTER_MS = 380
private const val EXIT_MS = 280

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
        // Home screen - using enhanced version
        composable(
            route = NavigationDestinations.Home.route,
            exitTransition = {
                if (targetState.destination.route == NavigationDestinations.CardDetail.route) {
                    fadeOut(tween(EXIT_MS, easing = EaseIn)) +
                        scaleOut(targetScale = 0.96f, animationSpec = tween(EXIT_MS, easing = EaseIn))
                } else {
                    null
                }
            },
            popEnterTransition = {
                if (initialState.destination.route == NavigationDestinations.CardDetail.route) {
                    fadeIn(tween(ENTER_MS, easing = EaseOut)) +
                        scaleIn(initialScale = 0.96f, animationSpec = tween(ENTER_MS, easing = EaseOut))
                } else {
                    null
                }
            },
        ) {
            EnhancedHomeScreen(
                onCardClick = { card ->
                    navController.navigateToDetail(NavigationDestinations.CardDetail.createRoute(card.id))
                },
                onAddCardClick = {
                    navController.navigateToDetail(NavigationDestinations.AddCard.route)
                }
            )
        }
        
        // Add card screen
        composable(NavigationDestinations.AddCard.route) { backStackEntry ->
            // Observe camera capture results reactively
            val savedStateHandle = backStackEntry.savedStateHandle
            val frontImagePath by savedStateHandle.getStateFlow<String?>(NavigationResultKeys.FRONT_IMAGE_PATH, null).collectAsStateWithLifecycle()
            val backImagePath by savedStateHandle.getStateFlow<String?>(NavigationResultKeys.BACK_IMAGE_PATH, null).collectAsStateWithLifecycle()
            val extractedData by savedStateHandle.getStateFlow<Map<String, String>?>(NavigationResultKeys.EXTRACTED_DATA, null).collectAsStateWithLifecycle()
            
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
            arguments = NavigationDestinations.CardDetail.arguments,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it / 5 },
                    animationSpec = tween(ENTER_MS, easing = EaseOut),
                ) + fadeIn(tween(ENTER_MS, easing = EaseOut)) +
                    scaleIn(initialScale = 0.95f, animationSpec = tween(ENTER_MS, easing = EaseOut))
            },
            exitTransition = {
                fadeOut(tween(EXIT_MS, easing = EaseIn)) +
                    scaleOut(targetScale = 0.97f, animationSpec = tween(EXIT_MS, easing = EaseIn))
            },
            popEnterTransition = {
                fadeIn(tween(ENTER_MS, easing = EaseOut)) +
                    scaleIn(initialScale = 0.97f, animationSpec = tween(ENTER_MS, easing = EaseOut))
            },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { it / 5 },
                    animationSpec = tween(EXIT_MS, easing = EaseIn),
                ) + fadeOut(tween(EXIT_MS, easing = EaseIn)) +
                    scaleOut(targetScale = 0.95f, animationSpec = tween(EXIT_MS, easing = EaseIn))
            },
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

