package com.technitedminds.wallet.domain.model

/**
 * Result of OCR processing containing extracted card data.
 * This is a pure Kotlin domain model with no Android dependencies.
 */
data class OCRResult(
    /** Whether the OCR processing was successful */
    val success: Boolean,
    
    /** Extracted data as key-value pairs (e.g., "cardNumber" -> "1234567890123456") */
    val extractedData: Map<String, String>,
    
    /** Raw text extracted from the image */
    val rawText: String,
    
    /** Confidence score from 0.0 to 1.0 */
    val confidence: Float,
    
    /** Error message if processing failed */
    val errorMessage: String? = null,
    
    /** Processing time in milliseconds */
    val processingTimeMs: Long = 0
) {
    /** Returns true if OCR was successful and extracted meaningful data */
    fun hasValidData(): Boolean = success && extractedData.isNotEmpty()

    /** Returns true if confidence is above the threshold */
    fun isConfidenceAcceptable(threshold: Float = 0.7f): Boolean = confidence >= threshold

    /** Gets the card number if extracted */
    fun getCardNumber(): String? = extractedData[FIELD_CARD_NUMBER]

    /** Gets the expiry date if extracted */
    fun getExpiryDate(): String? = extractedData[FIELD_EXPIRY_DATE]

    /** Gets the cardholder name if extracted */
    fun getCardholderName(): String? = extractedData[FIELD_CARDHOLDER_NAME]

    /** Gets the CVV if extracted */
    fun getCVV(): String? = extractedData[FIELD_CVV]

    /** Gets the bank name if extracted */
    fun getBankName(): String? = extractedData[FIELD_BANK_NAME]
    
    companion object {
        // Field keys for extracted data - pure domain constants
        const val FIELD_CARD_NUMBER = "cardNumber"
        const val FIELD_EXPIRY_DATE = "expiryDate"
        const val FIELD_CARDHOLDER_NAME = "cardholderName"
        const val FIELD_CVV = "cvv"
        const val FIELD_BANK_NAME = "bankName"
        
        /** Creates an empty failed result */
        fun failed(errorMessage: String? = null): OCRResult = OCRResult(
            success = false,
            extractedData = emptyMap(),
            rawText = "",
            confidence = 0f,
            errorMessage = errorMessage
        )
        
        /** Creates an empty success result (for non-OCR cards) */
        fun empty(): OCRResult = OCRResult(
            success = true,
            extractedData = emptyMap(),
            rawText = "",
            confidence = 1f
        )
    }
}

/**
 * Enum representing which side of the card is being processed.
 * Used in OCR processing to apply side-specific parsing logic.
 */
enum class ImageSide {
    /** Front side of the card - typically contains card number, name, expiry */
    FRONT,
    
    /** Back side of the card - typically contains CVV, signature strip */
    BACK
}
