package com.technitedminds.wallet.data.service

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardGradient
import com.technitedminds.wallet.domain.service.CardImageGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of CardImageGenerator that creates gradient card images.
 * Uses Android Graphics APIs for bitmap generation.
 */
@Singleton
class CardImageGeneratorImpl @Inject constructor() : CardImageGenerator {

    override suspend fun generateCardImage(
        card: Card,
        showAllDetails: Boolean,
        includeBack: Boolean
    ): String = withContext(Dispatchers.IO) {
        try {
            val width = CardImageGenerator.DEFAULT_CARD_WIDTH
            val height = CardImageGenerator.DEFAULT_CARD_HEIGHT
            
            if (includeBack) {
                // Generate combined front and back image
                val frontBitmap = generateCardBitmap(card, isBack = false, showAllDetails, width, height / 2)
                val backBitmap = generateCardBitmap(card, isBack = true, showAllDetails, width, height / 2)
                
                // Combine both images vertically
                val combinedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(combinedBitmap)
                
                canvas.drawBitmap(frontBitmap, 0f, 0f, null)
                canvas.drawBitmap(backBitmap, 0f, height / 2f, null)
                
                val fileName = "combined_card_${card.id}_${System.currentTimeMillis()}.png"
                val file = saveBitmapToTempFile(combinedBitmap, fileName)
                
                frontBitmap.recycle()
                backBitmap.recycle()
                combinedBitmap.recycle()
                
                file.absolutePath
            } else {
                val frontBitmap = generateCardBitmap(card, isBack = false, showAllDetails, width, height)
                val fileName = "card_${card.id}_${System.currentTimeMillis()}.png"
                val file = saveBitmapToTempFile(frontBitmap, fileName)
                frontBitmap.recycle()
                file.absolutePath
            }
        } catch (e: Exception) {
            "error_generating_card_${card.id}.png"
        }
    }

    override suspend fun generateCardFrontImagePath(
        card: Card,
        showAllDetails: Boolean,
        width: Int,
        height: Int
    ): String? = withContext(Dispatchers.IO) {
        try {
            val bitmap = generateCardBitmap(card, isBack = false, showAllDetails, width, height)
            val fileName = "front_${card.id}_${System.currentTimeMillis()}.png"
            val file = saveBitmapToTempFile(bitmap, fileName)
            bitmap.recycle()
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun generateCardBackImagePath(
        card: Card,
        showAllDetails: Boolean,
        width: Int,
        height: Int
    ): String? = withContext(Dispatchers.IO) {
        try {
            val bitmap = generateCardBitmap(card, isBack = true, showAllDetails, width, height)
            val fileName = "back_${card.id}_${System.currentTimeMillis()}.png"
            val file = saveBitmapToTempFile(bitmap, fileName)
            bitmap.recycle()
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun generateDefaultBackImage(
        cardName: String,
        cardTypeName: String,
        gradient: CardGradient,
        width: Int,
        height: Int
    ): String? = withContext(Dispatchers.IO) {
        try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Draw gradient background
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.parseColor(gradient.startColor),
                    Color.parseColor(gradient.endColor)
                )
            )
            gradientDrawable.setBounds(0, 0, width, height)
            gradientDrawable.cornerRadius = 40f
            gradientDrawable.draw(canvas)
            
            val paint = Paint().apply {
                isAntiAlias = true
                color = Color.WHITE
            }
            
            // Card type icon placeholder (centered)
            paint.textSize = height * 0.15f
            paint.textAlign = Paint.Align.CENTER
            paint.alpha = 128 // Semi-transparent
            canvas.drawText(cardTypeName.take(2).uppercase(), width / 2f, height * 0.45f, paint)
            
            // Card name
            paint.alpha = 255
            paint.textSize = height * 0.06f
            canvas.drawText(cardName, width / 2f, height * 0.65f, paint)
            
            // CardVault watermark
            paint.textSize = height * 0.04f
            paint.alpha = 180
            canvas.drawText("CardVault", width / 2f, height * 0.9f, paint)
            
            val fileName = "default_back_${System.currentTimeMillis()}.png"
            val file = saveBitmapToTempFile(bitmap, fileName)
            bitmap.recycle()
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun saveBitmapToTempFile(bitmap: Bitmap, fileName: String): File {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "card_sharing")
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
        
        val file = File(tempDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        }
        return file
    }

    private fun generateCardBitmap(
        card: Card,
        isBack: Boolean,
        showAllDetails: Boolean,
        width: Int,
        height: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Create gradient background
        val gradient = card.type.getDefaultGradient()
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                Color.parseColor(gradient.first),
                Color.parseColor(gradient.second)
            )
        )
        gradientDrawable.setBounds(0, 0, width, height)
        gradientDrawable.cornerRadius = 40f
        gradientDrawable.draw(canvas)
        
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
        }
        
        if (isBack) {
            drawCardBack(canvas, card, paint, showAllDetails, width, height)
        } else {
            drawCardFront(canvas, card, paint, showAllDetails, width, height)
        }
        
        return bitmap
    }

    private fun drawCardFront(
        canvas: Canvas,
        card: Card,
        paint: Paint,
        showAllDetails: Boolean,
        width: Int,
        height: Int
    ) {
        val margin = width * 0.08f
        
        // Card name
        paint.textSize = height * 0.08f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(card.name, margin, margin + paint.textSize, paint)
        
        // Card type
        paint.textSize = height * 0.05f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(card.type.getDisplayName(), margin, margin + paint.textSize * 3, paint)
        
        // Card number (if available)
        val cardNumber = card.extractedData["cardNumber"] ?: card.customFields["cardNumber"]
        if (!cardNumber.isNullOrEmpty()) {
            paint.textSize = height * 0.07f
            paint.typeface = Typeface.DEFAULT_BOLD
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
            paint.typeface = Typeface.DEFAULT
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

    private fun drawCardBack(
        canvas: Canvas,
        card: Card,
        paint: Paint,
        showAllDetails: Boolean,
        width: Int,
        height: Int
    ) {
        val margin = width * 0.08f
        
        // Magnetic stripe simulation
        paint.color = Color.BLACK
        canvas.drawRect(0f, height * 0.15f, width.toFloat(), height * 0.25f, paint)
        
        // CVV area
        paint.color = Color.WHITE
        val cvvRect = RectF(
            width * 0.6f, height * 0.35f,
            width * 0.9f, height * 0.45f
        )
        canvas.drawRect(cvvRect, paint)
        
        // CVV text
        val cvv = card.extractedData["cvv"] ?: card.customFields["cvv"]
        if (!cvv.isNullOrEmpty() && showAllDetails) {
            paint.color = Color.BLACK
            paint.textSize = height * 0.04f
            canvas.drawText(cvv, cvvRect.left + 10, cvvRect.bottom - 10, paint)
        }
        
        // Additional info
        paint.color = Color.WHITE
        paint.textSize = height * 0.03f
        canvas.drawText("Customer Service: 1-800-XXX-XXXX", margin, height * 0.6f, paint)
        canvas.drawText("For assistance, visit our website", margin, height * 0.65f, paint)
        
        // Card name
        paint.textSize = height * 0.04f
        canvas.drawText(card.name, margin, height * 0.85f, paint)
    }

    private fun formatCardNumber(cardNumber: String): String {
        return cardNumber.replace(" ", "").chunked(4).joinToString(" ")
    }
}
