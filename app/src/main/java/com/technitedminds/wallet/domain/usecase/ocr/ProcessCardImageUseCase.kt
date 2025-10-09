package com.technitedminds.wallet.domain.usecase.ocr

import com.technitedminds.wallet.data.ocr.MLKitTextRecognizer
import com.technitedminds.wallet.domain.model.CardType
import javax.inject.Inject

/**
 * Use case for processing card images with OCR for textual cards only. Extracts text information
 * from credit, debit cards using ML Kit.
 */
class ProcessCardImageUseCase @Inject constructor(
    private val mlKitTextRecognizer: MLKitTextRecognizer
) {

    /**
     * Processes a card image to extract text data
     * @param request The OCR processing request
     * @return Result containing extracted data or error
     */
    suspend operator fun invoke(request: ProcessCardImageRequest): Result<Map<String, String>> {
        return try {
            // Only process textual cards
            if (!request.cardType.supportsOCR()) {
                return Result.success(emptyMap())
            }

            // Validate image data
            if (!isValidImageData(request.imageData)) {
                return Result.failure(IllegalArgumentException("Invalid image data"))
            }

            // Process the image using ML Kit with side information
            val ocrResult = mlKitTextRecognizer.processImageSide(
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
     * Processes both front and back images of a textual card
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
            val frontResult = mlKitTextRecognizer.processImageSide(frontImageData, cardType, ImageSide.FRONT)
            
            // Process back side
            val backResult = mlKitTextRecognizer.processImageSide(backImageData, cardType, ImageSide.BACK)

            // Combine results
            val combinedData = mutableMapOf<String, String>()
            
            if (frontResult.success) {
                combinedData.putAll(frontResult.extractedData)
            }
            
            if (backResult.success) {
                combinedData.putAll(backResult.extractedData)
            }

            // Filter to only extract the 4 required fields: cardNumber, expiryDate, cardholderName, cvv
            val filteredData = mutableMapOf<String, String>()
            combinedData["cardNumber"]?.let { filteredData["cardNumber"] = it }
            combinedData["expiryDate"]?.let { filteredData["expiryDate"] = it }
            combinedData["cardholderName"]?.let { filteredData["cardholderName"] = it }
            combinedData["cvv"]?.let { filteredData["cvv"] = it }

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
     * Processes a textual card image to extract relevant information This is a placeholder
     * implementation - in a real app, this would use ML Kit
     */
    private suspend fun processTextualCard(
            imageData: ByteArray,
            imageSide: ImageSide
    ): Map<String, String> {
        // Placeholder implementation
        // In a real app, this would:
        // 1. Use ML Kit Text Recognition API
        // 2. Apply image preprocessing (contrast, rotation, etc.)
        // 3. Extract text using OCR
        // 4. Parse the extracted text for card-specific information
        // 5. Validate and format the extracted data

        val extractedData = mutableMapOf<String, String>()

        when (imageSide) {
            ImageSide.FRONT -> {
                // Front side typically contains:
                // - Card number
                // - Cardholder name
                // - Expiry date
                // - Bank name/logo

                // Placeholder extracted data
                extractedData["processingStatus"] = "placeholder_implementation"
                extractedData["imageSide"] = "front"
                extractedData["ocrConfidence"] = "0.0"
            }
            ImageSide.BACK -> {
                // Back side typically contains:
                // - CVV/CVC
                // - Signature strip
                // - Additional bank information
                // - Customer service information

                // Placeholder extracted data
                extractedData["processingStatus"] = "placeholder_implementation"
                extractedData["imageSide"] = "back"
                extractedData["ocrConfidence"] = "0.0"
            }
        }

        return extractedData
    }

    /** Validates image data for OCR processing */
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

    /**
     * Parses raw OCR text to extract structured card information This would contain the actual
     * parsing logic in a real implementation
     */
    private fun parseCardText(
            rawText: String,
            cardType: CardType,
            imageSide: ImageSide
    ): Map<String, String> {
        val extractedData = mutableMapOf<String, String>()

        // Placeholder parsing logic
        // Real implementation would use regex patterns and validation

        return extractedData
    }

    /** Validates extracted card data */
    private fun validateExtractedData(data: Map<String, String>): Map<String, String> {
        val validatedData = mutableMapOf<String, String>()

        // Validate card number (Luhn algorithm, format, etc.)
        data["cardNumber"]?.let { cardNumber ->
            if (isValidCardNumber(cardNumber)) {
                validatedData["cardNumber"] = formatCardNumber(cardNumber)
            }
        }

        // Validate expiry date
        data["expiryDate"]?.let { expiryDate ->
            if (isValidExpiryDate(expiryDate)) {
                validatedData["expiryDate"] = formatExpiryDate(expiryDate)
            }
        }

        // Validate cardholder name
        data["cardholderName"]?.let { name ->
            if (isValidCardholderName(name)) {
                validatedData["cardholderName"] = formatCardholderName(name)
            }
        }

        // Copy other fields as-is
        data.forEach { (key, value) ->
            if (key !in listOf("cardNumber", "expiryDate", "cardholderName")) {
                validatedData[key] = value
            }
        }

        return validatedData
    }

    // Placeholder validation methods
    private fun isValidCardNumber(cardNumber: String): Boolean = cardNumber.isNotBlank()
    private fun isValidExpiryDate(expiryDate: String): Boolean = expiryDate.isNotBlank()
    private fun isValidCardholderName(name: String): Boolean = name.isNotBlank()

    // Placeholder formatting methods
    private fun formatCardNumber(cardNumber: String): String = cardNumber.trim()
    private fun formatExpiryDate(expiryDate: String): String = expiryDate.trim()
    private fun formatCardholderName(name: String): String = name.trim().uppercase()
}

/** Request for processing a card image */
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

/** Enum representing which side of the card is being processed */
enum class ImageSide {
    FRONT,
    BACK
}
