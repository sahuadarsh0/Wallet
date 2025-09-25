package com.technitedminds.wallet.domain.model

/**
 * Data class representing a card category for organizing cards. Categories help users group their
 * cards by type, usage, or personal preference.
 */
data class Category(
        /** Unique identifier for the category */
        val id: String,

        /** Display name of the category */
        val name: String,

        /** Resource ID for the category icon */
        val iconResId: Int,

        /** Hex color code for the category (e.g., "#FF5722") */
        val colorHex: String,

        /** Whether this is a default system category that cannot be deleted */
        val isDefault: Boolean = false,

        /** Timestamp when the category was created */
        val createdAt: Long = System.currentTimeMillis(),

        /** Timestamp when the category was last updated */
        val updatedAt: Long = System.currentTimeMillis()
) {
    /** Returns true if the category has a valid color hex code */
    fun hasValidColor(): Boolean {
        return colorHex.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$"))
    }

    /** Returns true if the category can be deleted (not a default category) */
    fun canBeDeleted(): Boolean = !isDefault

    /** Returns true if the category can be modified */
    fun canBeModified(): Boolean = !isDefault

    companion object {
        /** Default category IDs for system categories */
        const val DEFAULT_CATEGORY_ID = "default"
        const val CREDIT_CARDS_CATEGORY_ID = "credit_cards"
        const val DEBIT_CARDS_CATEGORY_ID = "debit_cards"
        const val GIFT_CARDS_CATEGORY_ID = "gift_cards"
        const val MEMBERSHIP_CATEGORY_ID = "membership"

        /** Creates a default "Uncategorized" category */
        fun createDefaultCategory(): Category {
            return Category(
                    id = DEFAULT_CATEGORY_ID,
                    name = "Uncategorized",
                    iconResId = android.R.drawable.ic_menu_gallery, // Placeholder icon
                    colorHex = "#757575", // Material Grey 600
                    isDefault = true
            )
        }
    }
}
