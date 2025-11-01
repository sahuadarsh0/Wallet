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
private val iconMap = mapOf(
    // General
    "Category" to Icons.Default.Category,
    "Folder" to Icons.Default.Folder,
    "Label" to Icons.AutoMirrored.Filled.Label,
    "Bookmark" to Icons.Default.Bookmark,
    "Tag" to Icons.Default.Tag,
    "Apps" to Icons.Default.Apps,

    // Personal & Identity
    "Person" to Icons.Default.Person,
    "PersonOutline" to Icons.Default.PersonOutline,
    "AccountCircle" to Icons.Default.AccountCircle,
    "Badge" to Icons.Default.Badge,
    "ContactPage" to Icons.Default.ContactPage,
    "Fingerprint" to Icons.Default.Fingerprint,

    // Business & Work
    "Business" to Icons.Default.Business,
    "Work" to Icons.Default.Work,
    "BusinessCenter" to Icons.Default.BusinessCenter,
    "CorporateFare" to Icons.Default.CorporateFare,
    "Domain" to Icons.Default.Domain,
    "Apartment" to Icons.Default.Apartment,

    // Finance & Banking
    "AccountBalance" to Icons.Default.AccountBalance,
    "AttachMoney" to Icons.Default.AttachMoney,
    "CreditCard" to Icons.Default.CreditCard,
    "Payment" to Icons.Default.Payment,
    "AccountBalanceWallet" to Icons.Default.AccountBalanceWallet,
    "MonetizationOn" to Icons.Default.MonetizationOn,

    // Shopping & Retail
    "ShoppingCart" to Icons.Default.ShoppingCart,
    "ShoppingBag" to Icons.Default.ShoppingBag,
    "Store" to Icons.Default.Store,
    "Storefront" to Icons.Default.Storefront,
    "LocalMall" to Icons.Default.LocalMall,
    "Receipt" to Icons.Default.Receipt,

    // Food & Dining
    "Restaurant" to Icons.Default.Restaurant,
    "LocalDining" to Icons.Default.LocalDining,
    "LocalCafe" to Icons.Default.LocalCafe,
    "LocalBar" to Icons.Default.LocalBar,
    "LocalPizza" to Icons.Default.LocalPizza,
    "Fastfood" to Icons.Default.Fastfood,

    // Transportation
    "DirectionsCar" to Icons.Default.DirectionsCar,
    "DirectionsBus" to Icons.Default.DirectionsBus,
    "DirectionsTransit" to Icons.Default.DirectionsTransit,
    "Flight" to Icons.Default.Flight,
    "Train" to Icons.Default.Train,
    "LocalTaxi" to Icons.Default.LocalTaxi,

    // Health & Medical
    "LocalHospital" to Icons.Default.LocalHospital,
    "LocalPharmacy" to Icons.Default.LocalPharmacy,
    "MedicalServices" to Icons.Default.MedicalServices,
    "HealthAndSafety" to Icons.Default.HealthAndSafety,
    "Healing" to Icons.Default.Healing,
    "Vaccines" to Icons.Default.Vaccines,

    // Entertainment & Leisure
    "Movie" to Icons.Default.Movie,
    "TheaterComedy" to Icons.Default.TheaterComedy,
    "MusicNote" to Icons.Default.MusicNote,
    "SportsEsports" to Icons.Default.SportsEsports,
    "Casino" to Icons.Default.Casino,
    "Celebration" to Icons.Default.Celebration,

    // Education & Learning
    "School" to Icons.Default.School,
    "MenuBook" to Icons.AutoMirrored.Filled.MenuBook,
    "LibraryBooks" to Icons.AutoMirrored.Filled.LibraryBooks,
    "Science" to Icons.Default.Science,
    "Psychology" to Icons.Default.Psychology,
    "AutoStories" to Icons.Default.AutoStories,

    // Sports & Fitness
    "FitnessCenter" to Icons.Default.FitnessCenter,
    "SportsBasketball" to Icons.Default.SportsBasketball,
    "SportsFootball" to Icons.Default.SportsFootball,
    "SportsTennis" to Icons.Default.SportsTennis,
    "Pool" to Icons.Default.Pool,
    "DirectionsRun" to Icons.AutoMirrored.Filled.DirectionsRun,

    // Travel & Tourism
    "TravelExplore" to Icons.Default.TravelExplore,
    "Luggage" to Icons.Default.Luggage,
    "Hotel" to Icons.Default.Hotel,
    "BeachAccess" to Icons.Default.BeachAccess,
    "Landscape" to Icons.Default.Landscape,
    "Map" to Icons.Default.Map,

    // Technology & Communication
    "Computer" to Icons.Default.Computer,
    "PhoneAndroid" to Icons.Default.PhoneAndroid,
    "Wifi" to Icons.Default.Wifi,
    "Router" to Icons.Default.Router,
    "Cable" to Icons.Default.Cable,
    "Bluetooth" to Icons.Default.Bluetooth,

    // Home & Utilities
    "Home" to Icons.Default.Home,
    "ElectricalServices" to Icons.Default.ElectricalServices,
    "Plumbing" to Icons.Default.Plumbing,
    "Build" to Icons.Default.Build,
    "Construction" to Icons.Default.Construction,
    "CleaningServices" to Icons.Default.CleaningServices,

    // Security & Safety
    "Security" to Icons.Default.Security,
    "Lock" to Icons.Default.Lock,
    "Shield" to Icons.Default.Shield,
    "VerifiedUser" to Icons.Default.VerifiedUser,
    "AdminPanelSettings" to Icons.Default.AdminPanelSettings,
    "GppGood" to Icons.Default.GppGood,

    // Membership & Loyalty
    "CardMembership" to Icons.Default.CardMembership,
    "Stars" to Icons.Default.Stars,
    "Grade" to Icons.Default.Grade,
    "EmojiEvents" to Icons.Default.EmojiEvents,
    "Loyalty" to Icons.Default.Loyalty,
    "CardGiftcard" to Icons.Default.CardGiftcard,

    // Miscellaneous
    "Pets" to Icons.Default.Pets,
    "LocalFlorist" to Icons.Default.LocalFlorist,
    "Park" to Icons.Default.Park,
    "Nature" to Icons.Default.Nature,
    "WbSunny" to Icons.Default.WbSunny,
    "Nightlight" to Icons.Default.Nightlight,
)

fun getIconFromName(iconName: String?): ImageVector? {
    return iconMap[iconName]
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