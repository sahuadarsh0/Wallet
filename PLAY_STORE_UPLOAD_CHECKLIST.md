# CardVault — Play Store Upload Checklist

> End-to-end checklist for submitting CardVault to Google Play. Walks through pre-flight verification, build artifacts, Play Console configuration, store listing assets, policy compliance, and post-publish monitoring.
>
> Cross-references: [`SECURITY_CHECKLIST.md`](./SECURITY_CHECKLIST.md), [`DATA_SAFETY_FORM.md`](./DATA_SAFETY_FORM.md), [`README.md`](./README.md).
>
> **Legend:** [x] = done · [ ] = action required · [N/A] = not applicable

---

## Phase 1 — Pre-Flight Code & Security

| # | Item | Status | Reference |
|---|------|:------:|-----------|
| 1.1 | Full pass of [`SECURITY_CHECKLIST.md`](./SECURITY_CHECKLIST.md) closed (all action items resolved) | [ ] | §17 of security checklist |
| 1.2 | `./gradlew clean lint` — zero blocker / fatal lint warnings | [ ] | |
| 1.3 | `./gradlew test` — all unit tests pass | [ ] | |
| 1.4 | `./gradlew connectedAndroidTest` — instrumented tests pass on at least one physical device | [ ] | |
| 1.5 | Manual smoke test on min SDK device (Android 10, API 29) | [ ] | |
| 1.6 | Manual smoke test on target SDK device (Android 15, API 36) | [ ] | |
| 1.7 | Manual smoke test on tablet / large screen | [ ] | Required for foldable & tablet quality |
| 1.8 | RTL layout verified (Arabic / Hebrew) | [ ] | `supportsRtl="true"` already set |
| 1.9 | Light + Dark theme verified on every screen | [ ] | |
| 1.10 | Cold-start time < 3 s on mid-range device (Pixel 6a / equivalent) | [ ] | |
| 1.11 | No dropped frames / jank on scroll, card flip, sharing dialog (60 fps) | [ ] | |
| 1.12 | All strings externalised in `strings.xml` (no hardcoded user-facing text) | [ ] | |
| 1.13 | Memory leaks checked with LeakCanary in debug build | [ ] | |

---

## Phase 2 — Versioning & Build Configuration

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 2.1 | `versionCode` incremented (monotonic) | [ ] | Currently `1` in `app/build.gradle.kts` |
| 2.2 | `versionName` updated (semver: `MAJOR.MINOR.PATCH`) | [ ] | Currently `"1.0"` |
| 2.3 | `minSdk = 29`, `targetSdk = 36`, `compileSdk = 36` confirmed | [x] | |
| 2.4 | `applicationId = "com.technitedminds.wallet"` (immutable across releases) | [x] | |
| 2.5 | `isMinifyEnabled = true`, `isShrinkResources = true` for `release` | [x] | |
| 2.6 | `isDebuggable` not overridden in release | [x] | |
| 2.7 | Release signing config wired to `keystore.properties` | [x] | |
| 2.8 | No `// TODO`, `// FIXME`, `printStackTrace` in production code paths | [x] | Grep confirms |
| 2.9 | No leftover test endpoints / mock data flags | [x] | App is offline — no endpoints to begin with |

---

## Phase 3 — Signing Keys

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 3.1 | Play App Signing enrolled (Google holds app signing key) | [ ] | Strongly recommended; one-way enrollment |
| 3.2 | Upload key generated with `keytool -genkey -v -keystore upload-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload` | [ ] | |
| 3.3 | `keystore.properties` populated locally (NOT committed) | [ ] | `.gitignore` already excludes |
| 3.4 | Keystore + password backed up to encrypted password manager | [ ] | Loss of upload key requires Google support reset |
| 3.5 | Two-factor authentication enabled on Google Play Console account | [ ] | Required for publishing |
| 3.6 | Build a release AAB locally and verify signature: `bundletool` / `jarsigner -verify -verbose -certs app-release.aab` | [ ] | |

---

## Phase 4 — Build Artifacts

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 4.1 | Build Android App Bundle (`.aab`) — Play Store requires AAB, not APK | [ ] | `./gradlew bundleRelease` |
| 4.2 | AAB output location: `app/build/outputs/bundle/release/app-release.aab` | [ ] | |
| 4.3 | AAB size reasonable (target < 150 MB; current debug APK ~57 MB → release should shrink further with R8) | [ ] | |
| 4.4 | ProGuard / R8 mapping file: `app/build/outputs/mapping/release/mapping.txt` saved per release | [ ] | Upload to Play Console for crash deobfuscation |
| 4.5 | Native debug symbols (none — pure Kotlin/JVM) | [N/A] | |
| 4.6 | Test the AAB locally with `bundletool build-apks` + install on device | [ ] | Catches per-device-config issues |
| 4.7 | Baseline profile generated and bundled | [x] | `app/release/baselineProfiles/` exists |

