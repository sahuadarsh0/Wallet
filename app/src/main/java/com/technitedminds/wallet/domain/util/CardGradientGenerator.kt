package com.technitedminds.wallet.domain.util

import android.graphics.Bitmap
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Utility class for generating gradient card designs for sharing textual cards.
 * Creates visual card representations with extracted details including sensitive information.
 */
class CardGradientGenerator @Inject constructor(
    private val imageRepository: ImageRepository
) {
    
    /**
     * Generates a shareable card image with gradient design and extracted details.
     * 
     * @param card The card to generate image for
     * @param showAllDetails Whether to include all details including CVV
     * @param includeBack Whether to include back side information
     * @return Path to the generated card image
     */
    suspend fun generateCardImage(
        card: Card,
        showAllDetails: Boolean = true,
        includeBack: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        try {
            val width = 1200
            val height = 800
            
            if (includeBack) {
                // Generate combined front and back image
                val frontBitmap = generateCardBitmap(card, isBack = false, showAllDetails, width, height / 2)
                val backBitmap = generateCardBitmap(card, isBack = true, showAllDetails, width, height / 2)
                
                // Combine both images vertically
                val combinedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(combinedBitmap)
                
                // Draw front on top
                canvas.drawBitmap(frontBitmap, 0f, 0f, null)
                // Draw back on bottom
                canvas.drawBitmap(backBitmap, 0f, height / 2f, null)
                
                // Save combined image
                val fileName = "combined_card_${card.id}_${System.currentTimeMillis()}.png"
                val file = saveBitmapToTempFile(combinedBitmap, fileName)
                file.absolutePath
            } else {
                // Generate front only
                val frontBitmap = generateCardBitmap(card, isBack = false, showAllDetails, width, height)
                val fileName = "card_${card.id}_${System.currentTimeMillis()}.png"
                val file = saveBitmapToTempFile(frontBitmap, fileName)
                file.absolutePath
            }
        } catch (e: Exception) {
            "error_generating_card_${card.id}.png"
        }
    }
    
    /**
     * Save bitmap to temporary file for sharing
     */
    private suspend fun saveBitmapToTempFile(bitmap: Bitmap, fileName: String): java.io.File = withContext(Dispatchers.IO) {
        val tempDir = java.io.File(System.getProperty("java.io.tmpdir"), "card_sharing")
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
        
        val file = java.io.File(tempDir, fileName)
        java.io.FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        }
        file
    }
    
    /**
     * Generate front card image as bitmap
     */
    suspend fun generateCardFrontImage(
        card: Card,
        showAllDetails: Boolean = true,
        width: Int = 1200,
        height: Int = 800
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                generateCardBitmap(card, isBack = false, showAllDetails, width, height)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Generate back card image as bitmap
     */
    suspend fun generateCardBackImage(
        card: Card,
        showAllDetails: Boolean = true,
        width: Int = 1200,
        height: Int = 800
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                generateCardBitmap(card, isBack = true, showAllDetails, width, height)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Generate card bitmap with gradient background and text
     */
    private fun generateCardBitmap(
        card: Card,
        isBack: Boolean,
        showAllDetails: Boolean,
        width: Int,
        height: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        // Create gradient background
        val gradient = getCardTypeGradient(card)
        val gradientDrawable = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                android.graphics.Color.parseColor(gradient.first),
                android.graphics.Color.parseColor(gradient.second)
            )
        )
        gradientDrawable.setBounds(0, 0, width, height)
        gradientDrawable.cornerRadius = 40f
        gradientDrawable.draw(canvas)
        
        // Add card content
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
        }
        
        if (isBack) {
            drawCardBack(canvas, card, paint, showAllDetails, width, height)
        } else {
            drawCardFront(canvas, card, paint, showAllDetails, width, height)
        }
        
        return bitmap
    }
    
    /**
     * Draw front side content
     */
    private fun drawCardFront(
        canvas: android.graphics.Canvas,
        card: Card,
        paint: android.graphics.Paint,
        showAllDetails: Boolean,
        width: Int,
        height: Int
    ) {
        val margin = width * 0.08f
        
        // Card name
        paint.textSize = height * 0.08f
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        canvas.drawText(card.name, margin, margin + paint.textSize, paint)
        
        // Card type
        paint.textSize = height * 0.05f
        paint.typeface = android.graphics.Typeface.DEFAULT
        canvas.drawText(card.type.getDisplayName(), margin, margin + paint.textSize * 3, paint)
        
        // Card number (if available)
        val cardNumber = card.extractedData["cardNumber"] ?: card.customFields["cardNumber"]
        if (!cardNumber.isNullOrEmpty()) {
            paint.textSize = height * 0.07f
            paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
            val displayNumber = if (showAllDetails) {
                formatCardNumber(cardNumber)
            } else {
                "**** **** **** ${cardNumber.takeLast(4)}"
            }
            canvas.drawText(displayNumber, margin, height * 0.6f, paint)
        }
        
        // Expiry date
        val expiryDate = card.extractedData["expiryDate"] ?: card.customFields["expiryDate"]
        if (!expiryDate.isNullOrEmpty()) {
            paint.textSize = height * 0.05f
            canvas.drawText("VALID THRU", margin, height * 0.75f, paint)
            canvas.drawText(expiryDate, margin, height * 0.82f, paint)
        }
        
        // Cardholder name
        val holderName = card.extractedData["cardholderName"] ?: card.customFields["cardholderName"]
        if (!holderName.isNullOrEmpty()) {
            paint.textSize = height * 0.05f
            canvas.drawText(holderName.uppercase(), margin, height * 0.92f, paint)
        }
    }
    
    /**
     * Draw back side content
     */
    private fun drawCardBack(
        canvas: android.graphics.Canvas,
        card: Card,
        paint: android.graphics.Paint,
        showAllDetails: Boolean,
        width: Int,
        height: Int
    ) {
        val margin = width * 0.08f
        
        // Magnetic stripe simulation
        paint.color = android.graphics.Color.BLACK
        canvas.drawRect(0f, height * 0.15f, width.toFloat(), height * 0.25f, paint)
        
        // CVV area
        paint.color = android.graphics.Color.WHITE
        val cvvRect = android.graphics.RectF(
            width * 0.6f, height * 0.35f,
            width * 0.9f, height * 0.45f
        )
        canvas.drawRect(cvvRect, paint)
        
        // CVV text
        val cvv = card.extractedData["cvv"] ?: card.customFields["cvv"]
        if (!cvv.isNullOrEmpty() && showAllDetails) {
            paint.color = android.graphics.Color.BLACK
            paint.textSize = height * 0.04f
            canvas.drawText(cvv, cvvRect.left + 10, cvvRect.bottom - 10, paint)
        }
        
        // Additional info
        paint.color = android.graphics.Color.WHITE
        paint.textSize = height * 0.03f
        canvas.drawText("Customer Service: 1-800-XXX-XXXX", margin, height * 0.6f, paint)
        canvas.drawText("For assistance, visit our website", margin, height * 0.65f, paint)
        
        // Card name
        paint.textSize = height * 0.04f
        canvas.drawText(card.name, margin, height * 0.85f, paint)
    }

    /**
     * Formats card number with proper spacing (e.g., "1234 5678 9012 3456")
     */
    private fun formatCardNumber(cardNumber: String): String {
        return cardNumber.chunked(4).joinToString(" ")
    }
    
    /**
     * Gets the appropriate gradient colors for the card type
     */
    private fun getCardTypeGradient(card: Card): Pair<String, String> {
        return card.type.getDefaultGradient()
    }
}