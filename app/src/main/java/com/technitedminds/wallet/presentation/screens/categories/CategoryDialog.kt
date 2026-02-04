package com.technitedminds.wallet.presentation.screens.categories

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.ColorUtils
import com.technitedminds.wallet.domain.model.Category
import com.technitedminds.wallet.presentation.components.common.*
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.utils.asDisplayName

/**
 * Enhanced dialog for creating or editing categories with premium UI/UX
 * Features:
 * - Scrollable content with proper bounds
 * - Premium components throughout
 * - Character limits with feedback
 * - Real-time validation
 * - Smooth animations
 * - Improved visual hierarchy
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDialog(
    title: String,
    category: Category?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, colorHex: String, iconName: String?) -> Unit
) {
    // State management
    var name by remember(category) { mutableStateOf(category?.name ?: "") }
    var description by remember(category) { mutableStateOf(category?.description ?: "") }
    var selectedColor by remember(category) { mutableStateOf(category?.colorHex ?: "#1976D2") }
    var selectedIcon by remember(category) { mutableStateOf(category?.iconName ?: "Category") }
    
    // Validation states
    val nameError = when {
        name.isBlank() && name.isNotEmpty() -> "Category name is required"
        name.length > NAME_MAX_LENGTH -> "Name is too long"
        else -> null
    }
    val descriptionError = when {
        description.length > DESCRIPTION_MAX_LENGTH -> "Description is too long"
        else -> null
    }
    
    val isValid = name.isNotBlank() && 
                  name.length <= NAME_MAX_LENGTH && 
                  description.length <= DESCRIPTION_MAX_LENGTH
    
    // Color and icon parsing
    val parsedColor = remember(selectedColor) {
        runCatching { 
            Color(android.graphics.Color.parseColor(selectedColor))
        }.getOrElse { 
            Color(android.graphics.Color.parseColor("#1976D2"))
        }
    }
    
    val previewGradient = remember(parsedColor) {
        Brush.linearGradient(
            listOf(
                Color(
                    ColorUtils.blendARGB(
                        parsedColor.toArgb(),
                        Color.White.copy(alpha = 0.9f).toArgb(),
                        0.18f
                    )
                ),
                Color(
                    ColorUtils.blendARGB(
                        parsedColor.toArgb(),
                        Color.Black.copy(alpha = 0.7f).toArgb(),
                        0.08f
                    )
                )
            )
        )
    }
    
    val iconVector: ImageVector = getIconFromName(selectedIcon) ?: Icons.Default.Category

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.9f)
                .padding(vertical = 16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Fixed Header
                DialogHeader(
                    title = title,
                    onDismiss = onDismiss
                )
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Preview Section
                    CategoryPreviewSection(
                        name = name,
                        selectedColor = selectedColor,
                        iconVector = iconVector,
                        previewGradient = previewGradient,
                        parsedColor = parsedColor
                    )
                    
                    // Basic Information Section
                    BasicInformationSection(
                        name = name,
                        onNameChange = { if (it.length <= NAME_MAX_LENGTH) name = it },
                        nameError = nameError,
                        description = description,
                        onDescriptionChange = { if (it.length <= DESCRIPTION_MAX_LENGTH) description = it },
                        descriptionError = descriptionError
                    )
                    
                    // Appearance Section
                    AppearanceSection(
                        selectedColor = selectedColor,
                        onColorSelected = { selectedColor = it },
                        selectedIcon = iconVector,
                        onIconSelected = { icon ->
                            selectedIcon = getIconName(icon)
                        }
                    )
                    
                    // Bottom spacing for scroll comfort
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                // Fixed Footer with Actions
                DialogActions(
                    onDismiss = onDismiss,
                    onConfirm = {
                        onConfirm(name.trim(), description.trim(), selectedColor, selectedIcon)
                    },
                    isValid = isValid,
                    isEditMode = category != null
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
    title: String,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close dialog",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Category preview section with icon and color display
 */
@Composable
private fun CategoryPreviewSection(
    name: String,
    selectedColor: String,
    iconVector: ImageVector,
    previewGradient: Brush,
    parsedColor: Color
) {
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Preview",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            
            // Large preview icon
            Surface(
                modifier = Modifier.size(96.dp),
                shape = MaterialTheme.shapes.large,
                color = Color.Transparent,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.background(previewGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = rememberVectorPainter(image = iconVector),
                        contentDescription = null,
                        tint = if (parsedColor.luminance() > 0.5f) Color.Black else Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            // Category name preview
            Text(
                text = name.ifBlank { "Category Name" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            // Color hex display
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(parsedColor)
                    )
                    Text(
                        text = selectedColor.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Basic information section with name and description fields
 */
@Composable
private fun BasicInformationSection(
    name: String,
    onNameChange: (String) -> Unit,
    nameError: String?,
    description: String,
    onDescriptionChange: (String) -> Unit,
    descriptionError: String?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedSectionHeader(
            title = "Basic Information",
            icon = Icons.Default.Info,
            subtitle = "Name and description for your category"
        )
        
        PremiumCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category Name Field
                PremiumTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = "Category Name",
                    leadingIcon = Icons.Default.Category,
                    trailingIcon = {
                        Text(
                            text = "${name.length}/$NAME_MAX_LENGTH",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (name.length > NAME_MAX_LENGTH) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    isError = nameError != null,
                    errorMessage = nameError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Description Field (Optional)
                PremiumTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = "Description (Optional)",
                    leadingIcon = Icons.Default.Description,
                    trailingIcon = {
                        Text(
                            text = "${description.length}/$DESCRIPTION_MAX_LENGTH",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (description.length > DESCRIPTION_MAX_LENGTH) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    isError = descriptionError != null,
                    errorMessage = descriptionError,
                    singleLine = false,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Appearance section with color and icon pickers
 */
@Composable
private fun AppearanceSection(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    selectedIcon: ImageVector,
    onIconSelected: (ImageVector) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedSectionHeader(
            title = "Appearance",
            icon = Icons.Default.Palette,
            subtitle = "Customize the look of your category"
        )
        
        PremiumCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Color Picker
                ColorPicker(
                    selectedColor = selectedColor,
                    onColorSelected = onColorSelected,
                    showCustomColorOption = true
                )
                
                PremiumDivider()
                
                // Icon Picker
                IconPicker(
                    selectedIcon = selectedIcon,
                    onIconSelected = onIconSelected
                )
            }
        }
    }
}

/**
 * Dialog action buttons (Cancel and Confirm)
 */
@Composable
private fun DialogActions(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isValid: Boolean,
    isEditMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PremiumButton(
            onClick = onDismiss,
            text = "Cancel",
            variant = PremiumButtonVariant.Secondary,
            modifier = Modifier.weight(1f)
        )
        
        PremiumButton(
            onClick = onConfirm,
            text = if (isEditMode) "Update" else "Create",
            icon = if (isEditMode) Icons.Default.Edit else Icons.Default.Add,
            enabled = isValid,
            variant = PremiumButtonVariant.Primary,
            modifier = Modifier.weight(1f)
        )
    }
}

// Constants
private const val NAME_MAX_LENGTH = 30
private const val DESCRIPTION_MAX_LENGTH = 100
