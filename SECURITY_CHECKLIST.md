# CardVault — Security Checklist & Compliance Audit

> Comprehensive security audit covering encryption, authentication, permissions, manifest hardening, build configuration, data handling, and Play Store compliance. Re-run before every release.
>
> **Legend:** [x] = compliant · [ ] = action required · [N/A] = not applicable

---

## 1. Threat Model & Principles

| # | Item | Status | Evidence / Source |
|---|------|:------:|-------------------|
| 1.1 | Offline-only — zero network capability | [x] | `AndroidManifest.xml` removes `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE` via `tools:node="remove"` |
| 1.2 | No analytics, telemetry, ads, or third-party trackers | [x] | No Firebase/Crashlytics/Analytics/Ads dependencies in `gradle/libs.versions.toml` |
| 1.3 | All processing on-device (OCR, NFC, image gen) | [x] | ML Kit `text-recognition` (bundled, offline 16.0.1); CameraX local; NFC EMV local |
| 1.4 | App is sandboxed — no external storage writes | [x] | All paths use `context.filesDir` / `context.cacheDir`; FileProvider only for outbound shares |
| 1.5 | No remote config, no kill-switch, no dynamic code | [x] | No DexClassLoader, WebView, Reflection-driven code paths |

---

## 2. Permissions

| # | Permission | Declared | Justification | Status |
|---|------------|:--------:|---------------|:------:|
| 2.1 | `CAMERA` | yes | Card scanning via CameraX | [x] |
| 2.2 | `NFC` | yes | EMV contactless reading (optional hardware) | [x] |
| 2.3 | `INTERNET` | **REMOVED** | Enforced offline operation | [x] |
| 2.4 | `ACCESS_NETWORK_STATE` | **REMOVED** | Not needed | [x] |
| 2.5 | `ACCESS_WIFI_STATE` | **REMOVED** | Not needed | [x] |
| 2.6 | No `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` | confirmed | Sandboxed only | [x] |
| 2.7 | No `READ_MEDIA_IMAGES` / `READ_MEDIA_VIDEO` | confirmed | Camera capture only — never reads gallery | [x] |
| 2.8 | No location, contacts, microphone, SMS, calendar, phone | confirmed | Not used | [x] |
| 2.9 | Hardware features marked `required="false"` | yes | Camera and NFC declared as optional features | [x] |
| 2.10 | Runtime permission rationale UI for `CAMERA` | yes | `CameraPermission.kt` / `CameraPermissionComponent.kt` show rationale + "Open Settings" CTA | [x] |

---

## 3. Manifest Hardening

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 3.1 | `MainActivity` `android:exported="true"` only because of `LAUNCHER` intent filter | [x] | No other exported components |
| 3.2 | `FileProvider` `android:exported="false"` | [x] | Outbound URIs granted ad-hoc via `FLAG_GRANT_READ_URI_PERMISSION` |
| 3.3 | No exported services, receivers, or content providers | [x] | None declared |
| 3.4 | No custom permissions exposed to other apps | [x] | None declared |
| 3.5 | `android:allowBackup="true"` paired with strict exclusion rules | [x] | All sensitive data excluded — see §6 |
| 3.6 | `android:dataExtractionRules` set (API 31+) | [x] | `@xml/data_extraction_rules` |
| 3.7 | `android:fullBackupContent` set (legacy < API 31) | [x] | `@xml/backup_rules` |
| 3.8 | `android:enableOnBackInvokedCallback="true"` for predictive back | [x] | Modern back gesture support |
| 3.9 | No `android:debuggable="true"` in release | [x] | Default false; not overridden |
| 3.10 | No `android:usesCleartextTraffic="true"` | [x] | Not declared (and irrelevant — no network) |

---

## 4. Build & Signing

