package com.technitedminds.wallet.domain.service

import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardGradient

/**
 * Service interface for generating card images.
 * Abstracts image generation from the domain layer to maintain Clean Architecture.
 * 
 * Implementations handle:
 * - Generating gradient card images for OCR cards (Credit/Debit)
 * - Generating default back images for image-only cards
 * - Creating shareable card images with extracted data overlay
 */
interface CardImageGenerator {
    
    /**
     * Generates a shareable card image with gradient design and extracted details.
     * 
     * @param card The card to generate image for
     * @param showAllDetails Whether to include all details including CVV
     * @param includeBack Whether to include back side information
     * @return Path to the generated card image, or error path if generation failed
     */
    suspend fun generateCardImage(
        card: Card,
        showAllDetails: Boolean = true,
        includeBack: Boolean = false
    ): String
    
    /**
     * Generates front card image and saves to file.
     * 
     * @param card The card to generate image for
     * @param showAllDetails Whether to include all details including CVV
     * @param width Image width in pixels
     * @param height Image height in pixels
     * @return Path to the generated image, or null if generation failed
     */
    suspend fun generateCardFrontImagePath(
        card: Card,
        showAllDetails: Boolean = true,
        width: Int = DEFAULT_CARD_WIDTH,
        height: Int = DEFAULT_CARD_HEIGHT
    ): String?
    
    /**
     * Generates back card image and saves to file.
     * 
     * @param card The card to generate image for
     * @param showAllDetails Whether to include all details including CVV
     * @param width Image width in pixels
     * @param height Image height in pixels
     * @return Path to the generated image, or null if generation failed
     */
    suspend fun generateCardBackImagePath(
        card: Card,
        showAllDetails: Boolean = true,
        width: Int = DEFAULT_CARD_WIDTH,
        height: Int = DEFAULT_CARD_HEIGHT
    ): String?
    
    /**
     * Generates a default back image for image-only cards using the specified gradient.
     * 
     * @param cardName The name to display on the back
     * @param cardTypeName The card type name to display
     * @param gradient The gradient colors to use
     * @param width Image width in pixels
     * @param height Image height in pixels
     * @return Path to the generated image, or null if generation failed
     */
    suspend fun generateDefaultBackImage(
        cardName: String,
        cardTypeName: String,
        gradient: CardGradient,
        width: Int = DEFAULT_CARD_WIDTH,
        height: Int = DEFAULT_CARD_HEIGHT
    ): String?
    
    companion object {
        /** Default card image width */
        const val DEFAULT_CARD_WIDTH = 1200
        
        /** Default card image height */
        const val DEFAULT_CARD_HEIGHT = 800
    }
}
