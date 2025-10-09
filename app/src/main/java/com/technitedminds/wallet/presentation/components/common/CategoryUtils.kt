package com.technitedminds.wallet.presentation.components.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsFootball
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.technitedminds.wallet.domain.usecase.category.GetCategoryNameUseCase

/**
 * Utility functions for category-related operations in Compose.
 */

/**
 * Composable function to get category name by ID.
 * Returns the category name or a fallback value.
 */
@Composable
fun rememberCategoryName(
    categoryId: String?,
    getCategoryNameUseCase: GetCategoryNameUseCase? = null
): String {
    var categoryName by remember(categoryId) { mutableStateOf("General") }
    
    LaunchedEffect(categoryId) {
        if (getCategoryNameUseCase != null && !categoryId.isNullOrBlank()) {
            categoryName = getCategoryNameUseCase(categoryId)
        } else {
            categoryName = when {
                categoryId.isNullOrBlank() -> "General"
                categoryId == "personal" -> "Personal"
                categoryId == "business" -> "Business"
                categoryId == "travel" -> "Travel"
                categoryId == "shopping" -> "Shopping"
                categoryId == "health" -> "Health"
                categoryId == "entertainment" -> "Entertainment"
                else -> categoryId.replaceFirstChar { it.uppercase() }
            }
        }
    }
    
    return categoryName
}

/**
 * Simple category name resolver without dependency injection.
 * Useful for components that don't have access to ViewModels.
 */
fun resolveCategoryName(categoryId: String?): String {
    return when {
        categoryId.isNullOrBlank() -> "General"
        categoryId == "personal" -> "Personal"
        categoryId == "business" -> "Business"
        categoryId == "travel" -> "Travel"
        categoryId == "shopping" -> "Shopping"
        categoryId == "health" -> "Health"
        categoryId == "entertainment" -> "Entertainment"
        else -> categoryId.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase() else it.toString() 
        }
    }
}

/**
 * Helper function to get ImageVector from icon name
 */
