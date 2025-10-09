package com.technitedminds.wallet.presentation.components.sharing

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.FileProvider
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.util.CardGradientGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for handling card sharing operations with dual sharing strategy:
 * - For image-only cards: Share captured images via FileProvider
 * - For textual cards: Generate gradient card designs with extracted details
 */
@Singleton
class CardSharingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cardGradientGenerator: CardGradientGenerator
) {
    
    /**
     * Share card based on the sharing option and card type
     */
    suspend fun shareCard(
        card: Card,
        sharingOption: CardSharingOption,
        config: CardSharingConfig = CardSharingConfig()
    ): CardSharingResult = withContext(Dispatchers.IO) {
        try {
            val filesToShare = when {
                card.type.supportsOCR() -> {
                    // For textual cards (Credit/Debit), generate gradient card designs
                    generateGradientCardImages(card, sharingOption, config)
                }
                else -> {
                    // For image-only cards, share the captured images
                    getCapturedImageFiles(card, sharingOption)
                }
            }
            
            if (filesToShare.isEmpty()) {
                return@withContext CardSharingResult.Error("No images available to share")
            }
            
            shareFiles(filesToShare, card.name)
            CardSharingResult.Success
            
        } catch (e: Exception) {
            CardSharingResult.Error(e.message ?: "Failed to share card")
        }
    }
    
    /**
     * Generate gradient card images for textual cards
     */
    private suspend fun generateGradientCardImages(
        card: Card,
        sharingOption: CardSharingOption,
        config: CardSharingConfig
    ): List<File> {
        val files = mutableListOf<File>()
        
        when (sharingOption) {
            CardSharingOption.FrontOnly -> {
                val frontImage = cardGradientGenerator.generateCardFrontImage(
                    card = card,
                    showAllDetails = config.includeSensitiveInfo,
                    width = config.maxImageWidth,
                    height = config.maxImageHeight
                )
                frontImage?.let { bitmap ->
                    val file = saveBitmapToFile(bitmap, "${card.id}_front_gradient.png", config)
                    files.add(file)
                }
            }
            
            CardSharingOption.BackOnly -> {
                val backImage = cardGradientGenerator.generateCardBackImage(
                    card = card,
                    showAllDetails = config.includeSensitiveInfo,
                    width = config.maxImageWidth,
                    height = config.maxImageHeight
                )
                backImage?.let { bitmap ->
                    val file = saveBitmapToFile(bitmap, "${card.id}_back_gradient.png", config)
                    files.add(file)
                }
            }
            
            CardSharingOption.BothSides -> {
                // Generate both front and back
                val frontImage = cardGradientGenerator.generateCardFrontImage(
                    card = card,
                    showAllDetails = config.includeSensitiveInfo,
                    width = config.maxImageWidth,
                    height = config.maxImageHeight
                )
                val backImage = cardGradientGenerator.generateCardBackImage(
                    card = card,
                    showAllDetails = config.includeSensitiveInfo,
                    width = config.maxImageWidth,
                    height = config.maxImageHeight
                )
                
                frontImage?.let { bitmap ->
                    val file = saveBitmapToFile(bitmap, "${card.id}_front_gradient.png", config)
                    files.add(file)
                }
                
                backImage?.let { bitmap ->
                    val file = saveBitmapToFile(bitmap, "${card.id}_back_gradient.png", config)
                    files.add(file)
                }
            }
        }
        
        return files
    }
    
    /**
     * Get captured image files for image-only cards
     */
    private fun getCapturedImageFiles(
        card: Card,
        sharingOption: CardSharingOption
    ): List<File> {
        val files = mutableListOf<File>()
        
        when (sharingOption) {
            CardSharingOption.FrontOnly -> {
                if (card.frontImagePath.isNotBlank()) {
                    val frontFile = File(card.frontImagePath)
                    if (frontFile.exists()) {
                        files.add(frontFile)
                    }
                }
            }
            
            CardSharingOption.BackOnly -> {
                if (card.backImagePath.isNotBlank()) {
                    val backFile = File(card.backImagePath)
                    if (backFile.exists()) {
                        files.add(backFile)
                    }
                }
            }
            
            CardSharingOption.BothSides -> {
                if (card.frontImagePath.isNotBlank()) {
                    val frontFile = File(card.frontImagePath)
                    if (frontFile.exists()) {
                        files.add(frontFile)
                    }
                }
                
                if (card.backImagePath.isNotBlank()) {
                    val backFile = File(card.backImagePath)
                    if (backFile.exists()) {
                        files.add(backFile)
                    }
                }
            }
        }
        
        return files
    }
    
    /**
     * Save bitmap to file with optional watermark
     */
    private fun saveBitmapToFile(
        bitmap: Bitmap,
        fileName: String,
        config: CardSharingConfig
    ): File {
        val finalBitmap = if (config.addWatermark) {
            addWatermark(bitmap, config.watermarkText)
        } else {
            bitmap
        }
        
        val shareDir = File(context.cacheDir, "shared_cards")
        if (!shareDir.exists()) {
            shareDir.mkdirs()
        }
        
        val file = File(shareDir, fileName)
        FileOutputStream(file).use { out ->
            finalBitmap.compress(
                Bitmap.CompressFormat.PNG,
                (config.imageQuality * 100).toInt(),
                out
            )
        }
        
        return file
    }
    
    /**
     * Add watermark to bitmap
     */
    private fun addWatermark(bitmap: Bitmap, watermarkText: String): Bitmap {
        val result = bitmap.copy(bitmap.config ?: Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        
        val paint = Paint().apply {
            color = android.graphics.Color.WHITE
            alpha = 128 // 50% transparency
            textSize = 24f
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        
        val bounds = Rect()
        paint.getTextBounds(watermarkText, 0, watermarkText.length, bounds)
        
        val x = result.width - bounds.width() - 20f
        val y = result.height - 20f
        
        canvas.drawText(watermarkText, x, y, paint)
        
        return result
    }
    
    /**
     * Share files using Android's sharing system
     */
    private fun shareFiles(files: List<File>, cardName: String) {
        val uris = files.map { file ->
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }
        
        val shareIntent = Intent().apply {
            if (uris.size == 1) {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uris.first())
            } else {
                action = Intent.ACTION_SEND_MULTIPLE
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            }
            type = "image/*"
            putExtra(Intent.EXTRA_SUBJECT, cardName)
            putExtra(Intent.EXTRA_TEXT, "Shared from CardVault")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        val chooserIntent = Intent.createChooser(shareIntent, "Share $cardName")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
    
    /**
     * Clean up temporary sharing files
     */
    fun cleanupTempFiles() {
        try {
            val shareDir = File(context.cacheDir, "shared_cards")
            if (shareDir.exists()) {
                shareDir.listFiles()?.forEach { file ->
                    if (file.isFile && System.currentTimeMillis() - file.lastModified() > 3600000) { // 1 hour
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
}