package com.technitedminds.wallet.presentation.components.common

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.BorderStroke
import com.technitedminds.wallet.ui.theme.GlassSurface
import com.technitedminds.wallet.ui.theme.Glass
import com.technitedminds.wallet.ui.theme.WalletSpring

/**
 * Enhanced floating action button with premium animations and haptic feedback
 */
@Composable
fun PremiumFloatingActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = WalletSpring.bouncy(),
        label = "fab_scale",
    )

    // Entrance spring — FAB bounces in on first composition
    var appeared by remember { mutableStateOf(false) }
    val entranceScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = WalletSpring.bouncy(),
        label = "fab_entrance",
    )
    LaunchedEffect(Unit) { appeared = true }

    FloatingActionButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .graphicsLayer {
                scaleX = scale * entranceScale
                scaleY = scale * entranceScale
            }
            .size(64.dp),
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(28.dp)
        )
    }
}

/**
 * Enhanced card with gradient background and premium styling
 */
@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = WalletSpring.snappy(),
        label = "card_scale",
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                } else Modifier
            ),
        colors = colors,
        elevation = elevation,
        content = content
    )
}

/**
 * Animated section header with icon and optional action
 */
@Composable
fun AnimatedSectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it / 2 },
            animationSpec = WalletSpring.gentle(),
        ) + fadeIn(animationSpec = tween(400))
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            action?.invoke()
        }
    }
}

/**
 * Enhanced text field with premium styling and animations
 */
@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = leadingIcon?.let { icon ->
                {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isFocused) MaterialTheme.colorScheme.primary 
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            trailingIcon = trailingIcon,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
        )
        
        AnimatedVisibility(
            visible = isError && errorMessage != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Enhanced button with premium styling and animations
 */
@Composable
fun PremiumButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    variant: PremiumButtonVariant = PremiumButtonVariant.Primary
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = WalletSpring.snappy(),
        label = "button_scale",
    )

    val colors = when (variant) {
        PremiumButtonVariant.Primary -> ButtonDefaults.buttonColors()
        PremiumButtonVariant.Secondary -> ButtonDefaults.outlinedButtonColors()
        PremiumButtonVariant.Tertiary -> ButtonDefaults.textButtonColors()
    }

    val buttonModifier = modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .height(48.dp)

    when (variant) {
        PremiumButtonVariant.Primary -> {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
                enabled = enabled,
                colors = colors,
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                ),
                modifier = buttonModifier
            ) {
                ButtonContent(text, icon)
            }
        }
        PremiumButtonVariant.Secondary -> {
            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
                enabled = enabled,
                colors = colors,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (enabled) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                ),
                modifier = buttonModifier
            ) {
                ButtonContent(text, icon)
            }
        }
        PremiumButtonVariant.Tertiary -> {
            TextButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
                enabled = enabled,
                colors = colors,
                shape = RoundedCornerShape(12.dp),
                modifier = buttonModifier
            ) {
                ButtonContent(text, icon)
            }
        }
    }
}

@Composable
private fun ButtonContent(text: String, icon: ImageVector?) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium
            )
        )
    }
}

enum class PremiumButtonVariant {
    Primary, Secondary, Tertiary
}

/**
 * Enhanced chip with premium styling
 */
@Composable
fun PremiumChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = WalletSpring.snappy(),
        label = "chip_scale",
    )

    // Overshoot bounce on selection change — targets 1f but the spring
    // overshoots to ~1.08f before settling, creating a "pop" on selection
    val selectionScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = if (selected) WalletSpring.elastic() else WalletSpring.snappy(),
        label = "chip_selection_scale",
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
                     else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = WalletSpring.snappy(),
        label = "chip_background",
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary
                     else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = WalletSpring.snappy(),
        label = "chip_content",
    )

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier.graphicsLayer {
            scaleX = scale * selectionScale
            scaleY = scale * selectionScale
        },
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = if (!selected) BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}

/**
 * Enhanced loading indicator with premium animations
 */
@Composable
fun PremiumLoadingIndicator(
    modifier: Modifier = Modifier,
    text: String? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp,
            color = MaterialTheme.colorScheme.primary
        )
        
        text?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Enhanced divider with gradient effect
 */
@Composable
fun PremiumDivider(
    modifier: Modifier = Modifier,
    thickness: androidx.compose.ui.unit.Dp = 1.dp,
    startIndent: androidx.compose.ui.unit.Dp = 0.dp,
) {
    Box(
        modifier = modifier
            .padding(start = startIndent)
            .fillMaxWidth()
            .height(thickness)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        Color.Transparent,
                    ),
                ),
            ),
    )
}

/**
 * Glass morphism card variant — semi-transparent background with iridescent border.
 * Uses spring physics for press animation.
 */
@Composable
fun GlassPremiumCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = WalletSpring.snappy(),
        label = "glass_card_scale",
    )

    GlassSurface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                } else Modifier,
            ),
        content = content,
    )
}

/**
 * Premium gradient-tinted shadow using the platform shadow system.
 *
 * Uses [Modifier.shadow] with tinted [ambientColor] and [spotColor] derived from the
 * card's gradient. The platform shadow renders a proper Gaussian blur per-element so
 * there is **no overlap** between adjacent cards, **no hard stops**, and the falloff
 * is smooth and uniform.
 *
 * In dark mode the tint shifts slightly cooler for a premium neon-on-dark aesthetic.
 * In light mode the tint is warmer and lower-alpha so it reads as a subtle coloured lift.
 */
fun Modifier.gradientShadow(
    colors: List<Color>,
    shadowElevation: androidx.compose.ui.unit.Dp = 8.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 16.dp,
    @Suppress("UNUSED_PARAMETER") glow: androidx.compose.ui.unit.Dp = 0.dp,
): Modifier = composed {
    val isDark = isSystemInDarkTheme()

    // Blend gradient endpoints into a single tint
    val blended = if (colors.size >= 2) {
        lerp(colors[0], colors[1], 0.5f)
    } else {
        colors.firstOrNull() ?: Color.Black
    }

    // Shift cooler in dark mode
    val coolTint = Color(0xFF8CCBFF)
    val tinted = if (isDark) lerp(blended, coolTint, 0.20f) else blended

    // Alpha tuning: visible glow in dark, softer warmth in light
    val ambientAlpha = if (isDark) 0.45f else 0.26f
    val spotAlpha = if (isDark) 0.55f else 0.32f

    shadow(
        elevation = shadowElevation,
        shape = RoundedCornerShape(cornerRadius),
        ambientColor = tinted.copy(alpha = ambientAlpha),
        spotColor = tinted.copy(alpha = spotAlpha),
    )
}