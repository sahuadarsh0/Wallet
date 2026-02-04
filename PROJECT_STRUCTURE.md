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
│   │   ├── cache/                # Image and data caching ✅
│   │   │   └── CacheManager.kt   # Performance optimization caching
│   │   ├── database/             # Room database components ✅
│   │   │   ├── entities/         # Room entities ✅
│   │   │   │   ├── CardEntity.kt # Card entity with type converters
│   │   │   │   └── CategoryEntity.kt # Category entity with indices
│   │   │   ├── dao/              # Data Access Objects ✅
│   │   │   │   ├── CardDao.kt    # Card CRUD, search, filtering
│   │   │   │   └── CategoryDao.kt # Category management operations
│   │   │   ├── converters/       # Room type converters ✅
│   │   │   │   ├── CardTypeConverter.kt # CardType serialization
│   │   │   │   ├── CardGradientConverter.kt # CardGradient serialization
│   │   │   │   └── MapConverter.kt # Map<String,String> serialization
│   │   │   └── WalletDatabase.kt # Room database configuration ✅
│   │   ├── files/                # File system operations ✅
│   │   │   └── ImageFileManager.kt # Image storage and optimization
│   │   ├── preferences/          # User preferences ✅
│   │   │   └── SimplePreferencesManager.kt # Proto DataStore integration
│   │   └── storage/              # Storage management ✅
│   │       └── StorageManager.kt # File operations and cleanup
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
├── domain/                       # Domain Layer ✅ IMPLEMENTED
│   ├── model/                    # Domain models ✅
│   │   ├── Card.kt               # Card data class with gradient support
│   │   ├── CardType.kt           # Sealed class for 15+ card types
│   │   ├── CardGradient.kt       # Gradient customization system
│   │   ├── CardImage.kt          # Image metadata handling
│   │   └── Category.kt           # Category data class
│   ├── repository/               # Repository interfaces ✅
│   │   ├── CardRepository.kt     # Card CRUD and search operations
│   │   ├── CategoryRepository.kt # Category management operations
│   │   ├── ImageRepository.kt    # Image storage operations

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
├── presentation/                 # Presentation Layer ✅ IMPLEMENTED
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
│   │   │   ├── CategoryUtils.kt  # Category icon/color utilities
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
│       └── PerformanceOptimizer.kt # Performance optimization utilities
├── di/                           # Dependency Injection modules ✅
│   ├── CameraModule.kt           # CameraX and ML Kit dependencies
│   ├── DatabaseModule.kt         # Room database Hilt module
│   └── RepositoryModule.kt       # Repository interface bindings
└── utils/                        # Utility classes and extensions ✅
    └── Extensions.kt             # Kotlin extension functions
