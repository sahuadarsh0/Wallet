---
name: android-viewmodel
description: Best practices for implementing Android ViewModels with StateFlow for UI state and SharedFlow for one-off events. CardVault uses @HiltViewModel with Use Case delegation.
---

# Android ViewModel & State Management

## Instructions

Use `ViewModel` to hold UI state and coordinate with Use Cases. ViewModels must be annotated with `@HiltViewModel` and inject dependencies via constructor. Delegate business logic to Use Cases — ViewModels only manage UI state.

### 1. UI State (StateFlow)
*   **What**: Represents the persistent state of the UI (e.g., `Loading`, `Success(data)`, `Error`).
*   **Type**: `StateFlow<UiState>`.
*   **Initialization**: Must have an initial value.
*   **Exposure**: Expose as a read-only `StateFlow` backing a private `MutableStateFlow`.
*   **Naming**: Use `_uiState` (private mutable) / `uiState` (public immutable) convention.
    ```kotlin
    @HiltViewModel
    class HomeViewModel @Inject constructor(
        private val getCardsUseCase: GetCardsUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HomeUiState())
        val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    }
    ```
*   **Updates**: Update state using `.update { oldState -> oldState.copy(...) }` for thread safety.
*   **Flow to StateFlow**: Use `.stateIn()` to convert cold Flow from Room to hot StateFlow:
    ```kotlin
    val cards: StateFlow<List<Card>> = getCardsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    ```

### 2. One-Off Events (SharedFlow)
*   **What**: Transient events like "Show Snackbar", "Navigate", "Card saved".
*   **Type**: `SharedFlow<UiEvent>`.
*   **Configuration**: Must use `replay = 0` to prevent events from re-triggering on screen rotation.
    ```kotlin
    private val _uiEvent = MutableSharedFlow<UiEvent>(replay = 0)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()
    ```
*   **Sending**: Use `.emit(event)` (suspend) or `.tryEmit(event)`.

### 3. Collecting in Compose UI
*   **StateFlow**: Use `collectAsStateWithLifecycle()` — NEVER `collectAsState()`.
    ```kotlin
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ```
*   **SharedFlow**: Use `LaunchedEffect` to collect one-off events:
    ```kotlin
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.NavigateBack -> navController.popBackStack()
            }
        }
    }
    ```

### 4. Scope & Delegation
*   Use `viewModelScope` for all coroutines started by the ViewModel.
*   **Delegate** business logic to Use Cases (e.g., `AddCardUseCase`, `DeleteCardUseCase`).
*   **Retrieve navigation arguments** from `SavedStateHandle`:
    ```kotlin
    @HiltViewModel
    class CardDetailViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        private val getCardsUseCase: GetCardsUseCase,
    ) : ViewModel() {
        private val cardId: String = savedStateHandle["cardId"] ?: ""
    }
    ```

### 5. Error Handling
*   Use sealed classes for UI state to represent loading/success/error.
*   Catch exceptions in ViewModel and update state — never let exceptions propagate to UI.
    ```kotlin
    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                deleteCardUseCase(cardId)
                _uiEvent.emit(UiEvent.CardDeleted)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    ```
