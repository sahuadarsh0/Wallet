package com.technitedminds.wallet.presentation.components.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.core.graphics.toColorInt
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.GradientDirection
import com.technitedminds.wallet.presentation.components.sharing.CardSharingOption
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.ui.theme.GlassSurface
import java.io.File

/**
 * Back side of the card display with textured magnetic stripe, realistic signature
 * strip + CVV panel, and compact glassmorphic security badge.
 */
@Composable
fun CardBack(
    card: Card,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    showShareButton: Boolean = false,
    onShare: ((CardSharingOption) -> Unit)? = null
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = getCardBackGradient(card),
                shape = RoundedCornerShape(if (isCompact) 8.dp else 12.dp)
            )
    ) {
        // Background image for non-OCR cards
        if (!card.type.supportsOCR() && card.backImagePath.isNotBlank()) {
            val imageFile = File(card.backImagePath)
            if (imageFile.exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageFile)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Card back image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(if (isCompact) 8.dp else 12.dp)),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Black.copy(alpha = 0.5f)
                                )
                            )
                        )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompact) 12.dp else 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Textured magnetic stripe
            if (!isCompact) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1A1A1A),
                                    Color(0xFF2D2D2D),
                                    Color(0xFF1A1A1A),
                                    Color(0xFF333333),
                                    Color(0xFF1A1A1A),
                                )
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }

            // Middle section - Signature strip + CVV
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val cvv = card.extractedData["cvv"]
                if (!cvv.isNullOrEmpty() && !isCompact) {
                    // Realistic signature strip with CVV right-aligned
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFE8E8E8),
                                        Color(0xFFF5F5F5),
                                        Color(0xFFE0E0E0),
                                    )
                                ),
                                shape = RoundedCornerShape(4.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = AppConstants.UIText.CVV_LABEL,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.DarkGray
                            )
                            Text(
                                text = cvv,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp
                                ),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Compact glassmorphic security badge
                if (!isCompact) {
                    GlassSurface(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Protected by CardVault",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Bottom section - Bank name and date
            Column {
                val bankName = card.extractedData["bankName"]
                if (!bankName.isNullOrEmpty()) {
                    Text(
                        text = bankName,
                        style = if (isCompact) {
                            MaterialTheme.typography.bodyMedium
                        } else {
                            MaterialTheme.typography.titleSmall
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (!isCompact) {
                    Text(
                        text = String.format(
                            AppConstants.UIText.CARD_ADDED_DATE_LABEL,
                            formatDate(card.createdAt)
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Share button (top right)
        if (showShareButton && onShare != null && !isCompact) {
            ShareButton(
                onShare = { onShare(CardSharingOption.BackOnly) },
                contentDescription = AppConstants.ContentDescriptions.SHARE_BACK,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(32.dp)
            )
        }
    }
}

private fun getCardBackGradient(card: Card): Brush {
    val gradient = card.getGradient()

    val startColor = try {
        val baseColor = Color(gradient.startColor.toColorInt())
        Color(
            red = (baseColor.red * 0.6f).coerceIn(0f, 1f),
            green = (baseColor.green * 0.6f).coerceIn(0f, 1f),
            blue = (baseColor.blue * 0.6f).coerceIn(0f, 1f),
            alpha = baseColor.alpha
        )
    } catch (e: Exception) {
        Color(0xFF37474F)
    }

    val endColor = try {
        val baseColor = Color(gradient.endColor.toColorInt())
        Color(
            red = (baseColor.red * 0.5f).coerceIn(0f, 1f),
            green = (baseColor.green * 0.5f).coerceIn(0f, 1f),
            blue = (baseColor.blue * 0.5f).coerceIn(0f, 1f),
            alpha = baseColor.alpha
        )
    } catch (e: Exception) {
        Color(0xFF263238)
    }

    return when (gradient.direction) {
        GradientDirection.TopToBottom ->
            Brush.verticalGradient(colors = listOf(startColor, endColor))
        GradientDirection.LeftToRight ->
            Brush.horizontalGradient(colors = listOf(startColor, endColor))
        GradientDirection.DiagonalTopLeftToBottomRight ->
            Brush.linearGradient(colors = listOf(startColor, endColor))
        GradientDirection.DiagonalTopRightToBottomLeft ->
            Brush.linearGradient(
                colors = listOf(startColor, endColor),
                start = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, 0f),
                end = androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY)
            )
    }
}

private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return formatter.format(date)
}

@Composable
private fun ShareButton(
    onShare: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) AppConstants.AnimationValues.SCALE_PRESSED else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "share_button_scale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 20.dp),
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onShare()
                }
            ),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.2f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(20.dp)
                )
            }
        }
    }
}
