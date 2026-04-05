---
name: android-testing
description: Comprehensive testing strategy for CardVault involving Unit, Integration, Hilt, and Compose UI tests. Offline-first, no network mocking needed.
---

# Android Testing Strategies (CardVault)

This skill provides expert guidance on testing the CardVault offline-first Android app. Covers **Unit Tests**, **Room Integration Tests**, **Hilt Tests**, and **Compose UI Tests**.

## Testing Pyramid

1.  **Unit Tests**: Fast, isolate logic (ViewModels, Use Cases, Mappers, Repositories with fakes).
2.  **Integration Tests**: Test real Room database operations, DataStore, file operations.
3.  **UI Tests**: Verify Compose UI correctness and navigation flows.

## Dependencies (`libs.versions.toml`)

```toml
[libraries]
junit = { module = "junit:junit", version = "4.13.2" }
androidx-test-ext-junit = { group = "androidx.test.ext", name = "junit", version = "1.3.0" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version = "3.7.0" }
compose-ui-test = { group = "androidx.compose.ui", name = "ui-test-junit4" }
compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
```

## Unit Tests

### ViewModel Testing

```kotlin
@Test
fun `loading cards updates UI state to success`() = runTest {
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val fakeRepository = FakeCardRepository(testCards)
    val getCardsUseCase = GetCardsUseCase(fakeRepository)
    val viewModel = HomeViewModel(getCardsUseCase, testDispatcher)

    viewModel.loadCards()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.cards.isNotEmpty())
    assertFalse(state.isLoading)
}
```

### Use Case Testing

```kotlin
@Test
fun `add card validates name is not blank`() = runTest {
    val fakeRepository = FakeCardRepository()
    val addCardUseCase = AddCardUseCase(fakeRepository)

    val result = addCardUseCase(card.copy(name = ""))

    assertTrue(result.isFailure)
}
```

### Mapper Testing

```kotlin
@Test
fun `card mapper converts entity to domain correctly`() {
    val mapper = CardMapper()
    val entity = createTestCardEntity()

    val domain = mapper.toDomain(entity)

    assertEquals(entity.id, domain.id)
    assertEquals(entity.name, domain.name)
    assertIs<CardType.Credit>(domain.type)
}
```

## Room Integration Tests

Test real database operations with in-memory database:

```kotlin
@HiltAndroidTest
class CardDaoTest {
    @get:Rule var hiltRule = HiltAndroidRule(this)

    private lateinit var database: WalletDatabase
    private lateinit var cardDao: CardDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WalletDatabase::class.java
        ).allowMainThreadQueries().build()
        cardDao = database.cardDao()
    }

    @After
    fun teardown() { database.close() }

    @Test
    fun insertAndRetrieveCard() = runTest {
        val entity = createTestCardEntity(id = "test-uuid")
        cardDao.insert(entity)

        val cards = cardDao.getAllCards().first()
        assertEquals(1, cards.size)
        assertEquals("test-uuid", cards[0].id)
    }

    @Test
    fun searchCardsByName() = runTest {
        cardDao.insert(createTestCardEntity(name = "My Visa"))
        cardDao.insert(createTestCardEntity(name = "Work Debit"))

        val results = cardDao.searchCards("Visa").first()
        assertEquals(1, results.size)
        assertEquals("My Visa", results[0].name)
    }
}
```

## Compose UI Tests

```kotlin
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysCardList() {
        val testCards = listOf(createTestCard(name = "My Card"))

        composeTestRule.setContent {
            WalletTheme {
                EnhancedHomeScreen(
                    cards = testCards,
                    onNavigateToAddCard = {},
                    onNavigateToCardDetail = {}
                )
            }
        }

        composeTestRule.onNodeWithText("My Card").assertIsDisplayed()
    }

    @Test
    fun homeScreen_emptyState_showsMessage() {
        composeTestRule.setContent {
            WalletTheme {
                EnhancedHomeScreen(
                    cards = emptyList(),
                    onNavigateToAddCard = {},
                    onNavigateToCardDetail = {}
                )
            }
        }

        composeTestRule.onNodeWithText("No cards yet").assertIsDisplayed()
    }
}
```

## Key Testing Patterns for CardVault

### No Network Mocking Needed
CardVault is 100% offline — no Retrofit, no `MockWebServer`, no network error simulation. Focus on:
- Room DAO operations (CRUD, search, category filtering)
- DataStore Preferences read/write
- File operations (image save/delete/validate)
- OCR text parsing (`CardTextParser` with sample text)
- Security (PIN hashing, recovery codes)

### Testing OCR Parsing

```kotlin
@Test
fun `card text parser extracts card number`() {
    val rawText = "4532 0123 4567 8901\nEXP 12/25\nJOHN DOE"

    val result = CardTextParser.parseCardText(rawText)

    assertEquals("4532012345678901", result.cardNumber)
    assertEquals("12/25", result.expiryDate)
    assertEquals("JOHN DOE", result.cardholderName)
}
```

### Testing Security

```kotlin
@Test
fun `pin hasher produces different hash for different salt`() {
    val pinHasher = PinHasher()
    val hash1 = pinHasher.hashPin("1234", "salt1")
    val hash2 = pinHasher.hashPin("1234", "salt2")

    assertNotEquals(hash1, hash2)
}

@Test
fun `pin hasher verifies correct pin`() {
    val pinHasher = PinHasher()
    val salt = pinHasher.generateSalt()
    val hash = pinHasher.hashPin("1234", salt)

    assertTrue(pinHasher.verifyPin("1234", hash, salt))
    assertFalse(pinHasher.verifyPin("5678", hash, salt))
}
```

## Running Tests

```bash
./gradlew test                     # Unit tests
./gradlew connectedAndroidTest     # Instrumented tests (requires device/emulator)
```
