package com.technitedminds.wallet.domain.model

/**
 * Data extracted from an EMV contactless (NFC) payment card.
 *
 * NFC can retrieve PAN, expiry, and cardholder name but **not** the CVV
 * (which is printed on the physical card and not stored on the contactless chip).
 */
data class NfcCardData(
    val cardNumber: String,
    val expiryDate: String,
    val cardholderName: String,
    val aid: String,
    val cardScheme: String,
) {

    fun toExtractedDataMap(): Map<String, String> = buildMap {
        if (cardNumber.isNotBlank()) put(Card.CARD_NUMBER_KEY, cardNumber)
        if (expiryDate.isNotBlank()) put(Card.EXPIRY_DATE_KEY, expiryDate)
        if (cardholderName.isNotBlank()) put(Card.CARDHOLDER_NAME_KEY, cardholderName)
    }

    fun maskedCardNumber(): String {
        val digits = cardNumber.replace(" ", "")
        return if (digits.length >= 4) "**** ${digits.takeLast(4)}" else cardNumber
    }
}
