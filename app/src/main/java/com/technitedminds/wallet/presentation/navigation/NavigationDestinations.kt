package com.technitedminds.wallet.presentation.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.presentation.constants.AppConstants

/**
 * Navigation destinations for the CardVault app
 */
sealed class NavigationDestinations(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    
    /**
     * Home screen - main card list
     */
    object Home : NavigationDestinations(NavigationRoutes.HOME)
    
    /**
     * Add card screen - multi-step card creation
     */
    object AddCard : NavigationDestinations(NavigationRoutes.ADD_CARD)
    
    /**
     * Camera screen - card capture with OCR
     */
    object Camera : NavigationDestinations(
        route = "${NavigationRoutes.CAMERA}/{${NavigationParams.CARD_TYPE}}",
        arguments = listOf(
            navArgument(NavigationParams.CARD_TYPE) {
                type = NavType.StringType
                defaultValue = AppConstants.Defaults.DEFAULT_CARD_TYPE
            }
        )
    ) {
        fun createRoute(cardType: CardType): String {
            return "${NavigationRoutes.CAMERA}/${cardType.toNavigationString()}"
        }
    }
    
    /**
     * Card detail screen - view and manage individual card
     */
    object CardDetail : NavigationDestinations(
        route = "${NavigationRoutes.CARD_DETAIL}/{${NavigationParams.CARD_ID}}",
        arguments = listOf(
            navArgument(NavigationParams.CARD_ID) {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(cardId: String): String {
            return "${NavigationRoutes.CARD_DETAIL}/$cardId"
        }
    }
    
    /**
     * Edit card screen - modify existing card
     */
    object EditCard : NavigationDestinations(
        route = "${NavigationRoutes.EDIT_CARD}/{${NavigationParams.CARD_ID}}",
        arguments = listOf(
            navArgument(NavigationParams.CARD_ID) {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(cardId: String): String {
            return "${NavigationRoutes.EDIT_CARD}/$cardId"
        }
    }
    
    /**
     * Categories screen - manage card categories
     */
    object Categories : NavigationDestinations(NavigationRoutes.CATEGORIES)
    
    /**
     * Settings screen - app preferences and configuration
     */
    object Settings : NavigationDestinations(NavigationRoutes.SETTINGS)
    
    companion object {
        /**
         * Get all navigation destinations
         */
        fun getAllDestinations(): List<NavigationDestinations> = listOf(
            Home,
            AddCard,
            Camera,
            CardDetail,
            EditCard,
            Categories,
            Settings
        )
        
        /**
         * Get bottom navigation destinations (main tabs)
         */
        fun getBottomNavDestinations(): List<NavigationDestinations> = listOf(
            Home,
            Categories,
            Settings
        )
    }
}

/**
 * Navigation arguments data classes for type-safe parameter passing
 */
object NavigationArgs {
    
    /**
     * Arguments for camera screen
     */
    data class CameraArgs(
        val cardType: CardType
    )
    
    /**
     * Arguments for card detail screen
     */
    data class CardDetailArgs(
        val cardId: String
    )
    
    /**
     * Arguments for edit card screen
     */
    data class EditCardArgs(
        val cardId: String
    )
    
    /**
     * Result data for camera capture
     */
    data class CameraCaptureResult(
        val frontImagePath: String,
        val backImagePath: String?,
        val extractedData: Map<String, String>
    )
}

/**
 * Navigation routes as constants for easy reference
 */
object NavigationRoutes {
    const val HOME = "home"
    const val ADD_CARD = "add_card"
    const val CAMERA = "camera"
    const val CARD_DETAIL = "card_detail"
    const val EDIT_CARD = "edit_card"
    const val CATEGORIES = "categories"
    const val SETTINGS = "settings"
}

/**
 * Navigation parameters as constants
 */
object NavigationParams {
    const val CARD_ID = "cardId"
    const val CARD_TYPE = "cardType"
}

/**
 * Navigation result keys for passing data between screens
 */
object NavigationResultKeys {
    const val FRONT_IMAGE_PATH = "frontImagePath"
    const val BACK_IMAGE_PATH = "backImagePath"
    const val EXTRACTED_DATA = "extractedData"
    const val CARD_SAVED = "cardSaved"
    const val CARD_DELETED = "cardDeleted"
    const val CATEGORY_SELECTED = "categorySelected"
}