fun getIconFromName(iconName: String?): ImageVector? {
    return when (iconName) {
        // General
        "Category" -> Icons.Default.Category
        "Folder" -> Icons.Default.Folder
        "Label" -> Icons.AutoMirrored.Filled.Label
        "Bookmark" -> Icons.Default.Bookmark
        "Tag" -> Icons.Default.Tag
        "Apps" -> Icons.Default.Apps
        
        // Personal & Identity
        "Person" -> Icons.Default.Person
        "PersonOutline" -> Icons.Default.PersonOutline
        "AccountCircle" -> Icons.Default.AccountCircle
        "Badge" -> Icons.Default.Badge
        "ContactPage" -> Icons.Default.ContactPage
        "Fingerprint" -> Icons.Default.Fingerprint
        
        // Business & Work
        "Business" -> Icons.Default.Business
        "Work" -> Icons.Default.Work
        "BusinessCenter" -> Icons.Default.BusinessCenter
        "CorporateFare" -> Icons.Default.CorporateFare
        "Domain" -> Icons.Default.Domain
        "Apartment" -> Icons.Default.Apartment
        
        // Finance & Banking
        "AccountBalance" -> Icons.Default.AccountBalance
        "AttachMoney" -> Icons.Default.AttachMoney
        "CreditCard" -> Icons.Default.CreditCard
        "Payment" -> Icons.Default.Payment
        "AccountBalanceWallet" -> Icons.Default.AccountBalanceWallet
        "MonetizationOn" -> Icons.Default.MonetizationOn
        
        // Shopping & Retail
        "ShoppingCart" -> Icons.Default.ShoppingCart
        "ShoppingBag" -> Icons.Default.ShoppingBag
        "Store" -> Icons.Default.Store
        "Storefront" -> Icons.Default.Storefront
        "LocalMall" -> Icons.Default.LocalMall
        "Receipt" -> Icons.Default.Receipt
        
        // Food & Dining
        "Restaurant" -> Icons.Default.Restaurant
        "LocalDining" -> Icons.Default.LocalDining
        "LocalCafe" -> Icons.Default.LocalCafe
        "LocalBar" -> Icons.Default.LocalBar
        "LocalPizza" -> Icons.Default.LocalPizza
        "Fastfood" -> Icons.Default.Fastfood
        
        // Transportation
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "DirectionsBus" -> Icons.Default.DirectionsBus
        "DirectionsTransit" -> Icons.Default.DirectionsTransit
        "Flight" -> Icons.Default.Flight
        "Train" -> Icons.Default.Train
        "LocalTaxi" -> Icons.Default.LocalTaxi
        
        // Health & Medical
        "LocalHospital" -> Icons.Default.LocalHospital
        "LocalPharmacy" -> Icons.Default.LocalPharmacy
        "MedicalServices" -> Icons.Default.MedicalServices
        "HealthAndSafety" -> Icons.Default.HealthAndSafety
        "Healing" -> Icons.Default.Healing
        "Vaccines" -> Icons.Default.Vaccines
        
        // Entertainment & Leisure
        "Movie" -> Icons.Default.Movie
        "TheaterComedy" -> Icons.Default.TheaterComedy
        "MusicNote" -> Icons.Default.MusicNote
        "SportsEsports" -> Icons.Default.SportsEsports
        "Casino" -> Icons.Default.Casino
        "Celebration" -> Icons.Default.Celebration
        
        // Education & Learning
        "School" -> Icons.Default.School
        "MenuBook" -> Icons.AutoMirrored.Filled.MenuBook
        "LibraryBooks" -> Icons.AutoMirrored.Filled.LibraryBooks
        "Science" -> Icons.Default.Science
        "Psychology" -> Icons.Default.Psychology
        "AutoStories" -> Icons.Default.AutoStories
        
        // Sports & Fitness
        "FitnessCenter" -> Icons.Default.FitnessCenter
        "SportsBasketball" -> Icons.Default.SportsBasketball
        "SportsFootball" -> Icons.Default.SportsFootball
        "SportsTennis" -> Icons.Default.SportsTennis
        "Pool" -> Icons.Default.Pool
        "DirectionsRun" -> Icons.AutoMirrored.Filled.DirectionsRun
        
        // Travel & Tourism
        "TravelExplore" -> Icons.Default.TravelExplore
        "Luggage" -> Icons.Default.Luggage
        "Hotel" -> Icons.Default.Hotel
        "BeachAccess" -> Icons.Default.BeachAccess
        "Landscape" -> Icons.Default.Landscape
        "Map" -> Icons.Default.Map
        
        // Technology & Communication
        "Computer" -> Icons.Default.Computer
        "PhoneAndroid" -> Icons.Default.PhoneAndroid
        "Wifi" -> Icons.Default.Wifi
        "Router" -> Icons.Default.Router
        "Cable" -> Icons.Default.Cable
        "Bluetooth" -> Icons.Default.Bluetooth
        
        // Home & Utilities
        "Home" -> Icons.Default.Home
        "ElectricalServices" -> Icons.Default.ElectricalServices
        "Plumbing" -> Icons.Default.Plumbing
        "Build" -> Icons.Default.Build
        "Construction" -> Icons.Default.Construction
        "CleaningServices" -> Icons.Default.CleaningServices
        
        // Security & Safety
        "Security" -> Icons.Default.Security
        "Lock" -> Icons.Default.Lock
        "Shield" -> Icons.Default.Shield
        "VerifiedUser" -> Icons.Default.VerifiedUser
        "AdminPanelSettings" -> Icons.Default.AdminPanelSettings
        "GppGood" -> Icons.Default.GppGood
        
        // Membership & Loyalty
        "CardMembership" -> Icons.Default.CardMembership
        "Stars" -> Icons.Default.Stars
        "Grade" -> Icons.Default.Grade
        "EmojiEvents" -> Icons.Default.EmojiEvents
        "Loyalty" -> Icons.Default.Loyalty
        "CardGiftcard" -> Icons.Default.CardGiftcard
        
        // Miscellaneous
        "Pets" -> Icons.Default.Pets
        "LocalFlorist" -> Icons.Default.LocalFlorist
        "Park" -> Icons.Default.Park
        "Nature" -> Icons.Default.Nature
        "WbSunny" -> Icons.Default.WbSunny
        "Nightlight" -> Icons.Default.Nightlight
        
        else -> null
    }
}

/**
 * Helper function to get icon name from ImageVector
 */
