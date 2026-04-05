---
name: compose-ui
description: Best practices for building UI with Jetpack Compose (2026), focusing on state hoisting, performance optimizations, theming, and CardVault's glassmorphic design system. Use when writing or refactoring Composable functions.
---

# Jetpack Compose Best Practices

## Instructions

Follow these guidelines to create performant, reusable, and testable Composables. CardVault targets 60fps on mid-range devices with glassmorphic design.

### 1. State Hoisting (Unidirectional Data Flow)
Make Composables **stateless** whenever possible by moving state to the caller.

*   **Pattern**: Function signature should usually look like:
    ```kotlin
    @Composable
    fun MyComponent(
        value: String,              // State flows down
        onValueChange: (String) -> Unit, // Events flow up
        modifier: Modifier = Modifier // Standard modifier parameter
    )
    ```
*   **Benefit**: Decouples the UI from state storage, making it easier to preview and test.
*   **ViewModel Integration**: Screen-level Composable retrieves state from ViewModel (`viewModel.uiState.collectAsStateWithLifecycle()`) and passes it down.
*   **Hoisting rules**: Hoist to lowest common parent (read), highest level changed (write), and together if triggered by same event.

### 2. Modifiers
*   **Default Parameter**: Always provide a `modifier: Modifier = Modifier` as the **last** named parameter.
*   **Application**: Apply this `modifier` to the *root* layout element of your Composable.
*   **Ordering matters**: `padding().clickable()` is different from `clickable().padding()`.
*   **Lambda modifiers**: Use `Modifier.offset { ... }` (lambda) instead of `Modifier.offset(...)` for frequently changing values to defer state reads.

### 3. Performance Optimization
*   **`remember`**: Cache expensive calculations. Use keys for invalidation: `remember(key) { ... }`.
*   **`derivedStateOf`**: Limit recompositions from rapidly changing state (scroll position, animation progress).
    ```kotlin
    val showButton by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }
    ```
*   **Lambda Stability**: Use `remember(key) { { viewModel.doAction(key) } }` or method references to stabilize lambdas.
*   **Stable keys**: Always provide `key` in `LazyColumn`/`LazyGrid` items using stable IDs.
*   **Defer state reads**: Wrap in lambda modifiers to push reads from composition to layout/draw phase.
*   **No backwards writes**: Never write to state already read during the same composition.
*   **`@Stable`/`@Immutable`**: Annotate data classes used as composable params for efficient skipping.

### 4. Theming and Design System
*   Use `MaterialTheme.colorScheme` and `MaterialTheme.typography` — never hardcode colors or text styles.
*   **Glassmorphic design**: Use `GlassTheme` for frosted-glass aesthetics and `PhysicsConstants` for physics-based animations.
*   **Enhanced components**: Prefer `PremiumCard`, `PremiumTextField`, `AnimatedSectionHeader` over plain M3 components for consistency.
*   Both light and dark themes must be supported and tested.

### 5. Previews
*   Create a `@Preview` function for every public Composable.
*   Use `@Preview(showBackground = true)` and include Light/Dark mode previews.
*   Pass dummy data (static) to the stateless Composable.
*   For card-related previews, create sample `Card` and `Category` objects.

### 6. Side Effects
*   Use `LaunchedEffect(key)` for coroutine-based side effects — NEVER `LaunchedEffect(true)`.
*   Use `DisposableEffect` for cleanup (camera lifecycle, listeners).
*   Use `SideEffect` for non-suspend code that must run after every successful composition.
*   Never perform I/O, database, or heavy work during composition.
