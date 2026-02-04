package com.technitedminds.wallet.domain.service

import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.model.OCRResult
import com.technitedminds.wallet.domain.model.ImageSide

/**
 * Service interface for OCR (Optical Character Recognition) operations.
 * Abstracts the OCR implementation from the domain layer to maintain Clean Architecture.
 * 
 * Implementations of this interface should handle text extraction from card images
 * for Credit and Debit cards only.
 */
interface OCRService {
    
    /**
     * Processes a card image to extract text data.
     * 
     * @param imageData The image data as byte array (JPEG or PNG format)
     * @param cardType The type of card being processed
     * @return OCRResult containing extracted data, confidence score, and status
     */
    suspend fun processImage(imageData: ByteArray, cardType: CardType): OCRResult
    
    /**
     * Processes a specific side of a card image.
     * 
     * @param imageData The image data as byte array
     * @param cardType The type of card being processed
     * @param imageSide Which side of the card (FRONT or BACK)
     * @return OCRResult containing extracted data specific to that side
     */
    suspend fun processImageSide(
        imageData: ByteArray,
        cardType: CardType,
        imageSide: ImageSide
    ): OCRResult
}
