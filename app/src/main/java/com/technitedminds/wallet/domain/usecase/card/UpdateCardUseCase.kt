package com.technitedminds.wallet.domain.usecase.card

import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.repository.CardRepository
import com.technitedminds.wallet.domain.repository.CategoryRepository
import com.technitedminds.wallet.domain.repository.ImageRepository
import javax.inject.Inject

/** Use case for updating an existing card with proper error handling. */
class UpdateCardUseCase
@Inject
constructor(
        private val cardRepository: CardRepository,
        private val categoryRepository: CategoryRepository,
        private val imageRepository: ImageRepository
) {

    /**
     * Updates an existing card
     * @param request The card update request
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(request: UpdateCardRequest): Result<Unit> {
        return try {
            // Check if card exists
            val existingCard =
                    cardRepository.getCardById(request.cardId)
                            ?: return Result.failure(
                                    IllegalArgumentException("Card not found: ${request.cardId}")
                            )

            // Validate the request
            val validationResult = validateRequest(request, existingCard)
            if (!validationResult.isValid) {
                return Result.failure(IllegalArgumentException(validationResult.errorMessage))
            }

            // Check if category exists (if changed)
            if (request.categoryId != null && request.categoryId != existingCard.categoryId) {
                val categoryExists = categoryRepository.categoryExists(request.categoryId)
                if (!categoryExists) {
                    return Result.failure(
                            IllegalArgumentException(
                                    "Category does not exist: ${request.categoryId}"
                            )
                    )
                }
            }

            // Handle image updates
            val frontImagePath =
                    handleImageUpdate(
                            newImageData = request.frontImageData,
                            existingImagePath = existingCard.frontImagePath,
                            fileName = "${request.cardId}_front",
                            imageType = com.technitedminds.wallet.domain.model.ImageType.FRONT
                    )

            val backImagePath =
                    handleImageUpdate(
                            newImageData = request.backImageData,
                            existingImagePath = existingCard.backImagePath,
                            fileName = "${request.cardId}_back",
                            imageType = com.technitedminds.wallet.domain.model.ImageType.BACK
                    )

            // Create updated card
            val updatedCard =
                    existingCard.copy(
                            name = request.name?.trim() ?: existingCard.name,
                            type = request.type ?: existingCard.type,
                            categoryId = request.categoryId ?: existingCard.categoryId,
                            frontImagePath = frontImagePath,
                            backImagePath = backImagePath,
                            extractedData = request.extractedData ?: existingCard.extractedData,
                            customFields = request.customFields ?: existingCard.customFields,
                            updatedAt = System.currentTimeMillis()
                    )

            // Update the card
            cardRepository.updateCard(updatedCard)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates only the extracted data of a card (useful after re-running OCR)
     * @param cardId The ID of the card to update
     * @param extractedData The new extracted data
     * @return Result indicating success or failure
     */
    suspend fun updateExtractedData(
            cardId: String,
            extractedData: Map<String, String>
    ): Result<Unit> {
        return try {
            val existingCard =
                    cardRepository.getCardById(cardId)
                            ?: return Result.failure(
                                    IllegalArgumentException("Card not found: $cardId")
                            )

            val updatedCard = existingCard.withExtractedData(extractedData)
            cardRepository.updateCard(updatedCard)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates only the custom fields of a card
     * @param cardId The ID of the card to update
     * @param customFields The new custom fields
     * @return Result indicating success or failure
     */
    suspend fun updateCustomFields(
            cardId: String,
            customFields: Map<String, String>
    ): Result<Unit> {
        return try {
            val existingCard =
                    cardRepository.getCardById(cardId)
                            ?: return Result.failure(
                                    IllegalArgumentException("Card not found: $cardId")
                            )

            val updatedCard = existingCard.withCustomFields(customFields)
            cardRepository.updateCard(updatedCard)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Handles image update logic - saves new image or keeps existing one */
    private suspend fun handleImageUpdate(
            newImageData: ByteArray?,
            existingImagePath: String,
            fileName: String,
            imageType: com.technitedminds.wallet.domain.model.ImageType
    ): String {
        return if (newImageData != null && newImageData.isNotEmpty()) {
            // Delete old image if it exists
            if (existingImagePath.isNotBlank()) {
                imageRepository.deleteImage(existingImagePath)
            }
            // Save new image
            imageRepository.saveImage(newImageData, fileName, imageType)
        } else {
            // Keep existing image
            existingImagePath
        }
    }

    /** Validates the update card request */
    private fun validateRequest(request: UpdateCardRequest, existingCard: Card): ValidationResult {
        // Validate card name if provided
        request.name?.let { name ->
            if (name.isBlank()) {
                return ValidationResult(false, "Card name cannot be empty")
            }
            if (name.length > 100) {
                return ValidationResult(false, "Card name cannot exceed 100 characters")
            }
        }

        // Validate category ID if provided
        request.categoryId?.let { categoryId ->
            if (categoryId.isBlank()) {
                return ValidationResult(false, "Category ID cannot be empty")
            }
        }

        // Validate image data if provided
        request.frontImageData?.let { imageData ->
            if (!isValidImageData(imageData)) {
                return ValidationResult(false, "Invalid front image data")
            }
        }

        request.backImageData?.let { imageData ->
            if (!isValidImageData(imageData)) {
                return ValidationResult(false, "Invalid back image data")
            }
        }

        // Validate custom fields if provided
        request.customFields?.let { customFields ->
            if (customFields.size > 20) {
                return ValidationResult(false, "Cannot have more than 20 custom fields")
            }

            for ((key, value) in customFields) {
                if (key.isBlank()) {
                    return ValidationResult(false, "Custom field key cannot be empty")
                }
                if (key.length > 50) {
                    return ValidationResult(false, "Custom field key cannot exceed 50 characters")
                }
                if (value.length > 500) {
                    return ValidationResult(
                            false,
                            "Custom field value cannot exceed 500 characters"
                    )
                }
            }
        }

        return ValidationResult(true)
    }

    /** Convenience overload to update when caller provides a full Card object */
    suspend operator fun invoke(card: Card): Result<Unit> {
        return try {
            val exists = cardRepository.getCardById(card.id) != null
            if (!exists) return Result.failure(IllegalArgumentException("Card not found: ${card.id}"))
            // Validate minimal required fields
            if (card.name.isBlank()) return Result.failure(IllegalArgumentException("Card name cannot be empty"))
            // Category check
            val categoryExists = categoryRepository.categoryExists(card.categoryId)
            if (!categoryExists) return Result.failure(IllegalArgumentException("Category does not exist: ${card.categoryId}"))
            cardRepository.updateCard(card.copy(updatedAt = System.currentTimeMillis()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
}

/**
 * Request data for updating a card All fields are optional - only provided fields will be updated
 */
data class UpdateCardRequest(
        val cardId: String,
        val name: String? = null,
        val type: CardType? = null,
        val categoryId: String? = null,
        val frontImageData: ByteArray? = null,
        val backImageData: ByteArray? = null,
        val extractedData: Map<String, String>? = null,
        val customFields: Map<String, String>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateCardRequest

        if (cardId != other.cardId) return false
        if (name != other.name) return false
        if (type != other.type) return false
        if (categoryId != other.categoryId) return false
        if (frontImageData != null) {
            if (other.frontImageData == null) return false
            if (!frontImageData.contentEquals(other.frontImageData)) return false
        } else if (other.frontImageData != null) return false
        if (backImageData != null) {
            if (other.backImageData == null) return false
            if (!backImageData.contentEquals(other.backImageData)) return false
        } else if (other.backImageData != null) return false
        if (extractedData != other.extractedData) return false
        if (customFields != other.customFields) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cardId.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (categoryId?.hashCode() ?: 0)
        result = 31 * result + (frontImageData?.contentHashCode() ?: 0)
        result = 31 * result + (backImageData?.contentHashCode() ?: 0)
        result = 31 * result + (extractedData?.hashCode() ?: 0)
        result = 31 * result + (customFields?.hashCode() ?: 0)
        return result
    }
}
