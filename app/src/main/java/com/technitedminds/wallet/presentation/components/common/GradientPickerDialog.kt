package com.technitedminds.wallet.presentation.components.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.technitedminds.wallet.domain.model.CardGradient
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.model.GradientDirection
import com.technitedminds.wallet.presentation.constants.AppConstants

/**
 * Modern gradient picker dialog with tab-based navigation and enhanced UX
 * Features:
 * - Preset gradients tab for quick selection
 * - Custom gradient creator tab for personalized gradients
 * - Large live preview with card styling
 * - Intuitive color and direction pickers
 * - Material 3 design language
 */
@Composable
fun GradientPickerDialog(
    isVisible: Boolean,
    cardType: CardType,
    selectedGradient: CardGradient?,
    onGradientSelected: (CardGradient) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    
    // State for current gradient being configured
    var currentGradient by remember(selectedGradient, cardType) {
        mutableStateOf(
            selectedGradient ?: CardGradient(
                startColor = cardType.getDefaultGradient().first,
                endColor = cardType.getDefaultGradient().second,
                direction = GradientDirection.TopToBottom,
                name = null
            )
        )
    }
    
    // Tab selection: 0 = Presets, 1 = Custom
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header with close button
                DialogHeader(
                    onDismiss = onDismiss
                )
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                // Large preview at top
                LiveGradientPreview(
                    gradient = currentGradient,
                    cardType = cardType
                )
                
                // Tab navigation
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { 
                            Text(
                                AppConstants.UIText.PRESETS_TAB,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                            ) 
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = AppConstants.UIText.PRESET_GRADIENTS_DESC,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { 
                            Text(
                                AppConstants.UIText.CUSTOM_TAB,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                            ) 
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = AppConstants.UIText.CUSTOM_GRADIENT_DESC,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                // Content area with tab switching (takes available space)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith 
                            fadeOut(animationSpec = tween(200))
                        },
                        label = "tab_content",
                        modifier = Modifier.fillMaxSize()
                    ) { tabIndex ->
                        when (tabIndex) {
                            0 -> PresetGradientsTab(
                                currentGradient = currentGradient,
                                onGradientChanged = { currentGradient = it }
                            )
                            1 -> CustomGradientTab(
                                currentGradient = currentGradient,
                                onGradientChanged = { currentGradient = it }
                            )
                        }
                    }
                }
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                // Action buttons
                DialogActions(
                    onDismiss = onDismiss,
                    onApply = { 
                        onGradientSelected(currentGradient)
                        onDismiss()
                    }
                )
            }
        }
    }
}

/**
 * Dialog header with title and close button
 */
