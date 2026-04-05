---
name: android-data-layer
description: Guidance on implementing the offline-first Data Layer using Repository pattern, Room (Local), DataStore Preferences, and File Storage. No networking.
---

# Android Data Layer — Offline-First (CardVault)

## Instructions

The Data Layer manages all local data persistence. **This project is 100% offline** — there is NO networking, NO remote data sources, NO sync.

### 1. Repository Pattern
*   **Role**: Single Source of Truth (SSOT) for local data.
*   **Implementation**: Repositories coordinate between Room, DataStore, and File Storage.
    ```kotlin
    class CardRepositoryImpl @Inject constructor(
        private val cardDao: CardDao,
        private val cardMapper: CardMapper,
        private val imageFileManager: ImageFileManager
    ) : CardRepository {
        override fun getAllCards(): Flow<List<Card>> =
            cardDao.getAllCards().map { entities -> entities.map(cardMapper::toDomain) }

        override suspend fun insertCard(card: Card) {
            cardDao.insert(cardMapper.toEntity(card))
        }

        override suspend fun deleteCard(card: Card) {
            cardDao.delete(cardMapper.toEntity(card))
            imageFileManager.deleteCardImages(card.id)
        }
    }
    ```

### 2. Local Persistence (Room 2.8.4)
*   **Primary storage**: Room database with `CardEntity` and `CategoryEntity`.
*   **IDs**: `String` UUIDs as primary keys (NOT `Long` auto-increment).
*   **Type Converters**: `CardTypeConverter`, `MapConverter`, `CardGradientConverter`.
*   **DAOs**: Return `Flow<T>` for observable data, `suspend` for one-shot operations.
*   **Mapping**: Always map Entity ↔ Domain via `CardMapper` / `CategoryMapper`.

### 3. Preferences (DataStore Preferences 1.2.0)
*   **Manager**: `SimplePreferencesManager` wraps DataStore Preferences API.
*   **Usage**: Theme preferences, security flags, app lock settings, category defaults.
*   **NOT Proto DataStore** — uses key-value pairs, not `.proto` schema.
    ```kotlin
    class SimplePreferencesManager @Inject constructor(
        private val dataStore: DataStore<Preferences>
    ) {
        val themeMode: Flow<String> = dataStore.data.map { prefs ->
            prefs[THEME_KEY] ?: "system"
        }
        suspend fun setThemeMode(mode: String) {
            dataStore.edit { prefs -> prefs[THEME_KEY] = mode }
        }
    }
    ```

### 4. File Storage
*   **ImageFileManager**: Handles card image compression, storage, and cleanup.
*   **StorageManager**: General storage utilities.
*   **Location**: App's private internal storage (`context.filesDir/images/`).
*   **Naming**: `{cardId}_front.jpg`, `{cardId}_back.jpg`.
*   **Compression**: JPEG quality 85 before saving.
*   **Cleanup**: Delete orphaned images when cards are deleted.

### 5. Security Data
*   **AppLockRepository**: PIN validation, biometric flags, lockout state.
*   **PinHasher**: PBKDF2 with HmacSHA256 (10,000 iterations, random salt).
*   **RecoveryCodeManager**: Generate and validate 16-character recovery codes.

### 6. Dependency Injection
*   Bind Repository interfaces to implementations in `RepositoryModule`.
    ```kotlin
    @Module
    @InstallIn(SingletonComponent::class)
    abstract class RepositoryModule {
        @Binds
        abstract fun bindCardRepository(impl: CardRepositoryImpl): CardRepository
        @Binds
        abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository
        @Binds
        abstract fun bindImageRepository(impl: ImageRepositoryImpl): ImageRepository
    }
    ```

### 7. NEVER Add
*   ❌ Retrofit, OkHttp, Ktor, or any networking library
*   ❌ Remote data sources or APIs
*   ❌ Sync/revalidation patterns
*   ❌ WorkManager for network sync
*   ❌ Firebase, cloud storage, or analytics
