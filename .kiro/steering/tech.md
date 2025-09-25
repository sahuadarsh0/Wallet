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
- **Navigation**: Compose Navigation with type-safe arguments
- **Async**: Kotlin Coroutines + Flow/StateFlow

## Key Libraries
- **Database**: Room with KSP
- **Preferences**: Proto DataStore
- **Camera**: CameraX
- **ML**: Google ML Kit (Text Recognition)
- **Security**: Android Keystore + Google Tink
- **Authentication**: Android Biometric Library
- **Image Loading**: Coil

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
- Repository pattern for data access
- ViewModel for UI state management
- Use cases/interactors for business logic
- Dependency injection with Hilt modules
- Reactive programming with Flow
- Lifecycle-aware components

## Performance Requirements
- 60fps animations on mid-range devices
- App launch < 3 seconds
- Smooth scrolling with LazyColumn
- Efficient image caching and loading
- Memory-conscious bitmap handling