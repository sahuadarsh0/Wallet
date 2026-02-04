# CardVault: Secure Offline Card Repository

CardVault is a secure, offline-first Android application designed to provide users with a premium digital repository for all their physical cards. The primary goal is to create an intuitive, visually stunning, and completely secure wallet app that operates entirely offline.

This project is built from the ground up using modern Android development practices, including a 100% Kotlin codebase, Jetpack Compose for the UI, and a robust clean architecture.

## ✨ Key Features

- **🔒 100% Offline Operation:** Operates entirely without an internet connection. No network permissions, no analytics, and no data ever leaves your device.
- **📱 Modern UI/UX:** A beautiful and intuitive interface built entirely with **Jetpack Compose** and **Material Design 3**, featuring smooth 60fps animations, including a realistic 3D card flip effect.
- **📸 Smart Card Scanning:** Add cards quickly using your camera. On-device **ML Kit** recognizes and extracts card details for textual cards (credit/debit), with manual fallback options.
- **🎨 Flexible Organization:** Group your cards into default or custom-created categories (e.g., 'Work', 'Travel', 'Memberships'), each with a distinct color theme and icon.
- **💳 Dual Card Processing:** Handles both textual cards (Credit/Debit with OCR processing) and image-only cards (15+ types including Transport, Gift, Loyalty, Membership, Insurance, ID, Voucher, Event, Business, Library, Hotel, Student, Access cards) with appropriate workflows.
- **🔍 Aspect Ratio Selection:** Camera overlay supports multiple aspect ratios (16:9, 4:3, 3:4, credit card) for different card types.
- **🎨 Custom Gradients:** Each card type features unique modern gradients with customization options for personalization.
- **📤 Card Sharing:** Share captured images for image-only cards, or generate gradient card designs with extracted details for textual cards.
- **⚡ High Performance:** Optimized for <3s app launch and smooth 60fps animations even on mid-range devices.

## 🛠️ Tech Stack & Architecture

This project follows the official Android architecture recommendations, employing a clean, reactive, and lifecycle-aware approach.

