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

        /** Type of the card (Credit, Debit, ATM, or ImageOnly) */
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
    fun getExpiryDate(): String? = extractedData[EXPIRY_DATE_KEY]

    /** Returns the cardholder name if available from extracted data */
    fun getCardholderName(): String? = extractedData[CARDHOLDER_NAME_KEY]

    /** Returns the CVV if available from extracted data */
    fun getCVV(): String? = extractedData[CVV_KEY]

    /** Returns the bank name if available from extracted data */
    fun getBankName(): String? = extractedData[BANK_NAME_KEY]

    /** Returns notes from custom fields */
    fun getNotes(): String? = customFields[NOTES_KEY]

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
    }
}
