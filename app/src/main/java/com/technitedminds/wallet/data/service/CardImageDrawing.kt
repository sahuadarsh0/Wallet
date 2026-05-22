package com.technitedminds.wallet.data.service

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardGradient

/**
 * Drawing primitives for [CardImageGeneratorImpl].
 *
 * Kept separate from the main service class so the large set of low-level
 * Canvas helpers don't clutter the lifecycle/coroutine surface area.
 */

private const val CORNER_RADIUS_RATIO = 0.06f

internal fun drawCardBackground(canvas: Canvas, gradient: CardGradient, width: Int, height: Int) {
    val start = parseColor(gradient.startColor, fallback = Color.parseColor("#667eea"))
    val end = parseColor(gradient.endColor, fallback = Color.parseColor("#764ba2"))
    val radius = height * CORNER_RADIUS_RATIO
    val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())

    val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = gradient.direction.toCanvasShader(width, height, start, end)
    }
    canvas.drawRoundRect(rect, radius, radius, bg)

    // Soft highlight along top edge for depth
    val highlight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = android.graphics.LinearGradient(
            0f, 0f, 0f, height * 0.5f,
            Color.argb(70, 255, 255, 255),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP,
        )
    }
    canvas.drawRoundRect(rect, radius, radius, highlight)

    // Bottom shadow for grounding
    val shade = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = android.graphics.LinearGradient(
            0f, height * 0.55f, 0f, height.toFloat(),
            Color.TRANSPARENT,
            Color.argb(60, 0, 0, 0),
            Shader.TileMode.CLAMP,
        )
    }
    canvas.drawRoundRect(rect, radius, radius, shade)
}

internal fun drawShimmerOverlay(canvas: Canvas, width: Int, height: Int) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = android.graphics.LinearGradient(
            width * 0.2f, 0f, width * 0.7f, height.toFloat(),
            intArrayOf(
                Color.TRANSPARENT,
                Color.argb(28, 255, 255, 255),
                Color.argb(48, 255, 255, 255),
                Color.argb(28, 255, 255, 255),
                Color.TRANSPARENT,
            ),
            floatArrayOf(0f, 0.35f, 0.5f, 0.65f, 1f),
            Shader.TileMode.CLAMP,
        )
    }
    val radius = height * CORNER_RADIUS_RATIO
    canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), radius, radius, paint)
}

/** EMV-style chip with three contact lines, inspired by real cards. */
internal fun drawChip(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = android.graphics.LinearGradient(
            x, y, x + w, y + h,
            Color.parseColor("#E8C674"),
            Color.parseColor("#A07B2C"),
            Shader.TileMode.CLAMP,
        )
    }
    val r = h * 0.18f
    canvas.drawRoundRect(RectF(x, y, x + w, y + h), r, r, body)

    val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(110, 0, 0, 0)
        strokeWidth = h * 0.04f
    }
    val pad = w * 0.18f
    val inner = RectF(x + pad, y + h * 0.18f, x + w - pad, y + h * 0.82f)
    canvas.drawLine(inner.left, inner.top, inner.right, inner.top, line)
    canvas.drawLine(inner.left, inner.centerY(), inner.right, inner.centerY(), line)
    canvas.drawLine(inner.left, inner.bottom, inner.right, inner.bottom, line)
    canvas.drawLine(inner.centerX(), inner.top, inner.centerX(), inner.bottom, line)
}

/** Contactless wave glyph (3 arcs). */
internal fun drawContactless(canvas: Canvas, cx: Float, cy: Float, size: Float, color: Int) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.STROKE
        strokeWidth = size * 0.08f
        strokeCap = Paint.Cap.ROUND
    }
    for (i in 0..2) {
        val r = size * (0.25f + i * 0.18f)
        val rect = RectF(cx - r, cy - r, cx + r, cy + r)
        canvas.drawArc(rect, -35f, 70f, false, paint)
    }
}

