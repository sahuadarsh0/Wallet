package com.technitedminds.wallet.domain.model

/**
 * Data class representing a category for organizing cards.
 */
data class Category(
    /** Unique identifier for the category */
    val id: String,
    
    /** Display name of the category */
    val name: String,
    
    /** Optional description of the category */
    val description: String? = null,
    
    /** Hex color code for the category (e.g., "#FF5722") */
    val colorHex: String = "#1976D2",
    
    /** Icon name or identifier for the category */
    val iconName: String? = null,
    
    /** Order/position for sorting categories */
    val sortOrder: Int = 0,
    
    /** Timestamp when the category was created */
    val createdAt: Long = System.currentTimeMillis(),
    
    /** Timestamp when the category was last updated */
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        /** Default category for uncategorized cards */
        val DEFAULT = Category(
            id = "default",
            name = "General",
            description = "Default category for cards",
            colorHex = "#1976D2",
            iconName = "Category"
        )
        
        /** Predefined categories */
        val PREDEFINED_CATEGORIES = listOf(
            Category(
                id = "personal",
                name = "Personal",
                description = "Personal cards and documents",
                colorHex = "#1976D2",
                iconName = "Person"
            ),
            Category(
                id = "business",
                name = "Business",
                description = "Work and business related cards",
                colorHex = "#37474F",
                iconName = "Business"
            ),
            Category(
                id = "travel",
                name = "Travel",
                description = "Travel and transportation cards",
                colorHex = "#00BCD4",
                iconName = "Flight"
            ),
            Category(
                id = "shopping",
                name = "Shopping",
                description = "Retail and shopping cards",
                colorHex = "#FF9800",
                iconName = "ShoppingCart"
            ),
            Category(
                id = "health",
                name = "Health",
                description = "Medical and health insurance cards",
                colorHex = "#4CAF50",
                iconName = "LocalHospital"
            ),
            Category(
                id = "entertainment",
                name = "Entertainment",
                description = "Entertainment and leisure cards",
                colorHex = "#E91E63",
                iconName = "Movie"
            )
        )
    }
    
    /** Returns a copy of this category with updated timestamp */
    fun withUpdatedTimestamp(): Category = copy(updatedAt = System.currentTimeMillis())
}