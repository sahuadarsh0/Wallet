package com.technitedminds.wallet.presentation.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.technitedminds.wallet.domain.model.Category
import com.technitedminds.wallet.presentation.components.animation.liquidPress
import com.technitedminds.wallet.presentation.components.common.PremiumCard
import com.technitedminds.wallet.presentation.components.common.getIconFromName
import com.technitedminds.wallet.presentation.components.common.gradientShadow
import com.technitedminds.wallet.ui.theme.WalletSpring
import com.technitedminds.wallet.ui.theme.WalletTiming
import kotlinx.coroutines.delay

/**
 * Immutable descriptor for a folder tile shown on the home screen.
 */
data class FolderItem(
    val folder: OpenedFolder,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val count: Int,
    val gradient: List<Color>,
)

/**
 * Build the list of folder tiles to display, in a stable order:
 *   All → category folders → Uncategorized (only when there are uncategorized cards).
 */
fun buildFolderItems(
    categories: List<Category>,
    folderCounts: Map<String, Int>,
): List<FolderItem> {
    val all = FolderItem(
        folder = OpenedFolder.All,
        title = "All Cards",
        subtitle = "Everything in your wallet",
        icon = Icons.Default.Apps,
        count = folderCounts[HomeViewModel.FOLDER_ALL_KEY] ?: 0,
        gradient = listOf(Color(0xFF4F46E5), Color(0xFF9333EA)),
    )

    val categoryFolders = categories.map { category ->
        val tint = parseHexColor(category.colorHex)
        val gradient = listOf(tint, tint.darken(0.25f))
        FolderItem(
            folder = OpenedFolder.Category(category.id),
            title = category.name,
            subtitle = category.description ?: "${category.name} cards",
            icon = getIconFromName(category.iconName) ?: Icons.Default.FolderSpecial,
            count = folderCounts[category.id] ?: 0,
            gradient = gradient,
        )
    }

    val uncategorizedCount = folderCounts[HomeViewModel.FOLDER_UNCATEGORIZED_KEY] ?: 0
    val uncategorized = if (uncategorizedCount > 0) {
        FolderItem(
            folder = OpenedFolder.Uncategorized,
            title = "Uncategorized",
            subtitle = "Cards without a category",
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            count = uncategorizedCount,
            gradient = listOf(Color(0xFF64748B), Color(0xFF334155)),
        )
    } else null

    return buildList {
        add(all)
        addAll(categoryFolders)
        uncategorized?.let { add(it) }
    }
}

/**
 * Responsive 2-column grid of glassmorphic folder tiles.
 *
 * Shows a soft empty state when there are no cards yet and the user has no
 * custom categories.
 */
@Composable
fun FoldersGrid(
    items: List<FolderItem>,
    onFolderClick: (OpenedFolder) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) {
        EmptyFoldersState(modifier = modifier.fillMaxSize())
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp, top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> folderKey(item.folder) },
        ) { index, item ->
            FolderStaggerItem(index = index) {
                FolderCard(
                    item = item,
                    onClick = { onFolderClick(item.folder) },
                )
            }
        }
    }
}

/**
 * Single folder tile — glassmorphic card with gradient backplate, category icon,
 * title and card count. Uses a subtle "folder tab" silhouette for recognition.
 */
@Composable
fun FolderCard(
    item: FolderItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradientColors = item.gradient
    val base = gradientColors.first()
    val contentColor = Color.White

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.95f),
    ) {
        // Folder "tab" silhouette that peeks above the card body.
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 16.dp, y = 0.dp)
                .width(72.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            base.copy(alpha = 0.95f),
                            base.copy(alpha = 0.75f),
                        ),
                    ),
                ),
        )

        PremiumCard(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp)
                .gradientShadow(
                    colors = gradientColors,
                    shadowElevation = 10.dp,
                    cornerRadius = 22.dp,
                )
                .liquidPress(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        brush = Brush.linearGradient(colors = gradientColors),
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.05f),
                            ),
                        ),
                        shape = RoundedCornerShape(22.dp),
                    ),
            ) {
                // Soft highlight to sell the glass look.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.22f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.20f))
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.35f),
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(22.dp),
                            )
                        }

                        CountBadge(count = item.count, contentColor = contentColor)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = contentColor,
                            maxLines = 1,
                        )
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.85f),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountBadge(count: Int, contentColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100))
            .background(Color.Black.copy(alpha = 0.20f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(100),
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = if (count == 1) "1 card" else "$count cards",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = contentColor,
        )
    }
}

/**
 * Breadcrumb-style header used previously when a folder was opened. The
 * folder name + back button are now rendered directly inside the home
 * screen's top app bar, so this component is intentionally left out.
 */

@Composable
private fun EmptyFoldersState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(64.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No folders yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Create a category to start organizing your cards",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FolderStaggerItem(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.9f,
        animationSpec = WalletSpring.bouncy(),
        label = "folder_scale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = WalletSpring.gentle(),
        label = "folder_alpha",
    )

    LaunchedEffect(Unit) {
        delay(index.toLong() * WalletTiming.STAGGER_DELAY_MS)
        visible = true
    }

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        },
    ) { content() }
}

/**
 * Stable key for a folder used in [itemsIndexed] keys.
 */
private fun folderKey(folder: OpenedFolder): String = when (folder) {
    OpenedFolder.All -> "folder_all"
    OpenedFolder.Uncategorized -> "folder_uncategorized"
    is OpenedFolder.Category -> "folder_cat_${folder.id}"
}

private fun parseHexColor(hex: String?): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex ?: "#1976D2"))
    } catch (_: Throwable) {
        Color(0xFF1976D2)
    }
}

private fun Color.darken(amount: Float): Color {
    val clamp = amount.coerceIn(0f, 1f)
    return Color(
        red = (red * (1f - clamp)).coerceIn(0f, 1f),
        green = (green * (1f - clamp)).coerceIn(0f, 1f),
        blue = (blue * (1f - clamp)).coerceIn(0f, 1f),
        alpha = alpha,
    )
}

/**
 * Resolve the full folder metadata for an opened folder. Used to render the
 * in-folder header. Falls back to an "All Cards" item when the folder is null
 * or its category has been deleted.
 */
fun resolveOpenedFolderItem(
    folder: OpenedFolder?,
    items: List<FolderItem>,
): FolderItem? {
    if (folder == null) return null
    return items.firstOrNull { it.folder == folder }
        ?: FolderItem(
            folder = folder,
            title = when (folder) {
                OpenedFolder.All -> "All Cards"
                OpenedFolder.Uncategorized -> "Uncategorized"
                is OpenedFolder.Category -> "Folder"
            },
            subtitle = "",
            icon = Icons.Default.Inventory2,
            count = 0,
            gradient = listOf(Color(0xFF4F46E5), Color(0xFF9333EA)),
        )
}
