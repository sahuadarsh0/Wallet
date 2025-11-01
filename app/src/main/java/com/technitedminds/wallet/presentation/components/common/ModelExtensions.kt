package com.technitedminds.wallet.presentation.components.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.model.Category

/**
 * Extension functions for domain models to provide UI-specific functionality
 */

/**
 * Returns the Material Icon for this card type
 */
fun CardType.getIcon(): ImageVector = when (this) {
    is CardType.Credit -> Icons.Default.CreditCard
    is CardType.Debit -> Icons.Default.AccountBalance
    is CardType.TransportCard -> Icons.Default.DirectionsBus
    is CardType.GiftCard -> Icons.Default.CardGiftcard
    is CardType.LoyaltyCard -> Icons.Default.Stars
    is CardType.MembershipCard -> Icons.Default.Badge
    is CardType.InsuranceCard -> Icons.Default.Security
    is CardType.IdentificationCard -> Icons.Default.Badge
    is CardType.Voucher -> Icons.Default.LocalOffer
    is CardType.Event -> Icons.Default.Event
    is CardType.BusinessCard -> Icons.Default.Business
    is CardType.LibraryCard -> Icons.Default.MenuBook
    is CardType.HotelCard -> Icons.Default.Hotel
    is CardType.StudentCard -> Icons.Default.School
    is CardType.AccessCard -> Icons.Default.Key
    is CardType.Custom -> Icons.Default.CreditCard
}

/**
 * Returns the Material Icon for this category
 */
fun Category.getIcon(): ImageVector {
    // Try to parse the icon name and return appropriate Material Icon
    return when (iconName?.lowercase()) {
        "creditcard" -> Icons.Default.CreditCard
        "accountbalance" -> Icons.Default.AccountBalance
        "directionsBus" -> Icons.Default.DirectionsBus
        "cardgiftcard" -> Icons.Default.CardGiftcard
        "stars" -> Icons.Default.Stars
        "badge" -> Icons.Default.Badge
        "security" -> Icons.Default.Security
        "localoffer" -> Icons.Default.LocalOffer
        "event" -> Icons.Default.Event
        "business" -> Icons.Default.Business
        "menubook" -> Icons.AutoMirrored.Filled.MenuBook
        "hotel" -> Icons.Default.Hotel
        "school" -> Icons.Default.School
        "key" -> Icons.Default.Key
        "shopping" -> Icons.Default.ShoppingCart
        "restaurant" -> Icons.Default.Restaurant
        "fitness" -> Icons.Default.FitnessCenter
        "medical" -> Icons.Default.LocalHospital
        "travel" -> Icons.Default.Flight
        "entertainment" -> Icons.Default.Movie
        "work" -> Icons.Default.Work
        "home" -> Icons.Default.Home
        "car" -> Icons.Default.DirectionsCar
        "phone" -> Icons.Default.Phone
        "computer" -> Icons.Default.Computer
        "games" -> Icons.Default.Games
        "music" -> Icons.Default.MusicNote
        "photo" -> Icons.Default.Photo
        "book" -> Icons.Default.Book
        "sports" -> Icons.Default.SportsBaseball
        "pets" -> Icons.Default.Pets
        "garden" -> Icons.Default.Grass
        "tools" -> Icons.Default.Build
        "art" -> Icons.Default.Palette
        "science" -> Icons.Default.Science
        else -> Icons.Default.Category // Default fallback
    }
}