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
        val CARD_LIST_ITEM_MENU_SIZE_COMPACT = 48.dp
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
        const val TERMS_OF_SERVICE_TITLE = "Terms of Service"
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
Last updated: May 2026
Published by Technited Minds

1. Introduction
CardVault ("we", "our", "the App") is an offline-first digital wallet operated by Technited Minds ("Technited Minds", "us"). This Privacy Policy describes the categories of data the App processes, the on-device safeguards applied to that data, and the rights you retain over it. The App is engineered to be operable without any network connection; consequently, Technited Minds does not, and cannot, receive your card information, images, preferences, or any derivative thereof.

2. Categories of Data Processed
CardVault processes only data that you voluntarily input or capture using the App:
   (a) Card metadata: card name, card type (e.g., Credit, Debit, Gift, Voucher, Membership), optional expiry date, free-form notes, and user-defined custom fields.
   (b) Financial information (Credit and Debit cards only): primary account number ("PAN"), card verification value ("CVV"), expiry date, and cardholder name, optionally extracted via on-device optical character recognition ("OCR"). These fields are encrypted at rest using AES-256-GCM.
   (c) Card images: photographs of the front and reverse of your cards, stored exclusively within the App's private, sandboxed storage on your device.
   (d) Authentication credentials: a four-digit Personal Identification Number ("PIN") and an optional 16-character recovery code, both stored as PBKDF2-HmacSHA256 hashes with per-credential random salts. Plaintext PINs and recovery codes are never persisted.
   (e) Application preferences: theme mode, folder theme, default category, and similar non-sensitive UI settings.

3. Lawful Basis and Purpose
All data is processed solely to provide the core functionality of the App: secure local storage, retrieval, display, and user-initiated sharing of your card information. Technited Minds does not use your data for analytics, advertising, profiling, machine-learning model training, fraud scoring, or any secondary purpose.

4. No Third-Party Sharing or Transfer
CardVault does not transmit, share, sell, license, or otherwise transfer your data to Technited Minds or to any third party. The Android `INTERNET` permission is explicitly removed from the application manifest, and the App contains no advertising, analytics, telemetry, or crash-reporting SDK. Your data leaves your device only when you, the user, expressly invoke a sharing action (see Section 6).

5. Data Security
   (a) Sensitive card fields are encrypted at rest using AES-256-GCM via Google Tink, with key material protected by the Android Keystore.
   (b) The PIN and recovery code are stored as PBKDF2-HmacSHA256 hashes (10,000 iterations, 16-byte random salt). Hash comparison is performed in constant time.
   (c) Biometric authentication, when enabled, is mediated entirely by the Android operating system; the App receives only a Boolean success or failure signal and never receives biometric templates.
   (d) On-device OCR is performed by the bundled Google ML Kit Text Recognition library; no image, text, or extracted field is transmitted off-device.
   (e) All files generated by the App are stored within the App's private, sandboxed file storage.
   (f) The application window is marked `FLAG_SECURE`, which instructs the operating system to suppress screenshot capture and screen recording while CardVault is in the foreground, and to omit the App from the recent-tasks preview.

6. User-Initiated Sharing and Manual Backup
   (a) Image and card sharing. The App provides functionality to share card images and rendered card representations through the Android system share-sheet. Once shared, the resulting content is governed by the privacy practices of the recipient application or service, over which Technited Minds has no control.
   (b) Manual backup. The App provides functionality to export an encrypted backup file in the proprietary `.wallet` format to the device's `Downloads/CardVault/` directory. The backup file is encrypted using AES-256-GCM with a key derived from a passphrase you supply via PBKDF2-HmacSHA256 (200,000 iterations, 32-byte random salt). The backup file contains: card metadata, decrypted card fields (re-encrypted under your passphrase), card images, and non-sensitive application preferences. The backup file does NOT contain: your PIN hash, recovery code, biometric flags, or the App's internal encryption keys.
   (c) Manual restore. The App can read a previously exported `.wallet` file selected by you via the Android Storage Access Framework. The App does not initiate any download or fetch operation; it reads only the bytes of the file you explicitly select.
   (d) Custody of exported files. Once a backup file is written to `Downloads/CardVault/`, or shared through the system share-sheet to a destination of your choice (including but not limited to cloud storage providers, messaging applications, or email clients), responsibility for the secure handling and storage of that file passes entirely to you. Technited Minds bears no responsibility for the privacy practices of any third-party service to which you transmit a backup file.

