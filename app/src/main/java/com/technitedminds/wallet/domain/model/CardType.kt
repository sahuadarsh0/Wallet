package com.technitedminds.wallet.domain.model

/**
 * Sealed class representing different types of cards supported by the application. Textual cards
 * (Credit, Debit, ATM) support OCR processing, while ImageOnly cards are stored as visual
 * references without text extraction.
 */
sealed class CardType {
    /** Credit card type - supports OCR processing for card details extraction */
    object Credit : CardType()

    /** Debit card type - supports OCR processing for card details extraction */
    object Debit : CardType()

    /** ATM card type - supports OCR processing for card details extraction */
    object ATM : CardType()

    /**
     * Image-only card type for cards that don't require OCR processing
     * @param typeName The display name for this card type (e.g., "Gym", "Voucher", "Gift Card")
     */
    data class ImageOnly(val typeName: String) : CardType()

    /** Returns true if this card type supports OCR processing */
    fun supportsOCR(): Boolean =
            when (this) {
                is Credit, is Debit, is ATM -> true
                is ImageOnly -> false
            }

    /** Returns the display name for this card type */
    fun getDisplayName(): String =
            when (this) {
                is Credit -> "Credit Card"
                is Debit -> "Debit Card"
                is ATM -> "ATM Card"
                is ImageOnly -> typeName
            }
}
