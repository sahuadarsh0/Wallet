# Project Structure & Organization

## Package Structure
The project follows a layered architecture with feature-based organization under `com.technitedminds.wallet`:

```
app/src/main/java/com/technitedminds/wallet/
├── MainActivity.kt              # Main entry point with @AndroidEntryPoint
├── WalletApplication.kt         # Application class with @HiltAndroidApp
├── data/                        # Data layer implementation
├── domain/                      # Business logic and models
├── presentation/                # UI layer with Compose screens
├── di/                          # Dependency injection modules
├── ui/
│   └── theme/                   # Material Design 3 theming
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── utils/
    └── Extensions.kt            # Kotlin extensions
```

## Recommended Architecture Layers

### Data Layer (`data/`)
```
data/
├── local/
│   ├── database/               # Room database implementation
│   │   ├── entities/          # Room entities (CardEntity, CategoryEntity)
│   │   ├── dao/               # Data Access Objects (CardDao, CategoryDao)
│   │   ├── converters/        # Type converters (CardTypeConverter, MapConverter)
│   │   └── WalletDatabase.kt  # Main database class
│   ├── files/                 # File system operations
│   │   └── ImageFileManager.kt # Image storage and optimization
│   └── preferences/           # User preferences
│       └── SimplePreferencesManager.kt
├── repository/                # Repository implementations
│   ├── CardRepositoryImpl.kt
│   ├── CategoryRepositoryImpl.kt
│   ├── ImageRepositoryImpl.kt
│   └── ExportImportRepositoryImpl.kt
├── mapper/                    # Data mapping utilities
│   ├── CardMapper.kt
│   └── CategoryMapper.kt
└── ocr/                       # OCR processing
    └── MLKitTextRecognizer.kt
```

### Domain Layer (`domain/`)
```
domain/
├── model/                     # Domain models
│   ├── Card.kt               # Core card model
│   ├── CardType.kt           # Card type sealed class
│   ├── Category.kt           # Category model
│   └── CardImage.kt          # Image metadata model
├── repository/                # Repository interfaces
│   ├── CardRepository.kt
│   ├── CategoryRepository.kt
│   ├── ImageRepository.kt
│   └── ExportImportRepository.kt
├── usecase/                   # Business logic use cases
│   ├── card/                 # Card-related use cases
│   ├── category/             # Category management
│   ├── ocr/                  # OCR processing
│   └── export/               # Data export/import
└── util/                      # Domain utilities
```

### Presentation Layer (`presentation/`)
```
presentation/
├── screens/                   # Feature screens
│   ├── home/                 # Main card list screen
│   ├── add_card/             # Card creation workflow
│   ├── card_detail/          # Card viewing and editing
│   ├── categories/           # Category management
│   └── settings/             # App settings
├── components/                # Reusable UI components
│   ├── common/               # Generic components
│   ├── camera/               # Camera-related components
│   └── animation/            # Animation components
└── navigation/                # Navigation setup
    ├── WalletNavigation.kt
    └── NavigationDestinations.kt
```

## File Naming Conventions
- **Activities**: `*Activity.kt` (e.g., `MainActivity.kt`)
- **ViewModels**: `*ViewModel.kt` (e.g., `HomeViewModel.kt`)
- **Repositories**: `*Repository.kt` (interface) + `*RepositoryImpl.kt` (implementation)
- **Use Cases**: `*UseCase.kt` (e.g., `AddCardUseCase.kt`, `GetCardsUseCase.kt`)
- **Entities**: `*Entity.kt` (e.g., `CardEntity.kt`)
- **DAOs**: `*Dao.kt` (e.g., `CardDao.kt`)
- **Composables**: PascalCase (e.g., `CardFlipAnimation.kt`, `CameraPreview.kt`)
- **Screens**: `*Screen.kt` (e.g., `HomeScreen.kt`)
- **Mappers**: `*Mapper.kt` (e.g., `CardMapper.kt`)
- **Managers**: `*Manager.kt` (e.g., `ImageFileManager.kt`)

## Resource Organization
```
app/src/main/res/
├── drawable/                  # Vector drawables, icons
├── mipmap-*/                  # App icons (various densities)
├── values/
│   ├── colors.xml            # Color resources
│   ├── strings.xml           # String resources
│   └── themes.xml            # XML themes
└── xml/
    ├── backup_rules.xml      # Backup configuration
    └── data_extraction_rules.xml
```

## Testing Structure
```
app/src/
├── test/                     # Unit tests
│   └── java/com/technitedminds/wallet/
└── androidTest/              # Instrumented tests
    └── java/com/technitedminds/wallet/
```

## Configuration Files
- `build.gradle.kts` - Module build configuration
- `gradle/libs.versions.toml` - Centralized dependency versions
- `proguard-rules.pro` - ProGuard/R8 rules
- `local.properties` - Local SDK paths (not in VCS)

## Hilt Module Organization
- `DatabaseModule` - Room database and DAO provision
- `RepositoryModule` - Repository interface to implementation bindings
- `UseCaseModule` - Use case dependencies (if needed)
- `CameraModule` - CameraX and ML Kit dependencies
- No `NetworkModule` - app is completely offline

## Code Style Guidelines
- Use Kotlin coding conventions and idiomatic patterns
- Prefer `val` over `var` for immutability
- Use trailing commas in multi-line constructs
- Group imports: Android, third-party, project
- Maximum line length: 120 characters
- Use meaningful variable and function names
- Add KDoc for public APIs and complex functions
- Use sealed classes for type-safe state management
- Prefer data classes for models
- Use extension functions for utility operations
- Follow Compose best practices (remember, derivedStateOf, etc.)

## Testing Guidelines
- Unit tests for ViewModels, Use Cases, and Repositories
- Integration tests for database operations
- Compose UI tests for screen interactions
- Test file naming: `*Test.kt` for unit tests, `*IntegrationTest.kt` for integration tests
- Use MockK for mocking in tests
- Test coverage focus on business logic and critical paths