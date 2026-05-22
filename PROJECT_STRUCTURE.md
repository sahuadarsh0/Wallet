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
│   │   │   ├── RecoveryCodeManager.kt
│   │   │   └── TinkEncryptionManager.kt
│   │   └── storage/              # Storage management
│   │       └── StorageManager.kt
│   ├── mapper/                   # Data mapping utilities
│   │   ├── CardMapper.kt
│   │   └── CategoryMapper.kt
│   ├── nfc/                      # NFC EMV reader (Credit/Debit only)
│   │   ├── EmvCardReader.kt
│   │   ├── NfcReaderCallback.kt
│   │   └── TlvParser.kt
│   ├── ocr/                      # OCR processing
│   │   ├── CardTextParser.kt
│   │   └── MLKitTextRecognizer.kt
│   ├── repository/               # Repository implementations
│   │   ├── CardRepositoryImpl.kt
│   │   ├── CategoryRepositoryImpl.kt
│   │   └── ImageRepositoryImpl.kt
│   └── service/                  # Service implementations
│       ├── CardImageDrawing.kt   # Pure Canvas drawing primitives (background, shimmer, EMV chip, text)
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
│   │   │   ├── PremiumSearchBar.kt
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
│   │   │   ├── FolderComponents.kt # Folder grid tiles + OpenedFolder state model
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

> Source of truth: `gradle/libs.versions.toml`. Versions below mirror the catalog at the time of writing — re-sync when the catalog changes.

### Core Android
- **AndroidX Core KTX**: `1.17.0`
- **Lifecycle Runtime**: `2.10.0`
- **Activity Compose**: `1.12.3`
- **Splash Screen**: `1.2.0`

### UI Framework
- **Jetpack Compose BOM**: `2026.01.01`
- **Material Design 3**: `1.4.0`
- **Material Icons**: Extended icon set (`material-icons-core` + `material-icons-extended`)
- **Compose UI Text Google Fonts**: `1.10.2`

### Architecture & DI
- **Hilt**: `2.59.1` (android, compiler, navigation-compose `1.3.0`)
- **Navigation Compose**: `2.9.7`
- **Room**: `2.8.4` (runtime, ktx, compiler via KSP)

### Camera & ML
- **CameraX**: `1.5.3` (core, camera2, lifecycle, view, extensions)
- **ML Kit Text Recognition**: `16.0.1` (bundled, offline)

### Data & Storage
- **DataStore Preferences**: `1.2.0` (core + preferences)
- **Coil Compose**: `2.7.0` (image loading)

### Security
- **AndroidX Biometric**: `1.1.0`
- **Google Tink Android**: `1.20.0` (AES-256-GCM encryption + Android Keystore)

### Serialization
- **Kotlinx Serialization JSON**: `1.10.0`

### Testing
- **JUnit**: `4.13.2`
- **AndroidX Test**: JUnit `1.3.0`, Espresso `3.7.0`
- **Compose UI Test**: junit4, test-manifest

## Security Configuration

### Network Permissions
The AndroidManifest.xml explicitly blocks all network permissions for complete offline operation:
- `android.permission.INTERNET` — Blocked (`tools:node="remove"`)
- `android.permission.ACCESS_NETWORK_STATE` — Blocked (`tools:node="remove"`)
- `android.permission.ACCESS_WIFI_STATE` — Blocked (`tools:node="remove"`)

### Allowed Permissions
- `android.permission.CAMERA` — Card scanning via CameraX (runtime, dangerous)
- `android.permission.NFC` — EMV contactless reading (normal); hardware feature declared as `required="false"`

### Backup Hardening
- `android:dataExtractionRules="@xml/data_extraction_rules"` (API 31+)
- `android:fullBackupContent="@xml/backup_rules"` (legacy)
- Excludes Room DB, encrypted Tink keyset prefs (`cardvault_keyset_prefs`), card images, thumbnails, datastore, and storage prefs from cloud backup AND device-transfer.

### Encryption
- **At-rest:** AES-256-GCM via Google Tink (`TinkEncryptionManager`); keyset encrypted under master key in Android Keystore (hardware-backed where supported)
- **PIN:** PBKDF2-HmacSHA256, 10 000 iterations, random per-user 16-byte salt (`PinHasher`)
- **Recovery code:** 16-character human-readable code (no O/0/I/1); same hashing scheme; plaintext shown ONCE
- **Sensitive Room columns:** `MapConverter` transparently encrypts JSON with `ENC:` prefix; backward-compatible with legacy plaintext rows

### File Sharing
- **FileProvider** configured for secure card image sharing
- Authority: `${applicationId}.fileprovider`
- Paths defined in `@xml/file_provider_paths` (only `files-path`, `cache-path`, `external-cache-path` — no `root-path`, no `external-files-path`)
- `android:exported="false"`; URI grants made via `FLAG_GRANT_READ_URI_PERMISSION` on a per-share basis

### Security Source Files
- `data/local/security/TinkEncryptionManager.kt` — AES-256-GCM Aead wrapper backed by Android Keystore
- `data/local/security/PinHasher.kt` — PBKDF2 hash + verify
- `data/local/security/RecoveryCodeManager.kt` — 16-char code generator + verifier
- `data/local/security/AppLockRepository.kt` — PIN setup, verification, rate limiting (5 → lockout, 10 → wipe), biometric flag, lock timeout, full-data wipe
- `presentation/screens/security/BiometricAuthManager.kt` — `BiometricPrompt` wrapper

### NFC / EMV Source Files
- `data/nfc/EmvCardReader.kt` — APDU exchange + AID select for EMV
- `data/nfc/TlvParser.kt` — BER-TLV decoder for card data tags (PAN, expiry, cardholder, etc.)
- `data/nfc/NfcReaderCallback.kt` — `NfcAdapter.ReaderCallback`-based scan session

## Build Configuration

- **AGP**: 9.0.0
- **Target SDK**: 36 (Android 15)
- **Min SDK**: 29 (Android 10)
- **Java Version**: 11
- **Kotlin**: 2.1.0
- **KSP**: 2.3.5
- **Compose Compiler**: managed by `kotlin-compose` plugin (Kotlin 2.1.0)
- **Release build:** `isMinifyEnabled = true`, `isShrinkResources = true`
- **Signing:** release config wired via `keystore.properties` (git-ignored)

## Compliance & Release Documentation

- [`SECURITY_CHECKLIST.md`](./SECURITY_CHECKLIST.md) — exhaustive security audit (re-run before every release)
- [`PLAY_STORE_UPLOAD_CHECKLIST.md`](./PLAY_STORE_UPLOAD_CHECKLIST.md) — end-to-end Play Console submission checklist
- [`DATA_SAFETY_FORM.md`](./DATA_SAFETY_FORM.md) — verbatim answers for the Play Console Data Safety form
