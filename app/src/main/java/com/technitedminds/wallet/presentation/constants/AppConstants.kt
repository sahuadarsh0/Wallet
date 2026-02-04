package com.technitedminds.wallet.presentation.constants

import androidx.compose.ui.unit.dp

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

        // Corner radius
        val CORNER_RADIUS_COMPACT = 8.dp
        val CORNER_RADIUS_NORMAL = 12.dp
        val CORNER_RADIUS_LARGE = 16.dp

        // Category Screen
        val CATEGORY_ITEM_ICON_SIZE = 56.dp
        val CATEGORY_ITEM_ICON_INNER_SIZE = 28.dp
        val CATEGORY_ACTION_BUTTON_PADDING = 96.dp

        // Empty State
        val EMPTY_STATE_ICON_SIZE = 64.dp
        val EMPTY_STATE_BUTTON_ICON_SIZE = 18.dp
        
        // Camera Capture
        val CAMERA_ICON_SIZE_LARGE = 120.dp

        // Settings Screen
        val SETTINGS_ITEM_ICON_SIZE = 24.dp

        // Card List Item
        val CARD_LIST_ITEM_MENU_SIZE_COMPACT = 40.dp
        val CARD_LIST_ITEM_MENU_SIZE_NORMAL = 48.dp

        // Card Type Selector
        val CARD_TYPE_SELECTOR_BUTTON_HEIGHT = 32.dp
        val CARD_TYPE_OPTION_HEIGHT = 80.dp
        val CARD_TYPE_OPTION_ICON_CONTAINER_SIZE = 40.dp
        val CARD_TYPE_OPTION_BORDER_WIDTH = 1.dp
        val CARD_TYPE_OPTION_BORDER_WIDTH_SELECTED = 2.dp
        val GRADIENT_PREVIEW_HEIGHT = 100.dp

        // Color Picker
        val COLOR_PICKER_ITEM_SIZE_LARGE = 32.dp
        val COLOR_PICKER_ITEM_SIZE_SMALL = 26.dp
        val COLOR_PICKER_ITEM_SIZE_SELECTED = 30.dp
        val CUSTOM_COLOR_PREVIEW_HEIGHT = 80.dp

        // Dropdown
        val DROPDOWN_ICON_SIZE = 20.dp

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
        const val FLIP_ANIMATION_DURATION = 600 // milliseconds
        const val CAMERA_DISTANCE = 12f
        const val PRESS_DELAY = 100L
        const val HOVER_SCALE_DURATION = 200 // milliseconds
        const val EXIT_ANIMATION_DURATION = 200 // milliseconds
        const val SLIDE_IN_DURATION_BASE = 300 // milliseconds
        const val SLIDE_IN_DURATION_INCREMENT = 50 // milliseconds
        const val SHIMMER_ANIMATION_DURATION = 1200 // milliseconds
    }

    // Loading Messages
    object LoadingMessages {
        const val LOADING_CARDS = "Loading cards..."
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
            "VISA", "MASTERCARD", "AMERICAN", "EXPRESS", "DISCOVER", "PLATINUM", "GOLD", "CLASSIC",
            "CHASE", "WELLS", "FARGO", "CITI", "CAPITAL", "ONE", "UNION", "FEDERAL", "TRUST", "SAVINGS",
            "REWARDS", "CASH", "BACK", "MILES", "POINTS", "SIGNATURE", "WORLD", "ELITE", "PREFERRED",
            "BUSINESS", "CORPORATE", "STUDENT", "SECURED", "PREPAID", "GIFT", "TRAVEL", "DINING",
            "ENTERTAINMENT", "GAS", "GROCERY", "DEPARTMENT", "STORE", "ONLINE", "SHOPPING", "MOBILE",
            "CONTACTLESS", "CHIP", "PIN", "MAGNETIC", "STRIPE", "SECURITY", "CODE", "CVV", "CVC",
            "CUSTOMER", "SERVICE", "PHONE", "NUMBER", "WEBSITE", "ADDRESS", "ZIP", "STATE", "AMRIT"
        )

        // Regex patterns
        const val CARD_NUMBER_PATTERN_16 = """(\d{4}[\s\-.]?\d{4}[\s\-.]?\d{4}[\s\-.]?\d{4})"""
        const val CARD_NUMBER_PATTERN_SPACES = """(\b\d{4}\s*\d{4}\s*\d{4}\s*\d{4}\b)"""
        const val CARD_NUMBER_PATTERN_CONTINUOUS_16 = """(\b\d{16}\b)"""
        const val CARD_NUMBER_PATTERN_15 = """(\d{4}[\s\-.]?\d{6}[\s\-.]?\d{5})"""
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
        const val CARD_NUMBER_CLEAN_PATTERN = """[\s\-.]"""
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
        const val CONFIRM_BUTTON = "Confirm"
        const val SAVE_BUTTON = "Save"
        const val EDIT_BUTTON = "Edit"

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
        const val CATEGORIES = "Categories"
        const val TOTAL_CARDS = "Total Cards"
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
        const val SAVE = "Save"
        const val EDIT = "Edit"
        const val CLEAR = "Clear"
        const val CLEAR_SEARCH = "Clear search"
        const val LIST_VIEW = "List view"
        const val GRID_VIEW = "Grid view"
        const val REFRESH = "Refresh"
        const val SHARE_BOTH_SIDES = "Share both sides"
        const val SHARE_FRONT = "Share front"
        const val SHARE_BACK = "Share back"
    }
    
    // UI Text
    object UIText {
        const val ACCESS_CARD_DESC = "Access and security cards"
        const val ADD_CARD_TYPE_PROMPT = "What type of card would you like to add?"
        const val ADD_CATEGORY = "Add category"
        const val ADD_DEFAULT_CATEGORIES = "Add Default Categories"
        const val ADD_FIELD = "Add Field"
        const val ADD_WATERMARK_LABEL = "Add watermark"
        const val ADDITIONAL_INFO_BACK = "Additional information on back"
        const val ADDITIONAL_INFORMATION = "Additional Information"
        const val ALL = "All"
        const val ALL_TYPES_LABEL = "All Types"
        const val APPEARANCE_SUBTITLE = "Card colors and gradient"
        const val APPEARANCE_TITLE = "Appearance"
        const val APPLY = "Apply"
        const val APP_TITLE = "CardVault"
        const val AUTO_DETECTED = "Auto-detected"
        const val BACK_SIDE_LABEL = "Back"
        const val BACK_SIDE_PREVIEW_TITLE = "Back Side Preview"
        const val BASIC_INFORMATION = "Basic Information"
        const val BASIC_INFORMATION_SUBTITLE = "Card name and category"
        const val BOTH_SIDES =  "Both"
        const val BUSINESS_CARD_DESC = "Business and corporate cards"
        const val CAMERA_ACCESS_DENIED_MESSAGE = "Camera permission is required to scan cards. Please enable camera access in your device settings to continue."
        const val CAMERA_ACCESS_DENIED_TITLE = "Camera Access Denied"
        const val CAMERA_DISABLED = "Camera has been disabled"
        const val CAMERA_DISABLED_SUGGESTION = "\n\nCheck your device settings to enable camera access."
        const val CAMERA_DISABLED_TITLE = "Camera Disabled"
        const val CAMERA_ERROR_TITLE = "Camera Error"
        const val CAMERA_IN_USE = "Camera is currently being used by another app"
        const val CAMERA_IN_USE_SUGGESTION = "\n\nTry closing other camera apps and try again."
        const val CAMERA_IN_USE_TITLE = "Camera In Use"
        const val CAMERA_PERMISSION_MESSAGE = "CardVault needs camera access to scan your cards. Your photos are stored locally and never shared."
        const val CAMERA_PERMISSION_REQUIRED_TITLE = "Camera Permission Required"
        const val CAMERA_UNAVAILABLE = "Camera is not available on this device"
        const val CAMERA_UNAVAILABLE_SUGGESTION = "\n\nThis device may not have a camera or it's not accessible."
        const val CAMERA_UNAVAILABLE_TITLE = "Camera Unavailable"
        const val CAPTURE_CARD_IMAGES_TITLE = "Capture Card Images"
        const val CAPTURE_CARD_PROMPT_SUBTITLE = "Take photos of both sides of your card for the best experience. We'll automatically extract the card information."
        const val CAPTURE_CARD_PROMPT_TITLE = "Capture Your %s"
        const val CAPTURE_FAILED_TITLE = "Capture Failed"
        const val CAPTURE_INSTRUCTIONS = "• Ensure good lighting\n• Keep the card flat\n• Fill the frame completely"
        const val CARDHOLDER_NAME_LABEL = "Cardholder Name"
        const val CARDHOLDER_NAME_PLACEHOLDER = "JOHN DOE"
        const val CARD_ADDED_DATE_LABEL = "Added %s"
        const val CARD_COLOR_LABEL = "Card Color"
        const val CARD_DETAILS_SUBTITLE = "Extracted card details"
        const val CARD_DETAILS_TITLE = "Card Details"
        const val CARD_IMAGE = "Card image"
        const val CARD_INFORMATION = "Card Information"
        const val CARD_NAME_LABEL = "Card Name"
        const val CARD_NAME_PLACEHOLDER = "My %s"
        const val CARD_NUMBER_FORMAT = "**** **** **** %s"
        const val CARD_NUMBER_LABEL = "Card Number"
        const val CARD_NUMBER_MASKED = "•••• %s"
        const val CARD_NUMBER_PLACEHOLDER = "1234 5678 9012 3456"
        const val CARD_PROTECTED_NOTICE = "This card is protected by advanced security features"
        const val CARD_TYPE_NAME = "Card Type Name"
        const val CARD_TYPE_NAME_PLACEHOLDER = "e.g., Gym Card, Voucher"
        const val CATEGORY_LABEL = "Category"
        const val CATEGORY_NAME_LABEL = "Category Name"
        const val CLEAN_UP_STORAGE_SUBTITLE_DEFAULT = "Optimize storage and clean temporary files"
        const val CLEAN_UP_STORAGE_SUBTITLE_WITH_FILES = "Remove %d orphaned files and optimize storage"
        const val CLEAN_UP_STORAGE_TITLE = "Clean Up Storage"
        const val CLEAR_ALL_AND_ENTER_MANUALLY = "Clear All & Enter Manually"
        const val CONTINUE = "Continue"
        const val CREATED_LABEL = "Created"
        const val CREATE = "Create"
        const val CREATE_CATEGORY = "Create Category"
        const val CREATE_CATEGORY_PROMPT = "Create categories to organize your cards better"
        const val CREATE_CUSTOM_CARD_TYPE = "Create Custom Card Type"
        const val CREDIT_CARD_DESC = "Credit and charge cards"
        const val CHOOSE_GRADIENT_TITLE = "Choose Gradient"
        const val CHOOSE_GRADIENT_SUBTITLE = "Customize your card's appearance"
        const val PRESETS_TAB = "Presets"
        const val CUSTOM_TAB = "Custom"
        const val CHOOSE_PRESET_GRADIENT = "Choose a preset gradient"
        const val CREATE_CUSTOM_GRADIENT = "Create your custom gradient"
        const val PRESET_GRADIENTS_DESC = "Preset gradients"
        const val CUSTOM_GRADIENT_DESC = "Custom gradient"
        const val CLOSE_DIALOG = "Close dialog"
        const val SELECTED_COLOR = "Selected color"
        const val CUSTOMIZE_CARD_GRADIENT = "Customize Card Gradient"
        const val CUSTOM_CARD_DESC = "Custom card type"
        const val CUSTOM_CARD_TYPE = "Custom Card Type"
        const val CUSTOM_COLOR = "Custom Color"
        const val CUSTOM_COLOR_FIELD = "customColor"
        const val CUSTOM_FIELDS_SUBTITLE = "Custom fields"
        const val CVV_LABEL = "CVV"
        const val CVV_PLACEHOLDER = "123"
        const val DEBIT_CARD_DESC = "Debit and bank cards"
        const val DELETE_CATEGORY = "Delete Category"
        const val DELETE_CATEGORY_CONFIRMATION = "Are you sure you want to delete \"%s\"? Cards in this category will be moved to the default category."
        const val DELETE_CATEGORY_CONTENT_DESC = "Delete category"
        const val DESCRIPTION_OPTIONAL_LABEL = "Description (Optional)"
        const val DISMISS_BUTTON_LABEL = "Dismiss"
        const val EDIT_CATEGORY = "Edit Category"
        const val EDIT_CATEGORY_CONTENT_DESC = "Edit category"
        const val EMPTY_STRING = ""
        const val END_COLOR = "End Color"
        const val ERROR_ICON = "Error"
        const val EVENT_TICKET_DESC = "Event tickets and passes"
        const val EXPIRY_DATE_LABEL = "Expiry Date"
        const val EXPIRY_DATE_PLACEHOLDER = "MM/YY"
        const val EXTRACTED_INFORMATION = "Extracted Information"
        const val FIELD_NAME_LABEL = "Field name"
        const val FLIP_INSTRUCTION = "Tap card to flip • Long press for share options"
        const val FRONT_SIDE_LABEL = "Front"
        const val FRONT_SIDE_PREVIEW_TITLE = "Front Side Preview"
        const val GIFT_CARD_DESC = "Gift cards and vouchers"
        const val GO_BACK = "Go Back"
        const val GRANT_CAMERA_PERMISSION_BUTTON = "Grant Camera Permission"
        const val GRADIENT = "Gradient"
        const val GRADIENT_DIRECTION = "Gradient Direction"
        const val GRADIENT_DIRECTION_TOP_TO_BOTTOM = "Top to Bottom"
        const val GRADIENT_DIRECTION_LEFT_TO_RIGHT = "Left to Right"
        const val GRADIENT_DIRECTION_DIAGONAL_DOWN = "Diagonal ↘"
        const val GRADIENT_DIRECTION_DIAGONAL_UP = "Diagonal ↙"
        const val GRADIENT_DIRECTION_ARROW_DOWN = "↓"
        const val GRADIENT_DIRECTION_ARROW_RIGHT = "→"
        const val GRADIENT_DIRECTION_ARROW_DIAGONAL_DOWN = "↘"
        const val GRADIENT_DIRECTION_ARROW_DIAGONAL_UP = "↙"
        const val GRADIENT_NAME = "Gradient Name"
        const val GRADIENT_NAME_OPTIONAL_LABEL = "Gradient Name (Optional)"
        const val GRADIENT_NAME_PLACEHOLDER = "My Custom Gradient"
        const val HEX_CODE_INSTRUCTION = "Enter 6-digit hex code (e.g., FF5722)"
        const val HEX_COLOR_CODE = "Hex Color Code"
        const val HEX_COLOR_CODE_PLACEHOLDER = "FF5722"
        const val HEX_COLOR_LABEL = "Hex Color"
        const val HOTEL_CARD_DESC = "Hotel and travel cards"
        const val ID_CARD_DESC = "ID cards and licenses"
        const val IMAGE_CAPTURE_ERROR = "Failed to capture image: %s"
        const val IMAGE_CAPTURE_ERROR_SUGGESTION = "\n\nMake sure you have enough storage space and try again."
        const val IMAGE_NOT_FOUND_LABEL = "Image Not Found"
        const val IMAGE_PREVIEW_TITLE = "Image Preview"
        const val IMAGE_QUALITY_LABEL = "Image quality"
        const val IMAGE_SIZE_LABEL = "Size: %d KB"
        const val INCLUDE_SENSITIVE_INFO_LABEL = "Include sensitive info"
        const val INCLUDE_SENSITIVE_INFO_SUBTITLE = "CVV, full card numbers, etc."
        const val INSURANCE_CARD_DESC = "Health and insurance cards"
        const val INVALID_HEX_COLOR = "Invalid hex color code"
        const val LIBRARY_CARD_DESC = "Library and education cards"
        const val LOADING_CARD_DETAIL = "Loading card..."
        const val LOYALTY_CARD_DESC = "Loyalty and rewards cards"
        const val MANUAL_ENTRY_SUBTITLE = "Please enter your %s details manually"
        const val MANUAL_ENTRY_TITLE = "Manual Entry"
        const val MANUAL_ENTRY_REQUIRED = "Manual Entry Required"
        const val MASKED_CVV = "***"
        const val MEMBERSHIP_CARD_DESC = "Gym and club memberships"
        const val NAME_LABEL = "Name"
        const val NOTES_FIELD_LABEL = "notes"
        const val NO_IMAGE_LABEL = "No %s Image"
        const val NO_CATEGORIES = "No Categories"
        const val OPEN_CAMERA = "Open Camera"
        const val OPEN_SETTINGS_BUTTON = "Open Settings"
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
        const val OPEN_SOURCE_LICENSES_SUBTITLE = "View third-party licenses"
        const val OPEN_SOURCE_LICENSES_TITLE = "Open Source Licenses"
        const val PASSWORD_FIELD = "password"
        const val PIN_FIELD = "pin"
        const val POSITION_BACK_OF_CARD_PROMPT = "Position the back of your %s"
        const val POSITION_FRONT_OF_CARD_PROMPT = "Position the front of your %s"
        const val PRESET_GRADIENT = "Preset Gradient"
        const val PRESET_GRADIENTS = "Preset Gradients"
        const val PREVIEW_TEXT = "Preview"
        const val PRIVACY_AND_SECURITY = "Privacy & Security"
        const val PRIVACY_NOTICE = "• All data stays on your device\n• No information is sent to servers\n• You can edit or clear any field"
        const val PRIVACY_POLICY_CONTENT = """CardVault Privacy Policy

• CardVault is completely offline - no data is transmitted over the network
• All card data is stored locally on your device in encrypted format
• No analytics, tracking, or data collection is performed
• Camera permission is used only for card scanning
• Storage permission is used only for backup/restore functionality
• Your data never leaves your device unless you explicitly export it

For questions, contact: privacy@cardvault.app"""
        const val PRIVACY_POLICY_SUBTITLE = "View our privacy policy"
        const val PRIVACY_POLICY_TITLE = "Privacy Policy"
        const val PROCESSING = "Processing..."
        const val PROCESSING_CARD_IMAGES_MESSAGE = "Processing card images..."
        const val PROCESSING_CARD_IMAGES_WITH_OCR_MESSAGE = "Processing card images and extracting text..."
        const val PROCESSING_TAKES_A_FEW_MOMENTS_MESSAGE = "This may take a few moments"
        const val PROTECT_SHARED_IMAGES_SUBTITLE = "Protect shared images"
        const val QUICK_SHARE_INSTRUCTION = "Use share buttons for quick sharing"
        const val REMOVE_CATEGORY = "Remove category"
        const val REMOVE_FIELD = "Remove field"
        const val RESET_BUTTON = "Reset"
        const val RESET_CATEGORIES_CONTENT = "This will restore the original category set. Your custom categories will be preserved."
        const val RESET_DEFAULT_CATEGORIES_SUBTITLE = "Restore original category set"
        const val RESET_DEFAULT_CATEGORIES_TITLE = "Reset Default Categories"
        const val RETAKE_BUTTON_LABEL = "Retake"
        const val SAVE_BUTTON = "Save"
        const val SAVE_CARD = "Save Card"
        const val SAVE_CHANGES_REMINDER = "Remember to save your changes before leaving"
        const val SAVING_CARD = "Saving card..."
        const val SEARCH_CARDS_PLACEHOLDER = "Search your cards..."
        const val SECURE_WATERMARK = "SECURE"
        const val SELECT_CARD_TYPE_TITLE = "Select Card Type"
        const val SHARING_OPTIONS_LABEL = "Sharing options:"
        const val SHARE_BOTH_SIDES =  "Both Sides"
        const val SHARE_FRONT_ONLY = "Front Only"
        const val SHARE_INFO_ONLY = "Info Only"
        const val SHARE_OPTIONS = "Share options"
        const val SKIP_CAMERA = "Skip Camera"
        const val SKIP_BACK = "Skip Back"
        const val CAPTURE_BACK = "Capture Back"
        const val START_COLOR = "Start Color"
        const val STEP_CAPTURE = "Capture"
        const val STEP_CONTENT_LABEL = "step_content"
        const val STEP_DETAILS = "Details"
        const val STEP_TYPE = "Type"
        const val STUDENT_CARD_DESC = "Student ID cards"
        const val TAP_TO_CAPTURE_PROMPT = "Tap to capture %s of card"
        const val TEXT_RECOGNITION_COMPLETE = "Text Recognition Complete"
        const val TEXT_RECOGNITION_SUBTITLE = "Extracted %d field%s from your %s"
        const val TRANSPORT_CARD_DESC = "Metro and transit cards"
        const val TRY_AGAIN = "Try Again"
        const val TYPE_LABEL = "Type"
        const val UNKNOWN_CAMERA_ERROR = "An unexpected error occurred: %s"
        const val UNKNOWN_CAMERA_ERROR_SUGGESTION = "\n\nRestart the app or try again later."
        const val UPDATED_LABEL = "Updated"
        const val UPDATE_CARD_DETAILS_SUBTITLE = "Update card details"
        const val VALID_THRU_LABEL = "Valid thru %s"
        const val VOUCHER_DESC = "Discount vouchers and coupons"
        const val WATERMARK_TEXT_LABEL = "Watermark text"
        const val WHAT_TO_SHARE_LABEL = "What to share:"
        const val YOUR_CARDS = "Your Cards"
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
        const val LABEL_FLIP = "card_flip"
        const val LABEL_SCALE = "card_scale"
        const val LABEL_ENHANCED_FLIP = "enhanced_card_flip"
        const val LABEL_ENHANCED_SCALE = "enhanced_card_scale"
        const val LABEL_HOVER_SCALE = "hover_scale"
        const val LABEL_SHARE_BUTTON_SCALE = "share_button_scale"
        const val LABEL_SHIMMER = "shimmer"
        const val LABEL_SHIMMER_TRANSLATE = "shimmer_translate"
        const val LABEL_BOUNCE_SCALE = "bounce_scale"
    }

    // Class Names and Reflection
    object ClassNames {
        const val BUILD_CONFIG_CLASS = "com.technitedminds.wallet.BuildConfig"
        const val BUILD_TYPE_FIELD = "BUILD_TYPE"
    }
}