package com.technitedminds.wallet.presentation.components.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.technitedminds.wallet.domain.usecase.category.GetCategoryNameUseCase
import kotlinx.coroutines.launch

/**
 * Utility functions for category-related operations in Compose.
 */

/**
 * Composable function to get category name by ID.
 * Returns the category name or a fallback value.
 */
@Composable
fun rememberCategoryName(
    categoryId: String?,
    getCategoryNameUseCase: GetCategoryNameUseCase? = null
): String {
    var categoryName by remember(categoryId) { mutableStateOf("General") }
    
    LaunchedEffect(categoryId) {
        if (getCategoryNameUseCase != null && !categoryId.isNullOrBlank()) {
            categoryName = getCategoryNameUseCase(categoryId)
        } else {
            categoryName = when {
                categoryId.isNullOrBlank() -> "General"
                categoryId == "personal" -> "Personal"
                categoryId == "business" -> "Business"
                categoryId == "travel" -> "Travel"
                categoryId == "shopping" -> "Shopping"
                categoryId == "health" -> "Health"
                categoryId == "entertainment" -> "Entertainment"
                else -> categoryId.replaceFirstChar { it.uppercase() }
            }
        }
    }
    
    return categoryName
}

/**
 * Simple category name resolver without dependency injection.
 * Useful for components that don't have access to ViewModels.
 */
fun resolveCategoryName(categoryId: String?): String {
    return when {
        categoryId.isNullOrBlank() -> "General"
        categoryId == "personal" -> "Personal"
        categoryId == "business" -> "Business"
        categoryId == "travel" -> "Travel"
        categoryId == "shopping" -> "Shopping"
        categoryId == "health" -> "Health"
        categoryId == "entertainment" -> "Entertainment"
        else -> categoryId.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase() else it.toString() 
        }
    }
}

/**
 * Helper function to get ImageVector from icon name
 */
fun getIconFromName(iconName: String?): ImageVector? {
    return when (iconName) {
        "Category" -> Icons.Default.Category
        "Person" -> Icons.Default.Person
        "Business" -> Icons.Default.Business
        "ShoppingCart" -> Icons.Default.ShoppingCart
        "Restaurant" -> Icons.Default.Restaurant
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "LocalHospital" -> Icons.Default.LocalHospital
        "Movie" -> Icons.Default.Movie
        "School" -> Icons.Default.School
        "FitnessCenter" -> Icons.Default.FitnessCenter
        "Flight" -> Icons.Default.Flight
        "AccountBalance" -> Icons.Default.AccountBalance
        "Security" -> Icons.Default.Security
        "CardMembership" -> Icons.Default.CardMembership
        "Stars" -> Icons.Default.Stars
        "CardGiftcard" -> Icons.Default.CardGiftcard
        else -> null
    }
}

/**
 * Helper function to get icon name from ImageVector
 */
fun getIconName(icon: ImageVector): String {
    return when (icon) {
        Icons.Default.Category -> "Category"
        Icons.Default.Person -> "Person"
        Icons.Default.Business -> "Business"
        Icons.Default.ShoppingCart -> "ShoppingCart"
        Icons.Default.Restaurant -> "Restaurant"
        Icons.Default.DirectionsCar -> "DirectionsCar"
        Icons.Default.LocalHospital -> "LocalHospital"
        Icons.Default.Movie -> "Movie"
        Icons.Default.School -> "School"
        Icons.Default.FitnessCenter -> "FitnessCenter"
        Icons.Default.Flight -> "Flight"
        Icons.Default.AccountBalance -> "AccountBalance"
        Icons.Default.Security -> "Security"
        Icons.Default.CardMembership -> "CardMembership"
        Icons.Default.Stars -> "Stars"
        Icons.Default.CardGiftcard -> "CardGiftcard"
        else -> "Category"
    }
}