| # | Item | Status | Evidence |
|---|------|:------:|----------|
| 4.1 | Release build uses R8/ProGuard (`isMinifyEnabled = true`) | [x] | `app/build.gradle.kts` line 41 |
| 4.2 | Resource shrinking enabled (`isShrinkResources = true`) | [x] | Line 42 |
| 4.3 | Release signing config externalised via `keystore.properties` | [x] | `app/build.gradle.kts` lines 27-37 |
| 4.4 | `keystore.properties`, `*.keystore`, `*.jks` git-ignored | [x] | `.gitignore` lines for "Signing" |
| 4.5 | Keystore stored encrypted off-repo (developer machine + secure backup) | [ ] | **Owner must verify** — not auditable from repo |
| 4.6 | Play App Signing enrolled (Google manages upload key separately) | [ ] | **Verify in Play Console** — recommended |
| 4.7 | Upload key stored separately from Play app signing key | [ ] | **Verify in Play Console** |
| 4.8 | Source-file & line-number attributes kept for crash deobfuscation | [x] | `proguard-rules.pro` `-keepattributes SourceFile,LineNumberTable` |
| 4.9 | `applicationId`, `versionCode`, `versionName` set | [x] | `com.technitedminds.wallet`, `1`, `"1.0"` |
| 4.10 | Target SDK 36 (Android 15), Min SDK 29 (Android 10) | [x] | Meets Play Console policy (Aug 2025: target API 35+; Android 15 = API 35; we exceed) |

---

## 5. ProGuard / R8 Rules

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 5.1 | Tink kept (reflection inside crypto primitives) | [x] | `-keep class com.google.crypto.tink.** { *; }` |
| 5.2 | ML Kit kept | [x] | `-keep class com.google.mlkit.** { *; }` |
| 5.3 | Room entities, DAOs, type converters kept | [x] | Annotation-based keeps |
| 5.4 | Hilt / Dagger kept | [x] | `-keep class dagger.hilt.** { *; }` |
| 5.5 | kotlinx.serialization KSerializer kept | [x] | Companion + `$$serializer` keeps |
| 5.6 | Domain models kept (used by JSON converters) | [x] | `com.technitedminds.wallet.domain.model.**` |
| 5.7 | NFC reader classes kept | [x] | `com.technitedminds.wallet.data.nfc.**` |
| 5.8 | Compose `@Stable` / `@Immutable` annotations preserved | [x] | `-dontwarn androidx.compose.**`; default rules retain runtime annotations |
| 5.9 | Mapping file uploaded to Play Console for crash symbolication | [ ] | **Verify per release** |

---

## 6. Backup / Auto-Restore Hardening

> Sensitive data must NEVER leave the device via Google Backup or device-transfer. Excluded:

| Path | API 31+ rules | Legacy rules |
|------|:-------------:|:------------:|
| `datastore/` (PIN hash, salt, recovery code, lock state) | [x] excluded | [x] excluded |
| `wallet_database` + `-shm` / `-wal` / `-journal` | [x] excluded | [x] excluded |
| `card_images/` | [x] excluded | [x] excluded |
| `thumbnails/` | [x] excluded | [x] excluded |
| `storage_prefs.xml` | [x] excluded | [x] excluded |
| `cardvault_keyset_prefs.xml` (Tink keyset) | [x] excluded | [x] excluded |

| # | Item | Status |
|---|------|:------:|
| 6.1 | Cloud backup excludes all sensitive paths | [x] |
| 6.2 | Device-to-device transfer excludes all sensitive paths | [x] |
| 6.3 | `allowBackup="true"` is intentional for non-sensitive prefs only — verified | [x] |
| 6.4 | Test: install → uninstall → reinstall → no card data restored | [ ] **Verify on device** |

---

## 7. Encryption at Rest

| # | Item | Status | Implementation |
|---|------|:------:|----------------|
| 7.1 | Sensitive card fields (PAN, CVV, expiry, cardholder, custom) encrypted | [x] | `MapConverter` + `TinkEncryptionManager` (AES-256-GCM, `ENC:` prefix) |
| 7.2 | Encryption key in Android Keystore (hardware-backed where available) | [x] | `AndroidKeysetManager` with `MASTER_KEY_URI = "android-keystore://cardvault_master_key"` |
| 7.3 | AEAD (authenticated encryption) used | [x] | `AesGcmKeyManager.aes256GcmTemplate()` |
| 7.4 | Tink initialized before first DB access | [x] | `WalletApplication` injects `TinkEncryptionManager` (Hilt singleton) |
| 7.5 | Backward-compatible decrypt path for legacy plaintext rows | [x] | `if (!ciphertext.startsWith("ENC:")) return ciphertext` |
| 7.6 | PIN never stored in plaintext | [x] | PBKDF2-HmacSHA256, 10 000 iterations, per-user 16-byte salt |
| 7.7 | Recovery code never stored in plaintext | [x] | Same hashing scheme as PIN; plaintext shown ONCE at onboarding |
| 7.8 | All hashes use cryptographically secure salts | [x] | `SecureRandom` in `PinHasher.generateSalt()` |
| 7.9 | Constant-time comparison for hash check | [x] | `MessageDigest.isEqual` used in `PinHasher.verify` (mitigates timing side-channels) |
| 7.10 | Image files NOT encrypted | [x] (by design) | App-private storage relies on Android FBE (file-based encryption) |

