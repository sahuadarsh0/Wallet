package com.technitedminds.wallet.presentation.components.sharing

/**
 * Options for sharing card images
 */
enum class CardSharingOption {
    /** Share only the front side of the card */
    FrontOnly,
    
    /** Share only the back side of the card */
    BackOnly,
    
    /** Share both sides of the card */
    BothSides
}

/**
 * Result of card sharing operation
 */
sealed class CardSharingResult {
    /** Sharing was successful */
    object Success : CardSharingResult()
    
    /** Sharing was cancelled by user */
    object Cancelled : CardSharingResult()
    
    /** Sharing failed with error */
    data class Error(val message: String) : CardSharingResult()
}

/**
 * Card sharing configuration
 */
data class CardSharingConfig(
    /** Whether to include sensitive information in shared images */
    val includeSensitiveInfo: Boolean = false,
    
    /** Quality of shared images (0.0 to 1.0) */
    val imageQuality: Float = 0.8f,
    
    /** Maximum width of shared images in pixels */
    val maxImageWidth: Int = 1200,
    
    /** Maximum height of shared images in pixels */
    val maxImageHeight: Int = 800,
    
    /** Whether to add watermark to shared images */
    val addWatermark: Boolean = false,
    
    /** Watermark text if enabled */
    val watermarkText: String = "CardVault"
)