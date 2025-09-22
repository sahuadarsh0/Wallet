# Wallet-CardVault: Secure Offline Card Repository

CardVault is a secure, offline-only Android application designed to provide users with a premium digital repository for all their physical cards. The primary goal is to create an intuitive, visually stunning, and completely secure wallet app that operates entirely offline.

This project is built from the ground up using modern Android development practices, including a 100% Kotlin codebase, Jetpack Compose for the UI, and a robust layered architecture.

## ✨ Key Features

-   **🔒 Offline-First by Design:** Operates entirely without an internet connection. No network permissions, no analytics, and no data ever leaves your device.
-   **📲 Modern UI/UX:** A beautiful and intuitive interface built entirely with **Jetpack Compose** and **Material Design 3**, featuring smooth 60fps animations, including a realistic 3D card flip effect.
-   **📸 Smart Card Scanning:** Add cards quickly using your camera. On-device **ML Kit** recognizes and extracts card details, with a manual fallback option.
-   **🎨 Flexible Organization:** Group your cards into default or custom-created categories (e.g., 'Work', 'Travel', 'Memberships'), each with a distinct color theme.
-   **⚙️ Custom Card Types:** For cards like vouchers or gift cards, add custom fields like expiry dates, balances, and a "Mark as Used" toggle.

## 🛠️ Tech Stack & Architecture

This project follows the official Android architecture recommendations, employing a layered, reactive, and lifecycle-aware approach.

-   **Core Language:** [Kotlin](https://kotlinlang.org/) (100%)
-   **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (latest BOM)
    -   **Theming:** Material Design 3
    -   **Navigation:** [Compose Navigation](https://developer.android.com/jetpack/compose/navigation) with type-safe arguments via `kotlinx.serialization`.
    -   **Animations:** Compose Animation APIs for fluid transitions and effects.
-   **Architecture:**
    -   **Pattern:** Model-View-ViewModel (MVVM) & Unidirectional Data Flow (UDF).
    -   **Frameworks:** [Jetpack ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel), [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html), and [Flow](https://developer.android.com/kotlin/flow) for managing asynchronous operations and UI state.
-   **Dependency Injection:** [Hilt](https://dagger.dev/hilt/) for managing dependencies across the application.
-   **Data Persistence:**
    -   **Database:** [Room](https://developer.android.com/training/data-storage/room) for local data storage.
    -   **Preferences:** [Proto DataStore](https://developer.android.com/topic/libraries/architecture/datastore) for user settings.
-   **Build System:**
    -   Gradle with Kotlin DSL (`build.gradle.kts`).
    -   [TOML Version Catalog](https://docs.gradle.org/current/userguide/platforms.html) (`libs.versions.toml`) for centralized dependency management.
    -   [KSP (Kotlin Symbol Processing)](https://kotlinlang.org/docs/ksp-overview.html) for annotation processing (Room, Hilt).
-   **Camera & ML:**
    -   [CameraX](https://developer.android.com/training/camerax) for a modern, lifecycle-aware camera implementation.
    -   [Google ML Kit](https://developers.google.com/ml-kit) (Text Recognition) for on-device card data extraction.
-   **Security:**
    -   [Google Tink](https://github.com/google/tink) for robust backup file encryption.

## 🚀 Getting Started

To build and run this project locally, follow these steps:

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/wallet-cardvault.git
    ```
2.  **Open in Android Studio:**
    -   Open Android Studio (latest stable version recommended).
    -   Click on `File > Open` and select the cloned project directory.
3.  **Gradle Sync:**
    -   Allow Android Studio to download all dependencies and sync the project with the Gradle files.
4.  **Build and Run:**
    -   Select the `app` configuration and a target device (emulator or physical device).
    -   Click the "Run" button.

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE.md) file for details.