7. Data Retention and Deletion
   (a) All data is retained on your device until you delete it or uninstall the App.
   (b) Individual cards may be deleted at any time, which permanently removes the card record, its associated images, and any thumbnails.
   (c) A full data-wipe action is available in Settings and is also triggered automatically after the maximum number of failed PIN attempts is exceeded (when this option is enabled). The full wipe permanently destroys all cards, images, categories, application preferences, the PIN hash, the recovery-code hash, and the App's internal encryption keys.
   (d) Uninstalling the App removes all App data from the device. Any backup files you previously exported to `Downloads/CardVault/` or to external destinations remain in those locations and must be deleted by you separately.

8. Permissions
   (a) `CAMERA` (runtime): granted only when you initiate card scanning; revocable at any time in Android Settings.
   (b) `NFC` (normal): used exclusively to read contactless EMV card data when you initiate a tap-to-read action; the App does not perform contactless payments.
   (c) The App does not request `INTERNET`, `ACCESS_NETWORK_STATE`, location, microphone, contacts, calendar, SMS, or external-storage management permissions.

9. Children's Privacy
The App is not directed to, and is not intended for use by, individuals under the age of thirteen (13). Technited Minds does not knowingly collect or process data from children under thirteen.

10. Changes to This Policy
Technited Minds may update this Privacy Policy from time to time to reflect new functionality, legal obligations, or clarifications. Material changes will be reflected in the "Last updated" date above and, where appropriate, surfaced through an in-app notice upon next launch.

11. Contact
For questions, requests, or concerns regarding this Privacy Policy, please contact:
support@technitedminds.com"""

        // Terms of Service content
        const val TERMS_OF_SERVICE_CONTENT = """CardVault Terms of Service
Last updated: May 2026
Published by Technited Minds

1. Acceptance of Terms
By installing, accessing, or using the CardVault application ("the App"), you ("you", "the User") agree to be bound by these Terms of Service ("Terms"). If you do not agree to these Terms in their entirety, you must not install, access, or use the App. Continued use of the App following any modification of these Terms constitutes acceptance of the modified Terms.

2. License Grant
Subject to your compliance with these Terms, Technited Minds ("Technited Minds", "we", "us") grants you a limited, non-exclusive, non-transferable, non-sublicensable, revocable license to install and use the App on a compatible Android device that you own or control, solely for your personal, non-commercial use.

3. User Responsibilities
   (a) Accuracy of input. You are solely responsible for the accuracy, completeness, and lawfulness of any data you input, capture, scan, or otherwise enter into the App, including card metadata, images, custom fields, and notes.
   (b) Authorization. You represent and warrant that you have the legal right to store, photograph, and otherwise process every card or document whose information you record in the App.
   (c) Device security. You are solely responsible for the physical and logical security of your device, including the secrecy of any device passcode, biometric enrollment, screen-lock setting, and the App PIN you configure.
   (d) Lawful use. You shall not use the App to store, process, or share information that you are not legally entitled to possess, including but not limited to stolen card data, counterfeit credentials, or information obtained in violation of applicable law.

4. Encryption Passphrase for Manual Backups
The App provides functionality to export an encrypted backup file ("Backup File") protected by a passphrase that you, and only you, supply. By using this functionality, you acknowledge and agree that:
   (a) The passphrase is never transmitted to, stored by, or otherwise made available to Technited Minds. Technited Minds has no copy of, and no means to recover, your passphrase.
   (b) IF YOU LOSE OR FORGET THE PASSPHRASE, THE BACKUP FILE CANNOT BE DECRYPTED BY ANY PARTY, INCLUDING TECHNITED MINDS. SUCH LOSS IS PERMANENT AND IRRECOVERABLE.
   (c) You are solely responsible for selecting a passphrase of sufficient strength, recording it in a secure location of your choosing, and protecting it against disclosure.
   (d) Technited Minds shall have no liability whatsoever for any data, financial, or other loss arising from a forgotten, weak, disclosed, or otherwise compromised passphrase.

