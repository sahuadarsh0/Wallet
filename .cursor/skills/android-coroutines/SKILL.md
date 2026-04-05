---
name: android-coroutines
description: Authoritative rules and patterns for production-quality Kotlin Coroutines on Android. Covers structured concurrency, lifecycle integration, and reactive streams for offline-first apps.
---

# Android Coroutines Expert Skill

This skill provides authoritative rules and patterns for writing production-quality Kotlin Coroutines code on Android (2026 standards). It enforces structured concurrency, lifecycle safety, and modern best practices for offline-first applications.

## Responsibilities

*   **Asynchronous Logic**: Implementing suspend functions, Dispatcher management, and parallel execution.
*   **Reactive Streams**: Implementing `Flow`, `StateFlow`, `SharedFlow`, and `callbackFlow`.
*   **Lifecycle Integration**: Managing scopes (`viewModelScope`, `lifecycleScope`) and safe collection (`repeatOnLifecycle`, `collectAsStateWithLifecycle`).
*   **Error Handling**: Implementing `CoroutineExceptionHandler`, `SupervisorJob`, and proper `try-catch` hierarchies.
*   **Cancellability**: Ensuring long-running operations are cooperative using `ensureActive()`.
*   **Testing**: Setting up `TestDispatcher` and `runTest`.

## Applicability

Activate this skill when the user asks to:
*   "Fetch data from the database."
*   "Perform background processing (OCR, image compression)."
*   "Fix a memory leak" related to threads/tasks.
*   "Convert a listener/callback to Coroutines."
*   "Implement a ViewModel with state management."
*   "Handle UI state updates with Flow."

## Critical Rules & Constraints

### 1. Dispatcher Injection (Testability)
*   **NEVER** hardcode Dispatchers inside classes.
*   **ALWAYS** inject a `CoroutineDispatcher` via the constructor.

```kotlin
// CORRECT
class CardRepositoryImpl(
    private val cardDao: CardDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CardRepository {
    override suspend fun insertCard(card: CardEntity) = withContext(ioDispatcher) {
        cardDao.insert(card)
    }
}
```

### 2. Main-Safety
*   All suspend functions in Data/Domain layers must be **main-safe**.
*   **One-shot calls**: Exposed as `suspend` functions.
*   **Data streams**: Exposed as `Flow` (Room DAOs already emit on background thread).
*   Use `withContext(ioDispatcher)` inside repository for file I/O, image operations, OCR.

### 3. Lifecycle-Aware Collection (Compose)
*   **ALWAYS** use `collectAsStateWithLifecycle()` in Compose for collecting `StateFlow`/`Flow`.
*   **NEVER** use `collectAsState()` on Android — it's not lifecycle-aware.

```kotlin
// CORRECT (Compose)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Render UI based on uiState
}
```

### 4. ViewModel Scope Usage
*   Use `viewModelScope` for initiating coroutines in ViewModels.
*   Expose `StateFlow` or `SharedFlow` — never suspend functions to the UI.
*   Use `.stateIn()` for converting cold Flow to hot StateFlow:

```kotlin
val cards: StateFlow<List<Card>> = getCardsUseCase()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

### 5. Mutable State Encapsulation
*   **NEVER** expose `MutableStateFlow` or `MutableSharedFlow` publicly.
*   Use `.asStateFlow()` or `.asSharedFlow()` for read-only exposure.

```kotlin
private val _uiState = MutableStateFlow(HomeUiState())
val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
```

### 6. GlobalScope Prohibition
*   **NEVER** use `GlobalScope`. It breaks structured concurrency and causes leaks.
*   For app-wide tasks, use an injected `applicationScope`.

### 7. Exception Handling
*   **NEVER** catch `CancellationException` without rethrowing.
*   Use specific exception handling for database/file operations.

```kotlin
try {
    cardDao.insert(cardEntity)
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    // Handle database error
}
```

### 8. Cancellability
*   **ALWAYS** call `ensureActive()` or `yield()` in tight loops (image processing, OCR parsing).
*   Standard functions like `delay()` and `withContext()` are already cancellable.

### 9. Callback Conversion (CameraX, ML Kit)
*   Use `suspendCoroutine` or `callbackFlow` to convert CameraX/ML Kit callbacks.
*   **ALWAYS** use `awaitClose` in `callbackFlow` to clean up listeners.

```kotlin
suspend fun captureImage(): Bitmap = suspendCoroutine { continuation ->
    imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(imageProxy: ImageProxy) {
            val bitmap = imageProxy.toBitmap()
            imageProxy.close()
            continuation.resume(bitmap)
        }
        override fun onError(exception: ImageCaptureException) {
            continuation.resumeWithException(exception)
        }
    })
}
```

## Code Patterns

### Repository with Room Flow

```kotlin
class CardRepositoryImpl @Inject constructor(
    private val cardDao: CardDao,
    private val cardMapper: CardMapper,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CardRepository {
    override fun getAllCards(): Flow<List<Card>> =
        cardDao.getAllCards().map { entities ->
            entities.map(cardMapper::toDomain)
        }.flowOn(ioDispatcher)

    override suspend fun deleteCard(cardId: String) = withContext(ioDispatcher) {
        cardDao.deleteById(cardId)
    }
}
```

### Parallel Image Processing

```kotlin
suspend fun processCardImages(frontBitmap: Bitmap, backBitmap: Bitmap) = coroutineScope {
    val frontDeferred = async { imageFileManager.saveImage(frontBitmap, "front") }
    val backDeferred = async { imageFileManager.saveImage(backBitmap, "back") }

    CardImages(
        frontPath = frontDeferred.await(),
        backPath = backDeferred.await()
    )
}
```

### Testing with runTest

```kotlin
@Test
fun `loading cards updates state`() = runTest {
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val fakeRepository = FakeCardRepository()
    val viewModel = HomeViewModel(GetCardsUseCase(fakeRepository), testDispatcher)

    viewModel.loadCards()
    advanceUntilIdle()

    assertEquals(expectedCards, viewModel.uiState.value.cards)
}
```
