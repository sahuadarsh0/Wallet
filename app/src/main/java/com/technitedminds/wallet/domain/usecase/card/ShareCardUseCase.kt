package com.technitedminds.wallet.domain.usecase.card

import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.repository.ImageRepository
import com.technitedminds.wallet.domain.service.CardImageGenerator
import javax.inject.Inject

/**
 * Use case for sharing cards with dual strategy:
 * - For textual cards (Credit/Debit): Generate gradient card designs with extracted details including CVV
 * - For image-only cards: Share the captured images
 */
class ShareCardUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val cardImageGenerator: CardImageGenerator
) {

    suspend operator fun invoke(
        card: Card,
        shareOption: ShareOption
    ): Result<List<String>> {
        return try {
            if (card.type.supportsOCR()) {
                // For textual cards (Credit/Debit), generate gradient card design with extracted details
                shareTextualCard(card, shareOption)
            } else {
                // For image-only cards, share the captured images
                shareImageCard(card, shareOption)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun shareTextualCard(card: Card, shareOption: ShareOption): Result<List<String>> {
        // Generate gradient card design showing all extracted details including CVV
        val gradientCardImage = cardImageGenerator.generateCardImage(
            card = card,
            showAllDetails = true, // Include CVV and all sensitive information
            includeBack = shareOption == ShareOption.BothSides || shareOption == ShareOption.BackOnly
        )
        return Result.success(listOf(gradientCardImage))
    }

    private suspend fun shareImageCard(card: Card, shareOption: ShareOption): Result<List<String>> {
        // Share the actual captured images
        val imagePaths = when (shareOption) {
            ShareOption.FrontOnly -> {
                if (card.frontImagePath.isNotBlank() && imageRepository.imageExists(card.frontImagePath)) {
                    listOf(card.frontImagePath)
                } else {
                    return Result.failure(Exception("Front image not found"))
                }
            }

            ShareOption.BackOnly -> {
                if (card.backImagePath.isNotBlank() && imageRepository.imageExists(card.backImagePath)) {
                    listOf(card.backImagePath)
                } else {
                    return Result.failure(Exception("Back image not found"))
                }
            }

            ShareOption.BothSides -> {
                val paths = mutableListOf<String>()
                if (card.frontImagePath.isNotBlank() && imageRepository.imageExists(card.frontImagePath)) {
                    paths.add(card.frontImagePath)
                }
                if (card.backImagePath.isNotBlank() && imageRepository.imageExists(card.backImagePath)) {
                    paths.add(card.backImagePath)
                }
                if (paths.isEmpty()) {
                    return Result.failure(Exception("No card images found"))
                }
                paths
            }
        }

        return Result.success(imagePaths)
    }
}

/**
 * Enum representing different sharing options for card images.
 */
enum class ShareOption {
    /** Share only the front image */
    FrontOnly,

    /** Share only the back image */
    BackOnly,

    /** Share both front and back images */
    BothSides
}
