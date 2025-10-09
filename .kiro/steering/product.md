# Product Overview

## CardVault - Secure Offline Digital Wallet

CardVault is an **offline-first Android application** that serves as a digital repository for physical cards (credit, debit, gift cards, vouchers, memberships, etc.). The app prioritizes complete data locality, premium UI/UX with smooth animations, and handles two distinct card workflows: textual cards with OCR capabilities and image-only cards for visual storage.

### Core Principles
- **100% Offline Operation**: No network permissions, no cloud dependencies, all data stays on device
- **Privacy First**: No analytics, tracking, or data collection - complete user control
- **Premium Experience**: 60fps animations, 3D card flip effects, Material Design 3
- **Smart Scanning**: Camera-based card capture with ML Kit OCR for automatic data extraction
- **Flexible Organization**: Custom categories, card types, and fields with visual customization
- **Data Portability**: Export/import functionality for data backup and migration

### Key Features
- **Dual Card Types**: 
  - Textual cards (Credit/Debit) with OCR text extraction
  - Image-only cards (Gym/Voucher/Gift) for visual storage
- **Camera Integration**: CameraX with card overlay and automatic capture
- **3D Card Animations**: Realistic flip animations with smooth transitions
- **Category Management**: Custom categories with icons, colors, and organization
- **Export/Import**: Data portability with .wallet file format
- **Storage Management**: Image optimization, cleanup, and storage statistics
- **Search & Filter**: Quick card discovery with category-based filtering

### Target Users
- **Frequent Travelers**: Need quick access to travel cards without connectivity
- **Deal Hunters**: Organize loyalty cards, vouchers, and track expiry dates
- **Privacy-Conscious Users**: Want complete control over their card data
- **Digital Minimalists**: Reduce physical wallet bulk while maintaining accessibility

### User Experience Goals
- **Intuitive Navigation**: Clear information hierarchy with bottom navigation
- **Visual Appeal**: Card-centric design with realistic card representations
- **Performance**: Smooth interactions with optimized image loading
- **Accessibility**: Material Design 3 compliance with proper contrast and touch targets

The app follows Android architecture best practices with MVVM pattern, Jetpack Compose UI, and a layered reactive architecture ensuring maintainability and testability.