# Project Structure & Organization

## Package Structure
The project follows a feature-based package organization under `com.technitedminds.wallet`:

```
app/src/main/java/com/technitedminds/wallet/
├── MainActivity.kt              # Main entry point with @AndroidEntryPoint
├── WalletApplication.kt         # Application class with @HiltAndroidApp
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
│   ├── database/               # Room database
│   │   ├── entities/          # Room entities
│   │   ├── dao/               # Data Access Objects
│   │   └── WalletDatabase.kt
│   ├── datastore/             # Proto DataStore
│   └── preferences/           # User preferences
├── repository/                # Repository implementations
└── mapper/                    # Data mapping utilities
```

### Domain Layer (`domain/`)
```
domain/
├── model/                     # Domain models
├── repository/                # Repository interfaces
├── usecase/                   # Business logic use cases
└── util/                      # Domain utilities
```

### Presentation Layer (`presentation/`)
```
presentation/
├── screens/                   # Feature screens
│   ├── home/
│   ├── card/
│   ├── scanner/
│   └── settings/
├── components/                # Reusable UI components
├── navigation/                # Navigation setup
└── theme/                     # Extended theming
```

## File Naming Conventions
- **Activities**: `*Activity.kt` (e.g., `MainActivity.kt`)
- **Fragments**: `*Fragment.kt` (if used)
- **ViewModels**: `*ViewModel.kt`
- **Repositories**: `*Repository.kt` + `*RepositoryImpl.kt`
- **Use Cases**: `*UseCase.kt` or `Get*UseCase.kt`
- **Entities**: `*Entity.kt`
- **DAOs**: `*Dao.kt`
- **Composables**: PascalCase (e.g., `CardFlipAnimation.kt`)

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
- `DatabaseModule` - Room database provision
- `RepositoryModule` - Repository bindings
- `NetworkModule` - If network features added

## Code Style Guidelines
- Use Kotlin coding conventions
- Prefer `val` over `var`
- Use trailing commas in multi-line constructs
- Group imports: Android, third-party, project
- Maximum line length: 120 characters
- Use meaningful variable and function names
- Add KDoc for public APIs