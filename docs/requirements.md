# Wallet-CardVault: Product Requirements Document (PRD)

## Overview \& Goals

CardVault is a secure, offline-only Android application designed to provide users with a premium digital repository for all their physical cards. The primary goal is to create an intuitive, visually stunning, and completely secure wallet app that operates entirely offline, ensuring maximum privacy and data security.

**Project Purpose:** To provide a secure, intuitive, and visually stunning offline repository for all a user's cards, from payment cards to custom vouchers, with a focus on premium UI/UX and smooth animations built entirely with Jetpack Compose.

**Key Objectives:**

- Deliver a completely offline experience with no network dependencies
- Provide military-grade security with AES-256 encryption
- Create a premium user experience with smooth 60fps animations
- Enable flexible organization through custom categories and card types
- Ensure data portability through encrypted backup/restore functionality


## User Personas

### Primary Persona 1: The "Frequent Traveler"

**Profile:** Sarah, 34, Management Consultant

- **Needs:** Quick access to passport, multiple credit cards, airline status cards, hotel loyalty cards, and airport lounge passes
- **Pain Points:** Fumbling through physical wallets at security checkpoints, losing important cards while traveling, carrying multiple wallets for different regions
- **Goals:** Streamlined access to travel-related cards, secure storage of sensitive documents, quick identification of relevant cards for specific situations
- **Usage Patterns:** Accesses app multiple times during travel days, needs offline functionality in areas with poor connectivity


### Primary Persona 2: The "Deal Hunter"

**Profile:** Mike, 28, Marketing Specialist

- **Needs:** Management of numerous gift cards, discount vouchers, membership cards, and promotional codes with varying expiry dates
- **Pain Points:** Missing expiry dates on valuable vouchers, losing track of remaining balances, cluttered physical wallet with rarely-used cards
- **Goals:** Visual tracking of card status and expiry dates, easy categorization of different types of deals, ability to mark cards as used/expired
- **Usage Patterns:** Regular app usage for shopping decisions, frequent adding/removing of temporary promotional cards


## User Stories \& Acceptance Criteria

### Card Management

**User Story:** "As a user, I can add a credit card by scanning it with my camera so I don't have to type the details manually."

**Acceptance Criteria:**

- Camera interface opens with clear viewfinder and card outline overlay
- ML Kit successfully recognizes and extracts card number, expiry date, and cardholder name
- Success state shows recognized data with edit capability before saving
- Error states provide clear feedback for poor lighting, unsupported cards, or recognition failures
- Manual input fallback is always available with pre-populated recognized data
- All sensitive data processing occurs on-device only


### Card Animation

**User Story:** "As a user, I can tap on any card in my list to flip it and view the security code (CVV) and signature strip on the back."

**Acceptance Criteria:**

- Animation must maintain 60fps on mid-range devices (API 29+)
- 3D flip effect uses realistic physics with proper perspective and depth
- Animation duration is between 300-500ms for optimal user experience
- Reverse animation triggers on second tap or automatic timeout after 10 seconds
- Both card faces display authentic styling with proper shadows and highlights
- Animation state is preserved during screen rotations


### Custom Sections

**User Story:** "As a user, I can create custom categories (e.g., 'Passport', 'Gym Membership', 'Coffee Vouchers') to organize my cards."

**Acceptance Criteria:**

- User can create unlimited custom categories with names up to 50 characters
- Each category can be assigned a distinct color theme from predefined palette
- All cards within a section inherit the category's color theme in list views
- Categories can be reordered, renamed, and deleted (with confirmation dialog)
- Empty categories display helpful onboarding content
- Default categories include "Payment Cards," "Identity," and "Memberships"


### Custom Card Types

**User Story:** "For a custom card (e.g., a voucher), I can set fields like Expiry Date, Balance, and a 'Mark as Used' toggle."

**Acceptance Criteria:**

- Card creation flow includes field customization options
- Available custom fields: Expiry Date, Balance, Notes, Status Toggle, Custom Labels
- Expired cards display visual indicators (faded appearance, red border)
- "Used" cards show clear visual stamp or overlay on card face
- Balance fields support currency formatting and decimal values
- Expiry date picker integrates with system calendar widget


### Data Portability

**User Story:** "As a user, I can export all my app data to a single, encrypted backup file for safekeeping."
**User Story:** "As a user, I can import a previously created backup file to restore my data on a new device."

**Acceptance Criteria:**

- Export creates encrypted .cvault file with user-defined password
- File includes all cards, categories, settings, and images
- Import process validates file integrity and version compatibility
- Password requirements: minimum 8 characters, mixed case and numbers
- Backup file header contains validation metadata and version info
- Import offers merge or replace options for existing data
- Clear progress indicators during export/import operations


### Offline-First

**User Story:** "As a user, I can use every feature of the app without an active internet connection."

**Acceptance Criteria:**

- Zero network permissions in Android manifest
- All features function identically online and offline
- No external API dependencies for any functionality
- Local ML Kit models for text recognition work offline
- Error handling never assumes network connectivity
- App launches and operates normally in airplane mode


## Non-Functional Requirements

### Security

- **Data at Rest:** AES-256-GCM encryption for all stored data using Android Keystore
- **Backup Security:** Strong encryption using AES-256 with PBKDF2 key derivation
- **Biometric Integration:** Support for fingerprint, face unlock, and pattern authentication
- **Safe Area Feature:** Additional authentication layer for sensitive cards
- **Memory Protection:** Secure memory handling with automatic data clearing
- **No Data Leakage:** Explicit user consent required for any data export


### Performance

- **Animation Performance:** Maintain 60fps for all animations on API 29+ devices
- **Launch Time:** App cold start under 3 seconds on mid-range devices
- **Memory Usage:** Maximum 150MB RAM usage under normal operation
- **Storage Optimization:** Efficient image compression without quality loss
- **Battery Impact:** Minimal battery drain through optimized background processing
- **Responsiveness:** UI interactions respond within 100ms


### Privacy

- **Offline-Only Design:** No data transmission to external servers
- **No Analytics:** Zero tracking, telemetry, or usage analytics
- **Local Processing:** All ML and image processing occurs on-device
- **No Permissions:** Minimal permissions (camera, storage) with clear explanations
- **Data Minimization:** Collect only essential data for app functionality
- **User Control:** Complete user ownership and control of all data

***