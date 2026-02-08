# CardVault: Secure Offline Card Repository

CardVault is a secure, offline-first Android application designed to provide users with a premium digital repository for all their physical cards. The primary goal is to create an intuitive, visually stunning, and completely secure wallet app that operates entirely offline.

This project is built from the ground up using modern Android development practices, including a 100% Kotlin codebase, Jetpack Compose for the UI, and a robust clean architecture.

## ✨ Key Features

- **🔒 100% Offline Operation:** Operates entirely without an internet connection. No network permissions, no analytics, and no data ever leaves your device.
- **🛡️ Advanced Security:**
  - **App Lock:** Secure the app with a 4-digit PIN, hashed locally using **PBKDF2 with HmacSHA256** and unique salts.
  - **Biometric Unlock:** Seamless integration with Android BiometricPrompt (Fingerprint/Face unlock).
  - **Rate Limiting:** Exponential backoff for failed PIN attempts to prevent brute-force attacks.
  - **Recovery System:** 16-character human-readable recovery codes for PIN resets.
  - **Auto Data Wipe:** Optional security feature to wipe all local data after maximum failed PIN attempts.
- **📱 Modern UI/UX:** A beautiful and intuitive interface built entirely with **Jetpack Compose** and **Material Design 3**, featuring smooth 60fps animations, including a realistic 3D card flip effect.
- **📸 Smart Card Scanning:** Add cards quickly using your camera. On-device **ML Kit** recognizes and extracts card details for textual cards (credit/debit), with manual fallback options.
- **🎨 Flexible Organization:** Group your cards into default or custom-created categories (e.g., 'Work', 'Travel', 'Memberships'), each with a distinct color theme and icon.
- **💳 Dual Card Processing:** Handles both textual cards (Credit/Debit with OCR processing) and image-only cards (15+ types) with appropriate workflows.
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
  - **Preferences:** [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore) for user settings and security flags
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
├── MainActivity.kt               # Main entry point with AppPhase state machine ✅
├── WalletApplication.kt          # Application class with @HiltAndroidApp ✅
├── ui/
│   └── theme/                    # Material Design 3 theming ✅
├── data/                         # Data Layer - COMPLETE ✅
│   ├── local/
│   │   ├── database/             # Room database (Cards, Categories) ✅
│   │   ├── files/                # File system operations ✅
│   │   ├── preferences/          # SimplePreferencesManager (DataStore) ✅
│   │   ├── security/             # Security Core ✅
│   │   │   ├── PinHasher.kt      # PBKDF2 hashing utility
│   │   │   ├── RecoveryCodeManager.kt # Recovery code generation/hashing
│   │   │   └── AppLockRepository.kt # Security state & rate limiting
│   │   └── storage/              # StorageManager (Cleanup & Stats) ✅
│   ├── ocr/                      # ML Kit implementation ✅
│   └── repository/               # Repository implementations ✅
├── domain/                       # Domain Layer - COMPLETE ✅
│   ├── model/                    # Domain models (Card, CardType, Category) ✅
│   ├── repository/               # Repository interfaces ✅
│   └── usecase/                  # Business logic use cases ✅
├── presentation/                 # Presentation Layer - COMPLETE ✅
│   ├── screens/                  # Feature screens ✅
│   │   ├── home/                 # EnhancedHomeScreen with stats & filters
│   │   ├── addcard/              # Multi-step AddCard wizard
│   │   ├── camera/               # Dedicated CameraScreen
│   │   ├── carddetail/           # 3D Flip CardDetail view
│   │   ├── categories/           # Category management UI
│   │   ├── security/             # AppLockScreen & BiometricAuthManager
│   │   └── settings/             # Redesigned SettingsScreen
│   ├── components/               # Premium UI Component Library ✅
│   │   ├── animation/            # Staggered entrance & liquid animations
│   │   ├── camera/               # Multi-aspect ratio overlays
│   │   └── common/               # PremiumCard, PremiumTextField, etc.
│   └── navigation/               # Navigation setup (Compose & NavHost) ✅
├── di/                           # Hilt DI Modules (Security, Database, etc.) ✅
└── utils/                        # Utility classes and extensions ✅
```

## 🚀 Getting Started

To build and run this project locally, follow these steps:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/sahuadarsh0/wallet.git
   ```

2. **Open in Android Studio:**
   - Open Android Studio (latest stable version recommended)
   - Click on `File > Open` and select the cloned project directory

3. **Gradle Sync:**
   - Allow Android Studio to download all dependencies and sync the project with the Gradle files

4. **Build and Run:**
   - Select the `app` configuration and a target device (emulator or physical device)
   - Click the "Run" button

## 🔒 Security & Privacy

CardVault is built with a **Privacy-First** philosophy:

- **Offline Mandate:** No internet permissions are requested. All processing, including OCR and image storage, happens on-device.
- **Cryptographic PIN Protection:** PINs are never stored in plaintext. We use PBKDF2 with 10,000 iterations and a random salt for every user.
- **Biometric Integration:** We use the system's secure biometric hardware. The app never sees your biometric data.
- **Secure Storage:** Card images are stored in internal storage, inaccessible to other applications.
- **Anti-Brute Force:** The app implements a lockout period that increases exponentially with failed attempts.

## 🎨 Visual Identity

CardVault supports 15+ card types with unique gradient designs:

- **OCR-Enabled:** Credit and Debit cards with automatic field extraction.
- **Image-Only:** Transport, Gift, Loyalty, Membership, Insurance, ID, Voucher, Event, Business, Library, Hotel, Student, Access cards.

## 📋 Implementation Status

### ✅ Complete Features
- **Security Core:** PIN hashing, Biometric unlock, Rate limiting, Recovery codes.
- **Clean Architecture:** Fully implemented MVVM with UDF and Hilt.
- **Premium UI:** Redesigned screens with staggered animations and liquid effects.
- **Smart OCR:** On-device text recognition using Google ML Kit.
- **Flexible Storage:** Automatic image optimization and comprehensive storage cleanup.
- **Privacy First:** 100% offline operation with no network dependencies.
- **Terms & Privacy:** Updated legal documentation integrated into the UI.

### 🔄 Recently Completed
- **Security Module:** Implemented `AppLockRepository`, `PinHasher`, and `RecoveryCodeManager`.
- **UI Redesign:** Added `PremiumCard`, `PremiumTextField`, and `EnhancedHomeScreen`.
- **App Lifecycle:** Implemented re-locking on app resume and initial startup.
- **Legal Update:** Refreshed Privacy Policy and added Terms of Service.

### 🚧 Next Steps
- **Advanced Encryption:** Integrate Google Tink for full database encryption.
- **Backup & Restore:** Implement encrypted local backup/restore functionality.
- **Testing:** Comprehensive unit and integration testing suite.
- **UI Polish:** Finalizing accessibility features and screen reader support.

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE.md) file for details.
