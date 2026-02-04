# Implementation Plan

- [ ] 1. Update CardDisplaySection with enhanced styling and spacing
  - Increase card height to 220dp minimum for better visibility
  - Update padding around card to 16dp for consistent spacing
  - Add spacing of 16dp between card and instructions
  - Ensure instructions use bodySmall typography and onSurfaceVariant color
  - Verify FlippableCard maintains existing flip animation and sharing functionality
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 2. Enhance CardInfoSection with improved visual hierarchy
  - Update InfoCard elevation to 2dp for subtle depth
  - Apply 16dp padding inside InfoCard containers
  - Set spacing between sections to 16dp
  - Update InfoRow spacing to 8dp between rows
  - Ensure label text uses bodyMedium with onSurfaceVariant color
  - Ensure value text uses bodyMedium with onSurface color for emphasis
  - Verify sensitive data masking works correctly (card number, CVV, PIN, password)
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 5.2, 5.3, 5.4_

- [x] 3. Integrate PremiumCard containers in EditCardSection
  - Replace existing Card components with PremiumCard in edit mode
  - Set elevation to 2dp for all PremiumCard instances
  - Apply 20dp padding inside each PremiumCard
  - Set spacing between PremiumCard sections to 16dp
  - Ensure PremiumCard uses proper Material 3 colors and shape
  - _Requirements: 2.1, 2.2, 4.5, 9.1, 9.2, 9.3_

- [x] 4. Add AnimatedSectionHeader to all edit sections
  - Add AnimatedSectionHeader to Basic Information section with Edit icon and subtitle
  - Add AnimatedSectionHeader to Card Information section with Scanner icon and subtitle (OCR cards only)
  - Add AnimatedSectionHeader to Additional Information section with Notes icon and subtitle
  - Add AnimatedSectionHeader to Appearance section with Palette icon and subtitle
  - Ensure each header displays icon in circular gradient background
  - Verify header animations (slide-in from top with fade) work smoothly
  - _Requirements: 2.3, 4.1, 4.2, 4.3, 4.4_

- [x] 5. Implement staggered slide-in animations for edit sections
  - Wrap Basic Information section with EnhancedSlideInItem (index 0)
  - Wrap Card Information section with EnhancedSlideInItem (index 1)
  - Wrap Additional Information section with EnhancedSlideInItem (index 2)
  - Wrap Appearance section with EnhancedSlideInItem (index 3)
  - Wrap Save Reminder section with EnhancedSlideInItem (index 4)
  - Verify 100ms stagger delay between sections
  - Test animation smoothness and timing
  - _Requirements: 2.5, 3.1, 3.2_

- [x] 6. Replace card name TextField with PremiumTextField
  - Replace existing OutlinedTextField with PremiumTextField for card name
  - Add credit card icon as leadingIcon
  - Ensure proper focus management and styling
  - Verify haptic feedback on interaction
  - Test validation and error display
  - _Requirements: 4.1, 4.5_

- [ ] 7. Add save reminder card at bottom of edit mode
  - Create new PremiumCard with primaryContainer background color
  - Add Info icon in circular background (40dp size)
  - Add reminder text using bodyMedium typography
  - Apply 20dp padding inside the card
  - Position as last item in EditCardSection
  - Wrap with EnhancedSlideInItem (index 4)
  - _Requirements: 3.3, 3.4, 3.5_

- [ ] 8. Update mode transition animations
  - Implement 300ms fade animation when entering edit mode
  - Implement 300ms fade animation when exiting edit mode
  - Ensure smooth crossfade between view and edit content
  - Verify no layout jumps during transition
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ] 9. Update top app bar actions and behavior
  - Verify view mode shows share, edit, and delete buttons
  - Verify edit mode shows cancel and save buttons
  - Ensure share button opens CardSharingDialog (existing)
  - Ensure delete button shows confirmation dialog
  - Update top app bar title to show card name in view mode and "Edit Card" in edit mode
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 5.1_

- [ ] 10. Update Scaffold window insets configuration
  - Set contentWindowInsets to WindowInsets(0, 0, 0, 0) in Scaffold
  - Verify no extra top padding appears on the screen
  - Test on different Android versions and screen sizes
  - _Requirements: 9.5_

- [ ] 11. Verify card type adaptation logic
  - Test OCR card (Credit/Debit) shows Extracted Information section in view mode
  - Test Image card (Gym/Voucher/Gift) hides Extracted Information section in view mode
  - Test OCR card shows ExtractedDataEditor in edit mode
  - Test Image card skips extracted data section in edit mode
  - Verify proper icons display for each card type
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ] 12. Update spacing and layout consistency across all sections
  - Apply 16dp padding around CardDisplaySection
  - Apply 16dp padding around CardInfoSection
  - Apply 16dp padding around EditCardSection
  - Set 16dp spacing between major sections
  - Set 12dp spacing between form fields within edit sections
  - Verify consistent spacing throughout the screen
  - _Requirements: 9.1, 9.2, 9.3, 9.4_

- [ ] 13. Verify loading and error states
  - Test initial loading shows centered indicator with "Loading card details" text
  - Test save operation shows LoadingOverlay with "Processing" text
  - Test delete operation shows LoadingOverlay with "Processing" text
  - Test share operation shows LoadingOverlay with "Processing" text
  - Test error messages display correctly and can be dismissed
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ] 14. Test all interactions and animations
  - Test card flip animation on tap (600ms duration, smooth rotation)
  - Test long-press on card opens sharing dialog
  - Test edit mode activation and cancellation
  - Test save button saves changes and exits edit mode
  - Test cancel button discards changes and exits edit mode
  - Test delete confirmation dialog and card deletion
  - Test all section animations appear with proper stagger
  - Test haptic feedback on all interactive elements
  - _Requirements: 1.3, 1.4, 3.1, 3.2, 3.3, 3.4, 3.5, 4.5_

- [ ] 15. Verify existing functionality preservation
  - Test CardSharingDialog opens and functions correctly
  - Test quick share buttons on card work as expected
  - Test ExtractedDataEditor allows editing OCR fields
  - Test CustomFieldsEditor allows adding/removing custom fields
  - Test CardTypeDropdown shows all card types with icons
  - Test CategoryDropdown shows all categories with colors
  - Test EnhancedColorPicker allows color selection with preview
  - Test all sharing options (front only, both sides, info only)
  - _Requirements: 6.3, 6.5, 8.1, 8.2, 8.3, 8.4, 8.5_
