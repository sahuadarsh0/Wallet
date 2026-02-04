package com.technitedminds.wallet.domain.usecase.ocr

import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.model.ImageSide
import com.technitedminds.wallet.domain.model.OCRResult
import com.technitedminds.wallet.domain.service.OCRService
import javax.inject.Inject

/**
 * Use case for processing card images with OCR for textual cards only.
 * Extracts text information from credit and debit cards using the OCR service.
 * 
 * This use case follows Clean Architecture by depending on the OCRService interface
 * rather than a concrete implementation.
 */
class ProcessCardImageUseCase @Inject constructor(
    private val ocrService: OCRService
) {

    /**
     * Processes a card image to extract text data.
     * 
     * @param request The OCR processing request
     * @return Result containing extracted data or error
     */
    suspend operator fun invoke(request: ProcessCardImageRequest): Result<Map<String, String>> {
        return try {
            // Only process textual cards (Credit/Debit)
            if (!request.cardType.supportsOCR()) {
                return Result.success(emptyMap())
            }

            // Validate image data
            if (!isValidImageData(request.imageData)) {
                return Result.failure(IllegalArgumentException("Invalid image data"))
            }

            // Process the image using OCR service with side information
            val ocrResult = ocrService.processImageSide(
                request.imageData, 
                request.cardType, 
                request.imageSide
            )
            
            if (ocrResult.success) {
                Result.success(ocrResult.extractedData)
            } else {
                Result.failure(Exception(ocrResult.errorMessage ?: "OCR processing failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Processes both front and back images of a textual card.
     * 
     * @param frontImageData The front image data
     * @param backImageData The back image data
     * @param cardType The type of card being processed
     * @return Result containing combined extracted data
     */
    suspend fun processBothSides(
        frontImageData: ByteArray,
        backImageData: ByteArray,
        cardType: CardType
    ): Result<Map<String, String>> {
        return try {
            if (!cardType.supportsOCR()) {
                return Result.success(emptyMap())
            }

            // Process front side
            val frontResult = ocrService.processImageSide(frontImageData, cardType, ImageSide.FRONT)
            
            // Process back side
            val backResult = ocrService.processImageSide(backImageData, cardType, ImageSide.BACK)

            // Combine results
            val combinedData = mutableMapOf<String, String>()
            
            if (frontResult.success) {
                combinedData.putAll(frontResult.extractedData)
            }
            
            if (backResult.success) {
                combinedData.putAll(backResult.extractedData)
            }

            // Filter to only extract the 4 required fields
            val filteredData = mutableMapOf<String, String>()
            combinedData[OCRResult.FIELD_CARD_NUMBER]?.let { 
                filteredData[OCRResult.FIELD_CARD_NUMBER] = it 
            }
            combinedData[OCRResult.FIELD_EXPIRY_DATE]?.let { 
                filteredData[OCRResult.FIELD_EXPIRY_DATE] = it 
            }
            combinedData[OCRResult.FIELD_CARDHOLDER_NAME]?.let { 
                filteredData[OCRResult.FIELD_CARDHOLDER_NAME] = it 
            }
            combinedData[OCRResult.FIELD_CVV]?.let { 
                filteredData[OCRResult.FIELD_CVV] = it 
            }

            // Return success if we got any data, even if one side failed
            if (filteredData.isNotEmpty()) {
                Result.success(filteredData.toMap())
            } else {
                val errorMessage = listOfNotNull(
                    frontResult.errorMessage?.let { "Front: $it" },
                    backResult.errorMessage?.let { "Back: $it" }
                ).joinToString("; ").ifEmpty { "OCR processing failed for both sides" }
                
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Processes only the front image of a textual card.
     * Used when back image is not available or skipped.
     * 
     * @param frontImageData The front image data
     * @param cardType The type of card being processed
     * @return Result containing extracted data from front side only
     */
    suspend fun processFrontOnly(
        frontImageData: ByteArray,
        cardType: CardType
    ): Result<Map<String, String>> {
        return try {
            if (!cardType.supportsOCR()) {
                return Result.success(emptyMap())
            }

            // Process front side only
            val frontResult = ocrService.processImageSide(frontImageData, cardType, ImageSide.FRONT)

            // Filter to only extract the 4 required fields
            val filteredData = mutableMapOf<String, String>()
            if (frontResult.success) {
                frontResult.extractedData[OCRResult.FIELD_CARD_NUMBER]?.let { 
                    filteredData[OCRResult.FIELD_CARD_NUMBER] = it 
                }
                frontResult.extractedData[OCRResult.FIELD_EXPIRY_DATE]?.let { 
                    filteredData[OCRResult.FIELD_EXPIRY_DATE] = it 
                }
                frontResult.extractedData[OCRResult.FIELD_CARDHOLDER_NAME]?.let { 
                    filteredData[OCRResult.FIELD_CARDHOLDER_NAME] = it 
                }
                // Note: CVV is typically on the back, so won't be available with front-only processing
            }

            // Return success if we got any data
            if (filteredData.isNotEmpty()) {
                Result.success(filteredData.toMap())
            } else {
                val errorMessage = frontResult.errorMessage ?: "OCR processing failed for front side"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validates image data for OCR processing.
     * Checks file size and image format headers.
     */
    private fun isValidImageData(imageData: ByteArray): Boolean {
        if (imageData.size < 100) return false // Too small
        if (imageData.size > 10 * 1024 * 1024) return false // Too large (>10MB)

        // Check for valid image headers
        val header = imageData.take(8).toByteArray()
        return when {
            // JPEG
            header.size >= 2 && header[0] == 0xFF.toByte() && header[1] == 0xD8.toByte() -> true
            // PNG
            header.size >= 8 &&
                header[0] == 0x89.toByte() &&
                header[1] == 0x50.toByte() &&
                header[2] == 0x4E.toByte() &&
                header[3] == 0x47.toByte() -> true
            else -> false
        }
    }
}

/**
 * Request for processing a card image.
 */
data class ProcessCardImageRequest(
    val imageData: ByteArray,
    val cardType: CardType,
    val imageSide: ImageSide
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProcessCardImageRequest

        if (!imageData.contentEquals(other.imageData)) return false
        if (cardType != other.cardType) return false
        if (imageSide != other.imageSide) return false

        return true
    }

    override fun hashCode(): Int {
        var result = imageData.contentHashCode()
        result = 31 * result + cardType.hashCode()
        result = 31 * result + imageSide.hashCode()
        return result
    }
}
