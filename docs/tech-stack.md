# Wallet-CardVault: Technical Stack & Architecture Guide (Cloud-Ready)

## Minimum SDK Requirements

- **Target SDK:** API Level 35 (Android 15)
- **Minimum SDK:** API Level 29 (Android 10.0)
- **Compile SDK:** API Level 35
- **NDK Version:** 26.1.10909125 (for native crypto operations)

## UI Layer

### Framework
- **Primary UI Framework:** Jetpack Compose (latest stable BOM)
- **Navigation:** Compose Navigation with type-safe arguments using kotlinx.serialization
- **Theme System:** Material Design 3 with custom CardVault design tokens
- **State Management:** Compose State and ViewModel integration

### Animation System
- **Compose Animation APIs:** (animateContentSize, AnimatedVisibility, updateTransition)
- **Custom FlipCard composable:** with rotationX and cameraDistance
- **Spring animations:** for natural card movements
- **Shared element transitions** between screens

### Camera & Image Handling
- **Camera Integration:** CameraX for real-time card scanning
- **Image Selection:** PhotoPicker API (Android 13+) with fallback to ActivityResultContracts
- **Image Processing:** Efficient bitmap handling with Coil for loading/caching
- **ML Integration:** ML Kit Text Recognition with CameraX Analyzer

## Data Layer

### Persistence Architecture
- **Local Database:** Room with KSP annotation processing
- **Preferences:** DataStore (Proto) for user settings and app configuration
- **Image Storage:** Internal app directory with encrypted file names
- **Backup Storage:**
  - **Local:** SAF (Storage Access Framework) for user-controlled backup location
  - **Cloud:** Google Drive App Folder for opt-in, user-authenticated backup/restore (encrypted files only)

### Text Recognition Pipeline
- **ML Kit Integration:** TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
- **Processing Pipeline:** CameraX ImageAnalysis → ML Kit TextRecognizer → Card Data Extraction

### Backup & Encryption System
- **Backup Architecture:**
  - Data class for BackupFile (header, encryptedData, checksum)
  - **Encryption:** Android Keystore for app data; AES-256-GCM with PBKDF2 (Tink library) for backups
  - **File Format:** Custom `.cvault` with version header; identical format for local/cloud backup
- **Cloud Integration:** All encryption/decryption local-only. Google Drive is used solely for storing pre-encrypted `.cvault` files. Restore must fetch, validate, then decrypt on device.

### Cloud & Authentication Layer (NEW)
- **Google Drive API:** For storing and retrieving encrypted backups (limited Drive folder access)
- **Firebase Auth:** Mandatory for accessing Drive backup features (Google Sign-In session required)
- **All other app features function offline, without authentication**

## Dependency Injection & Architecture
- **Pattern:** MVVM (Jetpack Compose View <-> ViewModel <-> Repository <-> Data Sources (Room, DataStore, Cloud modules))
- **DI Framework:** Hilt
- **New Modules:**
  - CloudModule (Google Drive client, backup manager)
  - AuthModule (Firebase Auth adapter, session manager)
- **Async:** Coroutines & Flow/StateFlow

## Modern Tooling & Libraries

### Core Dependencies
```gradle
// UI & Animation
implementation "androidx.compose:compose-bom:2024.09.00"
implementation "androidx.compose.animation:animation"
implementation "androidx.navigation:navigation-compose"

// Architecture
implementation "androidx.lifecycle:lifecycle-viewmodel-compose"
implementation "androidx.hilt:hilt-navigation-compose"
implementation "com.google.dagger:hilt-android:2.48"

// Database & Storage
implementation "androidx.room:room-runtime"
implementation "androidx.room:room-ktx"
implementation "androidx.datastore:datastore"
kapt "androidx.room:room-compiler" // (KSP migration recommended)

// ML & Camera
implementation "com.google.mlkit:text-recognition"
implementation "androidx.camera:camera-camera2"
implementation "androidx.camera:camera-lifecycle"
implementation "androidx.camera:camera-view"

// Security & Encryption
implementation "com.google.crypto.tink:tink-android"
implementation "androidx.biometric:biometric"

// Cloud Backup & Auth
implementation "com.google.android.gms:play-services-auth:21.0.0" // Google Sign-In
implementation "com.google.api-client:google-api-client-android:2.3.0" // Google Drive API client
implementation "com.google.http-client:google-http-client-gson:1.44.1"
implementation "com.google.firebase:firebase-auth-ktx:22.3.1" // Firebase Auth

// Utilities
implementation "io.coil-kt:coil-compose"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android"
```

### Build Configuration
```gradle
plugins {
    id "com.google.devtools.ksp" version "2.0.10-1.0.24"
}
compileOptions {
    sourceCompatibility JavaVersion.VERSION_17
    targetCompatibility JavaVersion.VERSION_17
}
kotlinOptions {
    jvmTarget = '17'
    freeCompilerArgs += [
        "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
        "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
    ]
}
```

## Additional Architecture Features

### File Format Specification
- **`.cvault` File Structure:** Header (magic, version, type, checksum), Encrypted Payload, Footer (hash, timestamp)

### Cloud Backup Workflow Example
```kotlin
// Backup to Drive (pseudocode)
fun backupToDrive(encryptedFile: File, userToken: String)
fun restoreFromDrive(fileId: String, userToken: String): File
```

## Performance Optimizations
- LazyColumn key-based recomposition
- Caching (image, database)
- Indexed tables & efficient queries
- Layer management for animations
- Secure memory and lifecycle-aware resource cleanup

--
This tech stack ensures robust, privacy-first offline operations while making cloud backup/restore and Google Auth available as opt-in options via strong client-side encryption.