```bash
# Recommended pre-upload sequence
./gradlew clean
./gradlew lint
./gradlew test
./gradlew bundleRelease
# Verify
ls -lh app/build/outputs/bundle/release/app-release.aab
ls -lh app/build/outputs/mapping/release/mapping.txt
```

---

## Phase 5 — Play Console: App Setup

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 5.1 | Developer account created & $25 one-time fee paid | [ ] | |
| 5.2 | Developer profile verified (D-U-N-S, government ID per Google's 2024+ policy) | [ ] | |
| 5.3 | App created in Play Console | [ ] | |
| 5.4 | App / Game classification: **App** | [ ] | |
| 5.5 | Free / Paid: **Free** | [ ] | (No IAP) |
| 5.6 | Default language: **English (United States) — en-US** | [ ] | |
| 5.7 | Contact email visible to users | [ ] | |
| 5.8 | Internal testing track set up first (recommended) | [ ] | |
| 5.9 | Closed testing → Open testing → Production graduation plan | [ ] | |

---

## Phase 6 — Store Listing Content

| # | Asset | Status | Spec |
|---|-------|:------:|------|
| 6.1 | **App name** (≤ 30 chars) | [ ] | "CardVault" or "CardVault: Offline Wallet" |
| 6.2 | **Short description** (≤ 80 chars) | [ ] | "Offline-only digital wallet for cards, with PIN lock and AES-256." |
| 6.3 | **Full description** (≤ 4000 chars) | [ ] | Highlight: 100% offline · No network · AES-256 + Tink · PIN + biometric · OCR + NFC · Glassmorphic UI |
| 6.4 | **App icon** — 512×512 px, 32-bit PNG | [ ] | Mipmap already exists in `res/mipmap-*` |
| 6.5 | **Feature graphic** — 1024×500 px, JPG/PNG (no alpha) | [ ] | Hero banner |
| 6.6 | **Phone screenshots** — 2 to 8, 1080×1920 (or 16:9 / 9:16), JPG/PNG | [ ] | Capture: Home, Add Card flow, Card Detail with flip, Settings, Onboarding |
| 6.7 | **7-inch tablet screenshots** — optional but recommended | [ ] | |
| 6.8 | **10-inch tablet screenshots** — optional but recommended | [ ] | |
| 6.9 | **Promo video** — YouTube URL, optional | [ ] | |
| 6.10 | App category: **Finance** | [ ] | |
| 6.11 | Tags (Play Console picker): "Wallet", "Privacy", "Offline" | [ ] | |
| 6.12 | Localised listings (optional, post v1) | [ ] | Plan for future |

---

## Phase 7 — App Content Declarations (Play Console → "App content")

### 7.1 Privacy Policy

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 7.1.1 | Privacy policy URL hosted on a public, stable domain | [ ] | Required because the app collects "Financial info" data type |
| 7.1.2 | Privacy policy URL added in Play Console | [ ] | |
| 7.1.3 | In-app link to privacy policy from Settings | [x] | `settings_privacy_policy` string exists; verify URL in code |
| 7.1.4 | Privacy policy explicitly states: no network, no analytics, no third-party sharing, encryption at rest, deletion rights | [ ] | Use [`DATA_SAFETY_FORM.md`](./DATA_SAFETY_FORM.md) as source of truth |

### 7.2 App Access

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 7.2.1 | "All functionality is available without special access" — **NO** | [ ] | App has PIN gate |
| 7.2.2 | Provide reviewer instructions | [ ] | Use the verbatim instructions in [`DATA_SAFETY_FORM.md` §"Play Console App Access Instructions"](./DATA_SAFETY_FORM.md) |

### 7.3 Ads

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 7.3.1 | "Does your app contain ads?" — **No** | [ ] | |

### 7.4 Content Rating (IARC questionnaire)

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 7.4.1 | Category: **Utility, Productivity, Communication, or Other** | [ ] | |
| 7.4.2 | All questions answered "No" (no violence, no UGC, no chat, no purchases) | [ ] | |
| 7.4.3 | Expected rating: **Everyone (PEGI 3 / ESRB E)** | [ ] | |

### 7.5 Target Audience & Content

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 7.5.1 | Target age range: **18+** (handles financial data) | [ ] | |
| 7.5.2 | Not designed for children — confirm Families Policy does NOT apply | [ ] | |

### 7.6 News App / Health App / Government App

| # | Item | Status |
|---|------|:------:|
| 7.6.1 | News app declaration: **No** | [ ] |
| 7.6.2 | COVID-19 / health app declaration: **No** | [ ] |
| 7.6.3 | Government app declaration: **No** | [ ] |

### 7.7 Data Safety Section

> Use [`DATA_SAFETY_FORM.md`](./DATA_SAFETY_FORM.md) as the verbatim source of truth.

| # | Item | Status |
|---|------|:------:|
| 7.7.1 | "Does your app collect or share any of the required user data types?" — **Yes** | [ ] |
| 7.7.2 | Data types collected: Financial info (card numbers, other financial info), Photos, Personal info (name) | [ ] |
| 7.7.3 | "Is any data shared with third parties?" — **No** | [ ] |
| 7.7.4 | "Is all data encrypted in transit?" — **Yes** (rationale in [`DATA_SAFETY_FORM.md`](./DATA_SAFETY_FORM.md): zero network) | [ ] |
| 7.7.5 | "Encrypted at rest?" — **Yes** (AES-256-GCM via Tink) | [ ] |
| 7.7.6 | "Can users request deletion?" — **Yes** (per-card delete + full data wipe) | [ ] |
| 7.7.7 | Data type collection details for each declared type | [ ] | Mirror exactly the answers in `DATA_SAFETY_FORM.md` |

### 7.8 Government Apps & Financial Features Declaration

> Required for Finance category apps that store payment-card data.

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 7.8.1 | Personal Loan declaration | [ ] | **No** — not a lender |
| 7.8.2 | Cryptocurrency declaration | [ ] | **No** |
| 7.8.3 | Restricted financial features (forex, day trading, etc.) | [ ] | **No** |
| 7.8.4 | Real-money gambling | [ ] | **No** |

### 7.9 Health Connect / Permissions Declarations

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 7.9.1 | Sensitive permissions declaration form | [N/A] | App uses only CAMERA + NFC, neither sensitive in Play Console policy |
| 7.9.2 | `QUERY_ALL_PACKAGES` declaration | [N/A] | Not used |
| 7.9.3 | Background location declaration | [N/A] | Not used |
| 7.9.4 | SMS / Call Log declaration | [N/A] | Not used |
| 7.9.5 | All Files Access (`MANAGE_EXTERNAL_STORAGE`) declaration | [N/A] | Not used — sandboxed only |

---

## Phase 8 — Policy Compliance (Read & Confirm)

| # | Policy | Status | Reference |
|---|--------|:------:|-----------|
| 8.1 | [Developer Program Policies](https://play.google.com/about/developer-content-policy/) — read | [ ] | |
| 8.2 | [User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311) — read | [ ] | |
| 8.3 | [Permissions and APIs that Access Sensitive Information](https://support.google.com/googleplay/android-developer/answer/9888170) — read | [ ] | |
| 8.4 | [Financial Services policy](https://support.google.com/googleplay/android-developer/answer/9876821) — read (storing card data) | [ ] | |
| 8.5 | [Restricted Content — Financial Products](https://support.google.com/googleplay/android-developer/answer/9876821) — read | [ ] | |
| 8.6 | [Target API Level requirements](https://support.google.com/googleplay/android-developer/answer/11926878) — meets target SDK 36 | [x] | Required: API 35 by Aug 2025 — we exceed |
| 8.7 | [Families Policy](https://support.google.com/googleplay/android-developer/answer/9893335) — N/A (18+) | [N/A] | |
| 8.8 | [Spam policy](https://support.google.com/googleplay/android-developer/answer/9888077) — read | [ ] | |
| 8.9 | [Deceptive Behavior policy](https://support.google.com/googleplay/android-developer/answer/9888077) — read | [ ] | |
| 8.10 | App functionality matches description (no hidden features, no unrelated functionality) | [x] | |

---

## Phase 9 — Pricing & Distribution

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 9.1 | Pricing: Free | [ ] | Cannot change to paid later — choose carefully |
| 9.2 | Distribution countries selected (start with all available; exclude embargoed) | [ ] | |
| 9.3 | Distribution on Google Play for ChromeOS | [ ] | Optional; Compose apps work well |
| 9.4 | Distribution on Wear OS / Android TV / Auto: **No** | [ ] | |
| 9.5 | Marketing opt-in: confirm | [ ] | |

---

## Phase 10 — Internal / Closed / Open Testing

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 10.1 | Internal testing track: upload AAB and verify install on tester devices | [ ] | Up to 100 testers; instant rollout |
| 10.2 | Closed testing: 14-day continuous test required for first-time personal-account publishers (post-Nov 2023 Google requirement) | [ ] | At least 12 testers |
| 10.3 | Crash-free sessions > 99.5% during testing | [ ] | Monitored via Play Console "Quality" → "Android vitals" |
| 10.4 | ANR rate < 0.47% | [ ] | |
| 10.5 | Tester feedback addressed | [ ] | |

---

## Phase 11 — Production Release

| # | Item | Status | Notes |
|---|------|:------:|-------|
| 11.1 | Release notes (≤ 500 chars per locale) drafted | [ ] | "Initial release: offline-only wallet for credit, debit, gift, and 13+ other card types. AES-256 encryption, PIN + biometric lock, on-device OCR, NFC card reading." |
| 11.2 | Staged rollout configured (start at 10% → 50% → 100%) | [ ] | Recommended for first launch |
| 11.3 | Mapping file uploaded for the release AAB | [ ] | |
| 11.4 | Release reviewed by another team member if possible | [ ] | |
| 11.5 | Submit for review | [ ] | Review window: typically 1–7 days; longer for first-time finance category publishers |

---

## Phase 12 — Post-Publish Monitoring

| # | Item | Status |
|---|------|:------:|
| 12.1 | Monitor Play Console "Android vitals" daily for first week | [ ] |
| 12.2 | Monitor crash reports & deobfuscate using uploaded mapping file | [ ] |
| 12.3 | Respond to user reviews within 48 h (especially negative ones) | [ ] |
| 12.4 | Monitor "Policy status" tab for any policy strikes | [ ] |
| 12.5 | If staged rollout reveals regressions: halt rollout immediately | [ ] |
| 12.6 | Post-launch retrospective + create v1.1 backlog | [ ] |

---

## Phase 13 — Ongoing Compliance Calendar

| Cadence | Action |
|---------|--------|
| Every release | Re-run [`SECURITY_CHECKLIST.md`](./SECURITY_CHECKLIST.md) + this checklist |
| Annual | Re-attest Data Safety form |
| Annual | Renew Google Play Developer account verification (gov ID) |
| Annual | Audit dependencies for CVEs (`./gradlew dependencyUpdates` + OWASP Dependency-Check) |
| When Google bumps target SDK requirement | Bump `targetSdk`, re-test, re-release within window |
| When dependencies change | Re-run lint, tests, Data Safety review |

---

## Quick-Reference Build Sequence

```bash
# 1. Pre-flight
./gradlew clean
./gradlew lint
./gradlew test

# 2. Build release AAB (requires keystore.properties)
./gradlew bundleRelease

# 3. Verify signature
jarsigner -verify -verbose -certs \
  app/build/outputs/bundle/release/app-release.aab

# 4. Upload artifacts to Play Console
#    - app/build/outputs/bundle/release/app-release.aab
#    - app/build/outputs/mapping/release/mapping.txt   (Android vitals → Deobfuscation files)

# 5. Tag the release in git
git tag -a v1.0.0 -m "Play Store v1.0.0"
git push origin v1.0.0
```

---

## Common Rejection Reasons to Pre-empt

| Risk | Mitigation Already in Place |
|------|----------------------------|
| Permissions misuse declaration | Only CAMERA + NFC, both with clear in-app rationale |
| Missing privacy policy for sensitive-data app | Privacy policy URL & in-app link mandatory before submit (Phase 7.1) |
| Inaccurate Data Safety section | Use `DATA_SAFETY_FORM.md` verbatim (Phase 7.7) |
| Reviewer cannot bypass PIN gate | Reviewer instructions documented in `DATA_SAFETY_FORM.md` (Phase 7.2.2) |
| Target SDK below required | Already on API 36 (Phase 8.6) |
| App stores card data without encryption | AES-256-GCM via Tink + Android Keystore documented |
| Unattributed open-source libraries | "Open Source Licenses" screen exists (Phase 6 / Settings → About) |
| Misleading store listing | Description matches actual functionality |
| Repeated background location / SMS / Contacts asks | None of those permissions used |

---

**Last updated:** 2026-05-22 · **Owner:** TechnitedMinds · **Companion docs:** [`SECURITY_CHECKLIST.md`](./SECURITY_CHECKLIST.md), [`DATA_SAFETY_FORM.md`](./DATA_SAFETY_FORM.md)
