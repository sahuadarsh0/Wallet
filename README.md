# CardVault: Secure Offline Card Repository

CardVault is a secure, offline-first Android application designed to provide users with a premium digital repository for all their physical cards. The primary goal is to create an intuitive, visually stunning, and completely secure wallet app that operates entirely offline.

This project is built from the ground up using modern Android development practices, including a 100% Kotlin codebase, Jetpack Compose for the UI, and a robust clean architecture.

## ✨ Key Features

- **🔒 100% Offline Operation:** Operates entirely without an internet connection. No network permissions, no analytics, and no data ever leaves your device.
- **📱 Modern UI/UX:** A beautiful and intuitive interface built entirely with **Jetpack Compose** and **Material Design 3**, featuring smooth 60fps animations, including a realistic 3D card flip effect.
- **📸 Smart Card Scanning:** Add cards quickly using your camera. On-device **ML Kit** recognizes and extracts card details for textual cards (credit/debit/ATM), with manual fallback options.
- **🎨 Flexible Organization:** Group your cards into default or custom-created categories (e.g., 'Work', 'Travel', 'Memberships'), each with a distinct color theme and icon.
- **💳 Dual Card Processing:** Handles both textual cards (with OCR processing) and image-only cards (for visual storage) with appropriate workflows.
- **🔍 Aspect Ratio Selection:** Camera overlay supports multiple aspect ratios (16:9, 4:3, 3:4, credit card) for different card types.
- **📤 Data Portability:** Export/import all your card data and images to a single .wallet file for backup or device migration.
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
  - [CameraX](https://developer.android.com/training/camerax) (core, camera2, lifecycle, view) for modern camera implementation
  - [Google ML Kit](https://developers.google.com/ml-kit) (Text Recognition) for on-device OCR processing
- **Image Loading:** [Coil](https://coil-kt.github.io/coil/) for efficient image loading and caching
- **Security:** [Google Tink](https://github.com/google/tink) for file encryption

## 🏗️ Project Structure

```
com.technitedminds.wallet/
├── MainActivity.kt               # Main entry point with @AndroidEntryPoint
├── WalletApplication.kt          # Application class with @HiltAndroidApp
├── ui/
│   └── theme/                    # Material Design 3 theming
│       ├── Color.kt              # Color definitions
│       ├── Theme.kt              # Theme configuration
│       └── Type.kt               # Typography definitions
├── data/                         # Data Layer - COMPLETE
│   ├── local/
│   │   ├── database/             # Room database components
│   │   │   ├── entities/         # Room entities (CardEntity, CategoryEntity)
│   │   │   ├── dao/              # Data Access Objects (CardDao, CategoryDao)
│   │   │   ├── converters/       # Type converters (CardType, Map)
│   │   │   └── WalletDatabase.kt # Room database configuration
│   │   ├── files/                # File system operations
│   │   │   └── ImageFileManager.kt # Image storage and optimization
│   │   └── preferences/          # User preferences
│   │       └── SimplePreferencesManager.kt # Proto DataStore integration
│   └── repository/               # Repository implementations
│       ├── CardRepositoryImpl.kt # Card operations with Room
│       ├── CategoryRepositoryImpl.kt # Category management
│       ├── ImageRepositoryImpl.kt # Image file operations
│       └── ExportImportRepositoryImpl.kt # Data portability
├── domain/                       # Domain Layer - COMPLETE
│   ├── model/                    # Domain models (Card, CardType, Category, CardImage)
│   ├── repository/               # Repository interfaces
│   ├── usecase/                  # Business logic use cases (card, ocr, category, export)
│   └── util/                     # Domain utilities
├── presentation/                 # Presentation Layer - COMPLETE
│   ├── screens/                  # Feature screens (home, addcard, carddetail, categories, settings)
│   ├── components/               # Reusable UI components (common, animation, camera)
│   └── navigation/               # Navigation setup
├── di/                           # Dependency Injection modules
│   ├── DatabaseModule.kt         # Room database Hilt module
│   ├── RepositoryModule.kt       # Repository interface bindings
│   └── AppModule.kt              # Additional Hilt bindings
└── utils/                        # Utility classes and extensions
```

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

## 📋 Implementation Status

### ✅ Complete Features
- **Complete Architecture:** Clean MVVM with UDF pattern
- **Textual Cards:** Credit/Debit/ATM cards with OCR processing via ML Kit
- **Image Cards:** Gym/Voucher/Other cards with simple image storage
- **Camera Integration:** Full CameraX implementation with overlay guides
- **3D Card Flip:** Smooth animations using Compose graphicsLayer
- **Category Management:** Custom categories with icons and colors
- **Data Export/Import:** Full card data and image portability
- **Performance Optimized:** 60fps animations and efficient image handling
- **Material Design 3:** Consistent UI with proper theming
- **Offline-First:** Zero network dependencies, all data stays on device

### 🔄 In Progress Features
- Navigation implementation and screen routing
- Complete test suite with unit and integration tests
- Additional performance optimizations for large card collections

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE.md) file for details.
