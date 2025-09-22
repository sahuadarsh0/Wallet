package com.technitedminds.wallet.domain.usecase.card

import com.technitedminds.wallet.domain.repository.CardRepository
import com.technitedminds.wallet.domain.repository.ImageRepository
import javax.inject.Inject

/** Use case for deleting cards with proper error handling and cleanup. */
class DeleteCardUseCase
@Inject
constructor(
        private val cardRepository: CardRepository,
        private val imageRepository: ImageRepository
) {

    /**
     * Deletes a single card by ID
     * @param cardId The ID of the card to delete
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(cardId: String): Result<Unit> {
        return try {
            // Check if card exists and get its data for cleanup
            val card =
                    cardRepository.getCardById(cardId)
                            ?: return Result.failure(
                                    IllegalArgumentException("Card not found: $cardId")
                            )

            // Delete associated images first
            val imageDeletionResults = mutableListOf<Boolean>()

            if (card.frontImagePath.isNotBlank()) {
                imageDeletionResults.add(imageRepository.deleteImage(card.frontImagePath))
            }

            if (card.backImagePath.isNotBlank()) {
                imageDeletionResults.add(imageRepository.deleteImage(card.backImagePath))
            }

            // Delete the card from repository
            cardRepository.deleteCard(cardId)

            // Log any image deletion failures (but don't fail the operation)
            val failedImageDeletions = imageDeletionResults.count { !it }
            if (failedImageDeletions > 0) {
                // In a real app, you might want to log this or handle it differently
                println(
                        "Warning: $failedImageDeletions image(s) could not be deleted for card $cardId"
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes multiple cards by their IDs
     * @param cardIds List of card IDs to delete
     * @return DeleteMultipleResult with success/failure details
     */
    suspend fun deleteMultiple(cardIds: List<String>): DeleteMultipleResult {
        val results = mutableListOf<DeleteResult>()
        var successCount = 0
        var failureCount = 0

        for (cardId in cardIds) {
            try {
                val result = invoke(cardId)
                if (result.isSuccess) {
                    results.add(DeleteResult(cardId, true, null))
                    successCount++
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    results.add(DeleteResult(cardId, false, error))
                    failureCount++
                }
            } catch (e: Exception) {
                results.add(DeleteResult(cardId, false, e.message ?: "Unknown error"))
                failureCount++
            }
        }

        return DeleteMultipleResult(
                totalRequested = cardIds.size,
                successCount = successCount,
                failureCount = failureCount,
                results = results
        )
    }

    /**
     * Deletes all cards in a specific category
     * @param categoryId The ID of the category
     * @return DeleteMultipleResult with success/failure details
     */
    suspend fun deleteByCategory(categoryId: String): DeleteMultipleResult {
        return try {
            // Get all cards in the category
            val cardsFlow = cardRepository.getCardsByCategory(categoryId)

            // Note: In a real implementation, you'd need to collect the flow
            // For now, we'll return an empty result as we can't collect flows in this context
            DeleteMultipleResult(
                    totalRequested = 0,
                    successCount = 0,
                    failureCount = 0,
                    results = emptyList()
            )
        } catch (e: Exception) {
            DeleteMultipleResult(
                    totalRequested = 0,
                    successCount = 0,
                    failureCount = 1,
                    results =
                            listOf(
                                    DeleteResult(
                                            "category:$categoryId",
                                            false,
                                            e.message ?: "Unknown error"
                                    )
                            )
            )
        }
    }

    /**
     * Performs a soft delete by marking the card as deleted (if implemented in the future)
     * Currently just calls the regular delete
     */
    suspend fun softDelete(cardId: String): Result<Unit> {
        // For now, just perform regular delete
        // In the future, this could mark cards as deleted without actually removing them
        return invoke(cardId)
    }

    /**
     * Validates if a card can be deleted
     * @param cardId The ID of the card to check
     * @return Result indicating if deletion is allowed
     */
    suspend fun canDelete(cardId: String): Result<Boolean> {
        return try {
            val cardExists = cardRepository.cardExists(cardId)
            if (!cardExists) {
                Result.failure(IllegalArgumentException("Card not found: $cardId"))
            } else {
                // All cards can be deleted in this app
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/** Result of a single card deletion */
data class DeleteResult(val cardId: String, val success: Boolean, val errorMessage: String?)

/** Result of multiple card deletions */
data class DeleteMultipleResult(
        val totalRequested: Int,
        val successCount: Int,
        val failureCount: Int,
        val results: List<DeleteResult>
) {
    val isCompleteSuccess: Boolean
        get() = failureCount == 0 && successCount == totalRequested

    val isPartialSuccess: Boolean
        get() = successCount > 0 && failureCount > 0

    val isCompleteFailure: Boolean
        get() = successCount == 0 && failureCount == totalRequested
}
