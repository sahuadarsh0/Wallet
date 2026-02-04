# Implementation Plan

- [x] 1. Enhance TypeSelectionStep with premium card type grid
  - Replace existing CardTypeSelector with enhanced grid layout
  - Create CardTypeItem composable with gradient background and icon
  - Organize types into two sections: "Cards with Text Recognition" and "Image-Only Cards"
  - Apply 2-column grid with 16dp spacing
  - Add selection border (2dp primary color) for selected type
  - Wrap each card with EnhancedBounceClickable for premium interaction
  - Use card type's default gradient as background
  - Display card type icon (32dp) and name (bodyMedium)
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 2. Implement enhanced StepProgressIndicator
  - Create three-step indicator: Type, Capture, Details
  - Display current step with filled primary circle
  - Display completed steps with checkmark icon in primary circle
  - Display future steps with number in muted circle
  - Connect steps with horizontal lines that change color based on completion
  - Apply circle size of 40dp with proper spacing
  - Add step labels below circles using labelSmall typography
  - Animate circle fill and checkmark appearance with spring animation
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [x] 3. Refactor CameraCaptureStep for front-first flow
  - Update to show front capture instructions first
  - Display large camera icon (120dp) with primary color
  - Show contextual instructions based on card type
  - After front capture, show "Capture Back" and "Skip Back" buttons
  - Update bottom bar to show appropriate actions based on capture state
  - Add CaptureState enum to track: AWAITING_FRONT, FRONT_CAPTURED, BACK_CAPTURED
  - Handle automatic progression to form details after captures
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9_

- [ ] 4. Implement OCR processing with image discard for textual cards
  - Update camera capture handler to process OCR for Credit/Debit cards
  - Extract text data from captured images using ML Kit
  - Store extracted data in ViewModel state
  - Discard original images after OCR processing completes
  - Handle OCR failure gracefully with manual entry fallback
  - Update extractedData map with: cardNumber, expiryDate, cardholderName, cvv
  - _Requirements: 3.4, 4.1, 4.2, 4.3_

- [ ] 5. Implement image storage for image-only cards
  - Save captured front image to file system for non-OCR cards
  - Save captured back image to file system if provided
  - Store image paths in ViewModel state
  - Optimize and compress images before saving
  - Handle image save failures with error messages
  - _Requirements: 3.5, 18.2, 18.5, 18.6, 18.7_

- [x] 6. Create OCRStatusCard and ManualEntryCard components
  - Implement OCRStatusCard with primaryContainer background
  - Display AutoAwesome icon and "Text Recognition Complete" message
  - Show count of extracted fields
  - Implement ManualEntryCard with secondaryContainer background
  - Display Edit icon and "Manual Entry Required" message
  - Apply 2dp elevation to both cards
  - _Requirements: 4.1, 4.2, 4.3_

- [x] 7. Enhance BasicInformationSection with premium components
  - Replace card name TextField with PremiumTextField
  - Add Badge icon as leading icon for card name
  - Ensure CategoryDropdown uses proper styling
  - Apply 12dp spacing between fields
  - Wrap section with PremiumCard container
  - Add AnimatedSectionHeader with appropriate icon
  - _Requirements: 5.1, 5.2, 8.1, 8.2_

- [x] 8. Implement CardInformationSection for OCR cards
  - Create section with four PremiumTextField components
  - Card Number field: Payment icon, auto-format with spaces every 4 digits
  - Expiry Date field: DateRange icon, auto-format as MM/YY
  - Cardholder Name field: Person icon, auto-convert to uppercase
  - CVV field: Security icon, limit to 3-4 digits
  - Add AutoAwesome trailing icon for auto-detected fields
  - Implement "Clear All and Enter Manually" button when OCR data exists
  - Apply field validation with error messages
  - Only show section for OCR card types
  - _Requirements: 5.3, 5.4, 5.5, 8.3, 14.1, 14.3, 15.1, 15.2, 15.3, 15.4_

- [ ] 9. Implement AppearanceSection with gradient picker for all card types
  - Create section for all card types (OCR and image-only)
  - Integrate EnhancedColorPicker component
  - Display gradient preview with selected colors
  - Provide default gradient based on card type
  - Allow custom gradient selection with start color, end color, and direction
  - Store selected gradient in ViewModel state
  - Wrap section with PremiumCard and AnimatedSectionHeader
  - _Requirements: 8.4, 17.1, 17.2, 17.3, 17.4, 17.5, 17.6, 17.7_

- [ ] 10. Enhance AdditionalInformationSection with custom fields
  - Display existing custom fields with OutlinedTextField
  - Add Close icon trailing button for field removal
  - Implement "Add Field" button with Add icon
  - Apply fade-out animation when removing fields
  - Support unlimited custom fields
  - Wrap section with PremiumCard and AnimatedSectionHeader
  - _Requirements: 8.1, 8.2, 9.1, 9.2, 9.3, 9.4, 9.5_

- [x] 11. Create PrivacyNoticeCard component
  - Use surfaceVariant background color
  - Display Security icon (20dp)
  - Show "Privacy and Security" title
  - Add explanation text about offline-only operation
  - Apply proper padding and spacing
  - Position at bottom of form
  - _Requirements: 8.6_