5. Custody of Exported Files and Shared Content
   (a) Backup files. When you export a Backup File, the file is written to the `Downloads/CardVault/` directory of your device. You may, at your sole election, share the file via the Android system share-sheet to any destination, including but not limited to cloud-storage providers, messaging applications, or email clients. Once a Backup File leaves the App's private storage, custody and responsibility for that file pass entirely to you.
   (b) Card images and rendered cards. The App provides functionality to share card images and rendered card representations through the Android system share-sheet. Once shared, the resulting content is governed by the privacy and security practices of the recipient application, service, or person, over which Technited Minds has no control.
   (c) No warranty regarding third parties. Technited Minds makes no representation and gives no warranty regarding the privacy practices, security posture, retention policies, or jurisdictional handling of any third-party application or service to which you elect to transmit content originating from the App.

6. Data Security and No Warranty
   (a) The App employs AES-256-GCM authenticated encryption (via Google Tink, with key material protected by the Android Keystore) for sensitive card fields, PBKDF2-HmacSHA256 hashing for the App PIN and recovery code, and AES-256-GCM with a PBKDF2-derived key for Backup Files. Cryptographic primitives are provided by the Android platform, by the Google Tink library, and by the Java Cryptography Architecture, and their security is subject to the warranties (if any) of those providers.
   (b) NO METHOD OF DIGITAL STORAGE OR CRYPTOGRAPHIC PROTECTION IS ABSOLUTELY SECURE. THE APP IS PROVIDED "AS IS" AND "AS AVAILABLE", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION ANY WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT, OR ACCURACY.

7. Data Loss
   (a) The App is offline-first and operates entirely on your device. Technited Minds does not maintain, and cannot recover, any copy of your data.
   (b) Manual backup is your sole defense against data loss. You are responsible for periodically exporting Backup Files using the App's manual-backup functionality and for storing those Backup Files in a location of your choosing, in accordance with Sections 4 and 5.
   (c) Loss, theft, damage, factory reset, ten consecutive failed PIN attempts (where automatic data wipe is enabled), uninstallation of the App, or revocation of the App's storage by the operating system may result in the permanent loss of all data not previously exported and securely stored. Technited Minds shall have no liability for any such loss.

8. Limitation of Liability
TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW, TECHNITED MINDS, ITS AFFILIATES, OFFICERS, EMPLOYEES, AGENTS, AND LICENSORS SHALL NOT BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL, EXEMPLARY, OR PUNITIVE DAMAGES, OR FOR ANY LOSS OF DATA, PROFITS, REVENUE, GOODWILL, OR BUSINESS, ARISING OUT OF OR IN CONNECTION WITH YOUR USE OF, OR INABILITY TO USE, THE APP, WHETHER BASED ON WARRANTY, CONTRACT, TORT (INCLUDING NEGLIGENCE), OR ANY OTHER LEGAL THEORY, AND WHETHER OR NOT TECHNITED MINDS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.

