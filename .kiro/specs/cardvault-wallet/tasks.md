# Implementation Plan

- [x] 1. Project Foundation and Core Dependencies
  - Set up project structure with proper package organization following feature-based architecture
  - Configure build.gradle.kts with all required dependencies (CameraX, ML Kit, Proto DataStore)
  - Update AndroidManifest.xml to explicitly block network permissions and add camera permission
  - Create WalletApplication class with Hilt setup
  - _Requirements: 8.1, 8.2, 8.3_

- [x] 2. Domain Layer Implementation
- [x] 2.1 Create core domain models and enums
  - Implement Card data class with all required fields (id, name, type, categoryId, imagePaths, extractedData, customFields, timestamps)
  - Create CardType sealed class with all card types (Credit, Debit, TransportCard, GiftCard, LoyaltyCard, MembershipCard, InsuranceCard, IdentificationCard, Voucher, Event, BusinessCard, LibraryCard, HotelCard, StudentCard, AccessCard, Custom)
  - Implement Category data class with id, name, iconResId, colorHex, isDefault fields
  - Create CardImage data class for image metadata handling
  - Add CardGradient and GradientDirection models for gradient customization
  - _Requirements: 1.1, 1.2, 1.3, 6.1, 6.2_

- [x] 2.2 Define repository interfaces
  - Create CardRepository interface with CRUD operations and search functionality
  - Implement CategoryRepository interface for category management
  - Create ImageRepository interface for image storage and retrieval operations
  - _Requirements: 4.1, 4.2, 6.1_

- [-] 2.3 Implement domain use cases
  - Create AddCardUseCase with validation logic for both textual and image card types
  - Implement GetCardsUseCase with filtering and sorting capabilities
  - Create UpdateCardUseCase and DeleteCardUseCase with proper error handling
  - Implement ProcessCardImageUseCase for OCR processing of textual cards only
  - Create category management use cases (GetCategoriesUseCase, ManageCategoryUseCase)
  - Implement ShareCardUseCase with dual sharing strategy (captured images for image-only cards, gradient designs for textual cards)
  - _Requirements: 1.1, 1.2, 2.1, 2.2, 3.1, 6.1_

- [x] 3. Data Layer Implementation
- [x] 3.1 Set up Room database with entities and DAOs
  - Create CardEntity with proper Room annotations and type converters for Map fields
  - Implement CategoryEntity with relationship mappings
  - Create CardDao with queries for CRUD operations, search, and category filtering
  - Implement CategoryDao with category management operations
  - Set up WalletDatabase class with proper configuration and migrations
  - _Requirements: 4.1, 4.2, 6.1, 10.1, 10.2_

- [x] 3.2 Implement local file storage for images
  - Create ImageFileManager class for handling image file operations in app's sandboxed storage
  - Implement image compression and optimization for efficient storage
  - Create file naming convention and directory structure management
  - Add image cleanup functionality for deleted cards
  - Implement image validation and error handling
  - _Requirements: 2.1, 2.2, 3.1, 4.1, 10.1, 10.3_

- [x] 3.3 Set up Proto DataStore for user preferences
  - Define user preferences proto schema for app settings and categories
  - Create UserPreferencesManager for reading and writing preferences
  - Implement default category initialization and management
  - Add preference validation and migration handling
  - _Requirements: 6.1, 6.2, 10.1_

- [x] 3.4 Implement repository implementations
  - Create CardRepositoryImpl with Room database integration and error handling
  - Implement CategoryRepositoryImpl with DataStore integration
  - Create ImageRepositoryImpl with file system operations and caching
  - Add proper error mapping from data layer to domain layer
  - _Requirements: 4.1, 4.2, 10.1_

- [x] 4. Camera and OCR Integration
- [x] 4.1 Set up CameraX integration
  - Implement CameraPreview composable with proper lifecycle management
  - Create image capture functionality with quality optimization
  - Add camera permission handling and error states
  - Implement camera configuration for optimal card scanning
  - _Requirements: 2.1, 2.2, 3.1, 9.1_

