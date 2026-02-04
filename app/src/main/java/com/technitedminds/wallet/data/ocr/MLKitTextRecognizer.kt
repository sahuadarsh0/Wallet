package com.technitedminds.wallet.data.ocr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.model.ImageSide
import com.technitedminds.wallet.presentation.constants.AppConstants
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * ML Kit-based text recognition implementation for offline OCR processing.
 * Enhanced with image preprocessing for improved accuracy on credit/debit cards.
 * 
 * Preprocessing techniques:
 * - Grayscale conversion for consistent processing
 * - Contrast enhancement for better text visibility
 * - Multiple processing attempts with different preprocessing levels
 */
@Singleton
class MLKitTextRecognizer @Inject constructor(
    private val cardTextParser: CardTextParser
) {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    companion object {
        // Preprocessing contrast levels to try (from normal to high contrast)
        private val CONTRAST_LEVELS = listOf(1.0f, 1.3f, 1.6f, 2.0f)
        // Brightness adjustment
        private const val BRIGHTNESS_ADJUSTMENT = 10f
    }

    /**
     * Processes an image to extract text using ML Kit with enhanced preprocessing.
     * Tries multiple contrast levels to find the best result.
     * 
     * @param imageData The image data as byte array
     * @param cardType The type of card being processed
     * @return Map of extracted text data with confidence scores
     */
    suspend fun processImage(imageData: ByteArray, cardType: CardType): OCRResult {
        val startTime = System.currentTimeMillis()
        
        try {
            // Convert byte array to bitmap
            val originalBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                ?: throw IllegalArgumentException("Invalid image data")
            
            // Try processing with different preprocessing levels
            var bestResult: OCRResult? = null
            
            for (contrastLevel in CONTRAST_LEVELS) {
                val preprocessedBitmap = preprocessBitmap(originalBitmap, contrastLevel)
                val result = processSingleImage(preprocessedBitmap, cardType, ImageSide.FRONT)
                
                if (preprocessedBitmap != originalBitmap) {
                    preprocessedBitmap.recycle()
                }
                
                // If we found card number, use this result
                if (result.getCardNumber() != null) {
                    val validatedResult = validateAndEnhanceResult(result)
                    if (validatedResult.confidence > (bestResult?.confidence ?: 0f)) {
                        bestResult = validatedResult
                    }
                    // If confidence is good enough, stop trying
                    if (validatedResult.confidence >= 0.85f) {
                        break
                    }
                } else if (bestResult == null || result.rawText.length > (bestResult.rawText.length)) {
                    bestResult = result
                }
            }
            
            originalBitmap.recycle()
            
            val processingTime = System.currentTimeMillis() - startTime
            return (bestResult ?: OCRResult(
                success = false,
                extractedData = emptyMap(),
                rawText = "",
                confidence = 0f,
                errorMessage = "No text could be extracted",
                processingTimeMs = processingTime
            )).copy(processingTimeMs = processingTime)
            
        } catch (e: Exception) {
            return OCRResult(
                success = false,
                extractedData = emptyMap(),
                rawText = "",
                confidence = 0f,
                errorMessage = e.message,
                processingTimeMs = System.currentTimeMillis() - startTime
            )
        }
    }
    
    /**
     * Preprocesses a bitmap for better OCR accuracy.
     * Applies grayscale conversion and contrast enhancement.
     */
    private fun preprocessBitmap(original: Bitmap, contrastLevel: Float): Bitmap {
        if (contrastLevel == 1.0f) return original
        
        val width = original.width
        val height = original.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        
        // Create color matrix for contrast and brightness adjustment
        val colorMatrix = ColorMatrix()
        
        // First, convert to grayscale for more consistent text detection
        val grayscaleMatrix = ColorMatrix(floatArrayOf(
            0.299f, 0.587f, 0.114f, 0f, 0f,
            0.299f, 0.587f, 0.114f, 0f, 0f,
            0.299f, 0.587f, 0.114f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        
        // Then apply contrast enhancement
        val translate = (1f - contrastLevel) * 128f + BRIGHTNESS_ADJUSTMENT
        val contrastMatrix = ColorMatrix(floatArrayOf(
            contrastLevel, 0f, 0f, 0f, translate,
            0f, contrastLevel, 0f, 0f, translate,
            0f, 0f, contrastLevel, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))
        
        // Combine matrices: first grayscale, then contrast
        colorMatrix.setConcat(contrastMatrix, grayscaleMatrix)
        
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        
        canvas.drawBitmap(original, 0f, 0f, paint)
        
        return result
    }
    
    /**
     * Processes a single bitmap and returns OCR result
     */
    private suspend fun processSingleImage(
        bitmap: Bitmap, 
        cardType: CardType, 
        imageSide: ImageSide
    ): OCRResult = suspendCoroutine { continuation ->
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            
            textRecognizer
                .process(inputImage)
                .addOnSuccessListener { visionText ->
                    val extractedData = cardTextParser.parseCardText(visionText.text, cardType, imageSide)
                    val confidence = calculateFieldBasedConfidence(extractedData, cardType)
                    
                    continuation.resume(OCRResult(
                        success = true,
                        extractedData = extractedData,
                        rawText = visionText.text,
                        confidence = confidence,
                        processingTimeMs = 0
                    ))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(OCRResult(
                        success = false,
                        extractedData = emptyMap(),
                        rawText = "",
                        confidence = 0f,
                        errorMessage = exception.message,
                        processingTimeMs = 0
                    ))
                }
        } catch (e: Exception) {
            continuation.resume(OCRResult(
                success = false,
                extractedData = emptyMap(),
                rawText = "",
                confidence = 0f,
                errorMessage = e.message,
                processingTimeMs = 0
            ))
        }
    }
    
    /**
     * Validates extracted data and enhances confidence scoring
     */
    private fun validateAndEnhanceResult(result: OCRResult): OCRResult {
        val enhancedData = result.extractedData.toMutableMap()
        var confidenceBoost = 0f
        
        // Validate card number with Luhn algorithm
        result.getCardNumber()?.let { cardNumber ->
            val digitsOnly = cardNumber.replace(" ", "")
            if (isValidLuhn(digitsOnly)) {
                confidenceBoost += 0.15f
            } else {
                // Try to correct common OCR mistakes
                val correctedNumber = correctCommonOCRMistakes(digitsOnly)
                if (isValidLuhn(correctedNumber)) {
                    enhancedData[AppConstants.CardProcessing.FIELD_CARD_NUMBER] = formatCardNumber(correctedNumber)
                    confidenceBoost += 0.10f
                }
            }
        }
        
        // Validate expiry date format and reasonableness
        result.getExpiryDate()?.let { expiry ->
            if (isValidExpiryDate(expiry)) {
                confidenceBoost += 0.05f
            }
        }
        
        return result.copy(
            extractedData = enhancedData,
            confidence = (result.confidence + confidenceBoost).coerceAtMost(1.0f)
        )
    }
    
    /**
     * Luhn algorithm validation for card numbers
     */
    private fun isValidLuhn(cardNumber: String): Boolean {
        if (cardNumber.length < 13 || cardNumber.length > 19) return false
        if (!cardNumber.all { it.isDigit() }) return false
        
        var sum = 0
        var alternate = false
        
        for (i in cardNumber.length - 1 downTo 0) {
            var digit = cardNumber[i].digitToInt()
            
            if (alternate) {
                digit *= 2
                if (digit > 9) digit -= 9
            }
            
            sum += digit
            alternate = !alternate
        }
        
        return sum % 10 == 0
    }
    
    /**
     * Corrects common OCR mistakes in card numbers
     */
    private fun correctCommonOCRMistakes(cardNumber: String): String {
        return cardNumber
            .replace('O', '0')
            .replace('o', '0')
            .replace('I', '1')
            .replace('l', '1')
            .replace('S', '5')
            .replace('B', '8')
            .replace('G', '6')
    }
    
    /**
     * Validates expiry date is in reasonable range
     */
    private fun isValidExpiryDate(expiry: String): Boolean {
        val parts = expiry.split("/")
        if (parts.size != 2) return false
        
        val month = parts[0].toIntOrNull() ?: return false
        val year = parts[1].toIntOrNull() ?: return false
        
        if (month < 1 || month > 12) return false
        
        // Year should be reasonable (current year to +10 years)
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) % 100
        return year in currentYear..(currentYear + 15)
    }
    
    /**
     * Formats card number with spaces
     */
    private fun formatCardNumber(cardNumber: String): String {
        return when (cardNumber.length) {
            15 -> "${cardNumber.take(4)} ${cardNumber.substring(4, 10)} ${cardNumber.takeLast(5)}"
            16 -> cardNumber.chunked(4).joinToString(" ")
            else -> cardNumber.chunked(4).joinToString(" ")
        }
    }
    
    /**
     * Calculates confidence based on which fields were successfully extracted
     */
    private fun calculateFieldBasedConfidence(extractedData: Map<String, String>, cardType: CardType): Float {
        if (extractedData.isEmpty()) return 0f
        
        var confidence = 0.3f // Base confidence for getting any text
        
        // Card number is most important
        if (extractedData.containsKey(AppConstants.CardProcessing.FIELD_CARD_NUMBER)) {
            confidence += 0.35f
        }
        
        // Expiry date
        if (extractedData.containsKey(AppConstants.CardProcessing.FIELD_EXPIRY_DATE)) {
            confidence += 0.15f
        }
        
        // Cardholder name
        if (extractedData.containsKey(AppConstants.CardProcessing.FIELD_CARDHOLDER_NAME)) {
            confidence += 0.10f
        }
        
        // CVV (usually from back)
        if (extractedData.containsKey(AppConstants.CardProcessing.FIELD_CVV)) {
            confidence += 0.10f
        }
        
        return confidence.coerceAtMost(1.0f)
    }

    /**
     * Processes an image for a specific side (front or back) with enhanced preprocessing.
     * Tries multiple contrast levels to find the best result.
     */
    suspend fun processImageSide(imageData: ByteArray, cardType: CardType, imageSide: ImageSide): OCRResult {
        val startTime = System.currentTimeMillis()
        
        try {
            val originalBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                ?: throw IllegalArgumentException("Invalid image data")
            
            var bestResult: OCRResult? = null
            
            for (contrastLevel in CONTRAST_LEVELS) {
                val preprocessedBitmap = preprocessBitmap(originalBitmap, contrastLevel)
                val result = processSingleImage(preprocessedBitmap, cardType, imageSide)
                
                if (preprocessedBitmap != originalBitmap) {
                    preprocessedBitmap.recycle()
                }
                
                // For front side, prioritize card number; for back, prioritize CVV
                val hasKeyField = when (imageSide) {
                    ImageSide.FRONT -> result.getCardNumber() != null
                    ImageSide.BACK -> result.getCVV() != null
                }
                
                if (hasKeyField) {
                    val validatedResult = if (imageSide == ImageSide.FRONT) {
                        validateAndEnhanceResult(result)
                    } else {
                        result
                    }
                    
                    if (validatedResult.confidence > (bestResult?.confidence ?: 0f)) {
                        bestResult = validatedResult
                    }
                    
                    if (validatedResult.confidence >= 0.85f) {
                        break
                    }
                } else if (bestResult == null || result.rawText.length > (bestResult.rawText.length)) {
                    bestResult = result
                }
            }
            
            originalBitmap.recycle()
            
            val processingTime = System.currentTimeMillis() - startTime
            return (bestResult ?: OCRResult(
                success = false,
                extractedData = emptyMap(),
                rawText = "",
                confidence = 0f,
                errorMessage = "No text could be extracted",
                processingTimeMs = processingTime
            )).copy(processingTimeMs = processingTime)
            
        } catch (e: Exception) {
            return OCRResult(
                success = false,
                extractedData = emptyMap(),
                rawText = "",
                confidence = 0f,
                errorMessage = e.message,
                processingTimeMs = System.currentTimeMillis() - startTime
            )
        }
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