**Resolved (7.9):** `PinHasher.verify` now uses `MessageDigest.isEqual` for constant-time comparison:

```kotlin
import java.security.MessageDigest
fun verify(pin: String, salt: String, storedHash: String): Boolean {
    val computed = hash(pin, salt)
    return MessageDigest.isEqual(
        computed.toByteArray(Charsets.UTF_8),
        storedHash.toByteArray(Charsets.UTF_8),
    )
}
```

---

## 8. Authentication & Session

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 8.1 | 4-digit PIN required for app entry (when enabled) | [x] | `AppLockScreen` |
| 8.2 | Biometric unlock (BiometricPrompt) — optional, opt-in | [x] | `BiometricAuthManager` |
| 8.3 | Rate limiting on PIN attempts | [x] | 5 wrong → escalating lockout (30s/60s/2m/5m) |
| 8.4 | Auto data wipe after max failures | [x] | 10 wrong → `clearAllAppData()` (DB, files, prefs, keyset) |
| 8.5 | Recovery code path exists | [x] | 16-char (no O/0/I/1) — hash-stored, plaintext shown once |
| 8.6 | Configurable lock timeout (0/1/5/15 min) | [x] | `getAppLockTimeout()` |
| 8.7 | Auto-exit on card detail (180 s idle) | [x] | Reduces shoulder-surfing |
| 8.8 | App lock re-prompted after configured background time | [x] | `shouldLockNow()` checks `lastUnlockEpoch` vs timeout |
| 8.9 | Splash + onboarding overlaid on dark base to avoid white-flash leak | [x] | `SpaceEnd` background |
| 8.10 | `FLAG_SECURE` on activity displaying sensitive data | [x] | `MainActivity.onCreate` sets `WindowManager.LayoutParams.FLAG_SECURE` — blocks screenshots, screen recording, and hides content from recents thumbnail across the entire single-activity app |

**Resolved (8.10):** `MainActivity` now sets `FLAG_SECURE` for the entire window. Because CardVault is a single-activity Compose app, this covers every screen (CardDetail, AddCard review, AppLock, Onboarding, Home, Settings) without per-screen wiring.

---

## 9. Logging & Sensitive Data Hygiene

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 9.1 | Card numbers, CVV, cardholder name, PIN never logged | [x] | Verified — only Camera lifecycle logs use `Log.d` / `Log.e` |
| 9.2 | `printStackTrace()` not used in main code | [x] | Grep returns 0 matches |
| 9.3 | All `Log.*` calls confined to `CameraManager.kt` and `CameraPreview.kt` | [x] | None contain user data — only camera lifecycle traces |
| 9.4 | R8 strips debug logs in release builds | [x] | `proguard-rules.pro` strips `Log.d` / `Log.v` / `Log.i` via `-assumenosideeffects`; `Log.w` / `Log.e` retained for production diagnostics |
| 9.5 | No `BuildConfig.DEBUG`-gated insecure paths | [x] | None found |
| 9.6 | Toasts / Snackbars never echo card numbers | [x] | UI strings only show generic statuses |
| 9.7 | Crash reports disabled (no crash reporter SDK) | [x] | No Crashlytics / Bugsnag / Sentry |

**Resolved (9.4):** `proguard-rules.pro` now contains:

```proguard
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
```

> `Log.w` and `Log.e` are kept so genuine production failures are still visible to ADB during user-reported issues.

---

