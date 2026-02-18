# CardVault Project Structure

This document outlines the feature-based architecture implemented for the CardVault Android application.

## Package Organization

The project follows a clean architecture pattern with clear separation of concerns:

```
com.technitedminds.wallet/
├── MainActivity.kt               # Main entry point with AppPhase state machine ✅
├── WalletApplication.kt          # Application class with @HiltAndroidApp ✅
├── ui/
│   └── theme/                    # Material Design 3 theming & Glassmorphism ✅
│       ├── Color.kt              # Color definitions
│       ├── ColorUtils.kt         # Color manipulation utilities
│       ├── GlassTheme.kt         # Glassmorphism theme implementation
│       ├── PhysicsConstants.kt   # Physics-based animation constants
│       ├── Theme.kt              # Theme configuration
│       └── Type.kt               # Typography definitions
├── data/                         # Data Layer - COMPLETE ✅
│   ├── local/
│   │   ├── cache/                # Image and data caching
│   │   │   └── CacheManager.kt   # Performance optimization caching
│   │   ├── database/             # Room database components
│   │   │   ├── converters/       # Room type converters
│   │   │   │   ├── CardGradientConverter.kt
│   │   │   │   ├── CardTypeConverter.kt
│   │   │   │   └── MapConverter.kt
│   │   │   ├── dao/              # Data Access Objects
│   │   │   │   ├── CardDao.kt
│   │   │   │   └── CategoryDao.kt
│   │   │   ├── entities/         # Room entities
│   │   │   │   ├── CardEntity.kt
│   │   │   │   └── CategoryEntity.kt
│   │   │   └── WalletDatabase.kt # Room database configuration
│   │   ├── files/                # File system operations
│   │   │   └── ImageFileManager.kt
│   │   ├── preferences/          # User preferences
│   │   │   └── SimplePreferencesManager.kt
│   │   ├── security/             # Security Core
│   │   │   ├── AppLockRepository.kt
│   │   │   ├── PinHasher.kt
│   │   │   └── RecoveryCodeManager.kt
│   │   └── storage/              # Storage management
│   │       └── StorageManager.kt
│   ├── mapper/                   # Data mapping utilities
│   │   ├── CardMapper.kt
│   │   └── CategoryMapper.kt
│   ├── ocr/                      # OCR processing
│   │   ├── CardTextParser.kt
│   │   └── MLKitTextRecognizer.kt
│   ├── repository/               # Repository implementations
│   │   ├── CardRepositoryImpl.kt
│   │   ├── CategoryRepositoryImpl.kt
│   │   └── ImageRepositoryImpl.kt
│   └── service/                  # Service implementations
│       ├── CardImageGeneratorImpl.kt
│       ├── OCRServiceImpl.kt
│       └── StorageServiceImpl.kt
├── di/                           # Dependency Injection modules ✅
│   ├── CameraModule.kt
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   └── SecurityModule.kt
├── domain/                       # Domain Layer - COMPLETE ✅
│   ├── model/                    # Domain models
│   │   ├── Card.kt
│   │   ├── CardGradient.kt
│   │   ├── CardImage.kt
│   │   ├── CardType.kt
│   │   ├── Category.kt
│   │   ├── OCRResult.kt
│   │   └── StorageModels.kt
│   ├── repository/               # Repository interfaces
│   │   ├── CardRepository.kt
│   │   ├── CategoryRepository.kt
│   │   └── ImageRepository.kt
│   ├── service/                  # Service interfaces
│   │   ├── CardImageGenerator.kt
│   │   ├── OCRService.kt
│   │   └── StorageService.kt
│   ├── usecase/                  # Business logic use cases
│   │   ├── card/
│   │   │   ├── AddCardUseCase.kt
│   │   │   ├── DeleteCardUseCase.kt
│   │   │   ├── GetCardsUseCase.kt
│   │   │   ├── ShareCardUseCase.kt
│   │   │   └── UpdateCardUseCase.kt
│   │   ├── category/
│   │   │   ├── GetCategoriesUseCase.kt
│   │   │   ├── GetCategoryNameUseCase.kt
│   │   │   └── ManageCategoryUseCase.kt
│   │   ├── ocr/
│   │   │   └── ProcessCardImageUseCase.kt
│   │   └── storage/
│   │       └── StorageManagementUseCase.kt
│   └── util/                     # Domain utilities
├── presentation/                 # Presentation Layer - COMPLETE ✅
│   ├── components/               # Reusable UI components
│   │   ├── animation/            # Animation components
│   │   │   ├── AnimatedList.kt
│   │   │   ├── AnimationUtils.kt
│   │   │   ├── CardBack.kt
│   │   │   ├── CardFront.kt
│   │   │   ├── EnhancedAnimations.kt
│   │   │   ├── FlippableCard.kt
│   │   │   └── LiquidDrag.kt
│   │   ├── camera/               # Camera components
│   │   │   ├── CameraError.kt
│   │   │   ├── CameraManager.kt
│   │   │   ├── CameraPermission.kt
│   │   │   ├── CameraPermissionComponent.kt
│   │   │   ├── CameraPreview.kt
│   │   │   ├── CaptureButton.kt
│   │   │   ├── CardOverlay.kt
│   │   │   └── ImagePreview.kt
│   │   ├── common/               # Common UI components
│   │   │   ├── CardListItem.kt
│   │   │   ├── CardTypeDropdown.kt
│   │   │   ├── CardTypeSelector.kt
│   │   │   ├── CategoryChip.kt
│   │   │   ├── CategoryDropdown.kt
│   │   │   ├── CategoryPreview.kt
│   │   │   ├── CategoryUtils.kt
│   │   │   ├── ColorPicker.kt
│   │   │   ├── ConfirmationDialog.kt
│   │   │   ├── CustomFieldsEditor.kt
│   │   │   ├── EnhancedColorPicker.kt
│   │   │   ├── EnhancedComponents.kt
│   │   │   ├── ErrorMessage.kt
│   │   │   ├── ExtractedDataEditor.kt
│   │   │   ├── GradientPicker.kt
│   │   │   ├── GradientPickerDialog.kt
│   │   │   ├── IconPicker.kt
│   │   │   ├── LoadingIndicator.kt
│   │   │   ├── ModelExtensions.kt
│   │   │   ├── OCRStatusCards.kt
│   │   │   ├── PrivacyNoticeCard.kt
│   │   │   ├── SplashOverlay.kt
│   │   │   ├── StepProgressIndicator.kt
│   │   │   └── ValidatedTextField.kt
│   │   └── sharing/              # Sharing components
│   │       ├── CardSharingDialog.kt
│   │       ├── CardSharingManager.kt
│   │       └── CardSharingOption.kt
│   ├── constants/                # App constants
│   │   └── AppConstants.kt
│   ├── navigation/               # Navigation setup
│   │   ├── BottomNavigation.kt
│   │   ├── NavigationDestinations.kt
│   │   ├── NavigationExtensions.kt
│   │   ├── WalletAppScaffold.kt
│   │   └── WalletNavigation.kt
│   ├── screens/                  # Feature screens
│   │   ├── addcard/              # Add card flow
│   │   │   ├── AddCardScreen.kt
│   │   │   ├── AddCardViewModel.kt
│   │   │   └── components/
│   │   ├── camera/               # Camera screen
│   │   │   ├── CameraScreen.kt
│   │   │   └── CameraViewModel.kt
│   │   ├── carddetail/           # Card detail view
│   │   │   ├── CardDetailScreen.kt
│   │   │   └── CardDetailViewModel.kt
│   │   ├── categories/           # Category management
│   │   │   ├── CategoriesScreen.kt
│   │   │   ├── CategoriesViewModel.kt
│   │   │   └── CategoryDialog.kt
│   │   ├── home/                 # Home screen
│   │   │   ├── EnhancedHomeScreen.kt
│   │   │   └── HomeViewModel.kt
│   │   ├── onboarding/           # Onboarding flow
│   │   │   └── OnboardingPinScreen.kt
│   │   ├── security/             # Security screens
│   │   │   ├── AppLockScreen.kt
│   │   │   ├── AppLockViewModel.kt
│   │   │   └── BiometricAuthManager.kt
│   │   └── settings/             # App settings
│   │       ├── SettingsScreen.kt
│   │       └── SettingsViewModel.kt
│   └── utils/                    # Presentation utilities
│       └── PerformanceOptimizer.kt
└── utils/                        # General utilities
    └── Extensions.kt
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
