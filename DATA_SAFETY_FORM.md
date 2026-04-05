# CardVault - Google Play Data Safety Form Reference

Use this document when filling out the Data Safety section in Google Play Console.

## Overview

- **Does your app collect or share any of the required user data types?** Yes
- **Does your app transfer data off the device?** No

## Data Types Collected

### Financial Info > Credit/debit card numbers
- **Is this data collected, shared, or both?** Collected
- **Is this data processed ephemerally?** No (stored locally)
- **Is this data required for your app, or can users choose whether it's collected?** Optional
- **Why is this data collected?** App functionality
- **Is this data encrypted in transit?** N/A (never leaves device)
- **Is this data encrypted at rest?** Yes (AES-256-GCM via Google Tink, keyset backed by Android Keystore)
- **Can users request that their data be deleted?** Yes (users can delete individual cards or wipe all data)

### Financial Info > Other financial info (expiry dates, CVV, cardholder name)
- Same answers as above

### Photos and videos > Photos (card images)
- **Is this data collected, shared, or both?** Collected
- **Is this data processed ephemerally?** No (stored locally)
- **Is this data required?** Optional (users choose to capture images)
- **Why is this data collected?** App functionality
- **Is this data encrypted in transit?** N/A (never leaves device)
- **Is this data encrypted at rest?** Yes (stored in app-sandboxed private storage; device encryption applies)
- **Can users request that their data be deleted?** Yes

### Personal info > Name (cardholder name, card name)
- **Is this data collected, shared, or both?** Collected
- **Is this data processed ephemerally?** No (stored locally)
- **Is this data required?** Required (card name), Optional (cardholder name via OCR)
- **Why is this data collected?** App functionality
- **Encrypted in transit?** N/A
- **Encrypted at rest?** Yes (cardholder name in encrypted extracted_data field)
- **Deletable?** Yes

## Data NOT Collected
- Location: No
- Email: No
- Phone number: No
- App activity / web browsing: No
- Diagnostics / crash logs: No (no analytics, no crash reporting)
- Device identifiers: No
- Contacts: No
- Calendar: No
- Files and documents: No
- Audio: No
- Health and fitness: No

## Data Sharing
- **Is any data shared with third parties?** No
- The app operates 100% offline with no network access (INTERNET permission explicitly removed)

## Security Practices
- **Is all of the user data collected by your app encrypted in transit?** Yes
  - Rationale: The app has ZERO network communication (INTERNET permission is explicitly removed via `tools:node="remove"`). Since no user data is ever transmitted off the device, there is no unencrypted data in transit. Select "Yes" in the Play Console form.
- **Is data encrypted at rest?** Yes
  - Sensitive card fields (PAN, CVV, expiry, cardholder name) encrypted with AES-256-GCM
  - PIN stored as PBKDF2-HmacSHA256 hash with random salt
  - Encryption keys stored in Android Keystore-backed keyset
- **Can users request data deletion?** Yes
  - Individual card deletion removes card data and associated images
  - Full data wipe available in Settings and triggered automatically after max failed PIN attempts
- **Does your app follow the Families Policy?** N/A (not a children's app)

## Permissions Declared
| Permission | Purpose | Type |
|---|---|---|
| CAMERA | Card scanning via CameraX | Dangerous (runtime) |
| NFC | Contactless EMV card reading | Normal |
| INTERNET | Explicitly REMOVED (tools:node="remove") | N/A |

## Play Console App Access Instructions
The app uses a PIN-based lock screen. When a reviewer first launches the app:
1. The **Onboarding PIN screen** appears — set any 4-digit PIN (e.g., `1234`).
2. A **recovery code** is displayed — tap "I've Saved It" to proceed.
3. The app opens to the Home screen. Full functionality is now available.
4. If the app is closed and reopened, enter the PIN set in step 1 (`1234`).

**Provide these instructions in the "App access" section of the Play Console App content page.**

## Notes for Review
- ML Kit Text Recognition is the bundled variant (com.google.mlkit:text-recognition) and runs 100% on-device
- No Google Play Services dependency for core functionality
- No advertising SDK, analytics, or telemetry of any kind
- Poppins font is bundled locally (not downloaded via Google Fonts) for full offline support
