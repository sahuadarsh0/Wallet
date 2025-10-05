package com.technitedminds.wallet.presentation.components.camera

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

/**
 * States for the capture button
 */
enum class CaptureButtonState {
    IDLE,
    CAPTURING,
    PROCESSING,
    SUCCESS,
    ERROR
}

/**
 * Large circular capture button with animation feedback and multiple states.
 * Provides haptic feedback and visual state changes.
 */
@Composable
fun CaptureButton(
    onClick: () -> Unit,
    state: CaptureButtonState = CaptureButtonState.IDLE,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    
    // Animation for button scale on press
    val scale by animateFloatAsState(
        targetValue = when (state) {
            CaptureButtonState.CAPTURING -> 0.9f
            CaptureButtonState.PROCESSING -> 0.95f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 100),
        label = "capture_scale"
    )
    
    // Infinite pulse animation for capturing state
    val infiniteTransition = rememberInfiniteTransition(label = "capture_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    // Color animation based on state
    val buttonColor by animateColorAsState(
        targetValue = when (state) {
            CaptureButtonState.IDLE -> MaterialTheme.colorScheme.primary
            CaptureButtonState.CAPTURING -> MaterialTheme.colorScheme.secondary
            CaptureButtonState.PROCESSING -> MaterialTheme.colorScheme.tertiary
            CaptureButtonState.SUCCESS -> Color(0xFF4CAF50) // Green
            CaptureButtonState.ERROR -> MaterialTheme.colorScheme.error
        },
        animationSpec = tween(durationMillis = 300),
        label = "button_color"
    )
    
    Box(
        modifier = modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring with pulse effect
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(if (state == CaptureButtonState.CAPTURING) pulseScale else 1f)
                .clip(CircleShape)
                .background(buttonColor.copy(alpha = 0.2f))
        )
        
        // Main capture button
        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(buttonColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled && state != CaptureButtonState.PROCESSING
                ) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            CaptureButtonIcon(
                state = state,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

/**
 * Icon component for the capture button that changes based on state
 */
@Composable
private fun CaptureButtonIcon(
    state: CaptureButtonState,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (state) {
        CaptureButtonState.IDLE -> Icons.Default.PhotoCamera
        CaptureButtonState.CAPTURING -> Icons.Default.Stop
        CaptureButtonState.PROCESSING -> Icons.Default.HourglassEmpty
        CaptureButtonState.SUCCESS -> Icons.Default.Check
        CaptureButtonState.ERROR -> Icons.Default.Error
    }
    
    // Rotation animation for processing state
    val infiniteTransition = rememberInfiniteTransition(label = "icon_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Icon(
        imageVector = icon,
        contentDescription = getContentDescription(state),
        tint = tint,
        modifier = modifier
            .size(32.dp)
            .then(
                if (state == CaptureButtonState.PROCESSING) {
                    Modifier.graphicsLayer { rotationZ = rotation }
                } else {
                    Modifier
                }
            )
    )
}

/**
 * Gets content description for accessibility based on button state
 */
private fun getContentDescription(state: CaptureButtonState): String {
    return when (state) {
        CaptureButtonState.IDLE -> "Capture photo"
        CaptureButtonState.CAPTURING -> "Stop capture"
        CaptureButtonState.PROCESSING -> "Processing image"
        CaptureButtonState.SUCCESS -> "Capture successful"
        CaptureButtonState.ERROR -> "Capture failed"
    }
}

/**
 * Compact version of the capture button for smaller spaces
 */
@Composable
fun CompactCaptureButton(
    onClick: () -> Unit,
    state: CaptureButtonState = CaptureButtonState.IDLE,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    val buttonColor by animateColorAsState(
        targetValue = when (state) {
            CaptureButtonState.IDLE -> MaterialTheme.colorScheme.primary
            CaptureButtonState.CAPTURING -> MaterialTheme.colorScheme.secondary
            CaptureButtonState.PROCESSING -> MaterialTheme.colorScheme.tertiary
            CaptureButtonState.SUCCESS -> Color(0xFF4CAF50)
            CaptureButtonState.ERROR -> MaterialTheme.colorScheme.error
        },
        animationSpec = tween(durationMillis = 300),
        label = "compact_button_color"
    )
    
    FloatingActionButton(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier.size(48.dp),
        containerColor = buttonColor,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        CaptureButtonIcon(
            state = state,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Camera controls with capture button
 */
@Composable
fun CameraControls(
    onCaptureClick: () -> Unit,
    onFlashToggle: () -> Unit = {},
    onSwitchCamera: () -> Unit = {},
    captureButtonState: CaptureButtonState = CaptureButtonState.IDLE,
    isFlashOn: Boolean = false,
    hasFrontCamera: Boolean = true,
    isCameraReady: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Top controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Flash toggle
            IconButton(onClick = onFlashToggle) {
                Icon(
                    imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = if (isFlashOn) "Turn off flash" else "Turn on flash",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Camera switch
            if (hasFrontCamera) {
                IconButton(onClick = onSwitchCamera) {
                    Icon(
                        imageVector = Icons.Default.CameraRear,
                        contentDescription = "Switch camera",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Bottom capture button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            CaptureButton(
                onClick = onCaptureClick,
                state = if (!isCameraReady) CaptureButtonState.PROCESSING else captureButtonState,
                enabled = isCameraReady
            )
        }
    }
}