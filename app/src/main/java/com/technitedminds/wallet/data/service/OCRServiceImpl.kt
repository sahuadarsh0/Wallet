package com.technitedminds.wallet.data.service

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.technitedminds.wallet.data.ocr.CardTextParser
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.model.ImageSide
import com.technitedminds.wallet.domain.model.OCRResult
import com.technitedminds.wallet.domain.service.OCRService
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Implementation of OCRService using ML Kit for offline text recognition.
 * Specifically optimized for credit/debit card text extraction.
 */
@Singleton
class OCRServiceImpl @Inject constructor(
    private val cardTextParser: CardTextParser
) : OCRService {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun processImage(imageData: ByteArray, cardType: CardType): OCRResult =
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
                            processingTimeMs = 0
                        )

                        bitmap.recycle()
                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        bitmap.recycle()
                        continuation.resume(OCRResult.failed(exception.message))
                    }
            } catch (e: Exception) {
                continuation.resume(OCRResult.failed(e.message))
            }
        }

    override suspend fun processImageSide(
        imageData: ByteArray,
        cardType: CardType,
        imageSide: ImageSide
    ): OCRResult = suspendCoroutine { continuation ->
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
                    continuation.resume(OCRResult.failed(exception.message))
                }
        } catch (e: Exception) {
            continuation.resume(OCRResult.failed(e.message))
        }
    }

    /** Parses extracted text specifically for card information */
    private fun parseTextForCard(rawText: String, cardType: CardType): Map<String, String> {
        // Only process textual cards
        if (!cardType.supportsOCR()) {
            return emptyMap()
        }

        // Use the specialized card text parser - default to FRONT side
        return cardTextParser.parseCardText(rawText, cardType, ImageSide.FRONT)
    }

    /** Calculates overall confidence score from ML Kit results */
    private fun calculateOverallConfidence(visionText: com.google.mlkit.vision.text.Text): Float {
        if (visionText.textBlocks.isEmpty()) return 0f

        var totalConfidence = 0f
        var blockCount = 0

        for (block in visionText.textBlocks) {
            // ML Kit doesn't provide confidence scores in the free version
            // Using a placeholder 80% confidence
            totalConfidence += 0.8f
            blockCount++
        }

        return if (blockCount > 0) totalConfidence / blockCount else 0f
    }
}