## 10. File & IPC Security

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 10.1 | All card images live in app-private `filesDir/card_images/` | [x] | `ImageFileManager` |
| 10.2 | FileProvider authority namespaced to `${applicationId}.fileprovider` | [x] | Manifest line 48 |
| 10.3 | FileProvider paths declare only `files-path`, `cache-path`, `external-cache-path` | [x] | No `external-files-path`, no `root-path` |
| 10.4 | URI grants are short-lived (`FLAG_GRANT_READ_URI_PERMISSION`) | [x] | Per share intent |
| 10.5 | No `MODE_WORLD_READABLE` / `MODE_WORLD_WRITEABLE` | [x] | Grep returns 0 matches |
| 10.6 | Temp share files cleaned up after share | [x] | `shared_cards/` cache dir |
| 10.7 | Image deletion synced with card deletion (no orphaned files) | [x] | `DeleteCardUseCase` + `StorageManagementUseCase` cleanup |
| 10.8 | `WebView` not used (zero attack surface) | [x] | No WebView in code |

---

## 11. Sharing Surface

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 11.1 | User chooses what to share (front / back / both) | [x] | `CardSharingDialog` |
| 11.2 | User opts in to include sensitive data on shared image | [x] | Toggle in dialog |
| 11.3 | Share intent uses `Intent.createChooser` (no implicit broadcast) | [x] | `CardSharingManager` |
| 11.4 | Generated share image written to private cache, not `filesDir` | [x] | `cache/shared_cards/` |
| 11.5 | Watermark option to deter misuse | [x] | Configurable in dialog |

---

## 12. Camera & OCR

| # | Item | Status |
|---|------|:------:|
| 12.1 | Camera permission requested at runtime, not install-time | [x] |
| 12.2 | Camera released in `onDispose` / lifecycle `onStop` | [x] |
| 12.3 | OCR runs on background dispatcher (`Dispatchers.Default` / `IO`) | [x] |
| 12.4 | OCR text immediately scrubbed from memory after parse | [ ] | **Best-effort** — JVM GC; documented as acceptable for offline-only |
| 12.5 | Captured image cropped to overlay region (no extra surroundings) | [x] |
| 12.6 | No raw bitmap leaked outside repository layer | [x] |

---

## 13. NFC / EMV

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 13.1 | `android.hardware.nfc` declared `required="false"` | [x] | App still installs on non-NFC devices |
| 13.2 | NFC enabled only during the explicit "scan card" sheet | [x] | `NfcCardReaderManager` enables `enableReaderMode` and disables on close |
| 13.3 | Only public EMV TLV tags read (PAN, expiry, cardholder, AID) — never CVV (not on chip) | [x] | `EmvCardReader`, `TlvParser` |
| 13.4 | NFC data flows through same encrypted storage path | [x] | Stored via `MapConverter` (encrypted) |
| 13.5 | No background NFC scanning, no NDEF push | [x] |

---

## 14. UI Privacy

| # | Item | Status |
|---|------|:------:|
| 14.1 | Sensitive card fields hidden by default in detail view (Reveal/Hide toggle) | [x] |
| 14.2 | Sensitive section auto-hides on idle timeout | [x] |
| 14.3 | App content hidden in recents thumbnail | [x] | Covered by `FLAG_SECURE` on `MainActivity` (§8.10) |
| 14.4 | Privacy notice card shown in onboarding & settings | [x] |
| 14.5 | Clipboard usage limited to recovery code copy (intentional) — no auto-copy of card numbers | [x] |
| 14.6 | Accessibility: all card images have `contentDescription` | [x] |

---

## 15. Dependency Hygiene

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 15.1 | All deps managed via version catalog (`gradle/libs.versions.toml`) | [x] | Single source of truth |
| 15.2 | No SNAPSHOT / alpha / beta deps in release | [x] | All stable as of versions in catalog |
| 15.3 | No deps with known CVEs (manual scan recommended pre-release) | [ ] | **Run `./gradlew dependencyCheckAnalyze` or Gradle Versions plugin before release** |
| 15.4 | Tink at latest stable (1.20.0) | [x] |
| 15.5 | ML Kit Text Recognition is the **bundled** variant (not Play Services) | [x] | `com.google.mlkit:text-recognition` |
| 15.6 | Compose BOM 2026.01.01 — current | [x] |
| 15.7 | Hilt 2.59.1, Room 2.8.4 — current | [x] |
| 15.8 | No advertising/tracking SDKs (Facebook, Adjust, AppsFlyer, etc.) | [x] |

