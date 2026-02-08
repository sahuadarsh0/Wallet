package com.technitedminds.wallet.presentation.components.sharing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.presentation.components.common.GlassPremiumCard
import com.technitedminds.wallet.presentation.components.common.PremiumButton
import com.technitedminds.wallet.presentation.components.common.PremiumButtonVariant
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.ui.theme.Glass
import com.technitedminds.wallet.ui.theme.GlassSurface
import com.technitedminds.wallet.ui.theme.WalletSpring

/**
 * Modern card sharing dialog with glass morphism styling, spring physics,
 * and organized layout. Replaces the old AlertDialog-based approach.
 */
@Composable
fun CardSharingDialog(
    card: Card,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onShare: (CardSharingOption, CardSharingConfig) -> Unit,
    initialOption: CardSharingOption? = null,
    modifier: Modifier = Modifier,
) {
    if (!isVisible) return

    val haptic = LocalHapticFeedback.current
    var selectedOption by remember(isVisible, initialOption) {
        mutableStateOf(initialOption ?: CardSharingOption.FrontOnly)
    }
    var includeSensitiveInfo by remember { mutableStateOf(false) }
    var imageQuality by remember { mutableFloatStateOf(0.8f) }
    var addWatermark by remember { mutableStateOf(true) }
    var watermarkText by remember { mutableStateOf("CardVault") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // -- Header --
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column {
                            Text(
                                text = "Share Card",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = card.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                )

                // -- Content --
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    // Share option selector
                    Text(
                        text = AppConstants.UIText.WHAT_TO_SHARE_LABEL,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ShareOptionChip(
                            icon = Icons.Default.CreditCard,
                            label = "Front",
                            isSelected = selectedOption == CardSharingOption.FrontOnly,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedOption = CardSharingOption.FrontOnly
                            },
                            modifier = Modifier.weight(1f),
                        )

                        if (card.backImagePath.isNotBlank() || card.type.supportsOCR()) {
                            ShareOptionChip(
                                icon = Icons.Default.Security,
                                label = "Back",
                                isSelected = selectedOption == CardSharingOption.BackOnly,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedOption = CardSharingOption.BackOnly
                                },
                                modifier = Modifier.weight(1f),
                            )
                            ShareOptionChip(
                                icon = Icons.Default.ViewCarousel,
                                label = "Both",
                                isSelected = selectedOption == CardSharingOption.BothSides,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedOption = CardSharingOption.BothSides
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    )

                    // Settings section
                    Text(
                        text = AppConstants.UIText.SHARING_OPTIONS_LABEL,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )

                    // Sensitive info toggle
                    SettingRow(
                        icon = if (includeSensitiveInfo) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        title = AppConstants.UIText.INCLUDE_SENSITIVE_INFO_LABEL,
                        subtitle = AppConstants.UIText.INCLUDE_SENSITIVE_INFO_SUBTITLE,
                    ) {
                        Switch(
                            checked = includeSensitiveInfo,
                            onCheckedChange = { includeSensitiveInfo = it },
                        )
                    }

                    // Image quality
                    SettingRow(
                        icon = Icons.Default.HighQuality,
                        title = AppConstants.UIText.IMAGE_QUALITY_LABEL,
                        subtitle = "${(imageQuality * 100).toInt()}%",
                    ) {}
                    Slider(
                        value = imageQuality,
                        onValueChange = { imageQuality = it },
                        valueRange = 0.3f..1.0f,
                        steps = 6,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )

                    // Watermark toggle
                    SettingRow(
                        icon = Icons.Default.WaterDrop,
                        title = AppConstants.UIText.ADD_WATERMARK_LABEL,
                        subtitle = AppConstants.UIText.PROTECT_SHARED_IMAGES_SUBTITLE,
                    ) {
                        Switch(
                            checked = addWatermark,
                            onCheckedChange = { addWatermark = it },
                        )
                    }

                    AnimatedVisibility(
                        visible = addWatermark,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        OutlinedTextField(
                            value = watermarkText,
                            onValueChange = { watermarkText = it },
                            label = { Text(AppConstants.UIText.WATERMARK_TEXT_LABEL) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                )

                // -- Actions --
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PremiumButton(
                        onClick = onDismiss,
                        text = "Cancel",
                        variant = PremiumButtonVariant.Secondary,
                        modifier = Modifier.weight(1f),
                    )
                    PremiumButton(
                        onClick = {
                            val config = CardSharingConfig(
                                includeSensitiveInfo = includeSensitiveInfo,
                                imageQuality = imageQuality,
                                maxImageWidth = 1200,
                                maxImageHeight = 800,
                                addWatermark = addWatermark,
                                watermarkText = watermarkText,
                            )
                            onShare(selectedOption, config)
                            onDismiss()
                        },
                        text = "Share",
                        icon = Icons.Default.Share,
                        variant = PremiumButtonVariant.Primary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * Selectable chip for share options (Front / Back / Both).
 * Uses spring scale and glass surface when selected.
 */
@Composable
private fun ShareOptionChip(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.95f,
        animationSpec = WalletSpring.bouncy(),
        label = "chip_scale",
    )

    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(72.dp)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
            )
        }
    }
}

/**
 * Settings row with icon, title, subtitle, and trailing content.
 */
@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        trailing()
    }
}
