package com.technitedminds.wallet.presentation.components.common

import androidx.compose.runtime.*
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