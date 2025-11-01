package com.technitedminds.wallet.presentation.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.ColorUtils
import com.technitedminds.wallet.domain.model.Category
import com.technitedminds.wallet.presentation.components.common.getIconFromName
import com.technitedminds.wallet.presentation.components.common.getIconName
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.utils.asDisplayName

/**
 * Dialog for creating or editing a category
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryDialog(
    title: String,
    category: Category?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, colorHex: String, iconName: String?) -> Unit
) {
    var name by remember(category) { mutableStateOf(category?.name ?: "") }
    var description by remember(category) { mutableStateOf(category?.description ?: "") }
    var selectedColor by remember(category) { mutableStateOf(category?.colorHex ?: "#1976D2") }
    var selectedIcon by remember(category) {
        mutableStateOf(category?.iconName ?: "Category")
    }

    val isValid = name.isNotBlank()
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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
            tonalElevation = 6.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = MaterialTheme.shapes.large,
                            color = Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier.background(previewGradient),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Icon(
                                    painter = rememberVectorPainter(image = iconVector),
                                    contentDescription = null,
                                    tint = if (parsedColor.luminance() > 0.5f) Color.Black else Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = name.ifBlank { "Category preview" },
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AssistChip(
                                    onClick = {},
                                    enabled = false,
                                    label = { Text(selectedColor.uppercase()) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Palette, contentDescription = null)
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledLeadingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                                AssistChip(
                                    onClick = {},
                                    enabled = false,
                                    label = { Text(selectedIcon.asDisplayName()) },
                                    leadingIcon = {
                                        Icon(iconVector, contentDescription = null)
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledLeadingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Category Name") },
                        placeholder = { Text("Enter category name") },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)") },
                        placeholder = { Text("Enter description") },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    com.technitedminds.wallet.presentation.components.common.ColorPicker(
                        selectedColor = selectedColor,
                        onColorSelected = { selectedColor = it }
                    )

                    com.technitedminds.wallet.presentation.components.common.IconPicker(
                        selectedIcon = getIconFromName(selectedIcon) ?: Icons.Default.Category,
                        onIconSelected = { icon ->
                            selectedIcon = getIconName(icon)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(AppConstants.DialogText.CANCEL_BUTTON)
                    }
                    Button(
                        onClick = {
                            onConfirm(name.trim(), description.trim(), selectedColor, selectedIcon)
                        },
                        enabled = isValid,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (category == null) "Create" else "Update")
                    }
                }
            }
        }
    }
}


