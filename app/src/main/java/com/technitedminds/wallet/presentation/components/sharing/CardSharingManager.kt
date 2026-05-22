package com.technitedminds.wallet.presentation.components.sharing

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.service.CardImageGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

/**
 * Coordinates card sharing with a dual strategy:
 *  - Textual cards (Credit/Debit): render fresh gradient art via [CardImageGenerator] in-memory
 *    and write a single JPEG to the share cache.
 *  - Image-only cards: re-encode captured photos at the requested quality / max dimensions.
 *
 * In both paths images are written to `cacheDir/shared_cards/` and exposed through FileProvider,
 * keeping `filesDir/images/` (the canonical card store) untouched.
 */
@Singleton
class CardSharingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cardImageGenerator: CardImageGenerator,
) {
    private val cleanupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun shareCard(
        card: Card,
        sharingOption: CardSharingOption,
        config: CardSharingConfig = CardSharingConfig(),
    ): CardSharingResult = withContext(Dispatchers.IO) {
        try {
            val files = if (card.type.supportsOCR()) {
                renderTextualCardFiles(card, sharingOption, config)
            } else {
                prepareCapturedImageFiles(card, sharingOption, config)
            }

            if (files.isEmpty()) return@withContext CardSharingResult.Error("No images available to share")

            shareFiles(files, card)
            CardSharingResult.Success
        } catch (e: Exception) {
            CardSharingResult.Error(e.message ?: "Failed to share card")
        }
    }

    // ── Textual cards ──────────────────────────────────────────────────────

    private suspend fun renderTextualCardFiles(
        card: Card,
        option: CardSharingOption,
        config: CardSharingConfig,
    ): List<File> = buildList {
        val w = config.maxImageWidth
        val h = config.maxImageHeight
        val show = config.includeSensitiveInfo

        when (option) {
            CardSharingOption.FrontOnly -> {
                renderToFile(card, isBack = false, w, h, show, config, "${card.id}_front")?.let { add(it) }
            }
            CardSharingOption.BackOnly -> {
                renderToFile(card, isBack = true, w, h, show, config, "${card.id}_back")?.let { add(it) }
            }
            CardSharingOption.BothSides -> {
                renderToFile(card, isBack = false, w, h, show, config, "${card.id}_front")?.let { add(it) }
                renderToFile(card, isBack = true, w, h, show, config, "${card.id}_back")?.let { add(it) }
            }
        }
    }

    private suspend fun renderToFile(
        card: Card,
        isBack: Boolean,
        width: Int,
        height: Int,
        showAllDetails: Boolean,
        config: CardSharingConfig,
        baseName: String,
    ): File? {
        val rendered = cardImageGenerator.renderCardBitmap(card, isBack, showAllDetails, width, height) ?: return null
        return try {
            val finalBitmap = if (config.addWatermark) addWatermark(rendered, config.watermarkText) else rendered
            val file = writeJpeg(finalBitmap, baseName, config.imageQuality)
            if (finalBitmap !== rendered) finalBitmap.recycle()
            file
        } finally {
            if (!rendered.isRecycled) rendered.recycle()
        }
    }

    // ── Image-only cards ───────────────────────────────────────────────────

    private fun prepareCapturedImageFiles(
        card: Card,
        option: CardSharingOption,
        config: CardSharingConfig,
    ): List<File> = buildList {
        val front = card.frontImagePath.takeIf { it.isNotBlank() }?.let(::File)?.takeIf { it.exists() }
        val back = card.backImagePath.takeIf { it.isNotBlank() }?.let(::File)?.takeIf { it.exists() }

        when (option) {
            CardSharingOption.FrontOnly -> front?.let { processCaptured(it, "${card.id}_front", config)?.let(::add) }
            CardSharingOption.BackOnly -> back?.let { processCaptured(it, "${card.id}_back", config)?.let(::add) }
            CardSharingOption.BothSides -> {
                front?.let { processCaptured(it, "${card.id}_front", config)?.let(::add) }
                back?.let { processCaptured(it, "${card.id}_back", config)?.let(::add) }
            }
        }
    }

    private fun processCaptured(source: File, baseName: String, config: CardSharingConfig): File? {
        val sampled = decodeSampled(source, config.maxImageWidth, config.maxImageHeight) ?: return null
        return try {
            val final = if (config.addWatermark) addWatermark(sampled, config.watermarkText) else sampled
            val file = writeJpeg(final, baseName, config.imageQuality)
            if (final !== sampled) final.recycle()
            file
        } finally {
            if (!sampled.isRecycled) sampled.recycle()
        }
    }

    /** Decode with `inSampleSize` then scale-fit to keep memory bounded. */
    private fun decodeSampled(file: File, maxWidth: Int, maxHeight: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val sample = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxWidth, maxHeight)
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = BitmapFactory.decodeFile(file.absolutePath, opts) ?: return null

        val scale = min(maxWidth.toFloat() / decoded.width, maxHeight.toFloat() / decoded.height).coerceAtMost(1f)
        if (scale >= 0.999f) return decoded

        val scaled = Bitmap.createScaledBitmap(
            decoded,
            (decoded.width * scale).toInt().coerceAtLeast(1),
            (decoded.height * scale).toInt().coerceAtLeast(1),
            true,
        )
        if (scaled !== decoded) decoded.recycle()
        return scaled
    }

    private fun calculateInSampleSize(srcW: Int, srcH: Int, reqW: Int, reqH: Int): Int {
        var sample = 1
        if (srcH > reqH || srcW > reqW) {
            val halfH = srcH / 2
            val halfW = srcW / 2
            while ((halfH / sample) >= reqH && (halfW / sample) >= reqW) sample *= 2
        }
        return sample
    }

    // ── Watermark ──────────────────────────────────────────────────────────

    /**
     * Adds a small, scaled watermark pill in the bottom-right corner.
     * Sized relative to the image so it never looks tiny on large renders or huge on thumbnails.
     */
    private fun addWatermark(source: Bitmap, text: String): Bitmap {
        if (text.isBlank()) return source
        val result = source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val textSize = max(result.height * 0.028f, 18f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.textSize = textSize
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            letterSpacing = 0.05f
        }

        val padX = textSize * 0.6f
        val padY = textSize * 0.35f
        val textWidth = paint.measureText(text)
        val margin = result.width * 0.02f
        val pillRight = result.width - margin
        val pillBottom = result.height - margin
        val pillLeft = pillRight - textWidth - padX * 2
        val pillTop = pillBottom - textSize - padY * 2

        val pillBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(110, 0, 0, 0) }
        val r = (pillBottom - pillTop) * 0.45f
        canvas.drawRoundRect(RectF(pillLeft, pillTop, pillRight, pillBottom), r, r, pillBg)

        val baseline = pillBottom - padY - paint.fontMetrics.descent
        canvas.drawText(text, pillRight - padX, baseline, paint)
        return result
    }

    // ── IO ─────────────────────────────────────────────────────────────────

    private fun writeJpeg(bitmap: Bitmap, baseName: String, quality: Float): File {
        val dir = File(context.cacheDir, "shared_cards").apply { if (!exists()) mkdirs() }
        val file = File(dir, "$baseName.jpg")
        FileOutputStream(file).use { out ->
            val q = (quality * 100).toInt().coerceIn(40, 100)
            bitmap.compress(Bitmap.CompressFormat.JPEG, q, out)
        }
        return file
    }

    private fun shareFiles(files: List<File>, card: Card) {
        try {
            val uris = files.map { file ->
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }
            val intent = Intent().apply {
                if (uris.size == 1) {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uris.first())
                } else {
                    action = Intent.ACTION_SEND_MULTIPLE
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                }
                type = "image/jpeg"
                putExtra(Intent.EXTRA_SUBJECT, card.name)
                putExtra(Intent.EXTRA_TEXT, "${card.name} • Shared from CardVault")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share ${card.name}").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)

            cleanupScope.launch {
                delay(60_000)
                cleanupTempFiles()
            }
        } catch (_: Exception) {
            shareTextOnly(card.name)
        }
    }

    private fun shareTextOnly(cardName: String) {
        try {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, cardName)
                putExtra(Intent.EXTRA_TEXT, "$cardName • Shared from CardVault")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share $cardName").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Sharing is best-effort.
        }
    }

    fun cleanupTempFiles() {
        try {
            val shareDir = File(context.cacheDir, "shared_cards")
            if (shareDir.exists()) {
                val cutoff = System.currentTimeMillis() - 3_600_000L
                shareDir.listFiles()?.forEach { f ->
                    if (f.isFile && f.lastModified() < cutoff) f.delete()
                }
            }
        } catch (_: Exception) {
            // Ignore cleanup errors.
        }
    }

    fun forceCleanupAllTempFiles() {
        try {
            val shareDir = File(context.cacheDir, "shared_cards")
            if (shareDir.exists()) shareDir.listFiles()?.forEach { it.delete() }
        } catch (_: Exception) {
            // Ignore cleanup errors.
        }
    }

    fun getSharingStats(): SharingStats {
        val shareDir = File(context.cacheDir, "shared_cards")
        val files = shareDir.listFiles()
        return SharingStats(
            totalSharedFiles = files?.size ?: 0,
            totalSizeBytes = files?.sumOf { it.length() } ?: 0L,
            lastCleanup = System.currentTimeMillis(),
        )
    }
}

/** Statistics about sharing operations. */
data class SharingStats(
    val totalSharedFiles: Int,
    val totalSizeBytes: Long,
    val lastCleanup: Long,
)
