# CardVault Project Structure

This document outlines the feature-based architecture implemented for the CardVault Android application.

## Package Organization

The project follows a clean architecture pattern with clear separation of concerns:

```
com.technitedminds.wallet/
├── MainActivity.kt               # Main entry point with @AndroidEntryPoint ✅
├── WalletApplication.kt          # Application class with @HiltAndroidApp ✅
├── ui/
│   └── theme/                    # Material Design 3 theming ✅
│       ├── Color.kt              # Color definitions ✅
│       ├── Theme.kt              # Theme configuration ✅
│       └── Type.kt               # Typography definitions ✅
├── data/                         # Data Layer ✅ IMPLEMENTED
│   ├── local/
│   │   ├── database/             # Room database components ✅
│   │   │   ├── entities/         # Room entities ✅
│   │   │   │   ├── CardEntity.kt # Card entity with type converters
│   │   │   │   └── CategoryEntity.kt # Category entity with indices
│   │   │   ├── dao/              # Data Access Objects ✅
│   │   │   │   ├── CardDao.kt    # Card CRUD, search, filtering
│   │   │   │   └── CategoryDao.kt # Category management operations
│   │   │   ├── converters/       # Room type converters ✅
│   │   │   │   ├── CardTypeConverter.kt # CardType serialization
│   │   │   │   └── MapConverter.kt # Map<String,String> serialization
│   │   │   └── WalletDatabase.kt # Room database configuration ✅
│   │   ├── files/                # File system operations ✅
│   │   │   └── ImageFileManager.kt # Image storage and optimization
│   │   └── preferences/          # User preferences ✅
│   │       └── SimplePreferencesManager.kt # Proto DataStore integration
│   ├── repository/               # Repository implementations ✅
│   │   ├── CardRepositoryImpl.kt # Card operations with Room
│   │   ├── CategoryRepositoryImpl.kt # Category management
│   │   ├── ImageRepositoryImpl.kt # Image file operations
│   │   ├── ExportImportRepositoryImpl.kt # Data portability
│   │   └── ImageRepositoryImpl.kt # Image operations with caching
│   └── mapper/                   # Data mapping utilities ✅
│       └── CardMapper.kt         # Card entity ↔ domain mapping
├── domain/                       # Domain Layer ✅ IMPLEMENTED
│   ├── model/                    # Domain models ✅
│   │   ├── Card.kt               # Card data class with all fields
│   │   ├── CardType.kt           # Sealed class for card types
│   │   ├── CardImage.kt          # Image metadata handling
│   │   └── Category.kt           # Category data class with iconResId, colorHex, isDefault
│   ├── repository/               # Repository interfaces ✅
│   │   ├── CardRepository.kt     # Card CRUD and search operations
│   │   ├── CategoryRepository.kt # Category management operations
│   │   ├── ImageRepository.kt    # Image storage operations
│   │   └── ExportImportRepository.kt # Data portability operations
│   ├── usecase/                  # Business logic use cases ✅
│   │   ├── card/                 # Card-related use cases ✅
│   │   │   ├── AddCardUseCase.kt # Card creation with validation
│   │   │   ├── GetCardsUseCase.kt # Card retrieval with filtering
│   │   │   ├── UpdateCardUseCase.kt # Card updates with validation
│   │   │   └── DeleteCardUseCase.kt # Card deletion with cleanup
│   │   ├── ocr/                  # OCR processing use cases ✅
│   │   │   └── ProcessCardImageUseCase.kt # OCR for textual cards
│   │   ├── category/             # Category management use cases ✅
│   │   │   ├── GetCategoriesUseCase.kt # Get all categories
│   │   │   ├── GetCategoryNameUseCase.kt # Resolve category names
│   │   │   └── ManageCategoryUseCase.kt # CRUD operations for categories
│   │   └── export/               # Export/import use cases ✅
│   │       ├── ExportDataUseCase.kt # Data export functionality
│   │       └── ImportDataUseCase.kt # Data import functionality
│   └── util/                     # Domain utilities
├── presentation/                 # Presentation Layer ✅ IMPLEMENTED
│   ├── screens/                  # Feature screens ✅
│   │   ├── home/                 # Home screen components ✅
│   │   │   ├── HomeScreen.kt     # Main card list with search/filter
│   │   │   └── HomeViewModel.kt  # State management for home screen
│   │   ├── addcard/              # Add card flow ✅
│   │   │   ├── AddCardScreen.kt  # Multi-step add card workflow
│   │   │   └── AddCardViewModel.kt # State management for add card flow
│   │   ├── carddetail/           # Card detail view ✅
│   │   │   ├── CardDetailScreen.kt # Full-screen card view with flip animation
│   │   │   └── CardDetailViewModel.kt # State management for card details
│   │   ├── categories/           # Category management ✅
│   │   │   ├── CategoriesScreen.kt # List of categories with CRUD operations
│   │   │   ├── CategoriesViewModel.kt # State management for categories
│   │   │   └── AddEditCategoryDialog.kt # Dialog for category creation/editing
│   │   └── settings/             # App settings ✅
│   │       ├── SettingsScreen.kt # Settings UI with preferences
│   │       └── SettingsViewModel.kt # State management for settings
│   ├── components/               # Reusable UI components ✅
│   │   ├── common/               # Common UI components ✅
│   │   │   ├── CardListItem.kt   # Card list item with thumbnail and details
│   │   │   ├── CardTypeSelector.kt # Card type selection UI
│   │   │   ├── CategoryChip.kt   # Category display with color and icon
│   │   │   ├── ColorPicker.kt    # Color selection component
│   │   │   ├── ConfirmationDialog.kt # Generic confirmation dialog
│   │   │   ├── ErrorMessage.kt   # Error message display component
│   │   │   ├── LoadingIndicator.kt # Loading spinner component
│   │   │   └── ValidatedTextField.kt # Text field with validation
│   │   ├── animation/            # Animation components ✅
│   │   │   ├── AnimatedList.kt   # Animate list item changes
│   │   │   ├── AnimationUtils.kt # Common animation utilities
│   │   │   ├── CardBack.kt       # Card back side with flip animation
│   │   │   └── CardFront.kt      # Card front side with flip animation
│   │   └── camera/               # Camera-related components ✅
│   │       ├── CameraError.kt    # Camera error handling
│   │       ├── CameraPermission.kt # Camera permission utilities
│   │       ├── CameraPreview.kt  # CameraX implementation with live preview
│   │       ├── CaptureButton.kt  # Custom capture button with states
│   │       ├── CardOverlay.kt    # Multi-aspect ratio card positioning overlay
│   │       └── ImagePreview.kt   # Image preview with zoom/pan capabilities
│   ├── navigation/               # Navigation setup ✅
│   │   └── (navigation files present)
│   └── utils/                    # Presentation layer utilities ✅
│       └── (utility files present)
├── di/                           # Dependency Injection modules ✅
│   ├── DatabaseModule.kt         # Room database Hilt module
│   ├── RepositoryModule.kt       # Repository interface bindings
│   └── AppModule.kt              # Additional Hilt bindings (fixed duplicate issue)
└── utils/                        # Utility classes and extensions ✅
    └── Extensions.kt             # Kotlin extension functions
```

