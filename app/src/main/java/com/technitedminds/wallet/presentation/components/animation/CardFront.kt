package com.technitedminds.wallet.presentation.components.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.presentation.components.common.resolveCategoryName
import com.technitedminds.wallet.presentation.components.sharing.CardSharingOption
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.ui.theme.gradientContrastText
import java.io.File

/**
 * Front side of the card display with glassmorphic badges, holographic shimmer,
 * real-card labels, and refined typography.
 */
@Composable
fun CardFront(
    card: Card,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    showShareButton: Boolean = false,
    onShare: ((CardSharingOption) -> Unit)? = null
) {
    val context = LocalContext.current

    val gradient = card.getGradient()
    val cardTextColor = remember(gradient) {
        gradientContrastText(gradient.startColor, gradient.endColor)
    }

    // Holographic shimmer for OCR (textual) cards
    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by shimmerTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = getCardGradient(card),
                shape = RoundedCornerShape(if (isCompact) 8.dp else 12.dp)
            )
    ) {
        // Background image for non-OCR cards
        if (!card.type.supportsOCR() && card.frontImagePath.isNotBlank()) {
            val imageFile = File(card.frontImagePath)
            if (imageFile.exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageFile)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Card front image",
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
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                )
                            )
                        )
                )
            }
        }

        // Holographic shimmer overlay (OCR cards only, non-compact)
        if (card.type.supportsOCR() && !isCompact) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.05f),
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent,
                            ),
                            start = Offset(shimmerOffset * 600f, 0f),
                            end = Offset(shimmerOffset * 600f + 300f, 400f)
                        )
                    )
            )
        }

        // Card content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompact) 12.dp else 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section - Glassmorphic type and category badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = card.type.getDisplayName(),
                        style = MaterialTheme.typography.labelSmall,
                        color = cardTextColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (!isCompact && card.categoryId.isNotEmpty()) {
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = resolveCategoryName(card.categoryId),
                            style = MaterialTheme.typography.labelSmall,
                            color = cardTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Middle section - Card number with monospace typography
            if (!isCompact && card.type.supportsOCR() && card.extractedData.containsKey("cardNumber")) {
                val cardNumber = card.extractedData["cardNumber"]
                if (!cardNumber.isNullOrEmpty()) {
                    Text(
                        text = formatCardNumber(cardNumber),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 3.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = cardTextColor,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            // Bottom section - Card name with real-card labels
            Column {
                Text(
                    text = card.name,
                    style = if (isCompact) {
                        MaterialTheme.typography.titleSmall
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    color = cardTextColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = if (isCompact) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!isCompact) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Cardholder section with label
                        val holderName = card.extractedData["cardholderName"]
                        if (!holderName.isNullOrEmpty()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CARDHOLDER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.sp
                                    ),
                                    color = cardTextColor.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = holderName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = cardTextColor.copy(alpha = 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Expiry section with label
                        val expiryDate = card.extractedData["expiryDate"]
                        if (!expiryDate.isNullOrEmpty()) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "VALID THRU",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.sp
                                    ),
                                    color = cardTextColor.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = expiryDate,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = cardTextColor.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top right - share button only (removed redundant card type icon)
        if (showShareButton && onShare != null && !isCompact) {
            ShareButton(
                onShare = { onShare(CardSharingOption.FrontOnly) },
                contentDescription = AppConstants.ContentDescriptions.SHARE_FRONT,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(32.dp)
            )
        }
    }
}

/**
 * Get gradient colors based on card's custom gradient or type default.
 */
private fun getCardGradient(card: Card): Brush {
    val gradient = card.getGradient()

    val startColor = try {
        Color(gradient.startColor.toColorInt())
    } catch (e: Exception) {
        Color(Card.getDefaultGradientForType(card.type).startColor.toColorInt())
    }

    val endColor = try {
        Color(gradient.endColor.toColorInt())
    } catch (e: Exception) {
        Color(Card.getDefaultGradientForType(card.type).endColor.toColorInt())
    }

    return when (gradient.direction) {
        com.technitedminds.wallet.domain.model.GradientDirection.TopToBottom ->
            Brush.verticalGradient(colors = listOf(startColor, endColor))
        com.technitedminds.wallet.domain.model.GradientDirection.LeftToRight ->
            Brush.horizontalGradient(colors = listOf(startColor, endColor))
        com.technitedminds.wallet.domain.model.GradientDirection.DiagonalTopLeftToBottomRight ->
            Brush.linearGradient(colors = listOf(startColor, endColor))
        com.technitedminds.wallet.domain.model.GradientDirection.DiagonalTopRightToBottomLeft ->
            Brush.linearGradient(
                colors = listOf(startColor, endColor),
                start = Offset(Float.POSITIVE_INFINITY, 0f),
                end = Offset(0f, Float.POSITIVE_INFINITY)
            )
    }
}

private fun getCardTypeIcon(cardType: CardType) = when (cardType) {
    is CardType.Credit, is CardType.Debit -> Icons.Default.Payment
    is CardType.GiftCard -> Icons.Default.Redeem
    is CardType.LoyaltyCard -> Icons.Default.Star
    is CardType.MembershipCard -> Icons.Default.Person
    is CardType.InsuranceCard -> Icons.Default.Security
    is CardType.IdentificationCard -> Icons.Default.Person
    is CardType.Voucher -> Icons.Default.Redeem
    is CardType.Event -> Icons.Default.Event
    is CardType.TransportCard -> Icons.Default.Train
    is CardType.BusinessCard -> Icons.Default.Business
    is CardType.LibraryCard -> Icons.Default.Book
    is CardType.HotelCard -> Icons.Default.Hotel
    is CardType.StudentCard -> Icons.Default.School
    is CardType.AccessCard -> Icons.Default.VpnKey
    is CardType.Custom -> Icons.Default.Payment
}

private fun formatCardNumber(cardNumber: String): String {
    val cleaned = cardNumber.replace("\\s".toRegex(), "")
    return when {
        cleaned.length >= 16 -> {
            val first4 = cleaned.take(4)
            val last4 = cleaned.takeLast(4)
            "$first4 •••• •••• $last4"
        }
        cleaned.length >= 8 -> {
            val first4 = cleaned.take(4)
            val last4 = cleaned.takeLast(4)
            "$first4 •••• $last4"
        }
        else -> cleaned
    }
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
                indication = ripple(
                    bounded = false,
                    radius = 20.dp
                ),
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
