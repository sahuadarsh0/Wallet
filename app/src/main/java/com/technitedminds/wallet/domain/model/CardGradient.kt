package com.technitedminds.wallet.domain.model

/**
 * Data class representing gradient colors and direction for card backgrounds.
 * Used for both default card type gradients and custom user-defined gradients.
 */
data class CardGradient(
    /** Starting color of the gradient in hex format (e.g., "#667eea") */
    val startColor: String,
    
    /** Ending color of the gradient in hex format (e.g., "#764ba2") */
    val endColor: String,
    
    /** Direction of the gradient */
    val direction: GradientDirection = GradientDirection.TopToBottom,
    
    /** Optional name for the gradient */
    val name: String? = null
) {
    companion object {
        // Default gradients for different card types
        val DEFAULT = CardGradient("#667eea", "#764ba2", GradientDirection.TopToBottom, "Default")
        
        // Credit Card Gradients
        val CREDIT_CARD_GRADIENT = CardGradient("#667eea", "#764ba2", GradientDirection.DiagonalTopLeftToBottomRight, "Credit Card")
        val CREDIT_PREMIUM = CardGradient("#1e3c72", "#2a5298", GradientDirection.TopToBottom, "Premium Credit")
        val CREDIT_GOLD = CardGradient("#f7971e", "#ffd200", GradientDirection.DiagonalTopRightToBottomLeft, "Gold Credit")
        
        // Debit Card Gradients
        val DEBIT_CARD_GRADIENT = CardGradient("#f093fb", "#f5576c", GradientDirection.DiagonalTopLeftToBottomRight, "Debit Card")
        val DEBIT_CLASSIC = CardGradient("#4facfe", "#00f2fe", GradientDirection.LeftToRight, "Classic Debit")
        val DEBIT_SECURE = CardGradient("#43e97b", "#38f9d7", GradientDirection.TopToBottom, "Secure Debit")
        
        // Transport Card Gradients
        val TRANSPORT_CARD_GRADIENT = CardGradient("#4facfe", "#00f2fe", GradientDirection.LeftToRight, "Transport")
        val METRO_BLUE = CardGradient("#2196F3", "#21CBF3", GradientDirection.TopToBottom, "Metro Blue")
        val BUS_GREEN = CardGradient("#4CAF50", "#8BC34A", GradientDirection.DiagonalTopLeftToBottomRight, "Bus Green")
        
        // Gift Card Gradients
        val GIFT_CARD_GRADIENT = CardGradient("#a8edea", "#fed6e3", GradientDirection.DiagonalTopRightToBottomLeft, "Gift Card")
        val GIFT_FESTIVE = CardGradient("#ff9a9e", "#fecfef", GradientDirection.TopToBottom, "Festive Gift")
        val GIFT_ELEGANT = CardGradient("#ffecd2", "#fcb69f", GradientDirection.LeftToRight, "Elegant Gift")
        
        // Loyalty Card Gradients
        val LOYALTY_CARD_GRADIENT = CardGradient("#ffecd2", "#fcb69f", GradientDirection.TopToBottom, "Loyalty")
        val LOYALTY_GOLD = CardGradient("#f7971e", "#ffd200", GradientDirection.DiagonalTopLeftToBottomRight, "Gold Loyalty")
        val LOYALTY_PLATINUM = CardGradient("#c9d6ff", "#e2e2e2", GradientDirection.TopToBottom, "Platinum Loyalty")
        
        // Membership Card Gradients
        val MEMBERSHIP_CARD_GRADIENT = CardGradient("#43e97b", "#38f9d7", GradientDirection.DiagonalTopLeftToBottomRight, "Membership")
        val GYM_ENERGY = CardGradient("#ff6b6b", "#ffa726", GradientDirection.TopToBottom, "Gym Energy")
        val CLUB_PREMIUM = CardGradient("#667eea", "#764ba2", GradientDirection.LeftToRight, "Club Premium")
        
        // Insurance Card Gradients
        val INSURANCE_CARD_GRADIENT = CardGradient("#d299c2", "#fef9d7", GradientDirection.TopToBottom, "Insurance")
        val HEALTH_CARE = CardGradient("#4facfe", "#00f2fe", GradientDirection.DiagonalTopRightToBottomLeft, "Health Care")
        val LIFE_SECURE = CardGradient("#43e97b", "#38f9d7", GradientDirection.TopToBottom, "Life Secure")
        
        // ID Card Gradients
        val ID_CARD_GRADIENT = CardGradient("#89f7fe", "#66a6ff", GradientDirection.TopToBottom, "ID Card")
        val GOVERNMENT_ID = CardGradient("#2196F3", "#1976D2", GradientDirection.LeftToRight, "Government ID")
        val CORPORATE_ID = CardGradient("#607D8B", "#455A64", GradientDirection.DiagonalTopLeftToBottomRight, "Corporate ID")
        
        // Voucher Gradients
        val VOUCHER_GRADIENT = CardGradient("#fa709a", "#fee140", GradientDirection.DiagonalTopLeftToBottomRight, "Voucher")
        val DISCOUNT_SPECIAL = CardGradient("#ff9a9e", "#fecfef", GradientDirection.TopToBottom, "Discount Special")
        val COUPON_BRIGHT = CardGradient("#ffecd2", "#fcb69f", GradientDirection.LeftToRight, "Coupon Bright")
        
        // Event Gradients
        val EVENT_GRADIENT = CardGradient("#ffecd2", "#fcb69f", GradientDirection.DiagonalTopRightToBottomLeft, "Event")
        val CONCERT_VIBE = CardGradient("#667eea", "#764ba2", GradientDirection.TopToBottom, "Concert Vibe")
        val FESTIVAL_FUN = CardGradient("#ff9a9e", "#fecfef", GradientDirection.LeftToRight, "Festival Fun")
        
        // Business Card Gradients
        val BUSINESS_CARD_GRADIENT = CardGradient("#667eea", "#764ba2", GradientDirection.LeftToRight, "Business")
        val CORPORATE_BLUE = CardGradient("#2196F3", "#1976D2", GradientDirection.TopToBottom, "Corporate Blue")
        val PROFESSIONAL_GRAY = CardGradient("#607D8B", "#455A64", GradientDirection.DiagonalTopLeftToBottomRight, "Professional Gray")
        
        // Library Card Gradients
        val LIBRARY_CARD_GRADIENT = CardGradient("#43e97b", "#38f9d7", GradientDirection.TopToBottom, "Library")
        val ACADEMIC_BLUE = CardGradient("#4facfe", "#00f2fe", GradientDirection.LeftToRight, "Academic Blue")
        val KNOWLEDGE_GREEN = CardGradient("#4CAF50", "#8BC34A", GradientDirection.DiagonalTopRightToBottomLeft, "Knowledge Green")
        
        // Hotel Card Gradients
        val HOTEL_CARD_GRADIENT = CardGradient("#a8edea", "#fed6e3", GradientDirection.DiagonalTopLeftToBottomRight, "Hotel")
        val LUXURY_GOLD = CardGradient("#f7971e", "#ffd200", GradientDirection.TopToBottom, "Luxury Gold")
        val RESORT_BLUE = CardGradient("#4facfe", "#00f2fe", GradientDirection.LeftToRight, "Resort Blue")
        
        // Student Card Gradients
        val STUDENT_CARD_GRADIENT = CardGradient("#89f7fe", "#66a6ff", GradientDirection.TopToBottom, "Student")
        val CAMPUS_GREEN = CardGradient("#43e97b", "#38f9d7", GradientDirection.DiagonalTopLeftToBottomRight, "Campus Green")
        val UNIVERSITY_BLUE = CardGradient("#2196F3", "#21CBF3", GradientDirection.LeftToRight, "University Blue")
        
        // Access Card Gradients
        val ACCESS_CARD_GRADIENT = CardGradient("#d299c2", "#fef9d7", GradientDirection.TopToBottom, "Access")
        val SECURITY_RED = CardGradient("#f093fb", "#f5576c", GradientDirection.DiagonalTopRightToBottomLeft, "Security Red")
        val KEYCARD_GRAY = CardGradient("#607D8B", "#90A4AE", GradientDirection.LeftToRight, "Keycard Gray")
        
        /**
         * Get all predefined gradients
         */
        fun getAllPredefinedGradients(): List<CardGradient> = listOf(
            DEFAULT,
            CREDIT_CARD_GRADIENT, CREDIT_PREMIUM, CREDIT_GOLD,
            DEBIT_CARD_GRADIENT, DEBIT_CLASSIC, DEBIT_SECURE,
            TRANSPORT_CARD_GRADIENT, METRO_BLUE, BUS_GREEN,
            GIFT_CARD_GRADIENT, GIFT_FESTIVE, GIFT_ELEGANT,
            LOYALTY_CARD_GRADIENT, LOYALTY_GOLD, LOYALTY_PLATINUM,
            MEMBERSHIP_CARD_GRADIENT, GYM_ENERGY, CLUB_PREMIUM,
            INSURANCE_CARD_GRADIENT, HEALTH_CARE, LIFE_SECURE,
            ID_CARD_GRADIENT, GOVERNMENT_ID, CORPORATE_ID,
            VOUCHER_GRADIENT, DISCOUNT_SPECIAL, COUPON_BRIGHT,
            EVENT_GRADIENT, CONCERT_VIBE, FESTIVAL_FUN,
            BUSINESS_CARD_GRADIENT, CORPORATE_BLUE, PROFESSIONAL_GRAY,
            LIBRARY_CARD_GRADIENT, ACADEMIC_BLUE, KNOWLEDGE_GREEN,
            HOTEL_CARD_GRADIENT, LUXURY_GOLD, RESORT_BLUE,
            STUDENT_CARD_GRADIENT, CAMPUS_GREEN, UNIVERSITY_BLUE,
            ACCESS_CARD_GRADIENT, SECURITY_RED, KEYCARD_GRAY
        )
        
        /**
         * Get gradients for a specific card type
         */
        fun getGradientsForCardType(cardType: CardType): List<CardGradient> = when (cardType) {
            is CardType.Credit -> listOf(CREDIT_CARD_GRADIENT, CREDIT_PREMIUM, CREDIT_GOLD)
            is CardType.Debit -> listOf(DEBIT_CARD_GRADIENT, DEBIT_CLASSIC, DEBIT_SECURE)
            is CardType.TransportCard -> listOf(TRANSPORT_CARD_GRADIENT, METRO_BLUE, BUS_GREEN)
            is CardType.GiftCard -> listOf(GIFT_CARD_GRADIENT, GIFT_FESTIVE, GIFT_ELEGANT)
            is CardType.LoyaltyCard -> listOf(LOYALTY_CARD_GRADIENT, LOYALTY_GOLD, LOYALTY_PLATINUM)
            is CardType.MembershipCard -> listOf(MEMBERSHIP_CARD_GRADIENT, GYM_ENERGY, CLUB_PREMIUM)
            is CardType.InsuranceCard -> listOf(INSURANCE_CARD_GRADIENT, HEALTH_CARE, LIFE_SECURE)
            is CardType.IdentificationCard -> listOf(ID_CARD_GRADIENT, GOVERNMENT_ID, CORPORATE_ID)
            is CardType.Voucher -> listOf(VOUCHER_GRADIENT, DISCOUNT_SPECIAL, COUPON_BRIGHT)
            is CardType.Event -> listOf(EVENT_GRADIENT, CONCERT_VIBE, FESTIVAL_FUN)
            is CardType.BusinessCard -> listOf(BUSINESS_CARD_GRADIENT, CORPORATE_BLUE, PROFESSIONAL_GRAY)
            is CardType.LibraryCard -> listOf(LIBRARY_CARD_GRADIENT, ACADEMIC_BLUE, KNOWLEDGE_GREEN)
            is CardType.HotelCard -> listOf(HOTEL_CARD_GRADIENT, LUXURY_GOLD, RESORT_BLUE)
            is CardType.StudentCard -> listOf(STUDENT_CARD_GRADIENT, CAMPUS_GREEN, UNIVERSITY_BLUE)
            is CardType.AccessCard -> listOf(ACCESS_CARD_GRADIENT, SECURITY_RED, KEYCARD_GRAY)
            is CardType.Custom -> listOf(DEFAULT)
        }
    }
}

/**
 * Enum representing gradient directions
 */
enum class GradientDirection(val displayName: String) {
    TopToBottom("Top to Bottom"),
    LeftToRight("Left to Right"),
    DiagonalTopLeftToBottomRight("Diagonal ↘"),
    DiagonalTopRightToBottomLeft("Diagonal ↙");
    
    /**
     * Get the angle in degrees for this gradient direction
     */
    fun getAngleDegrees(): Float = when (this) {
        TopToBottom -> 90f
        LeftToRight -> 0f
        DiagonalTopLeftToBottomRight -> 45f
        DiagonalTopRightToBottomLeft -> 135f
    }
}