## Key Dependencies (as used in module)

### Core UI
- **Jetpack Compose**: UI toolkit (BOM `2024.09.00`, Material3, Icons)
- **SplashScreen**: AndroidX splash screen API

### Architecture
- **Hilt**: Dependency injection (runtime + navigation-compose)
- **Room**: Local database (runtime, ktx, compiler via KSP)
- **Navigation Compose**: In-app navigation

### Media/OCR
- **CameraX Core**: `androidx.camera:camera-core` (foundation)
- **Google Play Services ML Kit Text Recognition**: `play-services-mlkit-text-recognition(-common)`

### Images
- **Coil**: Image loading (`coil-compose`)

### Testing
- **JUnit 4**, **AndroidX Test**, **Compose UI Test**

### Declared in version catalog but not yet added to app module
- CameraX artifacts: `camera-camera2`, `camera-lifecycle`, `camera-view`, `camera-extensions`
- ML Kit: `com.google.mlkit:text-recognition`
- Security/Data: Proto DataStore, Biometric, Tink

## Security Configuration

The AndroidManifest.xml has been configured to explicitly block network permissions:
- `android.permission.INTERNET` - Blocked
- `android.permission.ACCESS_NETWORK_STATE` - Blocked  
- `android.permission.ACCESS_WIFI_STATE` - Blocked

Only the camera permission is allowed for card scanning functionality.

## Build Configuration

- **Target SDK**: 36 (Android 15)
- **Min SDK**: 29 (Android 10)
- **Java Version**: 11
- **Kotlin**: 2.0.0
- **Compose Compiler**: 2.0.0

