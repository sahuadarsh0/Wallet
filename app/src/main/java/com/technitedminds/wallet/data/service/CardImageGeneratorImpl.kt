package com.technitedminds.wallet.data.service

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.technitedminds.wallet.data.local.files.ImageFileManager
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardGradient
import com.technitedminds.wallet.domain.model.ImageType
import com.technitedminds.wallet.domain.service.CardImageGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [CardImageGenerator] that produces premium, share-ready card images
 * matching the in-app `CardFront` / `CardBack` glassmorphic look.
 *
 * Visual upgrades vs. previous version:
 * - ISO/IEC 7810 ID-1 aspect ratio (≈1.586:1) safe area
 * - Soft inner light + diagonal shimmer overlay for depth
 * - Embossed EMV-style chip and contactless glyph on textual cards
 * - Contrast-aware foreground using [gradientContrastTextHex]
 * - Cleaned-up back side: subtle magnetic stripe, signature panel with CVV, soft footer
 * - No fake "Customer Service: 1-800-XXX-XXXX" placeholders
 */
@Singleton
class CardImageGeneratorImpl @Inject constructor(
    private val imageFileManager: ImageFileManager,
) : CardImageGenerator {

    override suspend fun generateCardImage(
        card: Card,
        showAllDetails: Boolean,
        includeBack: Boolean,
    ): String = withContext(Dispatchers.IO) {
        try {
            val width = CardImageGenerator.DEFAULT_CARD_WIDTH
            val height = CardImageGenerator.DEFAULT_CARD_HEIGHT

            if (includeBack) {
                val gap = (height * 0.04f).toInt()
                val sideHeight = (height - gap) / 2
                val front = renderCardBitmapInternal(card, isBack = false, showAllDetails, width, sideHeight)
                val back = renderCardBitmapInternal(card, isBack = true, showAllDetails, width, sideHeight)
                val combined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(combined)
                canvas.drawColor(Color.TRANSPARENT)
                canvas.drawBitmap(front, 0f, 0f, null)
                canvas.drawBitmap(back, 0f, (sideHeight + gap).toFloat(), null)
                front.recycle(); back.recycle()
                val path = saveBitmap(combined, "combined_card_${card.id}_${System.currentTimeMillis()}", ImageType.FRONT)
                combined.recycle()
                path
            } else {
                val bitmap = renderCardBitmapInternal(card, isBack = false, showAllDetails, width, height)
                val path = saveBitmap(bitmap, "card_${card.id}_${System.currentTimeMillis()}", ImageType.FRONT)
                bitmap.recycle()
                path
            }
        } catch (e: Exception) {
            "error_generating_card_${card.id}.jpg"
        }
    }

    override suspend fun generateCardFrontImagePath(
        card: Card,
        showAllDetails: Boolean,
        width: Int,
        height: Int,
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = renderCardBitmapInternal(card, isBack = false, showAllDetails, width, height)
            val path = saveBitmap(bitmap, "front_${card.id}_${System.currentTimeMillis()}", ImageType.FRONT)
            bitmap.recycle()
            path
        }.getOrNull()
    }

    override suspend fun generateCardBackImagePath(
        card: Card,
        showAllDetails: Boolean,
        width: Int,
        height: Int,
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = renderCardBitmapInternal(card, isBack = true, showAllDetails, width, height)
            val path = saveBitmap(bitmap, "back_${card.id}_${System.currentTimeMillis()}", ImageType.BACK)
            bitmap.recycle()
            path
        }.getOrNull()
    }

    override suspend fun generateDefaultBackImage(
        cardName: String,
        cardTypeName: String,
        gradient: CardGradient,
        width: Int,
        height: Int,
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawCardBackground(canvas, gradient, width, height)

            val textColor = gradientContrastTextHex(gradient.startColor, gradient.endColor)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            paint.textAlign = Paint.Align.CENTER
            paint.alpha = 60
            paint.textSize = height * 0.22f
            paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            canvas.drawText(cardTypeName.take(2).uppercase(), width / 2f, height * 0.5f, paint)

            paint.alpha = 230
            paint.textSize = height * 0.07f
            paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            canvas.drawText(cardName, width / 2f, height * 0.78f, paint)

            paint.alpha = 140
            paint.textSize = height * 0.038f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("CardVault", width / 2f, height * 0.92f, paint)

            val path = saveBitmap(bitmap, "default_back_${System.currentTimeMillis()}", ImageType.BACK)
            bitmap.recycle()
            path
        }.getOrNull()
    }

    override suspend fun renderCardBitmap(
        card: Card,
        isBack: Boolean,
        showAllDetails: Boolean,
        width: Int,
        height: Int,
    ): Bitmap? = withContext(Dispatchers.IO) {
        runCatching { renderCardBitmapInternal(card, isBack, showAllDetails, width, height) }.getOrNull()
    }

    private fun renderCardBitmapInternal(
        card: Card,
        isBack: Boolean,
        showAllDetails: Boolean,
        width: Int,
        height: Int,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawCardBackground(canvas, card.getGradient(), width, height)
        drawShimmerOverlay(canvas, width, height)
        if (isBack) {
            drawCardBack(canvas, card, showAllDetails, width, height)
        } else {
            drawCardFront(canvas, card, showAllDetails, width, height)
        }
        return bitmap
    }

    private suspend fun saveBitmap(bitmap: Bitmap, fileName: String, imageType: ImageType): String {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        return imageFileManager.saveImage(out.toByteArray(), fileName, imageType)
    }

    companion object {
        private const val JPEG_QUALITY = 95
    }
}
