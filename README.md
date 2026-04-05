# 🏦 CardVault: Secure Offline Card Repository

CardVault is a secure, offline-first Android application that acts as a premium digital repository for all your physical cards — credit, debit, gift cards, vouchers, memberships, and 10+ more types. The app operates **100% on-device** with no network permissions, no analytics, and no data ever leaving the device.

---

## ✨ Feature Highlights

### 🔒 Security & Privacy
- **🔐 At-rest encryption** — sensitive card data (PAN, CVV, extracted fields) encrypted with **AES-256-GCM** via Google Tink, keyset stored in the **Android Keystore** (hardware-backed on supported devices). The `MapConverter` transparently encrypts/decrypts Room columns with an `ENC:` prefix, backward-compatible with legacy plaintext data.
- **🔑 PIN protection** — 4-digit PIN hashed with **PBKDF2-HmacSHA256**, 10 000 iterations, per-user random 16-byte salt. Never stored in plaintext.
- **👆 Biometric unlock** — AndroidX BiometricPrompt integration (Fingerprint/Face). The app never accesses raw biometric data.
- **🚫 Rate-limited lockout** — 5 failed attempts triggers escalating lockout tiers; 10 attempts triggers **automatic data wipe**.
- **🛟 Recovery codes** — 16-character human-readable codes (no ambiguous characters: O/0/I/1). Shown once at onboarding; hash+salt stored for later verification.
- **⏱️ Auto-exit on card detail** — a **180-second idle timer** closes the detail view in read mode to reduce shoulder-surfing risk.
- **⏰ Configurable lock timeout** — 0 / 1 / 5 / 15 minutes after backgrounding.
- **🌑 Layered startup** — splash → onboarding → lock screen overlaid on a dark base (`SpaceEnd` background) to avoid white-flash security leaks.

### 📸 Card Capture & Input
- **📷 Camera capture** — front and back images with CameraX 1.5.3. Supports multiple aspect ratios: `CREDIT_CARD` (ISO 1.586:1), `16:9`, `4:3`, `3:4`. Defaults auto-set by card type (Credit/Debit → credit card ratio; Business → 16:9; others → 4:3).
- **🔍 On-device OCR** — Google ML Kit Text Recognition (bundled, offline) extracts card number, expiry date, cardholder name, and CVV from credit/debit cards. OCR review step lets users verify and correct results.
- **📡 NFC EMV reading** — tap-to-read for credit/debit cards via `EmvCardReader` + `TlvParser`. Reads PAN, expiry, cardholder name, AID, and card scheme. CVV is entered manually via a follow-up dialog (not available over NFC). Available when hardware supports it.
- **🖼️ Image-only workflow** — 14 non-OCR card types store front/back photos as-is, no text processing.
- **✍️ Manual entry** — skip camera entirely and fill fields by hand.

### 🏠 Home Screen
- **🔎 Search** — collapsible search bar with **300 ms debounce**; searches across card name, type, extracted data, and custom fields.
- **🏷️ Filters** — chip bar + modal bottom sheet: filter by category, filter by card type, clear all.
- **⊞ Layout toggle** — switch between **grid** and **list** views.
- **🔃 Sort** — cards ordered by last-updated descending.
- **⏳ Loading state** — full-screen `PremiumLoadingIndicator` during data fetch.

### 💳 Card Detail
- **🔄 3D flip animation** — `FlippableCard` renders front/back with 300 ms perspective flip.
- **🙈 Sensitive data section** — extracted OCR fields shown separately; hidden from ambient view.
- **✏️ In-place edit mode** — name, category, type, OCR fields, custom fields, gradient, all editable inline with smooth mode transition.
- **🌈 Ambient background** — subtle gradient derived from the card's own colors.
- **📤 Share** — long-press or share button opens `CardSharingDialog`; configure which sides to include, watermark, and whether to include sensitive data.

### 📤 Card Sharing
- **🎨 Dual share strategy** — credit/debit cards get a **generated gradient image** with extracted details rendered on-canvas; all other types share the **original captured photos**.
- **🔗 `FileProvider` intents** — secure file sharing without exposing raw paths.
- **🖊️ Configurable** watermark and sensitive data inclusion.

### 🗂️ Category Management
- **📂 Default categories** — `General` (fallback), `Personal`, `Business`, `Travel`, `Shopping`, `Health`, `Entertainment`.
- **➕ Custom categories** — name, optional description, color picker, icon picker, sort order.
- **✏️ Edit & delete** — deleting a category safely migrates its cards to `General`.
- **🔄 Reset defaults** — restores predefined set without touching custom categories.

