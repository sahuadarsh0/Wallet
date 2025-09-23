package com.technitedminds.wallet.data.ocr

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
// ML Kit TextRecognizerOptions - using default options
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.technitedminds.wallet.domain.model.CardType
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * ML Kit-based text recognition implementation for offline OCR processing.
 * Specifically optimized for credit/debit/ATM card text extraction.
 */
@Singleton
class MLKitTextRecognizer @Inject constructor() {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Processes an image to extract text using ML Kit
     * @param imageData The image data as byte array
     * @param cardType The type of card being processed
     * @return Map of extracted text data with confidence scores
     */
    suspend fun processImage(imageData: ByteArray, cardType: CardType): OCRResult =
        suspendCoroutine { continuation ->
            try {
                // Convert byte array to bitmap
                val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                    ?: throw IllegalArgumentException("Invalid image data")

                // Create InputImage from bitmap
                val inputImage = InputImage.fromBitmap(bitmap, 0)

                // Process the image
                textRecognizer
                    .process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val extractedData = parseTextForCard(visionText.text, cardType)
                        val confidence = calculateOverallConfidence(visionText)

                        val result = OCRResult(
                            success = true,
                            extractedData = extractedData,
                            rawText = visionText.text,
                            confidence = confidence,
                            processingTimeMs = 0 // Would be calculated in real implementation
                        )

                        bitmap.recycle()
                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        bitmap.recycle()
                        val result = OCRResult(
                            success = false,
                            extractedData = emptyMap(),
                            rawText = "",
                            confidence = 0f,
                            errorMessage = exception.message,
                            processingTimeMs = 0
                        )
                        continuation.resume(result)
                    }
            } catch (e: Exception) {
                val result = OCRResult(
                    success = false,
                    extractedData = emptyMap(),
                    rawText = "",
                    confidence = 0f,
                    errorMessage = e.message,
                    processingTimeMs = 0
                )
                continuation.resume(result)
            }
        }

    /** Parses extracted text specifically for card information */
    private fun parseTextForCard(rawText: String, cardType: CardType): Map<String, String> {
        val extractedData = mutableMapOf<String, String>()

        // Only process textual cards
        if (!cardType.supportsOCR()) {
            return extractedData
        }

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }

        // Extract card number
        extractCardNumber(lines)?.let { cardNumber -> 
            extractedData["cardNumber"] = cardNumber 
        }

        // Extract expiry date
        extractExpiryDate(lines)?.let { expiryDate -> 
            extractedData["expiryDate"] = expiryDate 
        }

        // Extract cardholder name
        extractCardholderName(lines, cardType)?.let { name ->
            extractedData["cardholderName"] = name
        }

        // Extract CVV (usually on back)
        extractCVV(lines)?.let { cvv -> 
            extractedData["cvv"] = cvv 
        }

        // Extract bank name
        extractBankName(lines, cardType)?.let { bankName -> 
            extractedData["bankName"] = bankName 
        }

        return extractedData
    }

    /** Extracts card number from text lines */
    private fun extractCardNumber(lines: List<String>): String? {
        val cardNumberRegex = Regex("""(\d{4}[\s-]?\d{4}[\s-]?\d{4}[\s-]?\d{4})""")

        for (line in lines) {
            val match = cardNumberRegex.find(line)
            if (match != null) {
                val cardNumber = match.value.replace(Regex("""[\s-]"""), "")
                if (isValidCardNumber(cardNumber)) {
                    return formatCardNumber(cardNumber)
                }
            }
        }

        return null
    }

    /** Extracts expiry date from text lines */
    private fun extractExpiryDate(lines: List<String>): String? {
        val expiryRegexes = listOf(
            Regex("""(\d{2}/\d{2})"""), // MM/YY
            Regex("""(\d{2}/\d{4})"""), // MM/YYYY
            Regex("""(\d{2}-\d{2})"""), // MM-YY
            Regex("""(\d{2}\s+\d{2})"""), // MM YY
            Regex("""(VALID\s+THRU\s+\d{2}/\d{2})""", RegexOption.IGNORE_CASE),
            Regex("""(EXP\s+\d{2}/\d{2})""", RegexOption.IGNORE_CASE)
        )

        for (line in lines) {
            for (regex in expiryRegexes) {
                val match = regex.find(line)
                if (match != null) {
                    val expiryText = match.value
                    val extractedDate = extractDateFromText(expiryText)
                    if (extractedDate != null && isValidExpiryDate(extractedDate)) {
                        return extractedDate
                    }
                }
            }
        }

        return null
    }

    /** Extracts cardholder name from text lines */
    private fun extractCardholderName(lines: List<String>, cardType: CardType): String? {
        // Look for lines that could be names (usually all caps, 2+ words)
        val nameRegex = Regex("""^[A-Z][A-Z\s]{2,}[A-Z]$""")

        for (line in lines) {
            if (nameRegex.matches(line) &&
                !line.contains("BANK") &&
                !line.contains("CARD") &&
                !line.contains("CREDIT") &&
                !line.contains("DEBIT") &&
                line.split(" ").size >= 2
            ) {
                return formatCardholderName(line)
            }
        }

        return null
    }

    /** Extracts CVV from text lines (usually 3-4 digits) */
    private fun extractCVV(lines: List<String>): String? {
        val cvvRegex = Regex("""(\b\d{3,4}\b)""")

        for (line in lines) {
            val matches = cvvRegex.findAll(line)
            for (match in matches) {
                val cvv = match.value
                if (cvv.length in 3..4) {
                    return cvv
                }
            }
        }

        return null
    }

    /** Extracts bank name from text lines */
    private fun extractBankName(lines: List<String>, cardType: CardType): String? {
        val bankKeywords = listOf("BANK", "CREDIT", "UNION", "FINANCIAL", "TRUST")

        for (line in lines) {
            val upperLine = line.uppercase()
            if (bankKeywords.any { keyword -> upperLine.contains(keyword) } &&
                !upperLine.contains("CARD") &&
                line.length > 3
            ) {
                return formatBankName(line)
            }
        }

        return null
    }

    /** Validates card number using Luhn algorithm */
    private fun isValidCardNumber(cardNumber: String): Boolean {
        if (cardNumber.length !in 13..19) return false
        if (!cardNumber.all { it.isDigit() }) return false

        // Luhn algorithm
        var sum = 0
        var alternate = false

        for (i in cardNumber.length - 1 downTo 0) {
            var digit = cardNumber[i].digitToInt()

            if (alternate) {
                digit *= 2
                if (digit > 9) {
                    digit = (digit % 10) + 1
                }
            }

            sum += digit
            alternate = !alternate
        }

        return sum % 10 == 0
    }

    /** Validates expiry date format and ensures it's not expired */
    private fun isValidExpiryDate(expiryDate: String): Boolean {
        val dateRegex = Regex("""(\d{2})/(\d{2,4})""")
        val match = dateRegex.find(expiryDate) ?: return false

        val month = match.groupValues[1].toIntOrNull() ?: return false
        val year = match.groupValues[2].toIntOrNull() ?: return false

        if (month !in 1..12) return false

        // Convert 2-digit year to 4-digit
        val fullYear = if (year < 100) {
            if (year < 30) 2000 + year else 1900 + year
        } else {
            year
        }

        // Check if not expired (basic check)
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1

        return fullYear > currentYear || (fullYear == currentYear && month >= currentMonth)
    }

    /** Extracts date from text containing expiry information */
    private fun extractDateFromText(text: String): String? {
        val dateRegex = Regex("""(\d{2}/\d{2,4})""")
        return dateRegex.find(text)?.value
    }

    /** Formats card number with spaces for readability */
    private fun formatCardNumber(cardNumber: String): String {
        return cardNumber.chunked(4).joinToString(" ")
    }

    /** Formats cardholder name */
    private fun formatCardholderName(name: String): String {
        return name.trim().split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    /** Formats bank name */
    private fun formatBankName(bankName: String): String {
        return bankName.trim().split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    /** Calculates overall confidence score from ML Kit results */
    private fun calculateOverallConfidence(visionText: com.google.mlkit.vision.text.Text): Float {
        if (visionText.textBlocks.isEmpty()) return 0f

        var totalConfidence = 0f
        var blockCount = 0

        for (block in visionText.textBlocks) {
            // ML Kit doesn't provide confidence scores in the free version
            // This is a placeholder implementation
            totalConfidence += 0.8f // Assume 80% confidence
            blockCount++
        }

        return if (blockCount > 0) totalConfidence / blockCount else 0f
    }
}

/** Result of OCR processing */
data class OCRResult(
    val success: Boolean,
    val extractedData: Map<String, String>,
    val rawText: String,
    val confidence: Float,
    val errorMessage: String? = null,
    val processingTimeMs: Long = 0
) {
    /** Returns true if OCR was successful and extracted meaningful data */
    fun hasValidData(): Boolean = success && extractedData.isNotEmpty()

    /** Returns true if confidence is above the threshold */
    fun isConfidenceAcceptable(threshold: Float = 0.7f): Boolean = confidence >= threshold

    /** Gets the card number if extracted */
    fun getCardNumber(): String? = extractedData["cardNumber"]

    /** Gets the expiry date if extracted */
    fun getExpiryDate(): String? = extractedData["expiryDate"]

    /** Gets the cardholder name if extracted */
    fun getCardholderName(): String? = extractedData["cardholderName"]

    /** Gets the CVV if extracted */
    fun getCVV(): String? = extractedData["cvv"]

    /** Gets the bank name if extracted */
    fun getBankName(): String? = extractedData["bankName"]
}