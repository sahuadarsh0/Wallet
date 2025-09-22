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
  - Create CardType sealed class with Credit, Debit, ATM, and ImageOnly variants
  - Implement Category data class with id, name, iconResId, colorHex, isDefault fields
  - Create CardImage data class for image metadata handling
  - _Requirements: 1.1, 1.2, 1.3, 6.1, 6.2_

- [x] 2.2 Define repository interfaces
  - Create CardRepository interface with CRUD operations and search functionality
  - Implement CategoryRepository interface for category management
  - Create ImageRepository interface for image storage and retrieval operations
  - Define ExportImportRepository interface for data portability features
  - _Requirements: 4.1, 4.2, 6.1, 7.1_

- [x] 2.3 Implement domain use cases
  - Create AddCardUseCase with validation logic for both textual and image card types
  - Implement GetCardsUseCase with filtering and sorting capabilities
  - Create UpdateCardUseCase and DeleteCardUseCase with proper error handling
  - Implement ProcessCardImageUseCase for OCR processing of textual cards only
  - Create category management use cases (GetCategoriesUseCase, ManageCategoryUseCase)
  - Implement ExportDataUseCase and ImportDataUseCase for data portability
  - _Requirements: 1.1, 1.2, 2.1, 2.2, 3.1, 6.1, 7.1_

- [ ] 3. Data Layer Implementation
- [ ] 3.1 Set up Room database with entities and DAOs
  - Create CardEntity with proper Room annotations and type converters for Map fields
  - Implement CategoryEntity with relationship mappings
  - Create CardDao with queries for CRUD operations, search, and category filtering
  - Implement CategoryDao with category management operations
  - Set up WalletDatabase class with proper configuration and migrations
  - _Requirements: 4.1, 4.2, 6.1, 10.1, 10.2_

- [ ] 3.2 Implement local file storage for images
  - Create ImageFileManager class for handling image file operations in app's sandboxed storage
  - Implement image compression and optimization for efficient storage
  - Create file naming convention and directory structure management
  - Add image cleanup functionality for deleted cards
  - Implement image validation and error handling
  - _Requirements: 2.1, 2.2, 3.1, 4.1, 10.1, 10.3_

- [ ] 3.3 Set up Proto DataStore for user preferences
  - Define user preferences proto schema for app settings and categories
  - Create UserPreferencesManager for reading and writing preferences
  - Implement default category initialization and management
  - Add preference validation and migration handling
  - _Requirements: 6.1, 6.2, 10.1_

- [ ] 3.4 Implement repository implementations
  - Create CardRepositoryImpl with Room database integration and error handling
  - Implement CategoryRepositoryImpl with DataStore integration
  - Create ImageRepositoryImpl with file system operations and caching
  - Implement ExportImportRepositoryImpl for JSON serialization and file operations
  - Add proper error mapping from data layer to domain layer
  - _Requirements: 4.1, 4.2, 7.1, 7.2, 10.1_

- [ ] 4. Camera and OCR Integration
- [ ] 4.1 Set up CameraX integration
  - Implement CameraPreview composable with proper lifecycle management
  - Create image capture functionality with quality optimization
  - Add camera permission handling and error states
  - Implement camera configuration for optimal card scanning
  - _Requirements: 2.1, 2.2, 3.1, 9.1_

- [ ] 4.2 Integrate ML Kit for text recognition
  - Set up ML Kit Text Recognition API for offline OCR processing
  - Implement text extraction logic specifically for credit/debit/ATM cards
  - Create text parsing algorithms for card number, expiry date, and cardholder name extraction
  - Add confidence scoring and validation for extracted text
  - Implement fallback mechanisms when OCR fails
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 8.1_

- [ ] 4.3 Create camera UI components
  - Implement CardOverlay composable with different aspect ratio guides (16:9, 4:3, 3:4)
  - Create CaptureButton with proper touch feedback and states
  - Add camera controls for flash, focus, and image quality
  - Implement preview and retake functionality for both front and back images
  - _Requirements: 2.1, 2.2, 2.6, 2.7_