- [ ] 12. Implement CardGradientGenerator for OCR cards
  - Create utility class for generating gradient card images
  - Implement generateCreditCardFront method with card data overlay
  - Implement generateCreditCardBack method with CVV and magnetic stripe
  - Use Canvas API to draw gradient backgrounds
  - Overlay text data with proper typography and positioning
  - Save generated images to file system
  - Return image paths for storage in card entity
  - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 16.7_

- [ ] 13. Implement default back image generation for image cards with custom gradient
  - Create generateDefaultBack method in CardGradientGenerator accepting gradient parameter
  - Use user-selected gradient (or default if not customized)
  - Display card type icon (80dp, semi-transparent) in center
  - Show card name in center bottom
  - Add "CardVault" watermark at bottom
  - Save generated image to file system
  - _Requirements: 18.1, 18.3_

- [ ] 14. Update AddCardViewModel save logic for dual storage strategy
  - Check card type to determine storage strategy
  - For OCR cards: Generate gradient images, save extracted data, discard captures
  - For image cards: Save captured images, generate default back if needed
  - Update AddCardUseCase to handle both strategies
  - Store gradient information in card's custom fields
  - Handle save failures with specific error messages
  - _Requirements: 16.1, 16.2, 16.5, 18.1, 18.2, 18.4_

- [ ] 15. Implement contextual AddCardBottomBar
  - Hide bottom bar on type selection step
  - Show "Skip Camera" and "Open Camera" buttons before front capture
  - Show "Capture Back" and "Skip Back" buttons after front capture
  - Show full-width "Save Card" button on form details step
  - Display loading indicator on save button when saving
  - Disable save button when form is invalid
  - Use PremiumButton components with haptic feedback
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_

- [x] 16. Implement step transition animations
  - Apply slideInHorizontally + fadeIn for forward navigation (300ms)
  - Apply slideInHorizontally (from left) + fadeIn for backward navigation (300ms)
  - Use EaseOutCubic easing for smooth transitions
  - Ensure AnimatedContent wraps step content properly
  - Test animation performance maintains 60fps
  - _Requirements: 6.1, 6.2, 6.5_

- [x] 17. Implement section slide-in animations
  - Wrap each form section with EnhancedSlideInItem
  - Apply staggered delays: 0ms, 100ms, 200ms, 300ms, 400ms
  - Use slideInVertically (from bottom) + fadeIn effect
  - Set duration to 400ms with EaseOutCubic easing
  - Test animation smoothness and timing
  - _Requirements: 6.3, 6.4_

- [ ] 18. Implement form validation with real-time feedback
  - Validate card name: required, min 2 characters
  - Validate category: required selection
  - Validate images: front required (or skipped for manual entry)
  - For OCR cards: validate card number (13-19 digits), expiry (MM/YY format)
  - Display field-specific error messages below fields
  - Clear errors when user starts editing
  - Update canSave state based on validation results
  - _Requirements: 5.5, 10.1, 10.2, 10.3, 10.4, 10.5, 15.5_

- [ ] 19. Implement back navigation with data preservation
  - Add back button to top app bar (except on first step)
  - Handle back button click to navigate to previous step
  - Preserve all entered data when navigating backward
  - Apply reverse slide animation for backward navigation
  - Add close button (X) that exits flow from any step
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

- [ ] 20. Implement loading states and error handling
  - Display LoadingOverlay with "Saving card..." during save operation
  - Disable all form interactions while saving
  - Show specific error messages for validation failures
  - Show error messages for OCR processing failures
  - Show error messages for image save failures
  - Show error messages for gradient generation failures
  - Provide retry options for failed operations
  - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5_

- [ ] 21. Update spacing and layout consistency
  - Apply 16dp padding around all major content sections
  - Use 16dp spacing between form sections
  - Use 12dp spacing between form fields within sections
  - Apply 2dp elevation to all PremiumCard containers
  - Ensure consistent Material Design 3 spacing throughout
  - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5_

- [ ] 22. Implement card type adaptation logic
  - Show Card Information section only for OCR card types
  - Show Appearance section for all card types
  - Hide Card Information section for image-only card types
  - Pre-fill OCR fields when extracted data is available
  - Display empty fields for manual entry when no OCR data
  - Validate OCR fields as required, other fields as optional
  - Ensure gradient selection is available for all card types
  - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 17.1, 17.7_

- [ ] 23. Test complete add card flow for OCR cards
  - Test type selection → camera capture → OCR processing → form details → save
  - Test gradient generation with extracted data
  - Test custom gradient selection
  - Test manual entry when OCR fails
  - Test skip camera flow with manual entry
  - Test back navigation preserves data
  - Test validation prevents save with invalid data
  - Verify captured images are discarded after OCR
  - Verify generated gradient images are saved correctly
  - _Requirements: All OCR-related requirements_

- [ ] 24. Test complete add card flow for image cards
  - Test type selection → camera capture → form details → save
  - Test front image only with default back generation
  - Test front and back image capture and save
  - Test skip camera flow
  - Test back navigation preserves data
  - Verify captured images are saved correctly
  - Verify default back image is generated when needed
  - _Requirements: All image card-related requirements_

- [ ] 25. Verify animations and performance
  - Test step transition animations are smooth (300ms)
  - Test section slide-in animations with proper stagger (100ms)
  - Test progress indicator animations
  - Test button press animations with haptic feedback
  - Verify 60fps performance during all animations
  - Test on mid-range devices for performance
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_