@Composable
private fun DialogHeader(
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = AppConstants.UIText.CHOOSE_GRADIENT_TITLE,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = AppConstants.UIText.CHOOSE_GRADIENT_SUBTITLE,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = AppConstants.UIText.CLOSE_DIALOG,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Large live preview card showing current gradient
 */
@Composable
private fun LiveGradientPreview(
    gradient: CardGradient,
    cardType: CardType
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .height(140.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(createGradientBrush(gradient))
                .padding(20.dp)
        ) {
            // Card type icon in top right
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                Icon(
                    imageVector = getCardTypeIcon(cardType),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Card type name at bottom
            Column(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text(
                    text = cardType.getDisplayName(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                gradient.name?.let { name ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

/**
 * Presets tab content with gradient grid
 */
@Composable
private fun PresetGradientsTab(
    currentGradient: CardGradient,
    onGradientChanged: (CardGradient) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = AppConstants.UIText.CHOOSE_PRESET_GRADIENT,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Calculate grid height based on number of presets
        val presetCount = getPresetGradients().size
        val rows = (presetCount + 2) / 3 // 3 columns
        val gridHeight = (rows * 110).dp // 90dp card + 8dp name + 12dp gap
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(gridHeight),
            userScrollEnabled = false
        ) {
            items(getPresetGradients()) { preset ->
                PresetGradientCard(
                    gradient = preset,
                    isSelected = currentGradient.startColor == preset.startColor &&
                                currentGradient.endColor == preset.endColor &&
                                currentGradient.direction == preset.direction,
                    onClick = { onGradientChanged(preset) }
                )
            }
        }
    }
}

/**
 * Custom gradient tab content with color and direction pickers
 */
@Composable
private fun CustomGradientTab(
    currentGradient: CardGradient,
    onGradientChanged: (CardGradient) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = AppConstants.UIText.CREATE_CUSTOM_GRADIENT,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Start color picker
        ColorPickerSection(
            label = AppConstants.UIText.START_COLOR,
            selectedColor = currentGradient.startColor,
            onColorSelected = { color ->
                onGradientChanged(currentGradient.copy(startColor = color))
            }
        )
        
        // End color picker
        ColorPickerSection(
            label = AppConstants.UIText.END_COLOR,
            selectedColor = currentGradient.endColor,
            onColorSelected = { color ->
                onGradientChanged(currentGradient.copy(endColor = color))
            }
        )
        
        // Direction picker
        DirectionPickerSection(
            selectedDirection = currentGradient.direction,
            onDirectionSelected = { direction ->
                onGradientChanged(currentGradient.copy(direction = direction))
            }
        )
    }
}

/**
 * Individual preset gradient card
 */
@Composable
private fun PresetGradientCard(
    gradient: CardGradient,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "preset_border"
    )
    
    val interactionSource = remember { MutableInteractionSource() }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(createGradientBrush(gradient)),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = AppConstants.ContentDescriptions.SELECTED,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(6.dp)
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
        
        gradient.name?.let { name ->
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Color picker section with grid of colors
 */
@Composable
private fun ColorPickerSection(
    label: String,
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Calculate grid height: 60 colors / 8 columns = 8 rows (rounded up)
        // Each row: 36dp swatch + 8dp gap = 44dp per row
        val colorCount = getColorPalette().size
        val columns = 8
        val rows = (colorCount + columns - 1) / columns // Ceiling division
        val swatchSize = 36.dp
        val gap = 8.dp
        val gridHeight = (rows * (swatchSize + gap)).coerceAtMost(360.dp)
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(gap),
            modifier = Modifier.height(gridHeight),
            userScrollEnabled = false
        ) {
            items(getColorPalette()) { color ->
                ColorSwatch(
                    color = color,
                    isSelected = selectedColor.equals(color, ignoreCase = true),
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

/**
 * Individual color swatch
 */
@Composable
private fun ColorSwatch(
    color: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "swatch_scale"
    )
    
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(android.graphics.Color.parseColor(color)))
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = scale,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = AppConstants.UIText.SELECTED_COLOR,
                tint = if (isLightColor(color)) Color.Black else Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Direction picker section
 */
@Composable
private fun DirectionPickerSection(
    selectedDirection: GradientDirection,
    onDirectionSelected: (GradientDirection) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = AppConstants.UIText.GRADIENT_DIRECTION,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // 4 directions in 2 columns = 2 rows
        // Each row: 60dp card + 12dp gap = 72dp per row
        val directionCount = GradientDirection.entries.size
        val rows = (directionCount + 1) / 2 // 2 columns
        val cardHeight = 60.dp
        val gap = 12.dp
        val gridHeight = rows * (cardHeight + gap)
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(gap),
            modifier = Modifier.height(gridHeight),
            userScrollEnabled = false
        ) {
            items(GradientDirection.entries) { direction ->
                DirectionOption(
                    direction = direction,
                    isSelected = selectedDirection == direction,
                    onClick = { onDirectionSelected(direction) }
                )
            }
        }
    }
}

/**
 * Individual direction option with visual representation
 */
@Composable
private fun DirectionOption(
    direction: GradientDirection,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "direction_border"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getDirectionLabel(direction),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Text(
                text = getDirectionArrow(direction),
                style = MaterialTheme.typography.titleLarge,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

/**
 * Dialog action buttons
 */
@Composable
private fun DialogActions(
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
    ) {
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.height(48.dp)
        ) {
            Text(
                text = AppConstants.DialogText.CANCEL_BUTTON,
                style = MaterialTheme.typography.labelLarge
            )
        }
        
        FilledTonalButton(
            onClick = onApply,
            modifier = Modifier
                .height(48.dp)
                .width(120.dp)
        ) {
            Text(
                text = AppConstants.UIText.APPLY,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Get color palette for picker - organized by color families
 */
private fun getColorPalette(): List<String> = listOf(
    // Reds & Pinks
    "#F44336", "#E91E63", "#EC407A", "#F06292", "#ff9a9e", "#fa709a",
    "#f093fb", "#f5576c", "#fed6e3", "#fecfef",
    
    // Purples & Violets  
    "#9C27B0", "#673AB7", "#667eea", "#764ba2", "#a18cd1", "#fbc2eb",
    "#d299c2", "#BA68C8",
    
    // Blues
    "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4", "#4facfe", "#00f2fe",
    "#89f7fe", "#66a6ff", "#42A5F5", "#29B6F6",
    
    // Teals & Greens
    "#009688", "#4CAF50", "#8BC34A", "#CDDC39", "#43e97b", "#38f9d7",
    "#a8edea", "#26A69A", "#66BB6A", "#9CCC65",
    
    // Yellows & Oranges
    "#FFEB3B", "#FFC107", "#FF9800", "#FF5722", "#fee140", "#ffecd2",
    "#fcb69f", "#fad0c4", "#FFCA28", "#FFA726",
    
    // Neutrals
    "#795548", "#9E9E9E", "#607D8B", "#fef9d7", "#8D6E63", "#78909C",
    
    // Black & White
    "#000000", "#FFFFFF"
)

/**
 * Get preset gradients - curated collection with names
 */
private fun getPresetGradients(): List<CardGradient> = listOf(
    CardGradient("#667eea", "#764ba2", GradientDirection.TopToBottom, "Purple Haze"),
    CardGradient("#f093fb", "#f5576c", GradientDirection.TopToBottom, "Sunset"),
    CardGradient("#4facfe", "#00f2fe", GradientDirection.TopToBottom, "Ocean Blue"),
    CardGradient("#a8edea", "#fed6e3", GradientDirection.TopToBottom, "Cotton Candy"),
    CardGradient("#ffecd2", "#fcb69f", GradientDirection.TopToBottom, "Peach"),
    CardGradient("#43e97b", "#38f9d7", GradientDirection.TopToBottom, "Emerald"),
    CardGradient("#fa709a", "#fee140", GradientDirection.TopToBottom, "Sunrise"),
    CardGradient("#89f7fe", "#66a6ff", GradientDirection.TopToBottom, "Sky"),
    CardGradient("#d299c2", "#fef9d7", GradientDirection.TopToBottom, "Lavender"),
    CardGradient("#ff9a9e", "#fecfef", GradientDirection.LeftToRight, "Rose"),
    CardGradient("#a18cd1", "#fbc2eb", GradientDirection.DiagonalTopLeftToBottomRight, "Violet"),
    CardGradient("#fad0c4", "#ffd1ff", GradientDirection.DiagonalTopRightToBottomLeft, "Blush"),
    CardGradient("#30cfd0", "#330867", GradientDirection.TopToBottom, "Deep Sea"),
    CardGradient("#ff6e7f", "#bfe9ff", GradientDirection.LeftToRight, "Berry"),
    CardGradient("#ee9ca7", "#ffdde1", GradientDirection.TopToBottom, "Coral"),
    CardGradient("#2af598", "#009efd", GradientDirection.DiagonalTopLeftToBottomRight, "Aurora"),
    CardGradient("#f83600", "#f9d423", GradientDirection.TopToBottom, "Fire"),
    CardGradient("#00d2ff", "#3a7bd5", GradientDirection.LeftToRight, "Glacier"),
    CardGradient("#fc466b", "#3f5efb", GradientDirection.DiagonalTopLeftToBottomRight, "Neon"),
    CardGradient("#fddb92", "#d1fdff", GradientDirection.TopToBottom, "Pastel"),
    CardGradient("#F44336", "#9C27B0", GradientDirection.TopToBottom, "Magenta"),
    CardGradient("#4CAF50", "#2196F3", GradientDirection.LeftToRight, "Nature")
)

/**
 * Creates a Compose Brush from CardGradient
 */
private fun createGradientBrush(gradient: CardGradient): androidx.compose.ui.graphics.Brush {
    val startColor = Color(android.graphics.Color.parseColor(gradient.startColor))
    val endColor = Color(android.graphics.Color.parseColor(gradient.endColor))
    
    return when (gradient.direction) {
        GradientDirection.TopToBottom -> androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(startColor, endColor)
        )
        GradientDirection.LeftToRight -> androidx.compose.ui.graphics.Brush.horizontalGradient(
            colors = listOf(startColor, endColor)
        )
        GradientDirection.DiagonalTopLeftToBottomRight -> androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(startColor, endColor),
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
        GradientDirection.DiagonalTopRightToBottomLeft -> androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(startColor, endColor),
            start = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, 0f),
            end = androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY)
        )
    }
}

/**
 * Get icon for card type
 */
private fun getCardTypeIcon(cardType: CardType): androidx.compose.ui.graphics.vector.ImageVector = when (cardType) {
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
 * Get human-readable label for gradient direction
 */
private fun getDirectionLabel(direction: GradientDirection): String = when (direction) {
    GradientDirection.TopToBottom -> "Top to Bottom"
    GradientDirection.LeftToRight -> "Left to Right"
    GradientDirection.DiagonalTopLeftToBottomRight -> "Diagonal ↘"
    GradientDirection.DiagonalTopRightToBottomLeft -> "Diagonal ↙"
}

/**
 * Get arrow symbol for gradient direction
 */
private fun getDirectionArrow(direction: GradientDirection): String = when (direction) {
    GradientDirection.TopToBottom -> "↓"
    GradientDirection.LeftToRight -> "→"
    GradientDirection.DiagonalTopLeftToBottomRight -> "↘"
    GradientDirection.DiagonalTopRightToBottomLeft -> "↙"
}

/**
 * Check if a color is light (for determining check icon color)
 */
private fun isLightColor(hexColor: String): Boolean {
    return try {
        val color = android.graphics.Color.parseColor(hexColor)
        val red = android.graphics.Color.red(color)
        val green = android.graphics.Color.green(color)
        val blue = android.graphics.Color.blue(color)
        
        // Calculate perceived brightness (HSP Color Model)
        val brightness = Math.sqrt(
            red * red * 0.299 +
            green * green * 0.587 +
            blue * blue * 0.114
        )
        
        brightness > 130
    } catch (e: Exception) {
        false
    }
}