---
name: compose-navigation
description: Implement navigation in Jetpack Compose using Navigation Compose with string-based routes. Use when asked to set up navigation, pass arguments between screens, or structure multi-screen apps. CardVault uses string routes with navArgument(), not @Serializable type-safe routes.
---

# Compose Navigation (CardVault Pattern)

## Overview

CardVault uses **Compose Navigation 2.9.7** with **string-based routes** and `navArgument()` for parameter passing. This project does NOT use `@Serializable` type-safe routes.

## Setup

```kotlin
// build.gradle.kts
dependencies {
    implementation("androidx.navigation:navigation-compose:2.9.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
}
```

---

## Core Pattern: String-Based Routes

### 1. Define Routes and Destinations

```kotlin
object NavigationRoutes {
    const val HOME = "home"
    const val ADD_CARD = "add_card"
    const val CAMERA = "camera"
    const val CARD_DETAIL = "card_detail"
    const val EDIT_CARD = "edit_card"
    const val CATEGORIES = "categories"
    const val SETTINGS = "settings"
}

object NavigationParams {
    const val CARD_ID = "cardId"
    const val CARD_TYPE = "cardType"
}

sealed class NavigationDestinations(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    object Home : NavigationDestinations(NavigationRoutes.HOME)
    object AddCard : NavigationDestinations(NavigationRoutes.ADD_CARD)
    object Categories : NavigationDestinations(NavigationRoutes.CATEGORIES)
    object Settings : NavigationDestinations(NavigationRoutes.SETTINGS)

    object Camera : NavigationDestinations(
        route = "${NavigationRoutes.CAMERA}/{${NavigationParams.CARD_TYPE}}",
        arguments = listOf(
            navArgument(NavigationParams.CARD_TYPE) {
                type = NavType.StringType
                defaultValue = "Credit"
            }
        )
    ) {
        fun createRoute(cardType: CardType): String =
            "${NavigationRoutes.CAMERA}/${cardType.toNavigationString()}"
    }

    object CardDetail : NavigationDestinations(
        route = "${NavigationRoutes.CARD_DETAIL}/{${NavigationParams.CARD_ID}}",
        arguments = listOf(
            navArgument(NavigationParams.CARD_ID) { type = NavType.StringType }
        )
    ) {
        fun createRoute(cardId: String): String =
            "${NavigationRoutes.CARD_DETAIL}/$cardId"
    }
}
```

### 2. Create NavHost

```kotlin
@Composable
fun WalletNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.HOME,
        modifier = modifier
    ) {
        composable(NavigationRoutes.HOME) {
            EnhancedHomeScreen(
                onNavigateToAddCard = { navController.navigate(NavigationRoutes.ADD_CARD) },
                onNavigateToCardDetail = { cardId ->
                    navController.navigate(NavigationDestinations.CardDetail.createRoute(cardId))
                }
            )
        }

        composable(
            route = NavigationDestinations.CardDetail.route,
            arguments = NavigationDestinations.CardDetail.arguments
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString(NavigationParams.CARD_ID) ?: return@composable
            CardDetailScreen(cardId = cardId)
        }

        composable(
            route = NavigationDestinations.Camera.route,
            arguments = NavigationDestinations.Camera.arguments
        ) { backStackEntry ->
            val cardTypeString = backStackEntry.arguments?.getString(NavigationParams.CARD_TYPE) ?: "Credit"
            val cardType = CardType.fromSerializableName(cardTypeString)
            CameraScreen(cardType = cardType)
        }
    }
}
```

### 3. Scaffold with Bottom Navigation

```kotlin
@Composable
fun WalletAppScaffold() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigation(navController = navController)
        }
    ) { innerPadding ->
        WalletNavigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
```

---

## Argument Handling

### Retrieve Arguments in Composable

```kotlin
composable(
    route = NavigationDestinations.CardDetail.route,
    arguments = NavigationDestinations.CardDetail.arguments
) { backStackEntry ->
    val cardId = backStackEntry.arguments?.getString(NavigationParams.CARD_ID) ?: return@composable
    CardDetailScreen(cardId = cardId)
}
```

### Retrieve Arguments in ViewModel

```kotlin
@HiltViewModel
class CardDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCardsUseCase: GetCardsUseCase,
) : ViewModel() {
    private val cardId: String = savedStateHandle[NavigationParams.CARD_ID] ?: ""
    // Use cardId to load card data...
}
```

### Pass Results Back via savedStateHandle

```kotlin
object NavigationResultKeys {
    const val FRONT_IMAGE_PATH = "frontImagePath"
    const val BACK_IMAGE_PATH = "backImagePath"
    const val CARD_SAVED = "cardSaved"
}

// Setting a result
navController.previousBackStackEntry?.savedStateHandle?.set(
    NavigationResultKeys.CARD_SAVED, true
)
navController.popBackStack()

// Reading a result
val cardSaved = navController.currentBackStackEntry
    ?.savedStateHandle
    ?.getStateFlow(NavigationResultKeys.CARD_SAVED, false)
    ?.collectAsStateWithLifecycle()
```

---

## Bottom Navigation Pattern

```kotlin
@Composable
fun BottomNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == NavigationRoutes.HOME,
            onClick = {
                navController.navigate(NavigationRoutes.HOME) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        // Categories, Settings items follow same pattern...
    }
}
```

---

## Navigate with Options

```kotlin
// Navigate and clear back stack
navController.navigate(NavigationRoutes.HOME) {
    popUpTo(NavigationRoutes.HOME) { inclusive = true }
}

// Navigate single top (avoid duplicates)
navController.navigate(route) {
    launchSingleTop = true
}

// Navigate back
navController.popBackStack()
```

---

## Critical Rules

### DO
- Use `NavigationDestinations` sealed class for route definitions
- Use `createRoute()` methods for parameterized navigation
- Pass only String IDs as arguments (not complex objects)
- Use `SavedStateHandle` in ViewModels for argument retrieval
- Use `popUpTo` with `launchSingleTop` for bottom navigation
- Extract `NavHost` to a separate composable for testability

### DON'T
- ❌ Don't use `@Serializable` routes (project uses string-based pattern)
- ❌ Don't pass complex objects as navigation arguments
- ❌ Don't create `NavController` inside `NavHost`
- ❌ Don't navigate in `LaunchedEffect` without proper keys
- ❌ Don't hardcode route strings — use `NavigationRoutes` constants

---

## References

- [Navigation with Compose](https://developer.android.com/develop/ui/compose/navigation)
- [Pass Data Between Destinations](https://developer.android.com/guide/navigation/navigation-pass-data)
