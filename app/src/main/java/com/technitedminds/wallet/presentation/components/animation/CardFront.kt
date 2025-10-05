package com.technitedminds.wallet.presentation.components.animation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.technitedminds.wallet.domain.model.CardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.presentation.utils.resolveCategoryName
import com.technitedminds.wallet.presentation.components.sharing.CardSharingOption
import java.io.File
import androidx.core.graphics.toColorInt
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape

/**
 * Front side of the card display with proper image loading and card information.
 * Optimized for performance and visual appeal.
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
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = getCardGradient(card),
                shape = RoundedCornerShape(if (isCompact) 8.dp else 12.dp)
            )
    ) {
        // Background image if available
        if (card.frontImagePath.isNotBlank()) {
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
                
                // Overlay for text readability
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
        
        // Card content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompact) 12.dp else 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section - Card type and category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Card type indicator
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = card.type.getDisplayName(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                // Category indicator
                if (!isCompact && card.categoryId.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = resolveCategoryName(card.categoryId),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Middle section - Card number (if available and textual)
            if (!isCompact && card.type.supportsOCR() && card.extractedData.containsKey("cardNumber")) {
                val cardNumber = card.extractedData["cardNumber"]
                if (!cardNumber.isNullOrEmpty()) {
                    Text(
                        text = formatCardNumber(cardNumber),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            
            // Bottom section - Card name and holder
            Column {
                Text(
                    text = card.name,
                    style = if (isCompact) {
                        MaterialTheme.typography.titleSmall
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = if (isCompact) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!isCompact) {
                    val holderName = card.extractedData["cardholderName"]
                    if (!holderName.isNullOrEmpty()) {
                        Text(
                            text = holderName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    val expiryDate = card.extractedData["expiryDate"]
                    if (!expiryDate.isNullOrEmpty()) {
                        Text(
                            text = "Valid thru $expiryDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
        
        // Top right icons
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(if (isCompact) 8.dp else 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Share button
            if (showShareButton && onShare != null && !isCompact) {
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onShare(CardSharingOption.FrontOnly) },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share front",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(20.dp)
                    )
                }
            }
            
            // Card type icon
            Icon(
                imageVector = getCardTypeIcon(card.type),
                contentDescription = "${card.type.getDisplayName()} icon",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(if (isCompact) 20.dp else 24.dp)
            )
        }
    }
}

/**
 * Get gradient colors based on card type and custom color
 */
private fun getCardGradient(card: Card): Brush {
    val baseColor = try {
        Color(card.getDisplayColor().toColorInt())
    } catch (e: Exception) {
        // Fallback to type default if custom color is invalid
        Color(card.type.getDefaultColor().toColorInt())
    }
    
    // Create a darker shade for gradient
    val darkerColor = Color(
        red = (baseColor.red * 0.8f).coerceIn(0f, 1f),
        green = (baseColor.green * 0.8f).coerceIn(0f, 1f),
        blue = (baseColor.blue * 0.8f).coerceIn(0f, 1f),
        alpha = baseColor.alpha
    )
    
    return Brush.linearGradient(
        colors = listOf(baseColor, darkerColor)
    )
}

/**
 * Get icon for card type
 */
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

/**
 * Format card number for display (mask middle digits)
 */
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