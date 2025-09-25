package com.technitedminds.wallet.presentation.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Category chip component with icon and color customization.
 * Supports selection states and various sizes.
 */
@Composable
fun CategoryChip(
    category: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isCompact: Boolean = false,
    onClick: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    icon: ImageVector? = null,
    color: Color? = null
) {
    val chipColor = color ?: getCategoryColor(category)
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            chipColor
        } else {
            chipColor.copy(alpha = 0.2f)
        },
        animationSpec = tween(200),
        label = "chip_background"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            Color.White
        } else {
            chipColor
        },
        animationSpec = tween(200),
        label = "chip_content"
    )
    
    Surface(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(if (isCompact) 12.dp else 16.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (isCompact) 8.dp else 12.dp,
                vertical = if (isCompact) 4.dp else 6.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(if (isCompact) 14.dp else 16.dp)
                )
                Spacer(modifier = Modifier.width(if (isCompact) 4.dp else 6.dp))
            }
            
            // Category text
            Text(
                text = category,
                style = if (isCompact) {
                    MaterialTheme.typography.labelSmall
                } else {
                    MaterialTheme.typography.labelMedium
                },
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Close button
            if (onClose != null) {
                Spacer(modifier = Modifier.width(if (isCompact) 4.dp else 6.dp))
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove category",
                        tint = contentColor,
                        modifier = Modifier.size(if (isCompact) 12.dp else 14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Selectable category chip for filtering
 */
@Composable
fun SelectableCategoryChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color? = null
) {
    CategoryChip(
        category = category,
        isSelected = isSelected,
        onClick = onClick,
        icon = icon,
        color = color,
        modifier = modifier
    )
}

/**
 * Category chip with count indicator
 */
@Composable
fun CategoryChipWithCount(
    category: String,
    count: Int,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    icon: ImageVector? = null,
    color: Color? = null
) {
    val chipColor = color ?: getCategoryColor(category)
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            chipColor
        } else {
            chipColor.copy(alpha = 0.2f)
        },
        animationSpec = tween(200),
        label = "chip_count_background"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            Color.White
        } else {
            chipColor
        },
        animationSpec = tween(200),
        label = "chip_count_content"
    )
    
    Surface(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            
            // Category text
            Text(
                text = category,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
            
            // Count badge
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) {
                    Color.White.copy(alpha = 0.3f)
                } else {
                    chipColor.copy(alpha = 0.3f)
                }
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Horizontal list of category chips
 */
@Composable
fun CategoryChipRow(
    categories: List<String>,
    selectedCategories: Set<String>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    showIcons: Boolean = true
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories) { category ->
            SelectableCategoryChip(
                category = category,
                isSelected = category in selectedCategories,
                onClick = { onCategoryClick(category) },
                icon = if (showIcons) getCategoryIcon(category) else null
            )
        }
    }
}

/**
 * Category filter chips with "All" option
 */
@Composable
fun CategoryFilterChips(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    showCounts: Map<String, Int> = emptyMap()
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        // "All" chip
        item {
            SelectableCategoryChip(
                category = "All",
                isSelected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                icon = Icons.Default.Apps,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Category chips
        items(categories) { category ->
            if (showCounts.isNotEmpty()) {
                CategoryChipWithCount(
                    category = category,
                    count = showCounts[category] ?: 0,
                    isSelected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    icon = getCategoryIcon(category)
                )
            } else {
                SelectableCategoryChip(
                    category = category,
                    isSelected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    icon = getCategoryIcon(category)
                )
            }
        }
    }
}

/**
 * Get color for category based on name hash
 */
private fun getCategoryColor(category: String): Color {
    val colors = listOf(
        Color(0xFF1976D2), // Blue
        Color(0xFF388E3C), // Green
        Color(0xFFE91E63), // Pink
        Color(0xFFFF9800), // Orange
        Color(0xFF9C27B0), // Purple
        Color(0xFF607D8B), // Blue Grey
        Color(0xFF795548), // Brown
        Color(0xFF00BCD4), // Cyan
        Color(0xFFF44336), // Red
        Color(0xFF4CAF50)  // Light Green
    )
    
    return colors[category.hashCode().mod(colors.size)]
}

/**
 * Get icon for category based on common category names
 */
private fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "personal", "personal cards" -> Icons.Default.Person
        "business", "work" -> Icons.Default.Business
        "travel" -> Icons.Default.Flight
        "shopping", "retail" -> Icons.Default.ShoppingCart
        "food", "restaurant" -> Icons.Default.Restaurant
        "health", "medical" -> Icons.Default.LocalHospital
        "entertainment" -> Icons.Default.Movie
        "transport", "transportation" -> Icons.Default.DirectionsBus
        "finance", "banking" -> Icons.Default.AccountBalance
        "membership" -> Icons.Default.CardMembership
        "loyalty" -> Icons.Default.Stars
        "gift", "gift cards" -> Icons.Default.CardGiftcard
        "insurance" -> Icons.Default.Security
        "education" -> Icons.Default.School
        "gym", "fitness" -> Icons.Default.FitnessCenter
        else -> Icons.Default.Category
    }
}