```

## Card Type System

### Supported Card Types (15+ types)
The application supports a comprehensive set of card types with unique visual identities:

#### OCR-Enabled Cards (2 types)
- **Credit**: Purple-blue gradient, supports text extraction
- **Debit**: Pink-red gradient, supports text extraction

#### Image-Only Cards (13+ types)
- **TransportCard**: Blue-cyan gradient (metro, bus, train cards)
- **GiftCard**: Teal-pink gradient (gift cards, vouchers)
- **LoyaltyCard**: Cream-peach gradient (store loyalty cards)
- **MembershipCard**: Green-teal gradient (gym, club memberships)
- **InsuranceCard**: Purple-cream gradient (health, auto insurance)
- **IdentificationCard**: Light blue-blue gradient (ID cards, licenses)
- **Voucher**: Pink-yellow gradient (discount vouchers)
- **Event**: Cream-peach gradient (event tickets, passes)
- **BusinessCard**: Purple-blue gradient (business cards)
- **LibraryCard**: Green-teal gradient (library cards)
- **HotelCard**: Teal-pink gradient (hotel key cards)
- **StudentCard**: Light blue-blue gradient (student IDs)
- **AccessCard**: Purple-cream gradient (building access cards)
- **Custom**: User-defined types with customizable gradients

## Key Dependencies

### Core Android
- **AndroidX Core KTX**: `1.17.0`
- **Lifecycle Runtime**: `2.9.4`
- **Activity Compose**: `1.11.0`
- **Splash Screen**: `1.0.1`

### UI Framework
- **Jetpack Compose BOM**: `2025.09.01`
- **Material Design 3**: `1.4.0`
- **Material Icons**: Extended icon set
- **Google Fonts**: UI text with custom fonts

### Architecture & DI
- **Hilt**: `2.57.1` (android, compiler, navigation-compose `1.3.0`)
- **Navigation Compose**: `2.9.5` with kotlinx.serialization
- **Room**: `2.8.1` (runtime, ktx, compiler via KSP)

### Camera & ML
- **CameraX**: `1.5.0` (core, camera2, lifecycle, view, extensions)
- **ML Kit Text Recognition**: `16.0.1` (bundled, offline)

### Data & Storage
- **Proto DataStore**: `1.1.7` (core + preferences)
- **Coil Compose**: `2.7.0` (image loading)

### Security
- **Biometric**: `1.1.0`
- **Google Tink**: `1.18.0` (encryption)

### Serialization
- **Kotlinx Serialization JSON**: `1.7.0`

### Testing
- **JUnit**: `4.13.2`
- **AndroidX Test**: JUnit `1.3.0`, Espresso `3.7.0`
- **Compose UI Test**: junit4, test-manifest

## Security Configuration

### Network Permissions
The AndroidManifest.xml explicitly blocks all network permissions for complete offline operation:
- `android.permission.INTERNET` - Blocked (`tools:node="remove"`)
- `android.permission.ACCESS_NETWORK_STATE` - Blocked (`tools:node="remove"`)
- `android.permission.ACCESS_WIFI_STATE` - Blocked (`tools:node="remove"`)

### Allowed Permissions
- `android.permission.CAMERA` - Required for card scanning functionality

### File Sharing
- **FileProvider** configured for secure card image sharing
- Authority: `${applicationId}.fileprovider`
- Paths defined in `@xml/file_provider_paths`

## Build Configuration

- **Target SDK**: 36 (Android 15)
- **Min SDK**: 29 (Android 10)
- **Java Version**: 11
- **Kotlin**: 2.0.0
- **Compose Compiler**: 2.0.0

## Build Status: ✅ FULLY IMPLEMENTED WITH ENHANCED UI
The project is now fully implemented with enhanced UI components and premium user experience:
- Complete domain layer with 15+ card types and gradient system
- Advanced data layer with Room database, file storage, and cache management
- Comprehensive camera and OCR integration with ML Kit
- Material 3 theming and Compose setup with dynamic colors
- Complete Hilt dependency injection modules
- Full navigation system with type-safe arguments
- Card sharing functionality for both textual and image-only cards
- Performance optimizations and memory management
- **Enhanced UI Component Library:** PremiumCard, PremiumTextField, AnimatedSectionHeader, EnhancedSlideInItem
- **Enhanced Add Card Flow:** Multi-step wizard with premium animations and contextual guidance
- **Enhanced Card Detail Page:** Immersive viewing experience with smooth edit mode transitions
- **Advanced Animations:** Step transitions, section slide-ins, and enhanced interaction feedback
- All feature screens implemented with proper state management and premium styling

## Implementation Status

### ✅ Completed
- **Project Foundation**: Complete app structure with Hilt integration
  - MainActivity with Compose setup, splash screen, and Hilt integration
  - WalletApplication with Hilt configuration and context management
  - Material Design 3 theming (Color, Theme, Type) with dynamic colors
  - Build configuration with all dependencies and KSP setup
- **Domain Layer**: Complete implementation with all models, repositories, and use cases
  - 15+ card types with gradient system and sharing functionality
  - Card sharing functionality (images for image-only cards, gradient designs for textual cards)
  - Category management system with CRUD operations
  - Comprehensive validation and error handling
  - Storage management use cases for file operations
- **Data Layer**: Complete implementation with Room database and file storage
  - Room database with entities, DAOs, and type converters (CardType, CardGradient, Map)
  - Image file management with compression and optimization
  - Repository implementations with proper error handling
  - Data mapping utilities between domain and data models
  - Cache management for performance optimization
  - Storage management for file operations and cleanup
- **Camera & OCR Components**: Complete implementation
  - Multi-aspect ratio overlay for card positioning (16:9, 4:3, 3:4, credit card)
  - Comprehensive error handling for camera operations
  - ML Kit Text Recognition integration with confidence scoring
  - Camera UI components with proper lifecycle management
  - Card text parsing and validation for OCR results
- **Presentation Layer**: Complete implementation with all screens and components
  - Complete navigation system with type-safe arguments and bottom navigation
  - All feature screens implemented (Home, AddCard, Camera, CardDetail, Categories, Settings)
  - Comprehensive UI component library (common, animation, camera, sharing)
  - Performance optimization utilities and category management
  - Card sharing functionality with multiple options
- **Navigation System**: Complete implementation
  - WalletNavigation with all routes and arguments
  - WalletAppScaffold with bottom navigation integration
  - BottomNavigation with category count badges
  - Navigation extensions and utility functions
  - Type-safe navigation with proper back stack management

### 🚧 Next Steps
- **Testing**: Unit tests for ViewModels, use cases, and repository implementations
- **Integration Tests**: End-to-end testing for card workflows and camera integration
- **UI Polish**: Fine-tuning animations, transitions, and user experience
- **Accessibility**: Screen reader support and accessibility improvements
- **Performance Monitoring**: Memory usage optimization and startup time improvements
- **Documentation**: API documentation and user guides

### 📁 Current Project Structure
```
com.technitedminds.wallet/
├── MainActivity.kt ✅ (Splash screen, Hilt integration)
├── WalletApplication.kt ✅ (Hilt configuration, context management)
├── ui/theme/ ✅ (Color.kt, Theme.kt, Type.kt with dynamic colors)
├── data/ ✅ COMPLETE
│   ├── local/
│   │   ├── cache/ ✅ (CacheManager.kt)
│   │   ├── database/ ✅ (entities, dao, converters, WalletDatabase)
│   │   │   ├── entities/ ✅ (CardEntity.kt, CategoryEntity.kt)
│   │   │   ├── dao/ ✅ (CardDao.kt, CategoryDao.kt)
│   │   │   ├── converters/ ✅ (CardTypeConverter.kt, CardGradientConverter.kt, MapConverter.kt)
│   │   │   └── WalletDatabase.kt ✅
│   │   ├── files/ ✅ (ImageFileManager.kt)
│   │   ├── preferences/ ✅ (SimplePreferencesManager.kt)
│   │   └── storage/ ✅ (StorageManager.kt)
│   ├── mapper/ ✅ (CardMapper.kt, CategoryMapper.kt)
│   ├── ocr/ ✅ (CardTextParser.kt, MLKitTextRecognizer.kt)
│   ├── repository/ ✅ (CardRepositoryImpl.kt, CategoryRepositoryImpl.kt, ImageRepositoryImpl.kt)
│   └── service/ ✅ (CardImageGeneratorImpl.kt, OCRServiceImpl.kt, StorageServiceImpl.kt)
├── domain/ ✅ COMPLETE
│   ├── model/ ✅ (Card.kt, CardType.kt, Category.kt, CardImage.kt, CardGradient.kt)
│   ├── repository/ ✅ (CardRepository.kt, CategoryRepository.kt, ImageRepository.kt)
│   ├── service/ ✅ (CardImageGenerator.kt, OCRService.kt, StorageService.kt)
│   ├── usecase/ ✅ (card, category, ocr, storage use cases with card sharing)
│   │   ├── card/ ✅ (AddCardUseCase.kt, GetCardsUseCase.kt, UpdateCardUseCase.kt, DeleteCardUseCase.kt, ShareCardUseCase.kt)
│   │   ├── category/ ✅ (GetCategoriesUseCase.kt, GetCategoryNameUseCase.kt, ManageCategoryUseCase.kt)
│   │   ├── ocr/ ✅ (ProcessCardImageUseCase.kt)
│   │   └── storage/ ✅ (StorageManagementUseCase.kt)
├── presentation/ ✅ COMPLETE WITH ENHANCED UI
│   ├── components/
│   │   ├── animation/ ✅ (AnimatedList.kt, AnimationUtils.kt, CardBack.kt, CardFront.kt, FlippableCard.kt, EnhancedAnimations.kt)
│   │   ├── camera/ ✅ (CameraError.kt, CameraManager.kt, CameraPermission.kt, CameraPermissionComponent.kt, CameraPreview.kt, CaptureButton.kt, CardOverlay.kt, ImagePreview.kt)
│   │   ├── common/ ✅ (Enhanced UI Components Library)
│   │   │   ├── EnhancedComponents.kt ✅ (PremiumCard, PremiumTextField, AnimatedSectionHeader, PremiumButton, PremiumChip)
│   │   │   ├── StepProgressIndicator.kt ✅ (Multi-step progress tracking)
│   │   │   ├── OCRStatusCards.kt ✅ (OCRStatusCard, ManualEntryCard)
│   │   │   ├── PrivacyNoticeCard.kt ✅ (Offline-only operation notice)
│   │   │   ├── CardListItem.kt, CardTypeSelector.kt, CategoryChip.kt, CategoryPreview.kt ✅
│   │   │   ├── ColorPicker.kt, GradientPicker.kt, GradientPickerDialog.kt, IconPicker.kt ✅
│   │   │   ├── ConfirmationDialog.kt, ErrorMessage.kt, LoadingIndicator.kt, ValidatedTextField.kt ✅
│   │   │   ├── CustomFieldsEditor.kt, ExtractedDataEditor.kt ✅
│   │   │   └── CategoryUtils.kt, ModelExtensions.kt ✅
│   │   └── sharing/ ✅ (CardSharingManager.kt, CardSharingOption.kt, CardSharingDialog.kt)
│   ├── screens/ ✅ (All screens with enhanced UI and ViewModels)
│   │   ├── home/ ✅ (EnhancedHomeScreen.kt, HomeViewModel.kt)
│   │   ├── addcard/ ✅ (Enhanced AddCardScreen.kt with multi-step wizard, AddCardViewModel.kt)
│   │   │   └── components/ ✅ (Enhanced add card components)
│   │   ├── camera/ ✅ (CameraScreen.kt, CameraViewModel.kt)
│   │   ├── carddetail/ ✅ (Enhanced CardDetailScreen.kt with premium editing, CardDetailViewModel.kt)
│   │   ├── categories/ ✅ (CategoriesScreen.kt, CategoriesViewModel.kt, CategoryDialog.kt)
│   │   └── settings/ ✅ (SettingsScreen.kt, SettingsViewModel.kt)
│   ├── navigation/ ✅ (Complete navigation system)
│   │   ├── BottomNavigation.kt ✅
│   │   ├── NavigationDestinations.kt ✅
│   │   ├── NavigationExtensions.kt ✅
│   │   ├── WalletAppScaffold.kt ✅
│   │   └── WalletNavigation.kt ✅
│   ├── constants/ ✅ (AppConstants.kt)
│   └── utils/ ✅ (PerformanceOptimizer.kt)
├── di/ ✅ (CameraModule.kt, DatabaseModule.kt, RepositoryModule.kt)
└── utils/ ✅ (Extensions.kt)
```

**Notes**: 
- Empty placeholder folders (`add_card/`, `card_detail/`, `presentation/camera/`, `components/category/`, `util/`) exist in the codebase but are not documented as they contain no implementation files.
- Actual screen implementations are in `addcard/` and `carddetail/` (not the underscore variants).
- Camera screen is located in `presentation/screens/camera/` (not in presentation root).
- The `CategoryUtils.kt` file has been removed from `presentation/utils` as it was relocated to `presentation/components/common`.