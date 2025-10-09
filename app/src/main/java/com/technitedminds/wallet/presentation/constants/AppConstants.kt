package com.technitedminds.wallet.presentation.constants

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Application-wide constants to avoid hardcoded values
 */
object AppConstants {
    
    // Navigation Labels
    object NavigationLabels {
        const val HOME = "Home"
        const val CATEGORIES = "Categories"
        const val SETTINGS = "Settings"
        const val ADD_CARD = "Add Card"
        const val CAMERA = "Camera"
        const val CARD_DETAIL = "Card Detail"
        const val EDIT_CARD = "Edit Card"
    }
    
    // Theme Labels
    object ThemeLabels {
        const val LIGHT = "Light"
        const val DARK = "Dark"
        const val SYSTEM_DEFAULT = "System Default"
        const val LIGHT_DESCRIPTION = "Always use light theme"
        const val DARK_DESCRIPTION = "Always use dark theme"
        const val SYSTEM_DESCRIPTION = "Follow system setting"
    }
    
    // UI Dimensions
    object Dimensions {
        // Icon sizes
        val ICON_SIZE_SMALL = 16.dp
        val ICON_SIZE_MEDIUM = 20.dp
        val ICON_SIZE_LARGE = 24.dp
        val ICON_SIZE_EXTRA_LARGE = 32.dp
        
        // Card dimensions
        val CARD_THUMBNAIL_COMPACT = 48.dp
        val CARD_THUMBNAIL_NORMAL = 56.dp
        val CARD_ELEVATION_DEFAULT = 2.dp
        val CARD_ELEVATION_SELECTED = 8.dp
        
        // Spacing
        val SPACING_EXTRA_SMALL = 4.dp
        val SPACING_SMALL = 8.dp
        val SPACING_MEDIUM = 12.dp
        val SPACING_LARGE = 16.dp
        val SPACING_EXTRA_LARGE = 24.dp
        
        // Padding
        val PADDING_SMALL = 8.dp
        val PADDING_MEDIUM = 12.dp
        val PADDING_LARGE = 16.dp
        val PADDING_EXTRA_LARGE = 24.dp
        
        // Color picker
        val COLOR_PICKER_ITEM_SIZE = 40.dp
        val COLOR_PICKER_SELECTED_SIZE = 32.dp
        val COLOR_PICKER_UNSELECTED_SIZE = 36.dp
        val COLOR_PICKER_BORDER_WIDTH = 3.dp
        val COLOR_PICKER_GRID_COLUMNS = 6
        val COLOR_PICKER_HEIGHT = 120.dp
        
        // Image optimization
        val IMAGE_THUMBNAIL_SIZE = 150 // pixels
        val IMAGE_CARD_PREVIEW_SIZE = 400 // pixels
        val IMAGE_FULL_SIZE_LIMIT = 1024 // pixels
        val IMAGE_MAX_WIDTH = 400.dp
        val IMAGE_MAX_HEIGHT = 250.dp
    }
    
    // Animation Constants
    object Animation {
        const val CARD_FLIP_DURATION = 300 // milliseconds
        const val COLOR_ANIMATION_DURATION = 200 // milliseconds
        const val DEBOUNCE_DELAY = 300L // milliseconds
        const val PERFORMANCE_FRAME_MULTIPLIER = 1000f
    }
    
    // Storage Constants
    object Storage {
        const val BYTES_PER_KB = 1024
        const val BYTES_PER_MB = 1024 * 1024
        const val BYTES_PER_GB = 1024 * 1024 * 1024
        const val STORAGE_PERCENTAGE_MULTIPLIER = 100f
        const val DECIMAL_FORMAT_PATTERN = "%.1f"
        
        // File size units
        val STORAGE_UNITS = arrayOf("B", "KB", "MB", "GB", "TB")
        
        // Cache and cleanup
        const val CACHE_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes
        const val SEARCH_DEBOUNCE_MS = 300L
        const val BATCH_SIZE = 50
    }
    
    // Card Processing Constants
    object CardProcessing {
        // Card number validation
        const val MIN_CARD_NUMBER_LENGTH = 13
        const val MAX_CARD_NUMBER_LENGTH = 19
        const val MIN_NAME_WORD_LENGTH = 2
        const val MIN_NAME_WORDS = 2
        const val MAX_NAME_WORDS = 3
        const val MIN_LINE_LENGTH = 1
        const val MIN_NAME_LENGTH = 5
        const val MAX_NAME_LENGTH = 30
        const val NAME_LETTER_RATIO = 0.7
        