- [x] 4.2 Integrate ML Kit for text recognition
  - Set up ML Kit Text Recognition API for offline OCR processing
  - Implement text extraction logic specifically for credit/debit cards
  - Create text parsing algorithms for card number, expiry date, and cardholder name extraction
  - Add confidence scoring and validation for extracted text
  - Implement fallback mechanisms when OCR fails
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 8.1_

- [x] 4.3 Create camera UI components
  - Implement CardOverlay composable with different aspect ratio guides (16:9, 4:3, 3:4)
  - Create CaptureButton with proper touch feedback and states
  - Add camera controls for flash, focus, and image quality
  - Implement preview and retake functionality for both front and back images
  - _Requirements: 2.1, 2.2, 2.6, 2.7_

- [x] 5. Core UI Components and Animations
- [x] 5.1 Implement card flip animation system
  - Create FlippableCard composable with 3D rotation animation using graphicsLayer
  - Implement smooth 300ms flip transition with proper easing curves
  - Add animation state management for front/back card display
  - Ensure 60fps performance with proper animation optimization
  - Create CardFront and CardBack composables with proper image loading
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 9.1, 9.2_

- [x] 5.2 Create reusable UI components
  - Implement LoadingIndicator with Material Design 3 styling
  - Create ErrorMessage component with retry functionality
  - Build ConfirmationDialog for delete operations and critical actions
  - Implement CardListItem with thumbnail, title, and category display
  - Create CategoryChip component with icon and color customization
  - _Requirements: 5.1, 6.3, 9.1, 9.3_

- [x] 5.3 Implement list animations and transitions
  - Create smooth scroll animations for card lists using LazyColumn
  - Implement item addition/removal animations with proper spring physics
  - Add slide-in animations for new cards and categories
  - Create fade transitions between different UI states
  - Optimize animation performance for large card collections
  - _Requirements: 5.1, 5.2, 5.6, 9.1, 9.3_

- [x] 6. Screen Implementation - Home and Card Management
- [x] 6.1 Implement HomeScreen with card display
  - Create HomeScreen composable with card grid/list layout
  - Implement HomeViewModel with state management for card loading and filtering
  - Add search functionality with real-time filtering
  - Create category filtering with visual indicators
  - Implement pull-to-refresh and empty state handling
  - Add floating action button for adding new cards
  - _Requirements: 1.1, 4.1, 6.3, 9.1, 9.3_

- [x] 6.2 Create CardDetailScreen with flip functionality
  - Implement CardDetailScreen with full-screen card display
  - Create CardDetailViewModel for card data management and editing
  - Add edit mode with field modification capabilities
  - Implement delete functionality with confirmation dialog
  - Create image zoom and pan functionality for detailed viewing
  - Add sharing options (captured images for image-only cards, gradient designs for textual cards)
  - _Requirements: 4.1, 4.2, 5.1, 5.2, 5.3, 5.4_

- [x] 6.3 Implement AddCardScreen with type selection
  - Create AddCardScreen with card type selection interface
  - Implement AddCardViewModel with form state management and validation
  - Add CardTypeSelector component with visual type indicators
  - Create form fields for card name, category selection, and custom fields
  - Implement navigation flow between type selection, camera capture, and form completion
  - Add form validation with real-time error display
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 3.1_

- [ ] 7. Complete Camera Integration and Card Type Selector
- [ ] 7.1 Create dedicated CameraScreen for card capture
  - Implement standalone CameraScreen with full camera functionality
  - Create step-by-step capture flow (front → back → review)
  - Add OCR processing integration for textual cards (Credit/Debit)
  - Implement image preview and retake functionality
  - Create proper navigation integration with AddCardScreen
  - Add camera permission handling and error states
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

