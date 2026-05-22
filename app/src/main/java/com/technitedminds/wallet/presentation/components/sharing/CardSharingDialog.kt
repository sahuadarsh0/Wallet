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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.presentation.components.common.PremiumButton
import com.technitedminds.wallet.presentation.components.common.PremiumButtonVariant
import com.technitedminds.wallet.ui.theme.WalletSpring

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
    val accent = MaterialTheme.colorScheme.primary
    var selectedOption by remember(isVisible, initialOption) {
        mutableStateOf(initialOption ?: CardSharingOption.FrontOnly)
    }
    var includeSensitiveInfo by remember { mutableStateOf(false) }
    var imageQuality by remember { mutableFloatStateOf(0.85f) }
    var addWatermark by remember { mutableStateOf(false) }
    var watermarkText by remember { mutableStateOf("CardVault") }

    val qualityLabel = remember(imageQuality) {
        when {
            imageQuality < 0.55f -> "Standard"
            imageQuality < 0.85f -> "High"
            else -> "Maximum"
        }
    }

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
                // ── Gradient header ─────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    accent.copy(alpha = 0.14f),
                                    Color.Transparent,
                                ),
                            ),
                        )
                        .padding(start = 24.dp, end = 12.dp, top = 20.dp, bottom = 16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                accent.copy(alpha = 0.25f),
                                                accent.copy(alpha = 0.06f),
                                            ),
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(24.dp),
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
                                    style = MaterialTheme.typography.labelMedium,
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
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                )

                // ── Scrollable content ──────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    SectionHeader(
                        icon = Icons.Default.CreditCard,
                        title = "What to share",
                        accentColor = accent,
                    )

                    val hasBack = card.backImagePath.isNotBlank() || card.type.supportsOCR()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ShareOptionChip(
                            icon = Icons.Default.CreditCard,
                            label = "Front",
                            isSelected = selectedOption == CardSharingOption.FrontOnly,
                            accentColor = accent,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedOption = CardSharingOption.FrontOnly
                            },
                            modifier = Modifier.weight(1f),
                        )

                        if (hasBack) {
                            ShareOptionChip(
                                icon = Icons.Default.Security,
                                label = "Back",
                                isSelected = selectedOption == CardSharingOption.BackOnly,
                                accentColor = accent,
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
                                accentColor = accent,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedOption = CardSharingOption.BothSides
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    SectionHeader(
                        icon = Icons.Default.Tune,
                        title = "Share options",
                        accentColor = accent,
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SettingRow(
                                icon = if (includeSensitiveInfo) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                title = "Reveal sensitive details",
                                subtitle = "Show full card number and CVV",
                            ) {
                                Switch(
                                    checked = includeSensitiveInfo,
                                    onCheckedChange = { includeSensitiveInfo = it },
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            )

                            SettingRow(
                                icon = Icons.Default.HighQuality,
                                title = "Image quality",
                                subtitle = "$qualityLabel • ${(imageQuality * 100).toInt()}%",
                            ) {}

                            Slider(
                                value = imageQuality,
                                onValueChange = { imageQuality = it },
                                valueRange = 0.4f..1.0f,
                                steps = 5,
                                modifier = Modifier.padding(top = 4.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = accent,
                                    activeTrackColor = accent,
                                ),
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            )

                            SettingRow(
                                icon = Icons.Default.WaterDrop,
                                title = "Add watermark",
                                subtitle = "Mark images as shared from CardVault",
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
                                    label = { Text("Watermark text") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                )

                // ── Actions footer ──────────────────────────────────
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
                                maxImageWidth = 1600,
                                maxImageHeight = 1010,
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

// ── Internal components ─────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    accentColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ShareOptionChip(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.95f,
        animationSpec = WalletSpring.bouncy(),
        label = "chip_scale",
    )

    val bgColor = if (isSelected) accentColor.copy(alpha = 0.12f) else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val contentColor = if (isSelected) accentColor else
        MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) accentColor.copy(alpha = 0.5f) else
        MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

    Surface(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .height(76.dp)
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
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
            )
        }
    }
}

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
                    fontWeight = FontWeight.Medium,
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