### ⚙️ Settings
1. **📊 App statistics** — total cards, categories, storage used/available, orphaned file count, last cleanup timestamp.
2. **🎨 Appearance** — Light / Dark / System theme via `ThemeMode` enum; Material 3 dynamic color on API 31+.
3. **🔐 Privacy & security** — app lock toggle, biometric toggle, set/change PIN, regenerate + view recovery code, lock timeout picker.
4. **🗂️ Category management** — navigate to categories, reset defaults with confirmation.
5. **🗑️ Storage** — run cleanup with confirmation; low-storage warning row.
6. **ℹ️ App information** — version, build type, privacy policy, terms, open-source licenses.

### 🚀 Onboarding
1. 🎉 Welcome screen — animated background, three feature bullets (offline, secure storage, 16+ card types).
2. **🔐 Secure with PIN** → `AppLockScreen` in Setup mode → non-dismissible `RecoveryCodeOnboardingDialog` (user must acknowledge the code).
3. **⏭️ Skip for now** → skips PIN; lock can be enabled later in Settings.

---

## 🛠️ Tech Stack

| Component | Library | Version |
|---|---|---|
| 💬 Language | Kotlin | 2.1.0 |
| 🔨 Build system | Android Gradle Plugin | 9.0.0 |
| ⚙️ Annotation processing | KSP | 2.3.5 |
| 🖼️ UI toolkit | Jetpack Compose BOM | 2026.01.01 |
| 🎨 Design system | Material Design 3 | 1.4.0 |
| 💉 Dependency injection | Hilt | 2.59.1 |
| 🗺️ Navigation | Compose Navigation | 2.9.7 |
| 🗄️ Database | Room | 2.8.4 |
| 💾 Preferences | DataStore Preferences | 1.2.0 |
| 📷 Camera | CameraX | 1.5.3 |
| 🔍 OCR | ML Kit Text Recognition (bundled) | 16.0.1 |
| 🖼️ Image loading | Coil | 2.7.0 |
| 🔐 Encryption | Google Tink Android | 1.20.0 |
| 👆 Biometrics | AndroidX Biometric | 1.1.0 |
| 📦 Serialization | kotlinx.serialization | 1.10.0 |
| 💫 Splash screen | AndroidX Core Splashscreen | 1.2.0 |

**🎯 Build targets:** Min SDK 29 (Android 10) · Target SDK 36 (Android 15) · Java 11

---

## 🏗️ Architecture

Clean Architecture with MVVM + Unidirectional Data Flow (UDF):

```
User Action → ViewModel → Use Case → Repository → Data Source
     ↑               ↓
     UI ← StateFlow ← ViewModel ← Domain Model ← Repository
```

- **🖥️ Presentation** — Jetpack Compose screens and ViewModels; consumes `StateFlow` via `collectAsStateWithLifecycle()`.
- **⚙️ Domain** — pure Kotlin; business logic in use cases; zero Android imports.
- **💾 Data** — Room, DataStore, file system, Tink; maps entities to domain models via `CardMapper` / `CategoryMapper`.

### 💉 Hilt Modules
- `DatabaseModule` — `WalletDatabase`, `CardDao`, `CategoryDao` (Singleton)
- `RepositoryModule` — binds interfaces to implementations
- `CameraModule` — CameraX + ML Kit dependencies
- `SecurityModule` — `PinHasher`, `RecoveryCodeManager`, `TinkEncryptionManager`

---

## 📁 Project Structure