internal fun drawCardFront(
    canvas: Canvas,
    card: Card,
    showAllDetails: Boolean,
    width: Int,
    height: Int,
) {
    val padding = width * 0.06f
    val textHex = gradientContrastTextHex(card.getGradient().startColor, card.getGradient().endColor)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = textHex }
    val mutedAlpha = 170

    // ── Top: type badge + (optional) contactless ───────────────────────
    val typeText = card.type.getDisplayName().uppercase()
    paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
    paint.textSize = height * 0.045f
    paint.alpha = 255
    val badgeMetrics = paint.fontMetrics
    val badgePadX = width * 0.025f
    val badgePadY = height * 0.018f
    val badgeWidth = paint.measureText(typeText) + badgePadX * 2
    val badgeHeight = (badgeMetrics.descent - badgeMetrics.ascent) + badgePadY * 2
    val badgeRect = RectF(padding, padding, padding + badgeWidth, padding + badgeHeight)

    val badgeBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(38, 255, 255, 255)
    }
    val br = badgeHeight * 0.32f
    canvas.drawRoundRect(badgeRect, br, br, badgeBg)
    val badgeStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(80, 255, 255, 255)
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
    }
    canvas.drawRoundRect(badgeRect, br, br, badgeStroke)

    paint.textAlign = Paint.Align.LEFT
    canvas.drawText(typeText, badgeRect.left + badgePadX, badgeRect.bottom - badgePadY, paint)

    val isOcr = card.type.supportsOCR()
    if (isOcr) {
        val cl = height * 0.13f
        drawContactless(canvas, width - padding - cl * 0.5f, padding + cl * 0.5f, cl, applyAlpha(textHex, 220))
    }

    // ── Middle: chip + card number (OCR) or large name ─────────────────
    if (isOcr) {
        val chipW = width * 0.13f
        val chipH = chipW * 0.72f
        drawChip(canvas, padding, height * 0.34f, chipW, chipH)

        val cardNumber = card.extractedData["cardNumber"] ?: card.customFields["cardNumber"]
        if (!cardNumber.isNullOrBlank()) {
            val display = if (showAllDetails) formatCardNumber(cardNumber) else maskCardNumber(cardNumber)
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            paint.textSize = height * 0.085f
            paint.letterSpacing = 0.08f
            paint.alpha = 255
            paint.textAlign = Paint.Align.LEFT
            // Subtle text shadow for readability
            paint.setShadowLayer(4f, 0f, 2f, Color.argb(60, 0, 0, 0))
            canvas.drawText(display, padding, height * 0.62f, paint)
            paint.clearShadowLayer()
            paint.letterSpacing = 0f
        }
    }

    // ── Bottom: cardholder + valid thru OR card name for image-only ────
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

    if (isOcr) {
        val labelSize = height * 0.035f
        val valueSize = height * 0.055f

        val holder = card.extractedData["cardholderName"] ?: card.customFields["cardholderName"]
        val expiry = card.extractedData["expiryDate"] ?: card.customFields["expiryDate"]
        val baseY = height * 0.92f

        if (!holder.isNullOrBlank()) {
            paint.textSize = labelSize
            paint.alpha = mutedAlpha
            paint.textAlign = Paint.Align.LEFT
            paint.letterSpacing = 0.15f
            canvas.drawText("CARDHOLDER", padding, baseY - valueSize - height * 0.012f, paint)

            paint.letterSpacing = 0.05f
            paint.alpha = 255
            paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            paint.textSize = valueSize
            canvas.drawText(holder.uppercase(), padding, baseY, paint)
        }

        if (!expiry.isNullOrBlank()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = labelSize
            paint.alpha = mutedAlpha
            paint.textAlign = Paint.Align.RIGHT
            paint.letterSpacing = 0.15f
            canvas.drawText("VALID THRU", width - padding, baseY - valueSize - height * 0.012f, paint)

            paint.letterSpacing = 0.05f
            paint.alpha = 255
            paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            paint.textSize = valueSize
            canvas.drawText(expiry, width - padding, baseY, paint)
        }
        paint.letterSpacing = 0f
    } else {
        // Image-only types: prominent card name centered low
        paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        paint.textSize = height * 0.085f
        paint.alpha = 255
        paint.textAlign = Paint.Align.LEFT
        paint.setShadowLayer(4f, 0f, 2f, Color.argb(60, 0, 0, 0))
        canvas.drawText(card.name, padding, height * 0.92f, paint)
        paint.clearShadowLayer()
    }
}