- [ ] 7.2 Implement complete CardTypeSelector with gradient system
  - Implement CardTypeSelector with all card types from design (15+ types)
  - Add default gradient system for each card type (Credit: purple-blue, Debit: pink-red, TransportCard: blue-cyan, etc.)
  - Create GradientPickerDialog for custom gradient selection
  - Implement gradient direction options (TopToBottom, LeftToRight, DiagonalTopLeftToBottomRight, DiagonalTopRightToBottomLeft)
  - Add ColorPickerRow components for start/end color selection
  - Create gradient preview functionality with real-time updates
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [ ] 7.3 Enhance FlippableCard with sharing integration
  - Add share buttons to CardFront and CardBack components
  - Implement onShare callbacks for different sharing options (FrontOnly, BackOnly, BothSides)
  - Create CardSharingManager with dual sharing strategy:
    - For image-only cards: Share captured images via FileProvider
    - For textual cards: Generate gradient card designs with extracted details (including CVV)
  - Implement CardGradientGenerator for creating shareable card designs
  - Add share intent creation with proper MIME types
  - Create share button animations and visual feedback
  - _Requirements: 4.1, 4.2, 5.1, 5.2, 5.3_

- [ ] 8. Category Management System
- [ ] 8.1 Implement CategoriesScreen for category management
  - Create CategoriesScreen with category list and management options
  - Implement CategoriesViewModel for category CRUD operations
  - Add category creation dialog with icon and color selection
  - Create category editing functionality with validation
  - Implement category deletion with card reassignment handling
  - Add default category management and restoration
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ] 8.2 Create category selection and management components
  - Implement CategorySelector component for card assignment
  - Create IconPicker with predefined icon set for categories
  - Add ColorPicker with Material Design 3 color palette
  - Implement category preview with icon and color display
  - Create category usage statistics and card count display
  - Add CategoryFilterChips for HomeScreen filtering with count badges
  - _Requirements: 6.1, 6.2, 6.3_

- [ ] 9. Settings and Preferences
- [ ] 9.1 Create SettingsScreen for app configuration
  - Implement SettingsScreen with organized preference sections
  - Create SettingsViewModel for preference management
  - Add theme selection (Light/Dark/System) with immediate preview
  - Implement default category management and reset options
  - Create storage usage display with cleanup options
  - Add app information and version display
  - _Requirements: 6.1, 9.1, 10.1_

- [ ] 9.2 Implement app preferences and storage management
  - Create storage cleanup functionality for orphaned images
  - Implement cache management for temporary files
  - Add database optimization and maintenance routines
  - Create backup validation and repair functionality
  - Implement app reset functionality with confirmation
  - _Requirements: 10.1, 10.3, 10.4, 10.6_

- [ ] 10. Complete Navigation System
- [ ] 10.1 Implement bottom navigation with all screens
  - Create bottom navigation bar with Home, Categories, and Settings tabs
  - Add navigation to CameraScreen, CategoriesScreen, and SettingsScreen
  - Implement proper tab state management and restoration
  - Add navigation badges for categories with card counts
  - Create consistent app bar with contextual actions across all screens
  - _Requirements: 5.1, 9.1, 9.3_

- [ ] 10.2 Complete navigation routing and type-safe arguments
  - Add routes for all screens (Camera, Categories, Settings)
  - Create NavigationDestinations sealed class with all routes
  - Implement NavigationArgs data classes for type-safe parameter passing
  - Add WalletNavigation composable with centralized routing logic
  - Implement proper back stack management and state preservation
  - _Requirements: 5.1, 9.1_

- [ ] 11. Performance Optimization
- [ ] 11.1 Implement performance optimizations
  - Optimize image loading and caching with Coil integration
  - Implement lazy loading for large card collections
  - Add memory management for bitmap operations
  - Create efficient database queries with proper indexing
  - Implement background processing for OCR operations
  - Add performance monitoring and optimization for 60fps animations
  - _Requirements: 9.1, 9.2, 9.3, 9.5, 9.6_