- [ ] 5. Core UI Components and Animations
- [ ] 5.1 Implement card flip animation system
  - Create FlippableCard composable with 3D rotation animation using graphicsLayer
  - Implement smooth 300ms flip transition with proper easing curves
  - Add animation state management for front/back card display
  - Ensure 60fps performance with proper animation optimization
  - Create CardFront and CardBack composables with proper image loading
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 9.1, 9.2_

- [ ] 5.2 Create reusable UI components
  - Implement LoadingIndicator with Material Design 3 styling
  - Create ErrorMessage component with retry functionality
  - Build ConfirmationDialog for delete operations and critical actions
  - Implement CardListItem with thumbnail, title, and category display
  - Create CategoryChip component with icon and color customization
  - _Requirements: 5.1, 6.3, 9.1, 9.3_

- [ ] 5.3 Implement list animations and transitions
  - Create smooth scroll animations for card lists using LazyColumn
  - Implement item addition/removal animations with proper spring physics
  - Add slide-in animations for new cards and categories
  - Create fade transitions between different UI states
  - Optimize animation performance for large card collections
  - _Requirements: 5.1, 5.2, 5.6, 9.1, 9.3_

- [ ] 6. Screen Implementation - Home and Card Management
- [ ] 6.1 Implement HomeScreen with card display
  - Create HomeScreen composable with card grid/list layout
  - Implement HomeViewModel with state management for card loading and filtering
  - Add search functionality with real-time filtering
  - Create category filtering with visual indicators
  - Implement pull-to-refresh and empty state handling
  - Add floating action button for adding new cards
  - _Requirements: 1.1, 4.1, 6.3, 9.1, 9.3_

- [ ] 6.2 Create CardDetailScreen with flip functionality
  - Implement CardDetailScreen with full-screen card display
  - Create CardDetailViewModel for card data management and editing
  - Add edit mode with field modification capabilities
  - Implement delete functionality with confirmation dialog
  - Create image zoom and pan functionality for detailed viewing
  - Add sharing options for card information (without images for security)
  - _Requirements: 4.1, 4.2, 5.1, 5.2, 5.3, 5.4_

- [ ] 6.3 Implement AddCardScreen with type selection
  - Create AddCardScreen with card type selection interface
  - Implement AddCardViewModel with form state management and validation
  - Add CardTypeSelector component with visual type indicators
  - Create form fields for card name, category selection, and custom fields
  - Implement navigation flow between type selection, camera capture, and form completion
  - Add form validation with real-time error display
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 3.1_

- [ ] 7. Camera Integration Screens
- [ ] 7.1 Create camera capture workflow for textual cards
  - Implement CameraCaptureScreen with overlay guides and capture controls
  - Create step-by-step flow for front image → OCR processing → back image → review
  - Add OCR results display with editable fields for extracted data
  - Implement retry mechanism for poor quality captures
  - Create progress indicators for OCR processing
  - Add manual entry fallback when OCR fails
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

- [ ] 7.2 Create camera capture workflow for image cards
  - Implement simplified capture flow for image-only cards (gym, voucher, etc.)
  - Create basic form for card name and optional notes
  - Add optional expiry date field for vouchers and gift cards
  - Implement immediate save functionality without OCR processing
  - Create preview screen for captured images before saving
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.6, 3.7_

- [ ] 8. Category Management System
- [ ] 8.1 Implement CategoriesScreen for category management
  - Create CategoriesScreen with category list and management options
  - Implement CategoriesViewModel for category CRUD operations
  - Add category creation dialog with icon and color selection
  - Create category editing functionality with validation
  - Implement category deletion with card reassignment handling
  - Add default category management and restoration
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ] 8.2 Create category selection components
  - Implement CategorySelector component for card assignment
  - Create IconPicker with predefined icon set for categories
  - Add ColorPicker with Material Design 3 color palette
  - Implement category preview with icon and color display
  - Create category usage statistics and card count display
  - _Requirements: 6.1, 6.2, 6.3_

- [ ] 9. Data Export and Import System
- [ ] 9.1 Implement data export functionality
  - Create ExportScreen with export options and progress tracking
  - Implement export process that includes all card data, images, and categories
  - Create JSON serialization for card data with proper image encoding
  - Add file creation in app's external storage with .wallet extension
  - Implement export validation and integrity checking
  - Create export completion notification with file sharing options
  - _Requirements: 7.1, 7.2, 7.3, 7.6_

