# CardVault: Secure Offline Card Repository

CardVault is a secure, offline-first Android application designed to provide users with a premium digital repository for all their physical cards. The primary goal is to create an intuitive, visually stunning, and completely secure wallet app that operates entirely offline.

This project is built from the ground up using modern Android development practices, including a 100% Kotlin codebase, Jetpack Compose for the UI, and a robust clean architecture.

## ✨ Key Features

- **🔒 100% Offline Operation:** Operates entirely without an internet connection. No network permissions, no analytics, and no data ever leaves your device.
- **🛡️ Advanced Security:**
  - **App Lock:** Secure the app with a 4-digit PIN, hashed locally using **PBKDF2 with HmacSHA256** and unique salts.
  - **Biometric Unlock:** Seamless integration with Android BiometricPrompt (Fingerprint/Face unlock).
  - **Secure Onboarding:** Guided initial setup for PIN creation and security configuration.
  - **Rate Limiting:** Exponential backoff for failed PIN attempts to prevent brute-force attacks.
  - **Recovery System:** 16-character human-readable recovery codes for PIN resets.
  - **Auto Data Wipe:** Optional security feature to wipe all local data after maximum failed PIN attempts.
- **📱 Modern UI/UX:**
  - **Glassmorphic Design:** A modern, frosted-glass aesthetic implemented with custom shaders and composables.
  - **Physics-based Animations:** Fluid, natural interactions driven by physics constants for a premium feel.
  - **3D Card Flip:** Realistic 3D card flip effects for viewing card details.
  - **Material Design 3:** Built entirely with the latest Material Design components and theming.
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
  - **Theming:** Material Design 3 & Custom Glassmorphism
  - **Navigation:** [Compose Navigation](https://developer.android.com/jetpack/compose/navigation) with type-safe arguments via `kotlinx.serialization`
  - **Animations:** Compose Animation APIs & Physics-based interactions
- **Architecture:**
  - **Pattern:** Clean Architecture (MVVM & Unidirectional Data Flow)
  - **Layers:** Presentation, Domain, Data (See [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) for details)
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

For a detailed breakdown of the project structure, including package organization and layer responsibilities, please refer to [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md).

```
com.technitedminds.wallet/
├── MainActivity.kt               # Main entry point with AppPhase state machine
├── WalletApplication.kt          # Application class with @HiltAndroidApp
├── ui/theme/                     # Material 3 & Glassmorphism theming
├── data/                         # Data Layer (Repositories, Room, File System)
├── domain/                       # Domain Layer (Models, Use Cases, Interfaces)
├── presentation/                 # Presentation Layer (Screens, ViewModels, Components)
├── di/                           # Hilt DI Modules
└── utils/                        # Utility classes
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

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE.md) file for details.
