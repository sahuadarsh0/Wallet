package com.technitedminds.wallet.presentation.components.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Aspect ratios for different card types and orientations
 */
enum class CardAspectRatio(val ratio: Float, val displayName: String) {
    RATIO_16_9(16f / 9f, "16:9"),
    RATIO_4_3(4f / 3f, "4:3"),
    RATIO_3_4(3f / 4f, "3:4"),
    CREDIT_CARD(1.586f, "Credit Card") // Standard credit card ratio
}

/**
 * Overlay component that provides visual guides for card positioning during camera capture.
 * Shows a card-shaped outline with corner guides and instructions for different aspect ratios.
 */
@Composable
fun CardOverlay(
    modifier: Modifier = Modifier,
    aspectRatio: CardAspectRatio = CardAspectRatio.CREDIT_CARD,
    isCapturing: Boolean = false,
    showCrosshair: Boolean = true,
    overlayAlpha: Float = 0.4f
) {
    val density = LocalDensity.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val captureColor = MaterialTheme.colorScheme.secondary
    
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // Calculate card dimensions based on aspect ratio
        val (cardWidth, cardHeight) = calculateCardDimensions(
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            aspectRatio = aspectRatio.ratio
        )
        
        // Center the card overlay
        val cardLeft = (canvasWidth - cardWidth) / 2
        val cardTop = (canvasHeight - cardHeight) / 2
        
        // Draw semi-transparent overlay areas (top, bottom, left, right of card)
        val cornerRadius = with(density) { 12.dp.toPx() }
        
        // Top area
        drawRect(
            color = Color.Black.copy(alpha = overlayAlpha),
            topLeft = Offset(0f, 0f),
            size = Size(canvasWidth, cardTop)
        )
        
        // Bottom area
        drawRect(
            color = Color.Black.copy(alpha = overlayAlpha),
            topLeft = Offset(0f, cardTop + cardHeight),
            size = Size(canvasWidth, canvasHeight - (cardTop + cardHeight))
        )
        
        // Left area
        drawRect(
            color = Color.Black.copy(alpha = overlayAlpha),
            topLeft = Offset(0f, cardTop),
            size = Size(cardLeft, cardHeight)
        )
        
        // Right area
        drawRect(
            color = Color.Black.copy(alpha = overlayAlpha),
            topLeft = Offset(cardLeft + cardWidth, cardTop),
            size = Size(canvasWidth - (cardLeft + cardWidth), cardHeight)
        )
        
        // Draw card border
        val borderColor = if (isCapturing) captureColor else primaryColor
        val borderWidth = with(density) { if (isCapturing) 4.dp.toPx() else 2.dp.toPx() }
        
        drawRoundRect(
            color = borderColor,
            topLeft = Offset(cardLeft, cardTop),
            size = Size(cardWidth, cardHeight),
            cornerRadius = CornerRadius(cornerRadius),
            style = Stroke(width = borderWidth)
        )
        
        // Draw corner guides
        drawCornerGuides(
            cardLeft = cardLeft,
            cardTop = cardTop,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            color = borderColor,
            strokeWidth = borderWidth,
            cornerRadius = cornerRadius
        )
        
        // Draw center crosshair for alignment (optional)
        if (showCrosshair) {
            drawCrosshair(
                centerX = cardLeft + cardWidth / 2,
                centerY = cardTop + cardHeight / 2,
                color = borderColor.copy(alpha = 0.6f),
                strokeWidth = with(density) { 1.dp.toPx() }
            )
        }
        
        // Draw aspect ratio indicator
        drawAspectRatioIndicator(
            aspectRatio = aspectRatio,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            color = borderColor.copy(alpha = 0.8f),
            density = density
        )
    }
}

/**
 * Calculates optimal card dimensions for the given canvas size and aspect ratio
 */
private fun calculateCardDimensions(
    canvasWidth: Float,
    canvasHeight: Float,
    aspectRatio: Float
): Pair<Float, Float> {
    val maxWidth = canvasWidth * 0.8f
    val maxHeight = canvasHeight * 0.6f
    
    // Calculate dimensions based on aspect ratio
    val cardWidth: Float
    val cardHeight: Float
    
    if (aspectRatio > 1) {
        // Landscape orientation
        cardWidth = minOf(maxWidth, maxHeight * aspectRatio)
        cardHeight = cardWidth / aspectRatio
    } else {
        // Portrait orientation
        cardHeight = minOf(maxHeight, maxWidth / aspectRatio)
        cardWidth = cardHeight * aspectRatio
    }
    
    return Pair(cardWidth, cardHeight)
}

