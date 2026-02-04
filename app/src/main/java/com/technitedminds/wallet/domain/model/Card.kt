package com.technitedminds.wallet.domain.model

/**
 * Data class representing a card in the digital wallet. Supports both textual cards (with OCR data)
 * and image-only cards.
 */
data class Card(
        /** Unique identifier for the card */
        val id: String,

        /** User-defined name for the card */
        val name: String,

        /** Type of the card (Credit, Debit, or other predefined/image-only types) */
        val type: CardType,

        /** ID of the category this card belongs to */
        val categoryId: String,

        /** File path to the front image of the card */
        val frontImagePath: String,

        /** File path to the back image of the card */
        val backImagePath: String,

        /**
         * Data extracted from OCR processing (for textual cards only) Common keys: "cardNumber",
         * "expiryDate", "cardholderName", "cvv"
         */
        val extractedData: Map<String, String> = emptyMap(),

        /**
         * User-defined custom fields for additional information Examples: "notes", "pin",
         * "customerService", "website"
         */
        val customFields: Map<String, String> = emptyMap(),

        /** Optional expiry date for vouchers/gift cards */
        val expiryDate: String? = null,

        /** Optional notes for image cards */
        val notes: String? = null,

        /** Custom gradient colors, null uses default */
        val customGradient: CardGradient? = null,

        /** Timestamp when the card was created */
        val createdAt: Long,

        /** Timestamp when the card was last updated */
        val updatedAt: Long
) {
    /** Returns true if this card has OCR-extracted data */
    fun hasExtractedData(): Boolean = extractedData.isNotEmpty()

    /** Returns true if this card has custom fields */
    fun hasCustomFields(): Boolean = customFields.isNotEmpty()

    /** Returns true if both front and back images are present */
    fun hasCompleteImages(): Boolean = frontImagePath.isNotBlank() && backImagePath.isNotBlank()

    /** Returns the card number if available from extracted data */
    fun getCardNumber(): String? = extractedData[CARD_NUMBER_KEY]

    /** Returns the expiry date if available from extracted data */
    fun getExtractedExpiryDate(): String? = extractedData[EXPIRY_DATE_KEY]

    /** Returns the cardholder name if available from extracted data */
    fun getCardholderName(): String? = extractedData[CARDHOLDER_NAME_KEY]

    /** Returns the CVV if available from extracted data */
    fun getCVV(): String? = extractedData[CVV_KEY]

    /** Returns the bank name if available from extracted data */
    fun getBankName(): String? = extractedData[BANK_NAME_KEY]

    /** Returns notes from custom fields */
    fun getCustomNotes(): String? = customFields[NOTES_KEY]

    /** Returns the card's display color (custom color or type default) */
    fun getDisplayColor(): String = customFields["customColor"] ?: type.getDefaultColor()

    /** Returns true if this card has a custom color set */
    fun hasCustomColor(): Boolean = customFields.containsKey("customColor")

    /** Returns a copy of this card with a custom color */
    fun withCustomColor(colorHex: String): Card = 
        copy(customFields = customFields + ("customColor" to colorHex), updatedAt = System.currentTimeMillis())

    /** Returns a copy of this card with the custom color removed */
    fun withoutCustomColor(): Card = 
        copy(customFields = customFields - "customColor", updatedAt = System.currentTimeMillis())

    /** Returns true if the card supports OCR processing based on its type */
    fun supportsOCR(): Boolean = type.supportsOCR()

    /** Returns a copy of this card with updated timestamp */
    fun withUpdatedTimestamp(): Card = copy(updatedAt = System.currentTimeMillis())

    /** Returns a copy of this card with new extracted data */
    fun withExtractedData(newExtractedData: Map<String, String>): Card =
            copy(extractedData = newExtractedData, updatedAt = System.currentTimeMillis())

    /** Returns a copy of this card with new custom fields */
    fun withCustomFields(newCustomFields: Map<String, String>): Card =
            copy(customFields = newCustomFields, updatedAt = System.currentTimeMillis())

    /** Returns a copy of this card with a custom gradient */
    fun withCustomGradient(gradient: CardGradient?): Card =
            copy(customGradient = gradient, updatedAt = System.currentTimeMillis())

    /** Returns the gradient to use for this card (custom or default) */
    fun getGradient(): CardGradient = customGradient ?: getDefaultGradientForType(type)

    /** Returns true if this card has a custom gradient */
    fun hasCustomGradient(): Boolean = customGradient != null

    companion object {
        /** Common extracted data keys for OCR processing */
        const val CARD_NUMBER_KEY = "cardNumber"
        const val EXPIRY_DATE_KEY = "expiryDate"
        const val CARDHOLDER_NAME_KEY = "cardholderName"
        const val CVV_KEY = "cvv"
        const val BANK_NAME_KEY = "bankName"

        /** Common custom field keys */
        const val NOTES_KEY = "notes"
        const val PIN_KEY = "pin"
        const val CUSTOMER_SERVICE_KEY = "customerService"
        const val WEBSITE_KEY = "website"
        const val PHONE_KEY = "phone"

        /** Returns the default gradient for a given card type */
        fun getDefaultGradientForType(type: CardType): CardGradient = when (type) {
            is CardType.Credit -> CardGradient.CREDIT_CARD_GRADIENT
            is CardType.Debit -> CardGradient.DEBIT_CARD_GRADIENT
            is CardType.TransportCard -> CardGradient.TRANSPORT_CARD_GRADIENT
            is CardType.GiftCard -> CardGradient.GIFT_CARD_GRADIENT
            is CardType.LoyaltyCard -> CardGradient.LOYALTY_CARD_GRADIENT
            is CardType.MembershipCard -> CardGradient.MEMBERSHIP_CARD_GRADIENT
            is CardType.InsuranceCard -> CardGradient.INSURANCE_CARD_GRADIENT
            is CardType.IdentificationCard -> CardGradient.ID_CARD_GRADIENT
            is CardType.Voucher -> CardGradient.VOUCHER_GRADIENT
            is CardType.Event -> CardGradient.EVENT_GRADIENT
            is CardType.BusinessCard -> CardGradient.BUSINESS_CARD_GRADIENT
            is CardType.LibraryCard -> CardGradient.LIBRARY_CARD_GRADIENT
            is CardType.HotelCard -> CardGradient.HOTEL_CARD_GRADIENT
            is CardType.StudentCard -> CardGradient.STUDENT_CARD_GRADIENT
            is CardType.AccessCard -> CardGradient.ACCESS_CARD_GRADIENT
            is CardType.Custom -> CardGradient.DEFAULT
        }
    }
}
