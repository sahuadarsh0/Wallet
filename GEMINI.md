# Gemini Code Interaction Guide for CardVault Project

## Project Context
- **Project Type**: Android Studio Project (CardVault application)
- **Platform**: Android
- **Languages**: Kotlin (100%)
- **UI Toolkit**: Jetpack Compose
- **Location**: `/Users/adarshsahu/AndroidStudioProjects/Wallet`

## Key Principles & Notes for Gemini
- **Offline-First Mandate**: The application must operate **100% offline**. No network permissions are requested, and no code should attempt to access the internet. All processing and data storage is local.
- **Architecture**: Strictly follow the established **Clean Architecture** pattern (MVVM with UDF).
    - **Domain Layer**: Contains business logic (Use Cases) and models. This layer is pure Kotlin.
    - **Data Layer**: Manages data sources (Room DB, File System).
    - **Presentation Layer**: Jetpack Compose UI and ViewModels.
- **Card Types**: Understand the two distinct card workflows:
    1.  **Textual Cards** (Credit/Debit): Involve on-device OCR/ML to extract data.
    2.  **Image-Only Cards** (Gym, Vouchers): Simple front/back image storage with minimal metadata.
- **UI/UX Focus**: A premium user experience is critical.
    - Adhere to **Material Design 3** guidelines.
    - Animations should be smooth (target 60fps), especially the 3D card flip.
    - UI must be intuitive and visually appealing.
- **Data Handling**:
    - **Database**: Use the existing **Room** database (`WalletDatabase.kt`) for structured data.
    - **Images**: Use the `ImageRepository` for storing and retrieving card images from the local file system.
    - **Preferences**: Use **Proto DataStore** for user settings.
- **Dependency Management**: All dependencies are managed in the **TOML Version Catalog** (`gradle/libs.versions.toml`). Use this file to check for available libraries and versions.
- **Immutability & State**: Use immutable data classes for models and UI state. State should flow down and events should flow up (UDF).

## Project Structure
The project follows a feature-based modular structure.
```
com.technitedminds.wallet/
├── MainActivity.kt               # Main entry point with @AndroidEntryPoint
├── WalletApplication.kt          # Application class with @HiltAndroidApp
├── ui/
│   └── theme/                    # Material Design 3 theming
├── data/                         # Data Layer - COMPLETE
│   ├── local/
│   │   ├── database/             # Room database components
│   │   ├── files/                # File system operations
│   │   └── preferences/          # User preferences (Proto DataStore)
│   └── repository/               # Repository implementations
├── domain/                       # Domain Layer - COMPLETE
│   ├── model/                    # Domain models (Card, Category, etc.)
│   ├── repository/               # Repository interfaces
│   └── usecase/                  # Business logic use cases
├── presentation/                 # Presentation Layer - IN PROGRESS
│   ├── screens/                  # Feature screens (home, addcard, carddetail, etc.)
│   ├── components/               # Reusable UI components (common, animation, camera)
│   └── navigation/               # Navigation setup
├── di/                           # Dependency Injection (Hilt) modules
└── utils/                        # Utility classes and extensions
```

## Key Files for Context
- **Build & Dependencies**:
    - `settings.gradle.kts` - Project settings
    - `app/build.gradle.kts` - App-level build configuration and dependencies
    - `gradle/libs.versions.toml` - Centralized dependency version catalog
- **Project Documentation**:
    - `README.md` - High-level project overview, features, and tech stack.
    - `PROJECT_STRUCTURE.md` - Detailed breakdown of the project's architecture and implementation status.
    - `.kiro/specs/cardvault-wallet/requirements.md` - Detailed functional requirements and user stories.
    - `.kiro/specs/cardvault-wallet/design.md` - In-depth technical design, data models, and architecture diagrams.

## Interaction Preferences
- **Maintain Consistency**: Adhere strictly to the existing code style, naming conventions, and architectural patterns.
- **Performance First**: Be mindful of performance, especially regarding UI rendering (Compose), image handling (Coil), and database queries (Room).
- **Leverage Existing Components**: Before creating new components or utilities, check the `presentation/components` and `utils` packages for reusable code.
- **Follow Conventions**: Adhere to standard Android and Kotlin development best practices.

## Common Tasks
- **Implement Jetpack Compose UI**: Create and modify `@Composable` functions for UI elements and screens in the `presentation` layer.
- **Update ViewModels**: Modify `ViewModel` classes to manage state and handle UI events.
- **Add or Modify Use Cases**: Implement business logic in the `domain/usecase` package.
- **Manage Dependencies**: Add or update libraries by editing the `gradle/libs.versions.toml` and `app/build.gradle.kts` files.
- **Write Tests**: Add unit tests for ViewModels and Use Cases, and integration tests for the data layer.
- **Integrate CameraX/ML Kit**: Work on features related to camera interaction and on-device text recognition.