## Build Status: ✅ SUCCESSFUL
The project builds successfully with:
- Domain layer complete
- Data layer with Room and file storage
- Material 3 theming and Compose setup
- Hilt dependency injection modules
- Camera and OCR components present (integration foundation in place)

## Recent Development Progress
**Task 4: Camera and OCR Integration** - Advanced
- ✅ CameraX foundation added (camera-core), UI components implemented
- ✅ ML Kit Text Recognition wired via Play Services APIs
- ✅ Camera UI set: Overlay, Permission, Preview, Capture, Error, Image Preview
- 🚧 Remaining: Add additional CameraX artifacts (camera2/lifecycle/view) if needed and finalize end-to-end capture-to-OCR flow

## Implementation Status

### ✅ Completed
- **Project Foundation**: Basic app structure with Hilt integration
  - MainActivity with Compose setup and Hilt integration
  - WalletApplication with Hilt configuration
  - Material Design 3 theming (Color, Theme, Type)
  - Build configuration with core dependencies
- **Domain Layer**: Complete implementation with all models, repositories, and use cases
  - Core domain models (Card, CardType, Category, CardImage)
  - Repository interfaces for all data operations
  - Use cases for card management, OCR processing, category management, and data export/import
  - Comprehensive validation and error handling
- **Data Layer**: Complete implementation with Room database and file storage
  - Room database with entities, DAOs, and type converters
  - Image file management with compression and optimization
  - Repository implementations with proper error handling
  - Data mapping utilities between domain and data models
  - User preferences management with SharedPreferences
  - Export/import functionality with JSON serialization
- **Camera & OCR Components**: Partial implementation
  - **CardOverlay.kt**: Multi-aspect ratio overlay for card positioning
    - Supports 16:9, 4:3, 3:4, and Credit Card aspect ratios
    - Visual guides with corner indicators and crosshair alignment
    - Customizable overlay transparency and capture state feedback
    - Optimized card dimension calculations for different orientations
  - **CameraError.kt**: Comprehensive error handling for camera operations
    - Sealed class hierarchy for different error types
    - User-friendly error messages with recovery suggestions
    - Error mapping utilities and recoverability checks
    - Material Design 3 error UI components

### 🚧 In Progress / Next Steps
- **Camera & OCR Integration**: Finalize capture → process → save pipeline
  - 🚧 Consider adding `camera-camera2`, `camera-lifecycle`, `camera-view` for full feature set
  - 🚧 Verify permission flows and analyzer threading
- **Presentation Layer**: Wire navigation and state between screens
- **Navigation**: Add navigation graph and screen routes

### 📁 Current Project Structure
```
com.technitedminds.wallet/
├── MainActivity.kt ✅
├── WalletApplication.kt ✅
├── ui/theme/ ✅ (Color.kt, Theme.kt, Type.kt)
├── data/ ✅ COMPLETE
│   ├── local/database/ ✅ (entities, dao, converters, WalletDatabase)
│   ├── local/files/ ✅ (ImageFileManager)
│   ├── local/preferences/ ✅ (SimplePreferencesManager)
│   ├── repository/ ✅ (All repository implementations)
│   ├── mapper/ ✅ (CardMapper, CategoryMapper)
│   └── ocr/ 🚧 (Empty - MLKit integration pending)
├── domain/ ✅ COMPLETE
│   ├── model/ ✅ (Card, CardType, Category, CardImage)
│   ├── repository/ ✅ (All repository interfaces)
│   ├── usecase/ ✅ (card, category, export, ocr use cases)
│   └── util/ ✅
├── presentation/ ✅ PARTIAL
│   ├── components/
│   │   ├── camera/ ✅ (CameraError, CameraPermission, CameraPreview, CaptureButton, CardOverlay, ImagePreview)
│   │   ├── common/ ✅ (CardListItem, CardTypeSelector, CategoryChip, ColorPicker, ConfirmationDialog, ErrorMessage, LoadingIndicator, ValidatedTextField)
│   │   └── animation/ ✅ (AnimatedList, AnimationUtils, CardBack, CardFront)
│   ├── screens/ ✅ (HomeScreen/HomeViewModel, AddCardScreen/AddCardViewModel, CardDetailScreen/CardDetailViewModel)
│   └── navigation/ 📁 (Directory present)
├── di/ ✅ (DatabaseModule, RepositoryModule)
└── utils/ ✅ (Extensions.kt)
```