        // Expiry date
        const val EXPIRY_YEAR_LENGTH = 4
        const val EXPIRY_SHORT_YEAR_LENGTH = 2
        
        // OCR field keys
        const val FIELD_CARD_NUMBER = "cardNumber"
        const val FIELD_EXPIRY_DATE = "expiryDate"
        const val FIELD_CARDHOLDER_NAME = "cardholderName"
        const val FIELD_CVV = "cvv"
        const val FIELD_BANK_NAME = "bankName"
        
        // Excluded words for name detection
        val EXCLUDED_WORDS = setOf(
            "BANK", "CARD", "CREDIT", "DEBIT", "VALID", "THRU", "EXPIRES", "EXP", "MEMBER", "SINCE",
            "VISA", "MASTERCARD", "AMERICAN", "EXPRESS", "DISCOVER", "PLATINUM", "GOLD", "CLASSIC"
        )
        
        // Regex patterns
        const val CARD_NUMBER_PATTERN_16 = """(\d{4}[\s\-\.]?\d{4}[\s\-\.]?\d{4}[\s\-\.]?\d{4})"""
        const val CARD_NUMBER_PATTERN_SPACES = """(\b\d{4}\s*\d{4}\s*\d{4}\s*\d{4}\b)"""
        const val CARD_NUMBER_PATTERN_CONTINUOUS_16 = """(\b\d{16}\b)"""
        const val CARD_NUMBER_PATTERN_15 = """(\d{4}[\s\-\.]?\d{6}[\s\-\.]?\d{5})"""
        const val CARD_NUMBER_PATTERN_CONTINUOUS_15 = """(\b\d{15}\b)"""
        const val CARD_NUMBER_PATTERN_13 = """(\b\d{13}\b)"""
        const val CARD_NUMBER_PATTERN_14 = """(\b\d{14}\b)"""
        
        const val EXPIRY_PATTERN_SHORT = """(\d{2}/\d{2})"""
        const val EXPIRY_PATTERN_LONG = """(\d{2}/\d{4})"""
        const val EXPIRY_PATTERN_VALID_THRU = """(VALID\s+THRU\s+\d{2}/\d{2})"""
        const val EXPIRY_PATTERN_EXP = """(EXP\s*:?\s*\d{2}/\d{2})"""
        
        const val DATE_EXTRACT_PATTERN = """(\d{2}/\d{2,4})"""
        const val CLEAN_TEXT_PATTERN = """[^\w\s/\-.]"""
        const val WHITESPACE_PATTERN = """\s+"""
        const val CARD_NUMBER_CLEAN_PATTERN = """[\s\-\.]"""
        const val CVV_PATTERN = """(\b\d{3,4}\b)"""
    }
    
    // Dialog and UI Text Constants
    object DialogText {
        const val PRIVACY_POLICY_TITLE = "Privacy Policy"
        const val OPEN_SOURCE_LICENSES_TITLE = "Open Source Licenses"
        const val CLEAN_UP_STORAGE_TITLE = "Clean Up Storage"
        const val RESET_CATEGORIES_TITLE = "Reset Default Categories"
        
        const val CLOSE_BUTTON = "Close"
        const val OK_BUTTON = "OK"
        const val CANCEL_BUTTON = "Cancel"
        const val DELETE_BUTTON = "Delete"
        const val RESET_BUTTON = "Reset"
        const val CLEAN_UP_BUTTON = "Clean Up"
        const val CLEANING_BUTTON = "Cleaning..."
        const val DISMISS_BUTTON = "Dismiss"
        
        // Privacy policy content
        const val PRIVACY_POLICY_CONTENT = """CardVault Privacy Policy

• CardVault is completely offline - no data is transmitted over the network
• All card data is stored locally on your device in encrypted format
• No analytics, tracking, or data collection is performed
• Camera permission is used only for card scanning
• Storage permission is used only for backup/restore functionality
• Your data never leaves your device unless you explicitly export it

For questions, contact: privacy@cardvault.app"""
        
        // Open source licenses content
        const val OPEN_SOURCE_LICENSES_CONTENT = """CardVault uses the following open source libraries:

• Jetpack Compose - Apache License 2.0
• CameraX - Apache License 2.0
• ML Kit - Google Terms of Service
• Room Database - Apache License 2.0
• Hilt - Apache License 2.0
• Coil - Apache License 2.0
• Proto DataStore - Apache License 2.0
• Google Tink - Apache License 2.0

Full license texts are available in the app's source code."""
        
