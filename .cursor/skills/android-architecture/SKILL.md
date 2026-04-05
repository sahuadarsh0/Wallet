---
name: android-architecture
description: Expert guidance on maintaining the CardVault application architecture using Clean Architecture and Hilt. Single-module, offline-first, Compose-only. Use when asked about project structure, layer responsibilities, or dependency injection.
---

# Android Architecture — CardVault (Single-Module, Offline-First)

## Instructions

CardVault follows **Clean Architecture** in a **single-module** layout with strict layer separation. It is 100% Compose (no Fragments or XML) and 100% offline (no networking).

### 1. Three-Layer Architecture

Dependencies flow **inwards**: Presentation → Domain → Data.

*   **Presentation Layer** (`presentation/`, `ui/theme/`):
    *   **Responsibility**: Displaying data, handling user interactions, navigation.
    *   **Components**: Composables, ViewModels (`@HiltViewModel`), Navigation setup.
    *   **State**: `StateFlow` collected via `collectAsStateWithLifecycle()`.
    *   **No Fragments**: 100% Jetpack Compose with `@AndroidEntryPoint` on `MainActivity`.
    *   **Enhanced UI**: Glassmorphic design, PremiumCard, PremiumTextField, FlippableCard, animations.

*   **Domain Layer** (`domain/`):
    *   **Responsibility**: Business rules, validation, use cases.
    *   **Components**: Use Cases (one per operation), Domain Models, Repository/Service interfaces.
    *   **Pure Kotlin**: **ZERO** Android framework dependencies (no `android.*` imports).
    *   **Models**: `Card`, `CardType`, `Category`, `CardGradient`, `CardImage`, `OCRResult`, `StorageModels`.
    *   **Use Cases**: Organized by feature — `card/`, `category/`, `ocr/`, `storage/`.
    *   **IDs**: All domain models use `String` UUIDs.

*   **Data Layer** (`data/`):
    *   **Responsibility**: Persisting and retrieving data from local sources.
    *   **Components**: Repository implementations, Room DAOs/Entities, DataStore, File storage, OCR, Security.
    *   **Sub-packages**: `local/` (cache, database, files, preferences, security, storage), `mapper/`, `ocr/`, `repository/`, `service/`.
    *   **No Remote Sources**: NEVER add networking. All data is local.

### 2. Dependency Injection with Hilt

*   **@HiltAndroidApp**: `WalletApplication`
*   **@AndroidEntryPoint**: `MainActivity` (single Activity)
*   **@HiltViewModel**: All ViewModels; inject via constructor
*   **Modules**:
    *   `DatabaseModule` (`@Singleton`): Provides `WalletDatabase`, `CardDao`, `CategoryDao`
    *   `RepositoryModule`: Binds repository interfaces → implementations (use `@Binds`)
    *   `CameraModule`: Provides CameraX and ML Kit dependencies
    *   `SecurityModule`: Provides security dependencies (`PinHasher`, etc.)
    *   **NO `NetworkModule`** — app is completely offline

### 3. Single-Module Structure

CardVault is a **single `:app` module**. All packages live under `com.technitedminds.wallet/`:

```
app/src/main/java/com/technitedminds/wallet/
├── data/           # Data layer (Room, DataStore, files, OCR, security)
├── domain/         # Business logic (models, use cases, interfaces)
├── presentation/   # UI (screens, components, navigation)
├── di/             # Hilt modules
├── ui/theme/       # Material 3 + Glassmorphism theming
└── utils/          # Extension functions
```

No multi-module setup. No `build-logic/` convention plugins needed.

### 4. Data Flow (UDF)

```
User Action → ViewModel → Use Case → Repository → Room/DataStore/File
     ↑                                                          ↓
UI ← StateFlow ← ViewModel ← Domain Model ← Mapper ← Entity ←┘
```

### 5. Checklist for Implementation
- [ ] Domain layer has **zero** Android dependencies
- [ ] Repositories expose `suspend` functions (main-safe) and `Flow` streams
- [ ] ViewModels expose `StateFlow` (collected via `collectAsStateWithLifecycle()`)
- [ ] All entity IDs are `String` UUIDs
- [ ] Entity ↔ Domain mapping happens in `data/mapper/`
- [ ] Business logic lives in use cases, NOT in ViewModels or repositories
- [ ] No networking or cloud dependencies added
- [ ] Hilt modules follow `@Provides` for instances, `@Binds` for interfaces
