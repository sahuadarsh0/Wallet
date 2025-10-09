package com.technitedminds.wallet.data.ocr

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
// ML Kit TextRecognizerOptions - using default options
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.usecase.ocr.ImageSide
import com.technitedminds.wallet.presentation.constants.AppConstants
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * ML Kit-based text recognition implementation for offline OCR processing.
 * Specifically optimized for credit/debit card text extraction.
 */
@Singleton
class MLKitTextRecognizer @Inject constructor(
    private val cardTextParser: CardTextParser
) {

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
        // Only process textual cards
        if (!cardType.supportsOCR()) {
            return emptyMap()
        }

        // Use the specialized card text parser for better accuracy
        // Default to FRONT side if not specified (this method doesn't have side parameter)
        return cardTextParser.parseCardText(rawText, cardType, ImageSide.FRONT)
    }
    
    /**
     * Processes an image for a specific side (front or back)
     */
    suspend fun processImageSide(imageData: ByteArray, cardType: CardType, imageSide: ImageSide): OCRResult =
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
                        val extractedData = cardTextParser.parseCardText(visionText.text, cardType, imageSide)
                        val confidence = calculateOverallConfidence(visionText)

                        val result = OCRResult(
                            success = true,
                            extractedData = extractedData,
                            rawText = visionText.text,
                            confidence = confidence,
                            processingTimeMs = 0
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
    fun getCardNumber(): String? = extractedData[AppConstants.CardProcessing.FIELD_CARD_NUMBER]

    /** Gets the expiry date if extracted */
    fun getExpiryDate(): String? = extractedData[AppConstants.CardProcessing.FIELD_EXPIRY_DATE]

    /** Gets the cardholder name if extracted */
    fun getCardholderName(): String? = extractedData[AppConstants.CardProcessing.FIELD_CARDHOLDER_NAME]

    /** Gets the CVV if extracted */
    fun getCVV(): String? = extractedData[AppConstants.CardProcessing.FIELD_CVV]

    /** Gets the bank name if extracted */
    fun getBankName(): String? = extractedData[AppConstants.CardProcessing.FIELD_BANK_NAME]
}