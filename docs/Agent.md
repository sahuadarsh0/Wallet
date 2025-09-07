# Agent Profile: Wallet-CardVault Assistant

This document outlines the key technical and project guidelines for the Wallet-CardVault Android application, ensuring that AI-assisted development aligns with the project's architecture, stack, and goals.

## 1. Project Overview & Core Mission

**Project Name:** Wallet-CardVault
**Core Mission:** To be a secure, offline-only, and visually premium Android application for users to store and manage digital versions of their physical cards. The emphasis is on an intuitive user experience, robust security, and smooth animations, operating entirely offline.

**Key Objectives:**
-   **Offline-First:** No network dependencies or permissions. All features fully functional offline.
-   **Top-Tier Security:** AES-256-GCM encryption for data at rest (Android Keystore) and backups (Tink library). Biometric authentication and a "Safe Area" for sensitive cards.
-   **Premium User Experience:** Jetpack Compose-driven UI with Material 3, aiming for 60fps animations (e.g., card flip).
-   **Flexible Organization:** Custom card categories and types with customizable fields.
-   **Data Portability:** Encrypted backup and restore functionality using a custom `.cvault` file format via SAF.

## 2. Preferred Language & Core Libraries

-   **Primary Language:** Kotlin
-   **UI Toolkit:**
    -   Jetpack Compose (latest stable BOM)
    -   Material 3 for theming and components.
    -   Compose Navigation (type-safe with `kotlinx.serialization`).
    -   Compose Animation APIs.
    -   Accompanist libraries (as needed).
-   **Architecture Components:**
    -   Jetpack ViewModels for UI state management.
    -   Kotlin Coroutines and Flow for asynchronous operations and reactive data streams (StateFlow, SharedFlow).
-   **Dependency Injection:** Hilt (application-wide).
-   **Database & Preferences:**
    -   Room for local database (with KSP for annotation processing).
    -   DataStore (Proto) for user settings and app configuration.
-   **Build System:**
    -   Gradle with Kotlin DSL (`build.gradle.kts`).
    -   TOML for version catalog (`libs.versions.toml`).
    -   KSP for annotation processing (replacing kapt where possible).
-   **Camera & ML:**
    -   CameraX for card scanning.
    -   ML Kit Text Recognition (on-device) for data extraction.
-   **Image Handling:** Coil for image loading and caching.
-   **Security:**
    -   Google Tink for backup encryption.
    -   Android Keystore for at-rest encryption.
    -   `androidx.biometric` for biometric authentication.

## 3. Architectural Guidelines

-   **Adherence to Official Recommendations:** Prioritize official Android architecture guidelines and Jetpack libraries.
-   **Layered Architecture:**
    -   **UI Layer (Compose):** Displays application data and sends user events.
    -   **ViewModel Layer:** Holds UI state, exposes it via Flows (e.g., `StateFlow`), and handles user actions. Implements Unidirectional Data Flow (UDF). UI collects state изменениями in a lifecycle-aware manner.
    -   **Repository Layer:** Mediates between data sources (database, DataStore) and ViewModels. Exposes data via Flows.
    -   **Data Layer:** Contains data sources like Room database, DataStore, and handles data persistence, encryption, and backup logic.
-   **MVVM (Model-View-ViewModel):** As the primary architectural pattern.
    -   `View (Compose) <-> ViewModel <-> Repository <-> Data Sources`
-   **Dependency Injection with Hilt:** Manage dependencies across all layers. Modules for Database, Repositories, Network (local ML/Camera), Crypto, etc.
-   **Asynchronous Operations:** Kotlin Coroutines and Flow are standard. Use `ViewModelScope` for UI-related coroutines and application-scoped coroutines for background tasks.
-   **State Management:** Utilize Compose `State` and `ViewModel` integration. Employ `StateFlow` for observable UI state and `SharedFlow` for one-time events.

## 4. Build Configuration & SDKs

-   **Minimum SDK:** API Level 29 (Android 10.0)
-   **Target SDK:** API Level 35 (Android 15)
-   **Compile SDK:** API Level 35
-   **NDK Version (if native crypto used directly):** 26.1.10909125
-   **Kotlin & Java Versions:**
    -   Kotlin: As defined in `libs.versions.toml` (currently `2.0.0`).
    -   Java Compatibility: Version 17.
-   **KSP:** Actively used for Room and Hilt.

## 5. Key Non-Functional Requirements to Uphold

-   **Security:** Offline-only, AES-256 encryption, biometric auth, secure memory handling.
-   **Performance:** 60fps animations, fast app launch (<3s), optimized memory (<150MB) and battery usage.
-   **Privacy:** No analytics, no data transmission, minimal permissions, full user data control.

## 6. Development & Coding Style

-   **Consistency:** Maintain consistency with existing codebase patterns and style.
-   **Modern Practices:** Employ modern Android development techniques, including functional programming concepts with Compose and Flow where appropriate.
-   **Readability & Maintainability:** Code should be clear, well-documented (where necessary), and easy to maintain.
-   **Testing:** While not explicitly detailed here, assume unit tests are important, especially for ViewModel logic and Repositories.
-   **Validation:** Proposed changes should be validated by building relevant Gradle sub-projects, running unit tests, and then building the full application. Ask before running tests on all consumers if changes are in a library module.