9. Indemnification
You agree to indemnify, defend, and hold harmless Technited Minds and its affiliates, officers, employees, agents, and licensors from and against any and all claims, liabilities, damages, losses, costs, and expenses (including reasonable attorneys' fees) arising out of or in connection with: (a) your breach of these Terms; (b) your misuse of the App; (c) your violation of any applicable law or the rights of any third party in connection with your use of the App; or (d) any content you input, store, share, or transmit through or originating from the App.

10. Modification of the App
Technited Minds reserves the right, at any time and without prior notice, to modify, suspend, or discontinue the App or any part thereof. Technited Minds shall have no liability to you or to any third party for any such modification, suspension, or discontinuation.

11. Modification of Terms
Technited Minds may update these Terms from time to time. Material changes will be reflected in the "Last updated" date above and, where appropriate, surfaced through an in-app notice. Continued use of the App following such update constitutes your acceptance of the updated Terms. If you do not accept any update, your sole remedy is to cease use of the App and uninstall it.

12. Severability and Entire Agreement
If any provision of these Terms is held to be invalid or unenforceable, that provision shall be enforced to the maximum extent permissible, and the remaining provisions shall remain in full force and effect. These Terms constitute the entire agreement between you and Technited Minds with respect to the App and supersede any prior agreements, communications, or understandings, whether written or oral.

13. Contact
For questions regarding these Terms, please contact:
support@technitedminds.com"""

        // Open source licenses content
        const val OPEN_SOURCE_LICENSES_CONTENT = """CardVault uses the following open source libraries:

• Jetpack Compose - Apache License 2.0
• CameraX - Apache License 2.0
• ML Kit - Google Terms of Service
• Room Database - Apache License 2.0
• Hilt - Apache License 2.0
• Coil - Apache License 2.0
• DataStore Preferences - Apache License 2.0
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
        const val LIVE_PREVIEW_TITLE = "Live Preview"
        const val FRONT_CAPTURED = "Front captured"
        const val BACK_CAPTURED = "Back captured"
        const val ADD_WATERMARK_LABEL = "Add watermark"
        const val ADDITIONAL_INFO_BACK = "Additional information on back"
        const val ADDITIONAL_INFORMATION = "Additional Details"
        const val ALL = "All"
        const val ALL_TYPES_LABEL = "All Types"
        const val APPEARANCE_SUBTITLE = "Card colors and gradient"
        const val APPEARANCE_TITLE = "Appearance"
        const val APPLY = "Apply"
        const val APP_TITLE = "CardVault"
        const val AUTO_DETECTED = "Auto-detected"
        const val AUTO_LOCK_FORMAT = "Auto-locking in %d:%02d"
        const val AUTO_LOCK_WARNING = "Locking soon"
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
        const val CARDHOLDER_LABEL = "CARDHOLDER NAME"
        const val CARDHOLDER_NAME_PLACEHOLDER = "JOHN DOE"
        const val CARD_ADDED_DATE_LABEL = "Added %s"
        const val CARD_COLOR_LABEL = "Card Color"
        const val CARD_DETAILS_SUBTITLE = "Extracted card details"
        const val CARD_DETAILS_TITLE = "Card Details"
        const val CARD_IMAGE = "Card image"
        const val CARD_INFORMATION = "Card Details"
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
        const val GLOBAL_SEARCH_HEADER = "Search results"
        const val GLOBAL_SEARCH_EMPTY_TITLE = "No cards found"
        const val GLOBAL_SEARCH_EMPTY_SUBTITLE = "Try a different search term"
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
• DataStore Preferences - Apache License 2.0
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
Published by Technited Minds

CardVault is 100% offline — no data is transmitted over the network (INTERNET permission is explicitly removed). Sensitive card fields are encrypted at rest with AES-256-GCM (Google Tink + Android Keystore). No analytics, tracking, or data collection is performed. Camera permission is used only for card scanning. NFC permission is used only for contactless card reading. Your data never leaves your device unless you explicitly share it.

Data Retention & Deletion: All data is stored locally until you delete it. You may delete individual cards or perform a full data wipe in Settings. Uninstalling the app removes all data permanently.

For questions, contact: support@technitedminds.com"""
        const val PRIVACY_POLICY_SUBTITLE = "View our privacy policy"
        const val PRIVACY_POLICY_TITLE = "Privacy Policy"
        const val TERMS_OF_SERVICE_SUBTITLE = "Read our terms of service"
        const val TERMS_OF_SERVICE_TITLE = "Terms of Service"
        const val PROCESSING = "Processing..."
        const val PROTECTED_BY_LABEL = "Protected by CardVault"
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
        const val MANAGE_CATEGORIES_TITLE = "Manage Categories"
        const val MANAGE_CATEGORIES_SUBTITLE = "Add, edit, or delete categories"
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
        const val VALID_THRU_PREFIX = "VALID THRU"
        const val VOUCHER_DESC = "Discount vouchers and coupons"
        const val WATERMARK_TEXT_LABEL = "Watermark text"
        const val WHAT_TO_SHARE_LABEL = "What to share:"
        const val YOUR_CARDS = "Your Cards"
    }

    // Security / App Lock Labels
    object SecurityLabels {
        const val PRIVACY_AND_SECURITY = "Privacy & Security"
        const val APP_LOCK = "App Lock"
        const val APP_LOCK_SUBTITLE = "Require PIN to open CardVault"
        const val BIOMETRIC_UNLOCK = "Biometric Unlock"
        const val BIOMETRIC_UNLOCK_SUBTITLE = "Use fingerprint or face to unlock"
        const val BIOMETRIC_NOT_AVAILABLE = "No biometric hardware detected"
        const val SET_PIN = "Set PIN"
        const val CHANGE_PIN = "Change PIN"
        const val SET_PIN_SUBTITLE = "Create a 4-digit PIN to lock the app"
        const val CHANGE_PIN_SUBTITLE = "Update your 4-digit PIN"
        const val BACKUP_CODE = "Backup Code"
        const val BACKUP_CODE_SUBTITLE = "View or regenerate your recovery code"
        const val LOCK_TIMEOUT = "Lock Timeout"
        const val LOCK_TIMEOUT_SUBTITLE = "How quickly the app locks after leaving"
        const val TIMEOUT_IMMEDIATE = "Immediate"
        const val TIMEOUT_1_MIN = "After 1 minute"
        const val TIMEOUT_5_MIN = "After 5 minutes"
        const val TIMEOUT_15_MIN = "After 15 minutes"
        const val RECOVERY_CODE_TITLE = "Your Recovery Code"
        const val RECOVERY_CODE_MESSAGE = "Save this code in a safe place. You will need it if you forget your PIN. It will only be shown once."
        const val ENTER_NEW_PIN = "Enter New PIN"
        const val CONFIRM_NEW_PIN = "Confirm PIN"
        const val PIN_MISMATCH = "PINs do not match"
        const val PIN_SET_SUCCESS = "PIN set successfully"
        const val COPY_CODE = "Copy Code"
        const val CODE_COPIED = "Recovery code copied"
        const val DONE = "Done"
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

    // NFC Text and Labels
    object NfcText {
        const val SCAN_WITH_NFC = "Tap to Read"
        const val SCANNING_TITLE = "Ready to Scan"
        const val SCANNING_SUBTITLE = "Hold your card against the back of your phone"
        const val READ_SUCCESS_TITLE = "Card Read Successfully"
        const val READ_FAILED_TITLE = "Card Read Failed"
        const val CARD_READ_SUCCESS = "Card Read Successfully"
        const val CVV_PROMPT = "NFC cannot read the CVV printed on your card. Please enter it manually for a complete record."
        const val CVV_VALIDATION_ERROR = "CVV must be 3 or 4 digits"
        const val SKIP_CVV = "Skip for Now"
        const val NFC_NOT_AVAILABLE = "NFC is not available on this device"
        const val NFC_DISABLED = "NFC is disabled. Please enable it in your device settings."
        const val NFC_CONTENT_DESC = "NFC contactless scan"
    }

    // Class Names and Reflection
    object ClassNames {
        const val BUILD_CONFIG_CLASS = "com.technitedminds.wallet.BuildConfig"
        const val BUILD_TYPE_FIELD = "BUILD_TYPE"
    }

    // TryClothing Constants
    object TryClothing {
        const val REMOTE_CONFIG_ENDPOINT_KEY = "tryon_api_endpoint"
        const val REMOTE_CONFIG_API_KEY = "tryon_api_key"
    }
}