fun getIconName(icon: ImageVector): String {
    return when (icon) {
        // General
        Icons.Default.Category -> "Category"
        Icons.Default.Folder -> "Folder"
        Icons.AutoMirrored.Filled.Label -> "Label"
        Icons.Default.Bookmark -> "Bookmark"
        Icons.Default.Tag -> "Tag"
        Icons.Default.Apps -> "Apps"
        
        // Personal & Identity
        Icons.Default.Person -> "Person"
        Icons.Default.PersonOutline -> "PersonOutline"
        Icons.Default.AccountCircle -> "AccountCircle"
        Icons.Default.Badge -> "Badge"
        Icons.Default.ContactPage -> "ContactPage"
        Icons.Default.Fingerprint -> "Fingerprint"
        
        // Business & Work
        Icons.Default.Business -> "Business"
        Icons.Default.Work -> "Work"
        Icons.Default.BusinessCenter -> "BusinessCenter"
        Icons.Default.CorporateFare -> "CorporateFare"
        Icons.Default.Domain -> "Domain"
        Icons.Default.Apartment -> "Apartment"
        
        // Finance & Banking
        Icons.Default.AccountBalance -> "AccountBalance"
        Icons.Default.AttachMoney -> "AttachMoney"
        Icons.Default.CreditCard -> "CreditCard"
        Icons.Default.Payment -> "Payment"
        Icons.Default.AccountBalanceWallet -> "AccountBalanceWallet"
        Icons.Default.MonetizationOn -> "MonetizationOn"
        
        // Shopping & Retail
        Icons.Default.ShoppingCart -> "ShoppingCart"
        Icons.Default.ShoppingBag -> "ShoppingBag"
        Icons.Default.Store -> "Store"
        Icons.Default.Storefront -> "Storefront"
        Icons.Default.LocalMall -> "LocalMall"
        Icons.Default.Receipt -> "Receipt"
        
        // Food & Dining
        Icons.Default.Restaurant -> "Restaurant"
        Icons.Default.LocalDining -> "LocalDining"
        Icons.Default.LocalCafe -> "LocalCafe"
        Icons.Default.LocalBar -> "LocalBar"
        Icons.Default.LocalPizza -> "LocalPizza"
        Icons.Default.Fastfood -> "Fastfood"
        
        // Transportation
        Icons.Default.DirectionsCar -> "DirectionsCar"
        Icons.Default.DirectionsBus -> "DirectionsBus"
        Icons.Default.DirectionsTransit -> "DirectionsTransit"
        Icons.Default.Flight -> "Flight"
        Icons.Default.Train -> "Train"
        Icons.Default.LocalTaxi -> "LocalTaxi"
        
        // Health & Medical
        Icons.Default.LocalHospital -> "LocalHospital"
        Icons.Default.LocalPharmacy -> "LocalPharmacy"
        Icons.Default.MedicalServices -> "MedicalServices"
        Icons.Default.HealthAndSafety -> "HealthAndSafety"
        Icons.Default.Healing -> "Healing"
        Icons.Default.Vaccines -> "Vaccines"
        
        // Entertainment & Leisure
        Icons.Default.Movie -> "Movie"
        Icons.Default.TheaterComedy -> "TheaterComedy"
        Icons.Default.MusicNote -> "MusicNote"
        Icons.Default.SportsEsports -> "SportsEsports"
        Icons.Default.Casino -> "Casino"
        Icons.Default.Celebration -> "Celebration"
        
        // Education & Learning
        Icons.Default.School -> "School"
        Icons.AutoMirrored.Filled.MenuBook -> "MenuBook"
        Icons.AutoMirrored.Filled.LibraryBooks -> "LibraryBooks"
        Icons.Default.Science -> "Science"
        Icons.Default.Psychology -> "Psychology"
        Icons.Default.AutoStories -> "AutoStories"
        
        // Sports & Fitness
        Icons.Default.FitnessCenter -> "FitnessCenter"
        Icons.Default.SportsBasketball -> "SportsBasketball"
        Icons.Default.SportsFootball -> "SportsFootball"
        Icons.Default.SportsTennis -> "SportsTennis"
        Icons.Default.Pool -> "Pool"
        Icons.Default.DirectionsRun -> "DirectionsRun"
        
        // Travel & Tourism
        Icons.Default.TravelExplore -> "TravelExplore"
        Icons.Default.Luggage -> "Luggage"
        Icons.Default.Hotel -> "Hotel"
        Icons.Default.BeachAccess -> "BeachAccess"
        Icons.Default.Landscape -> "Landscape"
        Icons.Default.Map -> "Map"
        
        // Technology & Communication
        Icons.Default.Computer -> "Computer"
        Icons.Default.PhoneAndroid -> "PhoneAndroid"
        Icons.Default.Wifi -> "Wifi"
        Icons.Default.Router -> "Router"
        Icons.Default.Cable -> "Cable"
        Icons.Default.Bluetooth -> "Bluetooth"
        
        // Home & Utilities
        Icons.Default.Home -> "Home"
        Icons.Default.ElectricalServices -> "ElectricalServices"
        Icons.Default.Plumbing -> "Plumbing"
        Icons.Default.Build -> "Build"
        Icons.Default.Construction -> "Construction"
        Icons.Default.CleaningServices -> "CleaningServices"
        
        // Security & Safety
        Icons.Default.Security -> "Security"
        Icons.Default.Lock -> "Lock"
        Icons.Default.Shield -> "Shield"
        Icons.Default.VerifiedUser -> "VerifiedUser"
        Icons.Default.AdminPanelSettings -> "AdminPanelSettings"
        Icons.Default.GppGood -> "GppGood"
        
        // Membership & Loyalty
        Icons.Default.CardMembership -> "CardMembership"
        Icons.Default.Stars -> "Stars"
        Icons.Default.Grade -> "Grade"
        Icons.Default.EmojiEvents -> "EmojiEvents"
        Icons.Default.Loyalty -> "Loyalty"
        Icons.Default.CardGiftcard -> "CardGiftcard"
        
        // Miscellaneous
        Icons.Default.Pets -> "Pets"
        Icons.Default.LocalFlorist -> "LocalFlorist"
        Icons.Default.Park -> "Park"
        Icons.Default.Nature -> "Nature"
        Icons.Default.WbSunny -> "WbSunny"
        Icons.Default.Nightlight -> "Nightlight"
        
        else -> "Category"
    }
}