- **Core Language:** [Kotlin](https://kotlinlang.org/) (100%)
- **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (latest BOM)
  - **Theming:** Material Design 3
  - **Navigation:** [Compose Navigation](https://developer.android.com/jetpack/compose/navigation) with type-safe arguments via `kotlinx.serialization`
  - **Animations:** Compose Animation APIs for fluid transitions and effects
- **Architecture:**
  - **Pattern:** Clean Architecture (MVVM & Unidirectional Data Flow)
  - **Frameworks:** [Jetpack ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel), [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html), and [Flow](https://developer.android.com/kotlin/flow)
- **Dependency Injection:** [Hilt](https://dagger.dev/hilt/) for managing dependencies across the application
- **Data Persistence:**
  - **Database:** [Room](https://developer.android.com/training/data-storage/room) with type converters
  - **Preferences:** [Proto DataStore](https://developer.android.com/topic/libraries/architecture/datastore) for user settings
  - **Images:** Secure file storage with compression and optimization
- **Build System:**
  - Gradle with Kotlin DSL (`build.gradle.kts`)
  - [TOML Version Catalog](https://docs.gradle.org/current/userguide/platforms.html) (`gradle/libs.versions.toml`) for centralized dependency management
  - [KSP (Kotlin Symbol Processing)](https://kotlinlang.org/docs/ksp-overview.html) for annotation processing
- **Camera & ML:**
  - [CameraX](https://developer.android.com/training/camerax) v1.5.0 (core, camera2, lifecycle, view, extensions) for modern camera implementation
  - [Google ML Kit](https://developers.google.com/ml-kit) Text Recognition v16.0.1 (bundled, offline) for on-device OCR processing
- **Image Loading:** [Coil](https://coil-kt.github.io/coil/) v2.7.0 for efficient image loading and caching
- **Security:** 
  - [Google Tink](https://github.com/google/tink) v1.18.0 for file encryption
  - AndroidX Biometric v1.1.0 for authentication

## 🏗️ Project Structure

```
com.technitedminds.wallet/
├── MainActivity.kt               # Main entry point with @AndroidEntryPoint ✅
├── WalletApplication.kt          # Application class with @HiltAndroidApp ✅
├── ui/
│   └── theme/                    # Material Design 3 theming ✅
│       ├── Color.kt              # Color definitions ✅
│       ├── Theme.kt              # Theme configuration ✅
│       └── Type.kt               # Typography definitions ✅
├── data/                         # Data Layer - COMPLETE ✅
│   ├── local/
│   │   ├── cache/                # Cache management ✅
│   │   │   └── CacheManager.kt   # Image and data caching
│   │   ├── database/             # Room database components ✅
│   │   │   ├── entities/         # Room entities ✅
│   │   │   │   ├── CardEntity.kt # Card entity with type converters
│   │   │   │   └── CategoryEntity.kt # Category entity with indices
│   │   │   ├── dao/              # Data Access Objects ✅
│   │   │   │   ├── CardDao.kt    # Card CRUD, search, filtering
│   │   │   │   └── CategoryDao.kt # Category management operations
│   │   │   ├── converters/        # Room type converters ✅
│   │   │   │   ├── CardTypeConverter.kt # CardType serialization
│   │   │   │   ├── CardGradientConverter.kt # Gradient serialization
│   │   │   │   └── MapConverter.kt # Map<String,String> serialization
│   │   │   └── WalletDatabase.kt # Room database configuration ✅
│   │   ├── files/                # File system operations ✅
│   │   │   └── ImageFileManager.kt # Image storage and optimization
│   │   ├── preferences/          # User preferences ✅
│   │   │   └── SimplePreferencesManager.kt # Proto DataStore integration
│   │   └── storage/              # Storage management ✅
│   │       └── StorageManager.kt  # File operations and cleanup
│   ├── mapper/                   # Data mapping utilities ✅
│   │   ├── CardMapper.kt         # Card entity ↔ domain mapping
│   │   └── CategoryMapper.kt     # Category entity ↔ domain mapping
│   ├── ocr/                      # OCR processing ✅
│   │   ├── CardTextParser.kt     # Text parsing and validation
│   │   └── MLKitTextRecognizer.kt # ML Kit text recognition
│   └── repository/               # Repository implementations ✅
│       ├── CardRepositoryImpl.kt # Card operations with Room
│       ├── CategoryRepositoryImpl.kt # Category management
│       └── ImageRepositoryImpl.kt # Image file operations
├── domain/                       # Domain Layer - COMPLETE ✅
│   ├── model/                    # Domain models ✅
│   │   ├── Card.kt               # Card data class with gradient support
│   │   ├── CardType.kt           # Sealed class for 15+ card types
│   │   ├── CardGradient.kt       # Gradient customization system
│   │   ├── CardImage.kt          # Image metadata handling
│   │   └── Category.kt            # Category data class
│   ├── repository/               # Repository interfaces ✅
│   │   ├── CardRepository.kt     # Card CRUD and search operations
│   │   ├── CategoryRepository.kt # Category management operations
│   │   └── ImageRepository.kt     # Image storage operations
│   ├── usecase/                  # Business logic use cases ✅
│   │   ├── card/                 # Card-related use cases ✅
│   │   │   ├── AddCardUseCase.kt # Card creation with validation
│   │   │   ├── GetCardsUseCase.kt # Card retrieval with filtering
│   │   │   ├── UpdateCardUseCase.kt # Card updates with validation
│   │   │   ├── DeleteCardUseCase.kt # Card deletion with cleanup
│   │   │   └── ShareCardUseCase.kt # Card sharing functionality
│   │   ├── category/             # Category management use cases ✅
│   │   │   ├── GetCategoriesUseCase.kt # Get all categories
│   │   │   ├── GetCategoryNameUseCase.kt # Resolve category names
│   │   │   └── ManageCategoryUseCase.kt # CRUD operations for categories
│   │   ├── ocr/                  # OCR processing use cases ✅
│   │   │   └── ProcessCardImageUseCase.kt # OCR for textual cards
│   │   ├── storage/              # Storage management use cases ✅
│   │   │   └── StorageManagementUseCase.kt # Storage operations
│   │   └── util/                 # Domain utilities ✅
│   │       └── CardGradientGenerator.kt # Gradient generation
├── presentation/                 # Presentation Layer - COMPLETE ✅
│   ├── screens/                  # Feature screens ✅
│   │   ├── home/                 # Home screen components ✅
│   │   │   ├── HomeScreen.kt     # Main card list with search/filter
│   │   │   └── HomeViewModel.kt  # State management for home screen
│   │   ├── addcard/              # Add card flow ✅
│   │   │   ├── AddCardScreen.kt  # Multi-step add card workflow
│   │   │   └── AddCardViewModel.kt # State management for add card flow
│   │   ├── camera/               # Camera screen ✅
│   │   │   ├── CameraScreen.kt   # Dedicated camera capture screen
│   │   │   └── CameraViewModel.kt # Camera state management
│   │   ├── carddetail/           # Card detail view ✅
│   │   │   ├── CardDetailScreen.kt # Full-screen card view with flip animation
│   │   │   └── CardDetailViewModel.kt # State management for card details
│   │   ├── categories/           # Category management ✅
│   │   │   ├── CategoriesScreen.kt # List of categories with CRUD operations
│   │   │   ├── CategoriesViewModel.kt # State management for categories
│   │   │   └── CategoryDialog.kt # Dialog for category creation/editing
│   │   └── settings/             # App settings ✅
│   │       ├── SettingsScreen.kt # Settings UI with preferences
│   │       └── SettingsViewModel.kt # State management for settings
│   ├── components/               # Reusable UI components ✅
│   │   ├── common/               # Common UI components ✅
│   │   │   ├── CardListItem.kt   # Card list item with thumbnail and details
│   │   │   ├── CardTypeSelector.kt # Card type selection with gradients
│   │   │   ├── CategoryChip.kt   # Category display with color and icon
│   │   │   ├── CategoryPreview.kt # Category preview component
│   │   │   ├── ColorPicker.kt    # Color selection component
│   │   │   ├── ConfirmationDialog.kt # Generic confirmation dialog
│   │   │   ├── ErrorMessage.kt   # Error message display component
│   │   │   ├── GradientPicker.kt # Gradient selection component
│   │   │   ├── GradientPickerDialog.kt # Gradient picker dialog
│   │   │   ├── IconPicker.kt     # Icon selection component
│   │   │   ├── LoadingIndicator.kt # Loading spinner component
│   │   │   └── ValidatedTextField.kt # Text field with validation
│   │   ├── animation/            # Animation components ✅
│   │   │   ├── AnimatedList.kt   # Animate list item changes
│   │   │   ├── AnimationUtils.kt # Common animation utilities
│   │   │   ├── CardBack.kt       # Card back side with flip animation
│   │   │   ├── CardFront.kt      # Card front side with flip animation
│   │   │   └── FlippableCard.kt  # Complete flip animation component
│   │   ├── camera/               # Camera-related components ✅
│   │   │   ├── CameraError.kt    # Camera error handling
│   │   │   ├── CameraManager.kt  # Camera lifecycle management
│   │   │   ├── CameraPermission.kt # Camera permission utilities
│   │   │   ├── CameraPermissionComponent.kt # Permission UI component
│   │   │   ├── CameraPreview.kt  # CameraX implementation with live preview
│   │   │   ├── CaptureButton.kt  # Custom capture button with states
│   │   │   ├── CardOverlay.kt    # Multi-aspect ratio card positioning overlay
│   │   │   └── ImagePreview.kt   # Image preview with zoom/pan capabilities
│   │   └── sharing/              # Card sharing components ✅
│   │       ├── CardSharingManager.kt # Sharing functionality
│   │       └── CardSharingOption.kt # Sharing options
│   ├── navigation/               # Navigation setup ✅
│   │   ├── BottomNavigation.kt   # Bottom navigation bar
│   │   ├── NavigationDestinations.kt # Navigation routes and arguments
│   │   ├── NavigationExtensions.kt # Navigation utility functions
│   │   ├── WalletAppScaffold.kt # Main app scaffold
│   │   └── WalletNavigation.kt   # Navigation host
│   ├── constants/                # App constants ✅
│   │   └── AppConstants.kt       # App-wide constants
│   └── utils/                    # Presentation layer utilities ✅
│       ├── CategoryUtils.kt      # Category utility functions
│       └── PerformanceOptimizer.kt # Performance optimization utilities
├── di/                           # Dependency Injection modules ✅
│   ├── CameraModule.kt           # CameraX and ML Kit dependencies
│   ├── DatabaseModule.kt          # Room database Hilt module
│   └── RepositoryModule.kt       # Repository interface bindings
└── utils/                        # Utility classes and extensions ✅
    └── Extensions.kt             # Kotlin extension functions
```

> **Note**: Empty placeholder folders (`add_card/`, `card_detail/`, `presentation/camera/`, `components/category/`, `util/`) exist in the codebase but are not documented above as they contain no implementation files. Actual screen implementations use `addcard/` and `carddetail/` naming.

## 📦 Build & Version Information

### Build Configuration
- **Target SDK**: 36 (Android 15)
- **Min SDK**: 29 (Android 10)
- **Compile SDK**: 36
- **Java Version**: 11
- **Kotlin**: 2.0.0
- **Gradle Plugin**: 8.13.0
- **KSP**: 2.0.0-1.0.21
- **Compose BOM**: 2025.09.01

### Key Dependencies Versions
- **Hilt**: 2.57.1
- **Room**: 2.8.1
- **Navigation Compose**: 2.9.5
- **CameraX**: 1.5.0
- **ML Kit Text Recognition**: 16.0.1
- **Coil**: 2.7.0
- **Proto DataStore**: 1.1.7
- **Google Tink**: 1.18.0
- **Material3**: 1.4.0

All dependencies are managed via TOML version catalog (`gradle/libs.versions.toml`).

## 🚀 Getting Started

To build and run this project locally, follow these steps:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/cardvault.git
   ```

2. **Open in Android Studio:**
   - Open Android Studio (latest stable version recommended)
   - Click on `File > Open` and select the cloned project directory

3. **Gradle Sync:**
   - Allow Android Studio to download all dependencies and sync the project with the Gradle files

4. **Build and Run:**
   - Select the `app` configuration and a target device (emulator or physical device)
   - Click the "Run" button

## 🎨 Enhanced UI Components

CardVault features a comprehensive library of premium UI components designed for smooth animations and exceptional user experience:

### Core Enhanced Components
- **PremiumCard**: Enhanced cards with gradient backgrounds, haptic feedback, and scale animations
- **PremiumTextField**: Advanced text fields with leading icons, validation, and smooth focus transitions
- **AnimatedSectionHeader**: Section headers with icons in circular gradient backgrounds and slide-in animations
- **EnhancedSlideInItem**: Staggered slide-in animations for list items and sections with customizable delays
- **PremiumButton**: Enhanced buttons with haptic feedback, scale animations, and multiple variants
- **PremiumChip**: Animated chips with color transitions and premium styling

### Specialized Components
- **StepProgressIndicator**: Visual progress tracking through multi-step workflows with animated transitions
- **OCRStatusCard**: Success indicator for text recognition completion with sparkle icon
- **ManualEntryCard**: Prompt for manual entry when OCR fails with edit icon
- **PrivacyNoticeCard**: Offline-only operation explanations with security messaging
- **FlippableCard**: 3D card flip animations with realistic perspective and smooth transitions

### Enhanced User Flows
- **Multi-Step Add Card**: Premium wizard with TypeSelectionStep, CameraCaptureStep, and FormDetailsStep
- **Immersive Card Detail**: Enhanced viewing experience with smooth edit mode transitions
- **Staggered Animations**: Section slide-ins with 100ms delays for polished experience
- **Contextual Progress**: Visual indicators showing current position in workflows

## 🎨 Card Types & Visual Identity

CardVault supports 15+ card types with unique gradient designs:

### OCR-Enabled Cards (with text extraction):
- **Credit Cards**: Purple to blue gradient (professional banking feel)
- **Debit Cards**: Pink to red gradient (vibrant and accessible)

### Image-Only Cards (visual storage):
- **Transport Cards**: Blue to cyan gradient (tech-forward transit)
- **Gift Cards**: Soft teal to pink gradient (friendly and gift-like)
- **Loyalty Cards**: Cream to peach gradient (warm and rewarding)
- **Membership Cards**: Green to teal gradient (health and community)
- **Insurance Cards**: Purple to cream gradient (official and secure)
- **ID Cards**: Light blue to blue gradient (official and trustworthy)
- **Vouchers**: Pink to yellow gradient (exciting and promotional)
- **Event Cards**: Cream to peach gradient (warm and celebratory)
- **Business Cards**: Purple to blue gradient (professional and corporate)
- **Library Cards**: Green to teal gradient (educational and knowledge)
- **Hotel Cards**: Soft teal to pink gradient (hospitality and comfort)
- **Student Cards**: Light blue to blue gradient (academic and youthful)
- **Access Cards**: Purple to cream gradient (security and access)
- **Custom Cards**: User-defined types with customizable gradients

## 📋 Implementation Status

### ✅ Complete Features
- **Complete Architecture:** Clean MVVM with UDF pattern and Hilt dependency injection
- **Domain Layer:** Complete with 15+ card types, gradient system, and sharing functionality
- **Data Layer:** Room database with entities, DAOs, type converters, and repository implementations
- **Textual Cards:** Credit/Debit cards with ML Kit OCR processing and text extraction
- **Image Cards:** 13+ card types with visual storage and custom gradient designs
- **Camera Integration:** Full CameraX implementation with multi-aspect ratio overlays
- **3D Card Flip:** Smooth animations using Compose graphicsLayer with realistic transitions
- **Card Sharing:** Image sharing for image-only cards, gradient card generation for textual cards
- **Category Management:** Custom categories with icons, colors, and CRUD operations
- **Navigation System:** Complete Compose Navigation with type-safe arguments and bottom navigation
- **Performance Optimized:** 60fps animations, efficient image handling, and memory management
- **Material Design 3:** Consistent UI with proper theming and dynamic colors
- **Offline-First:** Zero network dependencies, all data stays on device
- **Splash Screen:** Animated splash screen with smooth transitions
- **Storage Management:** File compression, optimization, and cleanup utilities
- **OCR Processing:** ML Kit text recognition with confidence scoring and validation
- **Enhanced UI Components:** PremiumCard, PremiumTextField, AnimatedSectionHeader, EnhancedSlideInItem
- **Enhanced Add Card Flow:** Multi-step wizard with premium animations and contextual guidance
- **Enhanced Card Detail:** Immersive card viewing with smooth edit mode transitions

### 🔄 Recently Completed Features
- **Enhanced Components Library:** PremiumCard, PremiumTextField, AnimatedSectionHeader, EnhancedSlideInItem, and other premium UI components with haptic feedback and smooth animations
- **Enhanced Add Card Flow:** Complete redesign with TypeSelectionStep, CameraCaptureStep, and FormDetailsStep using premium components
- **Enhanced Card Detail Page:** Improved visual hierarchy, staggered animations, and premium editing experience
- **Step Progress Indicator:** Visual progress tracking through multi-step workflows
- **OCR Status Cards:** Visual feedback for text recognition completion and manual entry prompts
- **Privacy Notice Cards:** Offline-only operation explanations and security messaging
- **Advanced Animations:** Step transitions, section slide-ins, and enhanced interaction feedback

### 🚧 Next Steps
- **Advanced OCR Processing:** Image discard for textual cards and gradient generation
- **Card Gradient Generator:** Utility for creating gradient card images for OCR cards
- **Default Back Image Generation:** Custom gradient back images for image-only cards
- **Dual Storage Strategy:** OCR cards generate gradients, image cards save actual photos
- **Form Validation:** Real-time validation with field-specific error messages
- **Back Navigation:** Data preservation during step navigation
- **Loading States:** Enhanced loading overlays and error handling
- **Testing:** Unit tests for ViewModels, use cases, and repository implementations
- **Integration Tests:** End-to-end testing for card workflows and camera integration
- **UI Polish:** Fine-tuning animations, transitions, and user experience
- **Accessibility:** Screen reader support and accessibility improvements
- **Performance Monitoring:** Memory usage optimization and startup time improvements

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE.md) file for details.