- [ ] 9.2 Implement data import functionality
  - Create ImportScreen with file selection and import progress
  - Implement file validation for .wallet and .json formats
  - Add conflict resolution for existing cards and categories
  - Create image extraction and storage from import file
  - Implement data integrity validation during import
  - Add import completion summary with success/failure statistics
  - _Requirements: 7.3, 7.4, 7.5, 7.6_

- [ ] 10. Settings and Preferences
- [ ] 10.1 Create SettingsScreen for app configuration
  - Implement SettingsScreen with organized preference sections
  - Create SettingsViewModel for preference management
  - Add theme selection (Light/Dark/System) with immediate preview
  - Implement default category management and reset options
  - Create storage usage display with cleanup options
  - Add app information and version display
  - _Requirements: 6.1, 9.1, 10.1_

- [ ] 10.2 Implement app preferences and storage management
  - Create storage cleanup functionality for orphaned images
  - Implement cache management for temporary files
  - Add database optimization and maintenance routines
  - Create backup validation and repair functionality
  - Implement app reset functionality with confirmation
  - _Requirements: 10.1, 10.3, 10.4, 10.6_

- [ ] 11. Navigation and App Structure
- [ ] 11.1 Set up Compose Navigation with type-safe arguments
  - Create NavigationDestinations with proper argument definitions
  - Implement WalletNavigation with all screen routes and transitions
  - Add navigation animations between screens
  - Create deep linking support for card details and categories
  - Implement proper back stack management and state preservation
  - _Requirements: 5.1, 9.1_

- [ ] 11.2 Implement bottom navigation and app structure
  - Create main navigation structure with Home, Categories, and Settings tabs
  - Implement proper tab state management and restoration
  - Add navigation badges for categories with card counts
  - Create consistent app bar with contextual actions
  - Implement proper keyboard handling and focus management
  - _Requirements: 5.1, 9.1, 9.3_

- [ ] 12. Performance Optimization and Testing
- [ ] 12.1 Implement performance optimizations
  - Optimize image loading and caching with Coil integration
  - Implement lazy loading for large card collections
  - Add memory management for bitmap operations
  - Create efficient database queries with proper indexing
  - Implement background processing for OCR operations
  - Add performance monitoring and optimization for 60fps animations
  - _Requirements: 9.1, 9.2, 9.3, 9.5, 9.6_

- [ ] 12.2 Create comprehensive test suite
  - Write unit tests for all ViewModels with state verification
  - Implement repository tests with mock data sources
  - Create use case tests with business logic validation
  - Add UI tests for critical user flows (add card, flip animation, export/import)
  - Implement integration tests for database operations and file storage
  - Create performance tests for animation frame rates and app launch time
  - _Requirements: 9.1, 9.2, 9.4, 9.6_

- [ ] 13. Security and Privacy Implementation
- [ ] 13.1 Implement offline-only security measures
  - Verify AndroidManifest.xml blocks all network permissions
  - Implement file permission restrictions for app's private storage
  - Add data validation and sanitization for all user inputs
  - Create secure file deletion for sensitive card images
  - Implement app integrity checks and tamper detection
  - _Requirements: 8.1, 8.2, 8.3, 8.5, 8.6_

- [ ] 13.2 Add privacy protection features
  - Implement app backgrounding protection (hide content in recent apps)
  - Create screenshot prevention for sensitive screen
  - _Requirements: 8.1, 8.4, 8.5_

- [ ] 14. Final Integration and Polish
- [ ] 14.1 Complete app integration and testing
  - Integrate all screens and components into cohesive app flow
  - Perform end-to-end testing of all user scenarios
  - Optimize app startup time and memory usage
  - Implement proper error handling and recovery throughout the app
  - Add accessibility features and screen reader support
  - Create app icon and splash screen with proper theming
  - _Requirements: 9.1, 9.2, 9.6_

- [ ] 14.2 Final validation and deployment preparation
  - Validate all requirements are met through comprehensive testing
  - Optimize APK size and remove unused resources
  - _Requirements: 8.1, 8.2, 9.1, 9.6_