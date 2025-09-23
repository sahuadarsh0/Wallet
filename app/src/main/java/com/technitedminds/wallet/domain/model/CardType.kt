package com.technitedminds.wallet.domain.model

/**
 * Sealed class representing different types of cards supported by the application. Textual cards
 * (Credit, Debit, ATM) support OCR processing, while other cards are stored as visual
 * references without text extraction.
 */
sealed class CardType {
    /** Credit card type - supports OCR processing for card details extraction */
    object Credit : CardType()

    /** Debit card type - supports OCR processing for card details extraction */
    object Debit : CardType()

    /** ATM card type - supports OCR processing for card details extraction */
    object ATM : CardType()

    /** Gift card type - for gift cards, vouchers, and prepaid cards */
    object GiftCard : CardType()

    /** Loyalty card type - for store loyalty and rewards cards */
    object LoyaltyCard : CardType()

    /** Membership card type - for gym, club, and organization memberships */
    object MembershipCard : CardType()

    /** Insurance card type - for health, auto, and other insurance cards */
    object InsuranceCard : CardType()

    /** ID card type - for identification cards, licenses, and permits */
    object IdentificationCard : CardType()

    /** Transport card type - for metro, bus, train, and transit cards */
    object TransportCard : CardType()

    /** Business card type - for business and professional cards */
    object BusinessCard : CardType()

    /** Library card type - for library and educational institution cards */
    object LibraryCard : CardType()

    /** Hotel card type - for hotel key cards and loyalty cards */
    object HotelCard : CardType()

    /** Student card type - for student IDs and campus cards */
    object StudentCard : CardType()

    /** Access card type - for building access and security cards */
    object AccessCard : CardType()

    /**
     * Custom card type with user-defined name and color
     * @param typeName The display name for this card type
     * @param colorHex The hex color code for the card (e.g., "#FF5722")
     */
    data class Custom(val typeName: String, val colorHex: String) : CardType()

    /** Returns true if this card type supports OCR processing */
    fun supportsOCR(): Boolean =
            when (this) {
                is Credit, is Debit, is ATM -> true
                else -> false
            }

    /** Returns the display name for this card type */
    fun getDisplayName(): String =
            when (this) {
                is Credit -> "Credit Card"
                is Debit -> "Debit Card"
                is ATM -> "ATM Card"
                is GiftCard -> "Gift Card"
                is LoyaltyCard -> "Loyalty Card"
                is MembershipCard -> "Membership Card"
                is InsuranceCard -> "Insurance Card"
                is IdentificationCard -> "ID Card"
                is TransportCard -> "Transport Card"
                is BusinessCard -> "Business Card"
                is LibraryCard -> "Library Card"
                is HotelCard -> "Hotel Card"
                is StudentCard -> "Student Card"
                is AccessCard -> "Access Card"
                is Custom -> typeName
            }

    /** Returns the default color for this card type */
    fun getDefaultColor(): String =
            when (this) {
                is Credit -> "#1976D2"        // Blue
                is Debit -> "#388E3C"         // Green
                is ATM -> "#607D8B"           // Blue Grey
                is GiftCard -> "#E91E63"      // Pink
                is LoyaltyCard -> "#FF9800"   // Orange
                is MembershipCard -> "#9C27B0" // Purple
                is InsuranceCard -> "#795548"  // Brown
                is IdentificationCard -> "#424242" // Dark Grey
                is TransportCard -> "#00BCD4"  // Cyan
                is BusinessCard -> "#37474F"   // Blue Grey Dark
                is LibraryCard -> "#4CAF50"    // Light Green
                is HotelCard -> "#FF5722"      // Deep Orange
                is StudentCard -> "#3F51B5"    // Indigo
                is AccessCard -> "#757575"     // Medium Grey
                is Custom -> colorHex
            }

    companion object {
        /** Get all predefined card types */
        fun getAllPredefinedTypes(): List<CardType> = listOf(
            Credit, Debit, ATM, GiftCard, LoyaltyCard, MembershipCard,
            InsuranceCard, IdentificationCard, TransportCard, BusinessCard,
            LibraryCard, HotelCard, StudentCard, AccessCard
        )

        /** Create a custom card type */
        fun createCustom(name: String, color: String = "#757575"): Custom {
            return Custom(name, color)
        }
    }
}
