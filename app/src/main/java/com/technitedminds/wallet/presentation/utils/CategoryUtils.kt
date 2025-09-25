package com.technitedminds.wallet.presentation.utils

/**
 * Utility functions for category management
 */

/**
 * Resolve category name from ID
 * In a full implementation, this would query the CategoryRepository
 */
fun resolveCategoryName(categoryId: String): String {
    return when (categoryId) {
        "personal" -> "Personal"
        "business" -> "Business"
        "travel" -> "Travel"
        "shopping" -> "Shopping"
        "health" -> "Health"
        "entertainment" -> "Entertainment"
        else -> "Unknown"
    }
}

/**
 * Get default categories
 */
fun getDefaultCategories(): List<Pair<String, String>> {
    return listOf(
        "personal" to "Personal",
        "business" to "Business",
        "travel" to "Travel",
        "shopping" to "Shopping",
        "health" to "Health",
        "entertainment" to "Entertainment"
    )
}