        // Cleanup dialog content
        const val CLEANUP_DIALOG_CONTENT = """This will perform comprehensive storage cleanup including:
• Remove orphaned image files
• Clear temporary and cache files
• Optimize database
• Clean old log files

This action cannot be undone."""
        
        // Reset categories dialog content
        const val RESET_CATEGORIES_CONTENT = "This will restore the original category set. Your custom categories will be preserved."
    }
    
    // Statistics Labels
    object StatisticsLabels {
        const val TOTAL_CARDS = "Total Cards"
        const val CATEGORIES = "Categories"
        const val STORAGE_USED = "Storage Used"
        const val AVAILABLE_STORAGE = "Available Storage"
        const val ORPHANED_FILES = "Orphaned Files"
        const val LAST_CLEANUP = "Last Cleanup"
        const val VERSION = "Version"
        const val BUILD = "Build"
        const val UNKNOWN = "Unknown"
        const val MB_UNIT = "MB"
        
        // Section titles
        const val APP_STATISTICS = "App Statistics"
        const val APPEARANCE = "Appearance"
        const val CATEGORY_MANAGEMENT = "Category Management"
        const val STORAGE_MANAGEMENT = "Storage Management"
        const val APP_INFORMATION = "App Information"
    }
    
    // Error Messages
    object ErrorMessages {
        const val ERROR_PREFIX = "Error: "
        const val CLEANUP_SUCCESS = "Cleanup completed successfully!"
        const val STORAGE_LOW_WARNING = "Storage space is running low"
    }
    
    // Content Descriptions
    object ContentDescriptions {
        const val BACK = "Back"
        const val SELECTED = "Selected"
        const val RESTORE_DEFAULTS = "Restore defaults"
        const val ADD_CARD = "Add card"
        const val CARD_THUMBNAIL = "Card thumbnail"
        const val WARNING_ICON = "Warning"
        const val CARD_OPTIONS = "Card options"
    }
    
    // UI Text Constants
    object UIText {
        const val RESET_DEFAULT_CATEGORIES_TITLE = "Reset Default Categories"
        const val RESET_DEFAULT_CATEGORIES_SUBTITLE = "Restore original category set"
        const val CLEAN_UP_STORAGE_TITLE = "Clean Up Storage"
        const val CLEAN_UP_STORAGE_SUBTITLE_WITH_FILES = "Remove %d orphaned files and optimize storage"
        const val CLEAN_UP_STORAGE_SUBTITLE_DEFAULT = "Optimize storage and clean temporary files"
        const val PRIVACY_POLICY_SUBTITLE = "View our privacy policy"
        const val OPEN_SOURCE_LICENSES_SUBTITLE = "View third-party licenses"
        const val PREVIEW_TEXT = "Preview"
        const val CARD_NUMBER_MASKED = "•••• %s"
    }
    
    // Default Values
    object Defaults {
        const val DEFAULT_CARD_TYPE = "Credit"
        const val CREDIT_CARD_ASPECT_RATIO = 1.586f
        const val CARD_ICON_SIZE_RATIO = 0.5f
        const val BUTTON_WIDTH_RATIO = 0.6f
    }
    
    // Animation Values
    object AnimationValues {
        const val ALPHA_DISABLED = 0.2f
        const val ALPHA_SECONDARY = 0.3f
        const val ALPHA_MEDIUM = 0.5f
        const val ALPHA_HIGH = 0.6f
        const val ALPHA_VERY_HIGH = 0.7f
        const val ALPHA_NEAR_OPAQUE = 0.8f
        const val ALPHA_ALMOST_OPAQUE = 0.9f
        
        const val SCALE_PRESSED = 0.95f
        const val SCALE_CAPTURING = 0.9f
        const val SCALE_PROCESSING = 0.95f
        const val SCALE_PULSE_MIN = 0.8f
        const val SCALE_PULSE_MAX = 1.2f
        const val SCALE_PULSE_BUTTON_MAX = 1.1f
        
        const val LUMINANCE_THRESHOLD = 0.5
        const val LUMINANCE_RED_WEIGHT = 0.299
        const val LUMINANCE_GREEN_WEIGHT = 0.587
        const val LUMINANCE_BLUE_WEIGHT = 0.114
    }
    
    // Class Names and Reflection
    object ClassNames {
        const val BUILD_CONFIG_CLASS = "com.technitedminds.wallet.BuildConfig"
        const val BUILD_TYPE_FIELD = "BUILD_TYPE"
    }
}