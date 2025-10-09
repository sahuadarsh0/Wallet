package com.technitedminds.wallet.presentation.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Icon picker component for selecting category icons
 */
@Composable
fun IconPicker(
    selectedIcon: ImageVector,
    onIconSelected: (ImageVector) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Category Icon",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        // Icons grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(200.dp)
        ) {
            items(predefinedIcons) { icon ->
                IconOption(
                    icon = icon,
                    isSelected = selectedIcon == icon,
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onIconSelected(icon)
                    }
                )
            }
        }
    }
}

/**
 * Individual icon option component
 */
@Composable
private fun IconOption(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(200),
        label = "icon_background"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        },
        animationSpec = tween(200),
        label = "icon_border"
    )
    
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(200),
        label = "icon_color"
    )
    
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Predefined icon set for categories
 */
private val predefinedIcons = listOf(
    // General
    Icons.Default.Category,
    Icons.Default.Folder,
    Icons.Default.Label,
    Icons.Default.Bookmark,
    Icons.Default.Tag,
    Icons.Default.Apps,
    
    // Personal & Identity
    Icons.Default.Person,
    Icons.Default.PersonOutline,
    Icons.Default.AccountCircle,
    Icons.Default.Badge,
    Icons.Default.ContactPage,
    Icons.Default.Fingerprint,
    
    // Business & Work
    Icons.Default.Business,
    Icons.Default.Work,
    Icons.Default.BusinessCenter,
    Icons.Default.CorporateFare,
    Icons.Default.Domain,
    Icons.Default.Apartment,
    
    // Finance & Banking
    Icons.Default.AccountBalance,
    Icons.Default.AttachMoney,
    Icons.Default.CreditCard,
    Icons.Default.Payment,
    Icons.Default.AccountBalanceWallet,
    Icons.Default.MonetizationOn,
    
    // Shopping & Retail
    Icons.Default.ShoppingCart,
    Icons.Default.ShoppingBag,
    Icons.Default.Store,
    Icons.Default.Storefront,
    Icons.Default.LocalMall,
    Icons.Default.Receipt,
    
    // Food & Dining
    Icons.Default.Restaurant,
    Icons.Default.LocalDining,
    Icons.Default.LocalCafe,
    Icons.Default.LocalBar,
    Icons.Default.LocalPizza,
    Icons.Default.Fastfood,
    
    // Transportation
    Icons.Default.DirectionsCar,
    Icons.Default.DirectionsBus,
    Icons.Default.DirectionsTransit,
    Icons.Default.Flight,
    Icons.Default.Train,
    Icons.Default.LocalTaxi,
    
    // Health & Medical
    Icons.Default.LocalHospital,
    Icons.Default.LocalPharmacy,
    Icons.Default.MedicalServices,
    Icons.Default.HealthAndSafety,
    Icons.Default.Healing,
    Icons.Default.Vaccines,
    
    // Entertainment & Leisure
    Icons.Default.Movie,
    Icons.Default.TheaterComedy,
    Icons.Default.MusicNote,
    Icons.Default.SportsEsports,
    Icons.Default.Casino,
    Icons.Default.Celebration,
    
    // Education & Learning
    Icons.Default.School,
    Icons.Default.MenuBook,
    Icons.Default.LibraryBooks,
    Icons.Default.Science,
    Icons.Default.Psychology,
    Icons.Default.AutoStories,
    
    // Sports & Fitness
    Icons.Default.FitnessCenter,
    Icons.Default.SportsBasketball,
    Icons.Default.SportsFootball,
    Icons.Default.SportsTennis,
    Icons.Default.Pool,
    Icons.Default.DirectionsRun,
    
    // Travel & Tourism
    Icons.Default.TravelExplore,
    Icons.Default.Luggage,
    Icons.Default.Hotel,
    Icons.Default.BeachAccess,
    Icons.Default.Landscape,
    Icons.Default.Map,
    
    // Technology & Communication
    Icons.Default.Computer,
    Icons.Default.PhoneAndroid,
    Icons.Default.Wifi,
    Icons.Default.Router,
    Icons.Default.Cable,
    Icons.Default.Bluetooth,
    
    // Home & Utilities
    Icons.Default.Home,
    Icons.Default.ElectricalServices,
    Icons.Default.Plumbing,
    Icons.Default.Build,
    Icons.Default.Construction,
    Icons.Default.CleaningServices,
    
    // Security & Safety
    Icons.Default.Security,
    Icons.Default.Lock,
    Icons.Default.Shield,
    Icons.Default.VerifiedUser,
    Icons.Default.AdminPanelSettings,
    Icons.Default.GppGood,
    
    // Membership & Loyalty
    Icons.Default.CardMembership,
    Icons.Default.Stars,
    Icons.Default.Grade,
    Icons.Default.EmojiEvents,
    Icons.Default.Loyalty,
    Icons.Default.CardGiftcard,
    
    // Miscellaneous
    Icons.Default.Pets,
    Icons.Default.LocalFlorist,
    Icons.Default.Park,
    Icons.Default.Nature,
    Icons.Default.WbSunny,
    Icons.Default.Nightlight
)