```
com.technitedminds.wallet/
├── MainActivity.kt                    # 🚦 AppPhase state machine (SPLASH→ONBOARDING→APP_LOCK→READY)
├── WalletApplication.kt               # 🏁 @HiltAndroidApp — initialises TinkEncryptionManager before DB
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   ├── dao/                   # 🗄️ CardDao, CategoryDao
│   │   │   ├── entities/              # 📋 CardEntity, CategoryEntity
│   │   │   └── converters/            # 🔄 CardTypeConverter, MapConverter (Tink-encrypted), CardGradientConverter
│   │   ├── preferences/               # 💾 SimplePreferencesManager (DataStore Preferences)
│   │   ├── security/                  # 🔐 AppLockRepository, PinHasher, RecoveryCodeManager, TinkEncryptionManager
│   │   ├── files/                     # 🖼️ ImageFileManager
│   │   ├── cache/                     # ⚡ CacheManager
│   │   └── storage/                   # 📦 StorageManager
│   ├── nfc/                           # 📡 EmvCardReader, NfcReaderCallback, TlvParser
│   ├── ocr/                           # 🔍 MLKitTextRecognizer, CardTextParser
│   ├── mapper/                        # 🔁 CardMapper, CategoryMapper
│   ├── repository/                    # 🏛️ CardRepositoryImpl, CategoryRepositoryImpl, ImageRepositoryImpl
│   └── service/                       # ⚙️ OCRServiceImpl, CardImageGeneratorImpl, StorageServiceImpl
├── domain/
│   ├── model/                         # 📐 Card, CardType, Category, CardGradient, CardImage,
│   │                                  #    NfcCardData, OCRResult, StorageModels
│   ├── repository/                    # 📜 CardRepository, CategoryRepository, ImageRepository (interfaces)
│   ├── service/                       # 📜 CardImageGenerator, OCRService, StorageService (interfaces)
│   └── usecase/
│       ├── card/                      # 🃏 AddCard, DeleteCard, GetCards, UpdateCard, ShareCard
│       ├── category/                  # 🗂️ GetCategories, GetCategoryName, ManageCategory
│       ├── ocr/                       # 🔍 ProcessCardImage
│       └── storage/                   # 💾 StorageManagement
├── presentation/
│   ├── screens/
│   │   ├── home/                      # 🏠 EnhancedHomeScreen, HomeViewModel
│   │   ├── addcard/                   # ➕ AddCardScreen (add + edit), AddCardViewModel, NfcCardReaderManager
│   │   ├── camera/                    # 📷 CameraScreen, CameraViewModel
│   │   ├── carddetail/                # 🔎 CardDetailScreen, CardDetailViewModel
│   │   ├── categories/                # 🗂️ CategoriesScreen, CategoriesViewModel, CategoryDialog
│   │   ├── settings/                  # ⚙️ SettingsScreen, SettingsViewModel
│   │   ├── onboarding/                # 🎉 OnboardingPinScreen
│   │   └── security/                  # 🔐 AppLockScreen, AppLockViewModel, BiometricAuthManager
│   ├── components/
│   │   ├── common/                    # 🧩 PremiumCard, PremiumTextField, AnimatedSectionHeader,
│   │   │                              #    EnhancedSlideInItem, StepProgressIndicator, ColorPicker,
│   │   │                              #    GradientPicker, CustomFieldsEditor, NfcScanningSheet,
│   │   │                              #    OCRStatusCards, PrivacyNoticeCard, SplashOverlay, etc. (31 files)
│   │   ├── camera/                    # 📷 CameraPreview, CaptureButton, CardOverlay, ImagePreview,
│   │   │                              #    CameraPermission, CameraManager, CameraError (7 files)
│   │   ├── animation/                 # 🎬 FlippableCard, CardFront, CardBack, AnimatedList,
│   │   │                              #    EnhancedAnimations, LiquidDrag, AnimationUtils
│   │   └── sharing/                   # 📤 CardSharingDialog, CardSharingManager, CardSharingOption
│   ├── navigation/                    # 🗺️ WalletNavigation, WalletAppScaffold, BottomNavigation,
│   │                                  #    NavigationDestinations, NavigationExtensions
│   ├── constants/                     # 📌 AppConstants (dimensions, animation, OCR keys, UI strings)
│   └── utils/                         # 🔧 PerformanceOptimizer
├── di/                                # 💉 DatabaseModule, RepositoryModule, CameraModule, SecurityModule
├── ui/theme/                          # 🎨 Color, ColorUtils, GlassTheme, WalletSpring, WalletTiming, Theme, Type
└── utils/                             # 🔧 Extensions
```

---

## 🗺️ Navigation

String-based routes via `NavigationDestinations` sealed class with `navArgument()` for parameters (not `@Serializable` objects):

| 🖥️ Destination | 🔗 Route | 📝 Notes |
|---|---|---|
| 🏠 Home | `home` | Bottom nav tab |
| ➕ Add Card | `add_card` | Global center FAB button |
| 📷 Camera | `camera/{cardType}` | String arg; entered from add-card flow |
| 🔎 Card Detail | `card_detail/{cardId}` | String UUID arg |
| ✏️ Edit Card | `edit_card/{cardId}` | Reuses `AddCardScreen` |
| 🗂️ Categories | `categories` | Reached from Settings |
| ⚙️ Settings | `settings` | Bottom nav tab |

**🧭 Bottom navigation** has two tabs (🏠 Home, ⚙️ Settings) and a center ➕ **Add Card** button — Categories is not a tab.

📬 Camera results (captured image paths, OCR extracted data) are passed back to the add-card screen via `savedStateHandle` using `NavigationResultKeys`.

---

## 🎨 Design System