internal fun drawCardBack(
    canvas: Canvas,
    card: Card,
    showAllDetails: Boolean,
    width: Int,
    height: Int,
) {
    val padding = width * 0.06f
    val textHex = gradientContrastTextHex(card.getGradient().startColor, card.getGradient().endColor)

    // Magnetic stripe (subtle, full bleed minus rounded corners)
    val stripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = android.graphics.LinearGradient(
            0f, 0f, 0f, height * 0.18f,
            Color.argb(220, 18, 18, 22),
            Color.argb(200, 6, 6, 10),
            Shader.TileMode.CLAMP,
        )
    }
    canvas.drawRect(0f, height * 0.13f, width.toFloat(), height * 0.27f, stripePaint)

    // Signature panel
    val sigTop = height * 0.40f
    val sigBottom = height * 0.55f
    val sigRect = RectF(padding, sigTop, width - padding, sigBottom)
    val sigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(235, 245, 245, 248) }
    canvas.drawRoundRect(sigRect, 8f, 8f, sigPaint)

    // Diagonal hatch lines on signature panel
    val hatch = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(40, 0, 0, 0)
        strokeWidth = 2f
    }
    val step = (sigRect.height() * 0.4f)
    var sx = sigRect.left
    while (sx < sigRect.right) {
        canvas.drawLine(sx, sigRect.bottom, sx + sigRect.height(), sigRect.top, hatch)
        sx += step
    }

    // CVV box at right edge of signature panel
    val cvvWidth = width * 0.16f
    val cvvRect = RectF(sigRect.right - cvvWidth - 8f, sigRect.top + 8f, sigRect.right - 8f, sigRect.bottom - 8f)
    val cvvBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    canvas.drawRoundRect(cvvRect, 4f, 4f, cvvBg)

    val cvv = card.extractedData["cvv"] ?: card.customFields["cvv"]
    val cvvText = when {
        cvv.isNullOrBlank() -> "CVV"
        showAllDetails -> cvv
        else -> "•".repeat(cvv.length.coerceAtLeast(3))
    }
    val cvvPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#222222")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textSize = cvvRect.height() * 0.55f
    }
    val cvvFm = cvvPaint.fontMetrics
    val cvvY = cvvRect.centerY() - (cvvFm.ascent + cvvFm.descent) / 2f
    canvas.drawText(cvvText, cvvRect.centerX(), cvvY, cvvPaint)

    // "CVV" label above box
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = applyAlpha(textHex, 200)
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textSize = height * 0.032f
        letterSpacing = 0.15f
    }
    canvas.drawText("SECURITY CODE", cvvRect.right, sigRect.top - height * 0.012f, labelPaint)

    // Card name footer
    val footer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textHex
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textSize = height * 0.05f
        alpha = 240
    }
    canvas.drawText(card.name, padding, height * 0.78f, footer)

    // Subtle divider + tagline
    val divider = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = applyAlpha(textHex, 60)
        strokeWidth = 1f
    }
    canvas.drawLine(padding, height * 0.82f, width - padding, height * 0.82f, divider)

    val tagline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = applyAlpha(textHex, 160)
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textSize = height * 0.034f
        letterSpacing = 0.1f
    }
    canvas.drawText("CardVault • offline • encrypted", padding, height * 0.90f, tagline)
}

// ── Helpers ──────────────────────────────────────────────────────────────

internal fun parseColor(hex: String, fallback: Int): Int = try {
    Color.parseColor(hex)
} catch (_: Exception) {
    fallback
}

internal fun com.technitedminds.wallet.domain.model.GradientDirection.toCanvasShader(
    width: Int,
    height: Int,
    start: Int,
    end: Int,
): Shader = when (this) {
    com.technitedminds.wallet.domain.model.GradientDirection.TopToBottom ->
        android.graphics.LinearGradient(0f, 0f, 0f, height.toFloat(), start, end, Shader.TileMode.CLAMP)
    com.technitedminds.wallet.domain.model.GradientDirection.LeftToRight ->
        android.graphics.LinearGradient(0f, 0f, width.toFloat(), 0f, start, end, Shader.TileMode.CLAMP)
    com.technitedminds.wallet.domain.model.GradientDirection.DiagonalTopLeftToBottomRight ->
        android.graphics.LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), start, end, Shader.TileMode.CLAMP)
    com.technitedminds.wallet.domain.model.GradientDirection.DiagonalTopRightToBottomLeft ->
        android.graphics.LinearGradient(width.toFloat(), 0f, 0f, height.toFloat(), start, end, Shader.TileMode.CLAMP)
}

/**
 * Picks a black/white text hex by luminance of the averaged gradient endpoints.
 * Mirrors `ui.theme.gradientContrastText` but works for raw `android.graphics.Color`.
 */
internal fun gradientContrastTextHex(startHex: String, endHex: String, threshold: Float = 0.4f): Int {
    return try {
        val s = Color.parseColor(startHex)
        val e = Color.parseColor(endHex)
        val r = (Color.red(s) + Color.red(e)) / 2
        val g = (Color.green(s) + Color.green(e)) / 2
        val b = (Color.blue(s) + Color.blue(e)) / 2
        val lum = (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0
        if (lum > threshold) Color.parseColor("#1A1A1F") else Color.WHITE
    } catch (_: Exception) {
        Color.WHITE
    }
}

internal fun applyAlpha(color: Int, alpha: Int): Int =
    Color.argb(alpha.coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))

internal fun formatCardNumber(raw: String): String {
    val cleaned = raw.replace("\\s".toRegex(), "")
    return cleaned.chunked(4).joinToString("  ")
}

internal fun maskCardNumber(raw: String): String {
    val cleaned = raw.replace("\\s".toRegex(), "")
    return when {
        cleaned.length >= 16 -> "${cleaned.take(4)}  ••••  ••••  ${cleaned.takeLast(4)}"
        cleaned.length >= 8 -> "${cleaned.take(4)}  ••••  ${cleaned.takeLast(4)}"
        else -> cleaned
    }
}
