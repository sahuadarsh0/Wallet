package com.technitedminds.wallet.presentation.components.camera

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.io.File
import com.technitedminds.wallet.presentation.constants.AppConstants

/**
 * Image preview component with zoom, pan, and action buttons.
 * Supports both front and back card images.
 */
@Composable
fun ImagePreview(
    frontImageFile: File?,
    backImageFile: File? = null,
    onRetakeFront: () -> Unit,
    onRetakeBack: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    showBothSides: Boolean = true,
    modifier: Modifier = Modifier
) {
    var currentSide by remember { mutableStateOf(CardSide.FRONT) }
    var showFullscreen by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header with side selector
        if (showBothSides && backImageFile != null) {
            CardSideSelector(
                currentSide = currentSide,
                onSideChange = { currentSide = it },
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Image display area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val imageFile = when (currentSide) {
                CardSide.FRONT -> frontImageFile
                CardSide.BACK -> backImageFile
            }
            
            if (imageFile != null && imageFile.exists()) {
                ZoomableImagePlaceholder(
                    imageFile = imageFile,
                    onImageClick = { showFullscreen = true },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                ImagePlaceholder(
                    side = currentSide,
                    onCaptureClick = {
                        when (currentSide) {
                            CardSide.FRONT -> onRetakeFront()
                            CardSide.BACK -> onRetakeBack()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Action buttons
        ImagePreviewActions(
            currentSide = currentSide,
            hasImage = when (currentSide) {
                CardSide.FRONT -> frontImageFile?.exists() == true
                CardSide.BACK -> backImageFile?.exists() == true
            },
            onRetake = {
                when (currentSide) {
                    CardSide.FRONT -> onRetakeFront()
                    CardSide.BACK -> onRetakeBack()
                }
            },
            onConfirm = onConfirm,
            onCancel = onCancel,
            canConfirm = frontImageFile?.exists() == true,
            modifier = Modifier.padding(16.dp)
        )
    }
    
    // Fullscreen image dialog
    if (showFullscreen) {
        val imageFile = when (currentSide) {
            CardSide.FRONT -> frontImageFile
            CardSide.BACK -> backImageFile
        }
        
        if (imageFile != null) {
            FullscreenImageDialog(
                imageFile = imageFile,
                onDismiss = { showFullscreen = false }
            )
        }
    }
}

/**
 * Card side selector toggle
 */
@Composable
private fun CardSideSelector(
    currentSide: CardSide,
    onSideChange: (CardSide) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        SegmentedButton(
            selected = currentSide == CardSide.FRONT,
            onClick = { onSideChange(CardSide.FRONT) },
            label = "Front",
            icon = Icons.Default.CreditCard
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        SegmentedButton(
            selected = currentSide == CardSide.BACK,
            onClick = { onSideChange(CardSide.BACK) },
            label = "Back",
            icon = Icons.Default.CreditCard
        )
    }
}

/**
 * Segmented button component
 */
@Composable
private fun SegmentedButton(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    Button(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}

/**
 * Placeholder for zoomable image (simplified without external image loading)
 */
@Composable
private fun ZoomableImagePlaceholder(
    imageFile: File,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onImageClick() }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 3f)
                    
                    val maxX = (size.width * (scale - 1)) / 2
                    val maxY = (size.height * (scale - 1)) / 2
                    
                    offset = Offset(
                        x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                        y = (offset.y + pan.y).coerceIn(-maxY, maxY)
                    )
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "Card image",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Image Preview",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = imageFile.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Placeholder for missing images
 */
@Composable
private fun ImagePlaceholder(
    side: CardSide,
    onCaptureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No ${side.displayName} Image",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tap to capture ${side.displayName.lowercase()} of card",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onCaptureClick
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Capture ${side.displayName}")
            }
        }
    }
}

/**
 * Action buttons for image preview
 */
@Composable
private fun ImagePreviewActions(
    currentSide: CardSide,
    hasImage: Boolean,
    onRetake: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    canConfirm: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Cancel button
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(AppConstants.DialogText.CANCEL_BUTTON)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Retake button
        if (hasImage) {
            OutlinedButton(
                onClick = onRetake,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retake")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
        }
        
        // Confirm button
        Button(
            onClick = onConfirm,
            enabled = canConfirm,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(AppConstants.DialogText.CONFIRM_BUTTON)
        }
    }
}

/**
 * Fullscreen image dialog
 */
@Composable
private fun FullscreenImageDialog(
    imageFile: File,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            ZoomableImagePlaceholder(
                imageFile = imageFile,
                onImageClick = onDismiss,
                modifier = Modifier.fillMaxSize()
            )
            
            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = AppConstants.DialogText.CLOSE_BUTTON,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * Enum for card sides
 */
enum class CardSide(val displayName: String) {
    FRONT("Front"),
    BACK("Back")
}