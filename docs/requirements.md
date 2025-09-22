# Wallet-CardVault: Product Requirements Document (PRD)

***

## 1. Overview \& Goals

**Product Summary:**  
Wallet-CardVault is an **offline-only Android app** that serves as a digital repository for physical cards—credit, debit, vouchers, gift cards, and other custom card types. The app prioritizes **user convenience, a premium animated interface, and complete data locality** (no network/storage outside the device).

**Project Purpose:**  
Provide a visually rich, animation-driven digital wallet solution allowing **local-only storage** of card/voucher details (including front & back images) with a focus on aesthetics, speed, and simplicity.

**Primary Goals:**
- 100% local data (no cloud or remote backup, sync, or access)
- Best-in-class UI/UX for digital cards using **Jetpack Compose animations**
- Lightweight, fast launch—eligible for lower-end Android devices without compromise

***

## 2. Key Features \& Objectives

| Feature Category           | Objective / Description                                                                                                                                          |
|----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Offline Mode**           | Absolutely no network dependencies, API requests, or permissions.                                                                                                |
| **No Security Layer**      | No encryption, biometric, or backup features. Data is deliberately unprotected for simplicity and transparency.                                                  |
| **Premium Visuals**        | Smooth, animated interface—card flip, entry, and scroll effects at 60fps.                                                                                        |
| **Card Scanning/Autofill** | Camera capture for BOTH sides of every card, with overlays for standard sizes (16:9, 4:3, 3:4,etc.) and offline OCR-based autofill (number, name, expiry, etc.). |
| **Custom Card Types**      | Unlimited user-customizable card types/categories (with user-chosen icon, color, & name).                                                                        |
| **Card Flip Animation**    | 3D flip animation when viewing the back or front.                                                                                                                |
| **Local Image Storage**    | All card images stored internally in the app’s sandboxed storage.                                                                                                |
| **Export/Import**          | Complete data & images migration via a single, non-encrypted file (`.wallet`/`.json`).                                                                           |

***

## 3. User Personas

### Persona 1: Frequent Traveler ("Sarah", 34, Management Consultant)
- **Needs:** Fast access to all travel cards (passport, credit, airline status, hotel loyalty, lounge passes)
- **Pain Points:** Delays at checkpoints, risk of card misplacement, switching wallets for region-specific cards
- **Primary Goals:** Centralize travel cards, quick identification per scenario, robust offline operation
- **Usage:** Multiple daily accesses, especially during travel in low-connectivity environments

### Persona 2: Deal Hunter ("Mike", 28, Marketing Specialist)
- **Needs:** Organize numerous gift/loyalty cards, discount vouchers, memberships, with expiry tracking
- **Pain Points:** Losing value on expired vouchers, balance/usage tracking, cluttered wallet
- **Primary Goals:** Easy categorization, expiry alerts/visibility, ability to tag as used/expired
- **Usage:** Frequent addition/removal of cards, regular review for shopping/planning, total offline demand

***

## 4. User Stories & Acceptance Criteria

### 4.1 Card Management, Image Capture & Autofill
- User can **add** a card/voucher by capturing front & back images (card-shaped overlay, aspect ratio selection)
- App auto-detects/fills a card’s text fields (number, expiry, name) via **offline OCR** (editable before save)
- All fields/images always stored locally
- Users can assign name, icon & color per category and add custom card fields
- User can Update/Delete an existing card/voucher

**Acceptance Criteria:**
- Camera overlay matches common card shapes (16:9, 4:3, 3:4, custom)
- Retake/update either side before saving
- Supports full editing of all autofilled fields, always allows manual fallback

***

### 4.2 Card Animation (UX)
- Card tap triggers a **3D flip animation** (front-back)
- Smooth scrolling/list-entry animations throughout

**Acceptance Criteria:**
- 60fps animation on supported hardware
- Card images always instantly swappable front<>back
- Animation style consistent in all interactive areas

***

### 4.3 Custom Categories & Card Types
- Unlimited user-created card categories (icon, color, name)
- Each card can accept unlimited custom user fields
- Main list: Shows every saved card with thumbnail, title, category tag

**Acceptance Criteria:**
- Add/edit/delete card types/categories directly in the UI at any time

***

### 4.4 Data Portability
- Export/import the entire database including images/settings in one **unencrypted local file** (`.wallet` or `.json`)
- File is self-contained for plug-and-play migration

***

### 4.5 Offline-Only Guarantee
- Manifest blocks all network permissions
- All processing, storage, and features function with zero connectivity

***

## 5. Non-Functional Requirements

| Requirement         | Details                                                                                       |
|---------------------|-----------------------------------------------------------------------------------------------|
| **Security**        | No protection/encryption; data left in plain format for transparency.                         |
| **Performance**     | 60fps animation; app launch < 3 seconds on common hardware (e.g., mid-range Android devices). |
| **Privacy**         | No analytics/tracking SDKs; only requests camera & internal storage permissions.              |
| **Offline Mandate** | Explicitly prevent all network operations through manifest & architecture.                    |


***