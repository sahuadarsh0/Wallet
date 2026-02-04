package com.technitedminds.wallet.presentation.components.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.presentation.components.sharing.CardSharingOption
import com.technitedminds.wallet.presentation.constants.AppConstants
import java.io.File

/**
 * Back side of the card display with security information and additional details.
 * Optimized for performance and visual appeal.
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
                brush = getCardBackGradient(),
                shape = RoundedCornerShape(if (isCompact) 8.dp else 12.dp)
            )
    ) {
        // Background image if available
        if (card.backImagePath.isNotBlank()) {
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
                
                // Overlay for text readability
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
        
        // Card back content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompact) 12.dp else 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section - Magnetic stripe simulation
            if (!isCompact) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            Color.Black,
                            RoundedCornerShape(4.dp)
                        )
                )
            }
            
            // Middle section - Security information
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // CVV section
                val cvv = card.extractedData["cvv"]
                if (!cvv.isNullOrEmpty() && !isCompact) {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = AppConstants.UIText.CVV_LABEL,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cvv,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Security icon and text
                if (!isCompact) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .size(32.dp)
                            .padding(vertical = 8.dp)
                    )
                    
                    Text(
                        text = AppConstants.UIText.CARD_PROTECTED_NOTICE,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            
            // Bottom section - Additional information
            Column {
                // Bank or issuer information
                val bankName = card.extractedData["bankName"]
                if (!bankName.isNullOrEmpty()) {
                    Text(
                        text = bankName,
                        style = if (isCompact) {
                            MaterialTheme.typography.bodyMedium
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Additional custom fields
                if (!isCompact) {
                    card.customFields.forEach { (key, value) ->
                        if (key !in listOf("cardNumber", "expiryDate", "cardholderName", "cvv", "bankName") 
                            && value.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatFieldName(key),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                // Card creation date
                if (!isCompact) {
                    Text(
                        text = String.format(AppConstants.UIText.CARD_ADDED_DATE_LABEL, formatDate(card.createdAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        
        // Security watermark
        if (!isCompact) {
            Text(
                text = AppConstants.UIText.SECURE_WATERMARK,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White.copy(alpha = 0.1f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }
        
        // Share button (top right) with animations and haptic feedback
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

/**
 * Get gradient for card back (more subtle than front)
 */
private fun getCardBackGradient(): Brush {
    return Brush.linearGradient(
        colors = listOf(
            Color(0xFF37474F), // Dark Blue Grey
            Color(0xFF263238)
        )
    )
}

/**
 * Format field names for display
 */
private fun formatFieldName(fieldName: String): String {
    return fieldName
        .replace(Regex("([a-z])([A-Z])"), "$1 $2") // Add space before capital letters
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}

/**
 * Format date for display
 */
private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return formatter.format(date)
}

/**
 * Share button component with animations, haptic feedback, and loading state
 */
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
    
    // Scale animation for press feedback
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
        color = Color.White.copy(alpha = 0.2f)
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