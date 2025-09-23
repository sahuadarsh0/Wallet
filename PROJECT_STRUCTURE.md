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
├── data/                          # Data Layer ✅ IMPLEMENTED
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
│   │       └── SimplePreferencesManager.kt # SharedPreferences-based
│   ├── repository/               # Repository implementations ✅
│   │   ├── CardRepositoryImpl.kt # Card operations with Room
│   │   ├── CategoryRepositoryImpl.kt # Category management
│   │   ├── ImageRepositoryImpl.kt # Image file operations
│   │   └── ExportImportRepositoryImpl.kt # Data portability
│   └── mapper/                   # Data mapping utilities ✅
│       ├── CardMapper.kt         # Card entity ↔ domain mapping
│       └── CategoryMapper.kt     # Category entity ↔ domain mapping
├── domain/                       # Domain Layer ✅ IMPLEMENTED
│   ├── model/                    # Domain models ✅
│   │   ├── Card.kt               # Card data class with all fields
│   │   ├── CardType.kt           # Sealed class for card types
│   │   ├── CardImage.kt          # Image metadata handling
│   │   └── Category.kt           # Category data class
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
│   │   │   ├── GetCategoriesUseCase.kt # Category retrieval
│   │   │   └── ManageCategoryUseCase.kt # Category CRUD operations
│   │   └── export/               # Export/import use cases ✅
│   │       ├── ExportDataUseCase.kt # Data export functionality
│   │       └── ImportDataUseCase.kt # Data import functionality
│   └── util/                     # Domain utilities
├── presentation/                 # Presentation Layer
│   ├── screens/                  # Feature screens
│   │   ├── home/                 # Home screen components
│   │   ├── add_card/             # Add card flow
│   │   ├── card_detail/          # Card detail view
│   │   ├── categories/           # Category management
│   │   └── settings/             # App settings
│   ├── components/               # Reusable UI components
│   │   ├── common/               # Common UI components (empty)
│   │   ├── animation/            # Animation components (empty)
│   │   └── camera/               # Camera-related components (partial)
│   │       ├── CardOverlay.kt    # Multi-aspect ratio card positioning overlay ✅
│   │       └── CameraError.kt    # Camera error handling and UI ✅
│   └── navigation/               # Navigation setup
├── di/                           # Dependency Injection modules
└── utils/                        # Utility classes and extensions
```

## Key Dependencies Added

### Core Dependencies
- **CameraX**: Camera functionality for card scanning
- **ML Kit Text Recognition**: Offline OCR processing
- **Proto DataStore**: Type-safe preferences storage
- **Coil**: Image loading and caching
- **Biometric**: Biometric authentication
- **Tink**: Cryptographic operations

### Architecture Dependencies
- **Hilt**: Dependency injection
- **Room**: Local database
- **Navigation Compose**: Type-safe navigation
- **Jetpack Compose**: Modern UI toolkit

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
The project builds successfully with all implemented layers:
- Domain layer with complete business logic
- Data layer with Room database and file storage
- Basic UI foundation with Material Design 3
- Hilt dependency injection setup
- Partial camera and OCR components

## Recent Development Progress
**Task 4: Camera and OCR Integration** - Partially Completed
- ✅ **Task 4.1**: CameraX integration foundation
- ✅ **Task 4.2**: ML Kit text recognition structure  
- ✅ **Task 4.3**: Core camera UI components (CardOverlay with multi-aspect ratio support, CameraError handling)
- 🚧 **Remaining**: Complete dependency integration and remaining camera components

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
- **Camera & OCR Integration**: Complete CameraX and ML Kit integration
  - ✅ Task 4.1: CameraX integration foundation completed
  - ✅ Task 4.2: ML Kit text recognition structure implemented
  - ✅ Task 4.3: Core camera UI components (CardOverlay, CameraError) completed
  - 🚧 Add missing dependencies for CameraX, ML Kit, and Accompanist permissions
  - 🚧 Implement remaining components: CameraPreview, CameraPermission, CaptureButton
  - 🚧 Complete MLKitTextRecognizer for offline OCR processing
  - 🚧 Add camera controls and image preview functionality
- **Presentation Layer**: Implement ViewModels, Compose screens, and UI components
- **Dependency Injection**: Set up Hilt modules for all layers
- **UI Components**: Create card flip animations and reusable components
- **Navigation**: Set up Compose navigation between screens

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
├── presentation/ 🚧 PARTIAL
│   ├── components/
│   │   ├── camera/ 🚧 (CardOverlay, CameraError - 2/6 components)
│   │   ├── common/ 📁 (Empty)
│   │   └── animation/ 📁 (Empty)
│   ├── screens/ 📁 (Empty directories created)
│   └── navigation/ 📁 (Empty)
├── di/ 📁 (Empty - Hilt modules pending)
└── utils/ ✅ (Extensions.kt)
```