---

## 16. Code-Review Spot Checks (passed at audit time)

- [x] Grep `INTERNET` → only the explicit `tools:node="remove"` line in manifest.
- [x] Grep `Log\.(d|v|i|w|e)\(` → no card data in any log statement.
- [x] Grep `printStackTrace|TODO\(|FIXME` → no findings in `app/src/main`.
- [x] Grep `MODE_WORLD` → no findings.
- [x] Grep `android:exported` → only `MainActivity` (with launcher intent) + `FileProvider (false)`.
- [x] All exported components reviewed.
- [x] Database migrations / `fallbackToDestructiveMigration` reviewed (acceptable for v1; document migration plan from v2).

---

## 17. Pending Action Items (must close before public release)

> **2026-05-22 audit update:** Items 7.9, 8.10, and 9.4 — previously open in the prior audit — are now **resolved in code**. Remaining items below are environment / process tasks that cannot be auditted from the repo alone.

### Resolved this audit
1. ✅ **[7.9]** `PinHasher.verify` now uses `MessageDigest.isEqual` (constant-time comparison).
2. ✅ **[8.10]** `MainActivity.onCreate` sets `FLAG_SECURE` (blocks screenshots, screen recording, and recents thumbnail leak across the entire single-activity app).
3. ✅ **[9.4]** `proguard-rules.pro` strips `Log.d` / `Log.v` / `Log.i` in release via `-assumenosideeffects`.

### Still open — process / environment tasks
1. **[4.5–4.7]** Confirm release keystore is securely backed up off-device (encrypted password manager + at least one offline copy) and that **Play App Signing** is enrolled before first upload.
2. **[6.4]** Manually verify install → uninstall → reinstall → no card data, images, PIN, or keyset restored. Run on at least one device with cloud-backup enabled and one device-transfer scenario.
3. **[12.4]** Best-effort OCR text scrubbing in memory documented as acceptable trade-off for offline-only app; no further action required for v1.
4. **[15.3]** Run dependency vulnerability scan before each release: `./gradlew dependencyUpdates` and OWASP Dependency-Check.
5. **[Privacy Policy]** Host the privacy policy on a stable public URL; embed the URL in `strings.xml` (`settings_privacy_policy_url` or equivalent). **Required by Play Console because the app declares "Financial info" in Data Safety.**
6. **[Reviewer Access]** Mirror the verbatim reviewer instructions from `DATA_SAFETY_FORM.md` into the Play Console "App access" section.

---

## 18. Annual / Per-Release Re-Audit

Run this checklist:

- [ ] Before every Play Store release.
- [ ] After any new dependency added.
- [ ] After any new permission added.
- [ ] After any AndroidManifest change.
- [ ] After any change in `data/local/security/`, `MapConverter`, `WalletDatabase`, or `FileProvider` config.

---

**Last audit:** 2026-05-22 · **Auditor:** Codebase scan + manual review · **Next audit due:** before next Play Store release.

---

## Appendix A — How to re-run this audit

```bash
# 1. Permission posture
rg "uses-permission" app/src/main/AndroidManifest.xml

# 2. Sensitive-data hygiene (should return 0 user-data hits)
rg -n "Log\.(d|v|i|w|e)\(" app/src/main/java
rg -n "printStackTrace|TODO\(|FIXME"           app/src/main/java
rg -n "MODE_WORLD"                              app/src/main/java
rg -n "android:exported"                        app/src/main

# 3. Constant-time comparison
rg -n "MessageDigest\.isEqual"                  app/src/main/java/com/technitedminds/wallet/data/local/security

# 4. Screenshot block
rg -n "FLAG_SECURE"                             app/src/main/java/com/technitedminds/wallet

# 5. R8 log stripping
rg -n "assumenosideeffects.*android.util.Log"   app/proguard-rules.pro

# 6. Backup exclusions
cat app/src/main/res/xml/data_extraction_rules.xml
cat app/src/main/res/xml/backup_rules.xml
cat app/src/main/res/xml/file_provider_paths.xml

# 7. Dependency posture
rg "firebase|crashlytics|adjust|appsflyer|facebook" gradle/libs.versions.toml
```
