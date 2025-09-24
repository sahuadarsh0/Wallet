# Technology Stack & Build System

## Build System
- **Gradle**: Kotlin DSL (`build.gradle.kts`)
- **Version Catalog**: TOML-based dependency management (`gradle/libs.versions.toml`)
- **KSP**: Kotlin Symbol Processing for annotation processing (Room, Hilt)
- **Target SDK**: API 36 (Android 15)
- **Min SDK**: API 29 (Android 10)
- **Java Version**: 11

## Core Technologies
- **Language**: 100% Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM + Unidirectional Data Flow (UDF)
- **DI**: Hilt (Dagger)
- **Navigation**: Compose Navigation with type-safe arguments via kotlinx.serialization
- **Async**: Kotlin Coroutines + Flow/StateFlow

## Key Libraries
- **Database**: Room with KSP for annotation processing
- **Preferences**: Proto DataStore for user settings
- **Camera**: CameraX for lifecycle-aware camera implementation
- **ML**: Google ML Kit (Text Recognition) for on-device OCR
- **Security**: Google Tink for backup file encryption
- **Image Loading**: Coil for efficient image loading and caching
- **Serialization**: kotlinx.serialization for JSON handling

## Common Commands

### Build & Run
```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Install debug build
./gradlew installDebug
```

### Development
```bash
# Check for dependency updates
./gradlew dependencyUpdates

# Generate KSP sources
./gradlew kspDebugKotlin

# Lint check
./gradlew lint
```

## Architecture Patterns
- Repository pattern for data access abstraction
- ViewModel for UI state management with StateFlow
- Use cases/interactors for business logic encapsulation
- Dependency injection with Hilt modules (@Module, @InstallIn)
- Reactive programming with Flow and StateFlow
- Lifecycle-aware components with Compose integration
- Clean Architecture with clear layer separation (Data, Domain, Presentation)

## Data Management
- **Local Storage**: Room database with entities, DAOs, and converters
- **Image Storage**: Private app storage with compression and optimization
- **File Management**: ImageFileManager for card image operations
- **Type Converters**: Custom converters for complex data types (CardType, Map<String, String>)
- **Data Mapping**: Mapper classes for entity-to-domain model conversion

## Security & Privacy
- **Offline-First**: No network permissions, zero cloud dependencies
- **Local Encryption**: Google Tink for backup file encryption
- **Sandboxed Storage**: All data in app's private directory
- **No Analytics**: Zero tracking or data collection

## Performance Requirements
- 60fps animations on mid-range devices
- App launch < 3 seconds cold start
- Smooth scrolling with LazyColumn for card lists
- Efficient image caching and loading with Coil
- Memory-conscious bitmap handling with compression
- Optimized database queries with proper indexing