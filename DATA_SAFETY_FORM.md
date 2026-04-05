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
- **Is data encrypted in transit?** Not applicable (no network communication)
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

## Notes for Review
- ML Kit Text Recognition is the bundled variant (com.google.mlkit:text-recognition) and runs 100% on-device
- No Google Play Services dependency for core functionality
- No advertising SDK, analytics, or telemetry of any kind