### 🌫️ Glassmorphic Theme (`GlassTheme.kt`)
- `GlassColors` — light/dark frosted surface tokens, iridescent border brushes.
- `GlassSurface` — shared composable for frosted-glass containers.
- `PillCornerRadius` — shared radius for chips and bottom nav.
- `LocalGlassColors` — CompositionLocal providing glass tokens to the tree.

### 🌊 Physics-Based Animation (`WalletSpring`, `WalletTiming`)
- **🔁 Spring presets** — `bouncy`, `snappy`, `gentle`, `card`, `stiff`, `liquid`, `elastic`.
- **⏱️ Timing constants** — stagger delays, glass fade, card flip (300 ms), splash duration.
- **💧 `LiquidDrag`** — custom Compose modifier for press/drag "liquid" deformation effects on cards.
- **✨ `EnhancedAnimations`** — shimmer, slide-in, staggered entrance helpers.

### 🎨 Color Palette
- 💜 Wallet accents: `WalletPink`, `WalletPurple`, `WalletBlue`, `WalletGreen`.
- 🌌 Backgrounds: `SpaceStart`, `SpaceEnd` (deep space dark gradient).
- 🔵 M3 seed: purple family.
- 🖌️ `ColorUtils` — `contrastText`, `gradientContrastText`, `isLightColor` for readable text on any gradient.

---

## 🔐 Security Deep-Dive

| 🛡️ Layer | ⚙️ Mechanism | 🔧 Implementation |
|---|---|---|
| 💾 Data at rest | AES-256-GCM | `TinkEncryptionManager` + Android Keystore-backed keyset |
| 🗄️ Room map columns | Transparent encrypt/decrypt | `MapConverter` with `ENC:` prefix; legacy plaintext backward-compatible |
| 🔑 PIN storage | PBKDF2-HmacSHA256, 10k iterations | `PinHasher` — hash+salt stored, never plaintext |
| 👆 Biometric | AndroidX BiometricPrompt | `BiometricAuthManager` wrapper |
| 🚫 Brute-force protection | 5 attempts → escalating lockout; 10 → wipe | `AppLockRepository` rate limiter |
| 🛟 Recovery | 16-char code (no O/0/I/1) | `RecoveryCodeManager` — hash+salt stored, plaintext shown once |
| ⏱️ Auto-exit | 180 s idle countdown | `CardDetailScreen` security timer bar |
| 📁 File storage | App-private `filesDir/images/` | `ImageFileManager`; inaccessible to other apps |
| 📡 NFC read | PAN + expiry from EMV tags | `EmvCardReader` + `TlvParser`; CVV never on chip |
| 🚀 Startup order | Tink init before DB open | `WalletApplication` injects `TinkEncryptionManager` at startup |

---

## 💳 Card Types

| 🃏 Type | 📥 Input Method |
|---|---|
| 💳 **Credit** | 📷 Camera OCR + 📡 NFC EMV read |
| 💳 **Debit** | 📷 Camera OCR + 📡 NFC EMV read |
| 🚌 Transport | 🖼️ Image only |
| 🎁 Gift | 🖼️ Image only |
| 🏆 Loyalty | 🖼️ Image only |
| 🏅 Membership | 🖼️ Image only |
| 🏥 Insurance | 🖼️ Image only |
| 🪪 Identification | 🖼️ Image only |
| 🎟️ Voucher | 🖼️ Image only |
| 🎫 Event | 🖼️ Image only |
| 💼 Business | 🖼️ Image only |
| 📚 Library | 🖼️ Image only |
| 🏨 Hotel | 🖼️ Image only |
| 🎓 Student | 🖼️ Image only |
| 🔑 Access | 🖼️ Image only |
| ✨ **Custom** | 🖼️ Image only + custom name & color |

---

## 🚀 Getting Started

1. **📥 Clone the repository:**
   ```bash
   git clone https://github.com/sahuadarsh0/wallet.git
   ```

2. **🖥️ Open in Android Studio** — latest stable version, API 36 SDK installed.

3. **🔄 Gradle Sync** — let Android Studio download all dependencies.

4. **▶️ Build and run** on a device or emulator running Android 10+.

### 🔧 Useful build commands

```bash
./gradlew assembleDebug           # 🏗️ Build debug APK
./gradlew installDebug            # 📲 Install and launch on connected device
./gradlew kspDebugKotlin          # ⚙️  Generate KSP sources (Room, Hilt)
./gradlew test                    # 🧪 Unit tests
./gradlew connectedAndroidTest    # 📱 Instrumented tests
./gradlew clean                   # 🧹 Clean build outputs
```

---

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE.md) file for details.
