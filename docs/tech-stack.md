# Wallet-CardVault: Technical Stack \& Architecture Guide

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

```kotlin
// Core Animation APIs
- Compose Animation APIs (animateContentSize, AnimatedVisibility, updateTransition)
- Custom FlipCard composable with rotationX and cameraDistance
- Spring animations for natural card movements
- Shared element transitions between screens

// FlipCard Implementation Approach
@Composable
fun FlipCard(
    cardFace: CardFace,
    modifier: Modifier = Modifier,
    flipDuration: Int = 400,
    onFlip: (CardFace) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (cardFace == CardFace.FRONT) 0f else 180f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
    )
}
```


### Camera \& Image Handling

- **Camera Integration:** CameraX for real-time card scanning
- **Image Selection:** PhotoPicker API (Android 13+) with fallback to ActivityResultContracts
- **Image Processing:** Efficient bitmap handling with Coil for loading/caching
- **ML Integration:** ML Kit Text Recognition with CameraX Analyzer


## Data Layer

### Persistence Architecture

```kotlin
// Database Layer
@Database(
    entities = [Card::class, Category::class, CardImage::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CardVaultDatabase : RoomDatabase()

// Data Flow
Repository -> Room Database -> Flow<List<T>> -> ViewModel -> UI State
```


### Storage Components

- **Local Database:** Room with KSP annotation processing
- **Preferences:** DataStore (Proto) for user settings and app configuration
- **Image Storage:** Internal app directory with encrypted file names
- **Backup Storage:** SAF (Storage Access Framework) for user-controlled backup location


### Text Recognition Pipeline

```kotlin
// ML Kit Integration
private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

// Processing Pipeline
CameraX ImageAnalysis -> ML Kit TextRecognizer -> Card Data Extraction -> Validation -> UI Display
```


### Backup \& Encryption System

```kotlin
// Backup Architecture
data class BackupFile(
    val header: BackupHeader,
    val encryptedData: ByteArray,
    val checksum: String
)

// Encryption Stack
- Primary: Android Keystore for app data encryption
- Backup: AES-256-GCM with PBKDF2 key derivation using Tink library
- File Format: Custom .cvault format with version header
```


## Dependency Injection \& Architecture

### Architecture Pattern

```kotlin
// MVVM Architecture
View (Compose) <-> ViewModel <-> Repository <-> Data Sources (Room, DataStore)

// Data Flow
UI Events -> ViewModel -> Repository -> Database/DataStore
Database/DataStore -> Repository -> ViewModel -> UI State
```


### Dependency Injection

- **Framework:** Hilt for application-wide DI
- **Module Structure:**
    - DatabaseModule (Room, DataStore)
    - RepositoryModule (Data repositories)
    - NetworkModule (ML Kit, Camera dependencies)
    - CryptoModule (Encryption utilities)


### Async Operations

```kotlin
// Coroutines Architecture
- ViewModelScope for UI-bound operations
- Application scope for background tasks
- Flow for reactive data streams
- StateFlow for UI state management
- SharedFlow for one-time events
```


## Modern Tooling \& Libraries

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
kapt "androidx.room:room-compiler" // Transitioning to KSP

// ML & Camera
implementation "com.google.mlkit:text-recognition"
implementation "androidx.camera:camera-camera2"
implementation "androidx.camera:camera-lifecycle"
implementation "androidx.camera:camera-view"

// Security & Encryption
implementation "com.google.crypto.tink:tink-android"
implementation "androidx.biometric:biometric"

// Utilities
implementation "io.coil-kt:coil-compose"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android"
```


### Build Configuration

```gradle
// KSP Migration
plugins {
    id "com.google.devtools.ksp" version "2.0.10-1.0.24"
}

// Kotlin Configuration
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

### Safe Area Implementation

```kotlin
// Biometric Authentication Wrapper
sealed class AuthenticationResult {
    object Success : AuthenticationResult()
    object Failed : AuthenticationResult()
    object Error : AuthenticationResult()
}

// Safe Area Repository
class SafeAreaRepository {
    suspend fun authenticateForSafeArea(): AuthenticationResult
    fun getSafeAreaCards(): Flow<List<Card>>
}
```


### Design System Architecture

```kotlin
// CardVault Design Tokens
object CardVaultTheme {
    val colors: CardVaultColors
    val typography: CardVaultTypography
    val shapes: CardVaultShapes
    val elevations: CardVaultElevations
}

// Dark/Light Theme Support
@Composable
fun CardVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
)
```


### File Format Specification

```kotlin
// .cvault File Structure
Header (32 bytes): Magic number, version, encryption type, checksum
Encrypted Payload: Serialized app data (JSON/Protobuf)
Footer (16 bytes): Validation hash, timestamp

// File Extension Registration
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <data android:mimeType="application/cvault" />
</intent-filter>
```


### Performance Optimizations

- **LazyColumn Optimizations:** Key-based recomposition prevention
- **Image Caching:** Memory and disk caching with size limits
- **Database Indexing:** Strategic indices on frequently queried fields
- **Animation Optimizations:** Reduced overdraw through layer management
- **Memory Management:** Proper lifecycle-aware resource cleanup

This technical architecture ensures a robust, scalable, and maintainable codebase while delivering the premium user experience outlined in the PRD requirements.