/**
 * Draws corner guides at the four corners of the card overlay
 */
private fun DrawScope.drawCornerGuides(
    cardLeft: Float,
    cardTop: Float,
    cardWidth: Float,
    cardHeight: Float,
    color: Color,
    strokeWidth: Float,
    cornerRadius: Float
) {
    val guideLength = 30f
    val guideOffset = 8f
    
    // Top-left corner
    drawLine(
        color = color,
        start = Offset(cardLeft + cornerRadius, cardTop - guideOffset),
        end = Offset(cardLeft + cornerRadius + guideLength, cardTop - guideOffset),
        strokeWidth = strokeWidth * 1.5f
    )
    drawLine(
        color = color,
        start = Offset(cardLeft - guideOffset, cardTop + cornerRadius),
        end = Offset(cardLeft - guideOffset, cardTop + cornerRadius + guideLength),
        strokeWidth = strokeWidth * 1.5f
    )
    
    // Top-right corner
    drawLine(
        color = color,
        start = Offset(cardLeft + cardWidth - cornerRadius - guideLength, cardTop - guideOffset),
        end = Offset(cardLeft + cardWidth - cornerRadius, cardTop - guideOffset),
        strokeWidth = strokeWidth * 1.5f
    )
    drawLine(
        color = color,
        start = Offset(cardLeft + cardWidth + guideOffset, cardTop + cornerRadius),
        end = Offset(cardLeft + cardWidth + guideOffset, cardTop + cornerRadius + guideLength),
        strokeWidth = strokeWidth * 1.5f
    )
    
    // Bottom-left corner
    drawLine(
        color = color,
        start = Offset(cardLeft + cornerRadius, cardTop + cardHeight + guideOffset),
        end = Offset(cardLeft + cornerRadius + guideLength, cardTop + cardHeight + guideOffset),
        strokeWidth = strokeWidth * 1.5f
    )
    drawLine(
        color = color,
        start = Offset(cardLeft - guideOffset, cardTop + cardHeight - cornerRadius - guideLength),
        end = Offset(cardLeft - guideOffset, cardTop + cardHeight - cornerRadius),
        strokeWidth = strokeWidth * 1.5f
    )
    
    // Bottom-right corner
    drawLine(
        color = color,
        start = Offset(cardLeft + cardWidth - cornerRadius - guideLength, cardTop + cardHeight + guideOffset),
        end = Offset(cardLeft + cardWidth - cornerRadius, cardTop + cardHeight + guideOffset),
        strokeWidth = strokeWidth * 1.5f
    )
    drawLine(
        color = color,
        start = Offset(cardLeft + cardWidth + guideOffset, cardTop + cardHeight - cornerRadius - guideLength),
        end = Offset(cardLeft + cardWidth + guideOffset, cardTop + cardHeight - cornerRadius),
        strokeWidth = strokeWidth * 1.5f
    )
}

/**
 * Draws a crosshair at the center of the card for alignment
 */
private fun DrawScope.drawCrosshair(
    centerX: Float,
    centerY: Float,
    color: Color,
    strokeWidth: Float
) {
    val crosshairLength = 20f
    
    // Horizontal line
    drawLine(
        color = color,
        start = Offset(centerX - crosshairLength, centerY),
        end = Offset(centerX + crosshairLength, centerY),
        strokeWidth = strokeWidth
    )
    
    // Vertical line
    drawLine(
        color = color,
        start = Offset(centerX, centerY - crosshairLength),
        end = Offset(centerX, centerY + crosshairLength),
        strokeWidth = strokeWidth
    )
}

/**
 * Draws aspect ratio indicator text
 */
private fun DrawScope.drawAspectRatioIndicator(
    aspectRatio: CardAspectRatio,
    canvasWidth: Float,
    canvasHeight: Float,
    color: Color,
    density: androidx.compose.ui.unit.Density
) {
    // This would typically use drawText, but for simplicity we'll skip text rendering
    // In a real implementation, you'd use TextPaint and Canvas.nativeCanvas.drawText
    
    // Draw a small indicator circle instead
    val indicatorRadius = with(density) { 4.dp.toPx() }
    val indicatorX = canvasWidth - with(density) { 32.dp.toPx() }
    val indicatorY = with(density) { 32.dp.toPx() }
    
    drawCircle(
        color = color,
        radius = indicatorRadius,
        center = Offset(indicatorX, indicatorY)
    )
}