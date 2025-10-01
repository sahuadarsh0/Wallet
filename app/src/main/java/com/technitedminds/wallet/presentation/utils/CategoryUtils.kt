package com.technitedminds.wallet.presentation.utils

/**
 * Utility functions for category-related operations in the presentation layer.
 */

/**
 * Resolve category name from category ID.
 * This is a placeholder implementation - in a real app, this would look up the category name.
 */
fun resolveCategoryName(categoryId: String): String {
    return when (categoryId) {
        "personal" -> "Personal"
        "business" -> "Business"
        "travel" -> "Travel"
        "shopping" -> "Shopping"
        "food" -> "Food & Dining"
        "health" -> "Health & Medical"
        "entertainment" -> "Entertainment"
        "transport" -> "Transportation"
        "finance" -> "Finance"
        "membership" -> "Membership"
        "loyalty" -> "Loyalty"
        "gift" -> "Gift Cards"
        "insurance" -> "Insurance"
        "education" -> "Education"
        "gym" -> "Gym & Fitness"
        else -> categoryId.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

/**
 * Get default categories for the app
 */
fun getDefaultCategories(): List<Pair<String, String>> {
    return listOf(
        "personal" to "Personal",
        "business" to "Business",
        "travel" to "Travel",
        "shopping" to "Shopping",
        "food" to "Food & Dining",
        "health" to "Health & Medical",
        "entertainment" to "Entertainment",
        "transport" to "Transportation",
        "finance" to "Finance",
        "membership" to "Membership",
        "loyalty" to "Loyalty",
        "gift" to "Gift Cards",
        "insurance" to "Insurance",
        "education" to "Education",
        "gym" to "Gym & Fitness"
    )
}