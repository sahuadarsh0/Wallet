package com.technitedminds.wallet.domain.usecase.card

import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.repository.CardRepository
import com.technitedminds.wallet.domain.repository.CategoryRepository
import com.technitedminds.wallet.domain.repository.ImageRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Use case for adding a new card to the wallet. Handles validation logic for both textual and image
 * card types.
 */
class AddCardUseCase
@Inject
constructor(
        private val cardRepository: CardRepository,
        private val categoryRepository: CategoryRepository,
        private val imageRepository: ImageRepository
) {

    /**
     * Adds a new card with validation
     * @param request The card creation request
     * @return Result containing the created card ID or error
     */
    suspend operator fun invoke(request: AddCardRequest): Result<String> {
        return try {
            // Validate the request
            val validationResult = validateRequest(request)
            if (!validationResult.isValid) {
                return Result.failure(IllegalArgumentException(validationResult.errorMessage))
            }

            // Check if category exists
            val categoryExists = categoryRepository.categoryExists(request.categoryId)
            if (!categoryExists) {
                return Result.failure(
                        IllegalArgumentException("Category does not exist: ${request.categoryId}")
                )
            }

            // Save images
            val frontImagePath =
                    imageRepository.saveImage(
                            imageData = request.frontImageData,
                            fileName = "${request.cardId}_front",
                            imageType = com.technitedminds.wallet.domain.model.ImageType.FRONT
                    )

            val backImagePath =
                    imageRepository.saveImage(
                            imageData = request.backImageData,
                            fileName = "${request.cardId}_back",
                            imageType = com.technitedminds.wallet.domain.model.ImageType.BACK
                    )

            // Create the card
            val card =
                    Card(
                            id = request.cardId,
                            name = request.name.trim(),
                            type = request.type,
                            categoryId = request.categoryId,
                            frontImagePath = frontImagePath,
                            backImagePath = backImagePath,
                            extractedData = request.extractedData,
                            customFields = request.customFields,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                    )

            // Save the card
            val cardId = cardRepository.insertCard(card)
            Result.success(cardId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Validates the add card request */
    private fun validateRequest(request: AddCardRequest): ValidationResult {
        // Validate card name
        if (request.name.isBlank()) {
            return ValidationResult(false, "Card name cannot be empty")
        }

        if (request.name.length > 100) {
            return ValidationResult(false, "Card name cannot exceed 100 characters")
        }

        // Validate category ID
        if (request.categoryId.isBlank()) {
            return ValidationResult(false, "Category ID cannot be empty")
        }

        // Validate images
        if (request.frontImageData.isEmpty()) {
            return ValidationResult(false, "Front image is required")
        }

        if (request.backImageData.isEmpty()) {
            return ValidationResult(false, "Back image is required")
        }

        // Validate image data
        if (!isValidImageData(request.frontImageData)) {
            return ValidationResult(false, "Invalid front image data")
        }

        if (!isValidImageData(request.backImageData)) {
            return ValidationResult(false, "Invalid back image data")
        }

        // Validate extracted data for textual cards
        if (request.type.supportsOCR() && request.extractedData.isEmpty()) {
            // This is a warning, not an error - OCR might have failed
        }

        // Validate custom fields
        if (request.customFields.size > 20) {
            return ValidationResult(false, "Cannot have more than 20 custom fields")
        }

        for ((key, value) in request.customFields) {
            if (key.isBlank()) {
                return ValidationResult(false, "Custom field key cannot be empty")
            }
            if (key.length > 50) {
                return ValidationResult(false, "Custom field key cannot exceed 50 characters")
            }
            if (value.length > 500) {
                return ValidationResult(false, "Custom field value cannot exceed 500 characters")
            }
        }

        return ValidationResult(true)
    }

    /** Basic validation for image data */
    private fun isValidImageData(imageData: ByteArray): Boolean {
        if (imageData.size < 100) return false // Too small to be a valid image
        if (imageData.size > 10 * 1024 * 1024) return false // Larger than 10MB

        // Check for common image file signatures
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
     * Convenience overload to add a card when images are already handled upstream.
     * Skips image validation/saving and inserts the provided card as-is.
     */
    suspend operator fun invoke(card: Card): Result<String> {
        return try {
            val categoryExists = categoryRepository.categoryExists(card.categoryId)
            if (!categoryExists) {
                return Result.failure(IllegalArgumentException("Category does not exist: ${card.categoryId}"))
            }
            val id = cardRepository.insertCard(card)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/** Request data for adding a new card */
data class AddCardRequest(
        val cardId: String = UUID.randomUUID().toString(),
        val name: String,
        val type: CardType,
        val categoryId: String,
        val frontImageData: ByteArray,
        val backImageData: ByteArray,
        val extractedData: Map<String, String> = emptyMap(),
        val customFields: Map<String, String> = emptyMap()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddCardRequest

        if (cardId != other.cardId) return false
        if (name != other.name) return false
        if (type != other.type) return false
        if (categoryId != other.categoryId) return false
        if (!frontImageData.contentEquals(other.frontImageData)) return false
        if (!backImageData.contentEquals(other.backImageData)) return false
        if (extractedData != other.extractedData) return false
        if (customFields != other.customFields) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cardId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + categoryId.hashCode()
        result = 31 * result + frontImageData.contentHashCode()
        result = 31 * result + backImageData.contentHashCode()
        result = 31 * result + extractedData.hashCode()
        result = 31 * result + customFields.hashCode()
        return result
    }
}

/** Validation result for card requests */
data class ValidationResult(val isValid: Boolean, val errorMessage: String? = null)
