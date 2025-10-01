package com.technitedminds.wallet.domain.model

/**
 * Data class representing a custom gradient for card backgrounds.
 * Allows users to customize the visual appearance of their cards.
 */
data class CardGradient(
    /** Starting color of the gradient in hex format (e.g., "#FF5722") */
    val startColor: String,
    
    /** Ending color of the gradient in hex format (e.g., "#FFC107") */
    val endColor: String,
    
    /** Direction of the gradient */
    val direction: GradientDirection = GradientDirection.TopToBottom
) {
    /** Returns true if the gradient has valid color values */
    fun isValid(): Boolean {
        return isValidHexColor(startColor) && isValidHexColor(endColor)
    }
    
    /** Returns a copy of this gradient with swapped colors */
    fun reversed(): CardGradient = copy(startColor = endColor, endColor = startColor)
    
    private fun isValidHexColor(color: String): Boolean {
        return color.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"))
    }
    
    companion object {
        /** Default gradient for cards */
        val DEFAULT = CardGradient(
            startColor = "#667eea",
            endColor = "#764ba2",
            direction = GradientDirection.TopToBottom
        )
        
        /** Predefined gradients for different card types */
        val CREDIT_CARD_GRADIENT = CardGradient("#667eea", "#764ba2")
        val DEBIT_CARD_GRADIENT = CardGradient("#f093fb", "#f5576c")
        val TRANSPORT_CARD_GRADIENT = CardGradient("#4facfe", "#00f2fe")
        val GIFT_CARD_GRADIENT = CardGradient("#a8edea", "#fed6e3")
        val LOYALTY_CARD_GRADIENT = CardGradient("#ffecd2", "#fcb69f")
        val MEMBERSHIP_CARD_GRADIENT = CardGradient("#43e97b", "#38f9d7")
        val INSURANCE_CARD_GRADIENT = CardGradient("#d299c2", "#fef9d7")
        val ID_CARD_GRADIENT = CardGradient("#89f7fe", "#66a6ff")
        val VOUCHER_GRADIENT = CardGradient("#fa709a", "#fee140")
        val EVENT_GRADIENT = CardGradient("#ffecd2", "#fcb69f")
        val BUSINESS_CARD_GRADIENT = CardGradient("#667eea", "#764ba2")
        val LIBRARY_CARD_GRADIENT = CardGradient("#43e97b", "#38f9d7")
        val HOTEL_CARD_GRADIENT = CardGradient("#a8edea", "#fed6e3")
        val STUDENT_CARD_GRADIENT = CardGradient("#89f7fe", "#66a6ff")
        val ACCESS_CARD_GRADIENT = CardGradient("#d299c2", "#fef9d7")
    }
}

/**
 * Enum representing different gradient directions for card backgrounds.
 */
enum class GradientDirection {
    /** Gradient flows from top to bottom */
    TopToBottom,
    
    /** Gradient flows from left to right */
    LeftToRight,
    
    /** Diagonal gradient from top-left to bottom-right */
    DiagonalTopLeftToBottomRight,
    
    /** Diagonal gradient from top-right to bottom-left */
    DiagonalTopRightToBottomLeft;
    
    /** Returns the display name for this gradient direction */
    fun getDisplayName(): String = when (this) {
        TopToBottom -> "Top to Bottom"
        LeftToRight -> "Left to Right"
        DiagonalTopLeftToBottomRight -> "Diagonal ↘"
        DiagonalTopRightToBottomLeft -> "Diagonal ↙"
    }
}