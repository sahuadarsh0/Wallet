# Design Document: Enhanced Add Card Flow

## Overview

The Enhanced Add Card Flow redesign transforms the card creation experience into a premium, streamlined multi-step wizard that intelligently handles two distinct workflows: gradient-based textual cards (Credit/Debit) with OCR processing, and image-based cards (Gym/Voucher/Gift/etc.) with actual photo storage. The design emphasizes visual clarity, smooth animations, and efficient user input while maintaining the offline-first architecture.

### Design Goals

1. **Streamlined Workflow**: Minimize steps and user friction with intelligent defaults and optional captures
2. **Visual Excellence**: Leverage enhanced UI components for a premium, polished experience
3. **Smart Processing**: Differentiate between OCR cards (generate gradients) and image cards (save photos)
4. **Flexible Capture**: Make back image capture optional with intelligent fallbacks
5. **Clear Guidance**: Provide contextual instructions and progress indication throughout the flow

### Key Design Decisions

**Dual Storage Strategy**:
- **OCR Cards (Credit/Debit)**: Capture images temporarily for OCR processing, then discard. Generate gradient card images with extracted text data.
- **Image Cards (Others)**: Save actual captured images. Generate default gradient for back if not captured.

**Optional Back Capture**:
- Front image is always required (or skipped entirely for manual entry)
- Back image is optional for all card types
- System provides intelligent defaults when back is skipped

## Architecture

### Component Hierarchy

```
AddCardScreen (Main Container)
├── Scaffold
│   ├── AddCardTopBar
│   │   ├── Navigation (Back/Close buttons)
│   │   └── Title (Dynamic based on step)
│   ├── Content (AnimatedContent with step transitions)
│   │   ├── TypeSelectionStep
│   │   │   ├── Section Header ("Cards with Text Recognition")
│   │   │   ├── CardTypeGrid (Credit, Debit)
│   │   │   ├── Section Header ("Image-Only Cards")
│   │   │   └── CardTypeGrid (All other types)
│   │   ├── CameraCaptureStep
│   │   │   ├── CaptureInstructions
│   │   │   ├── CameraPreview (when active)
│   │   │   └── CaptureControls
│   │   └── FormDetailsStep
│   │       ├── OCRStatusCard (if OCR data available)
│   │       ├── ManualEntryCard (if no OCR data)
│   │       ├── BasicInformationSection
│   │       │   ├── PremiumTextField (Card Name)
│   │       │   └── CategoryDropdown
│   │       ├── CardInformationSection (OCR cards only)
│   │       │   ├── PremiumTextField (Card Number)
│   │       │   ├── PremiumTextField (Expiry Date)
│   │       │   ├── PremiumTextField (Cardholder Name)
│   │       │   └── PremiumTextField (CVV)
│   │       ├── AppearanceSection (OCR cards only)
│   │       │   └── EnhancedColorPicker
│   │       ├── AdditionalInformationSection
│   │       │   ├── CustomFieldsList
│   │       │   └── AddFieldButton
│   │       └── PrivacyNoticeCard
│   └── AddCardBottomBar
│       └── Contextual Actions (based on step)
├── StepProgressIndicator
├── LoadingOverlay
└── ErrorDialog

```

### State Management

The AddCardViewModel manages all state through StateFlow:

```kotlin
// Core State
val currentStep: StateFlow<AddCardStep>           // Current wizard step
val uiState: StateFlow<AddCardUiState>            // Form data and UI state
val selectedCardType: StateFlow<CardType?>        // Selected card type

// Derived State
val isFormValid: StateFlow<Boolean>               // Form validation status
val cardName: StateFlow<String>                   // Card name field
val selectedCategory: StateFlow<String>           // Selected category ID
val customFields: StateFlow<Map<String, String>>  // Custom fields map
val extractedData: StateFlow<Map<String, String>> // OCR extracted data
val categories: StateFlow<List<Category>>         // Available categories

// Events
val events: SharedFlow<AddCardEvent>              // One-time events (CardSaved)
```

### Capture Flow State Machine

```
[Type Selection] 
    ↓ (type selected)
[Camera Capture - Front]
    ↓ (front captured) OR (skip camera)
[Camera Capture - Back Decision]
    ├→ (capture back) → [Camera Capture - Back] → [Form Details]
    └→ (skip back) → [Form Details]
```

## Components and Interfaces

### 1. AddCardScreen (Main Composable)

**Purpose**: Root composable orchestrating the multi-step wizard

**Key Responsibilities**:
- Manage step transitions with animations
- Handle navigation events (back, close, save)
- Coordinate between ViewModel and UI components
- Display loading overlays and error dialogs

**Interface**:
```kotlin
@Composable
fun AddCardScreen(
    onNavigateBack: () -> Unit,
    onCardSaved: (Card) -> Unit,
    onCameraCapture: (CardType, CaptureTarget) -> Unit,
    modifier: Modifier = Modifier,
    capturedFrontImagePath: String? = null,
    capturedBackImagePath: String? = null,
    capturedExtractedData: Map<String, String> = emptyMap(),
    viewModel: AddCardViewModel = hiltViewModel()
)

enum class CaptureTarget {
    FRONT, BACK
}
```

### 2. TypeSelectionStep

**Purpose**: Display card types in organized grid with gradient previews

**Design Decisions**:
- Two distinct sections: OCR-enabled and Image-only
- Each card type shown as a card with gradient background
- Tapping a type immediately progresses to camera capture
- Uses EnhancedBounceClickable for premium interaction feedback

**Visual Specifications**:
- Grid: 2 columns with 16dp spacing
- Card aspect ratio: 1.586:1 (standard card ratio)
- Card elevation: 4dp
- Section headers: titleMedium typography with 16dp bottom padding
- Selected state: 2dp primary color border

**Interface**:
```kotlin
@Composable
private fun TypeSelectionStep(
    onCardTypeSelected: (CardType) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
private fun CardTypeItem(
    type: CardType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

### 3. CameraCaptureStep

**Purpose**: Guide users through front and optional back image capture

**Design Decisions**:
- Front capture is always first
- After front capture, offer "Capture Back" or "Skip Back" options
- For OCR cards: Process images immediately, discard after extraction
- For image cards: Save images to temporary location
- Display large camera icon with contextual instructions

**Capture States**:
1. **Pre-Front Capture**: Show camera icon, "Open Camera" button
2. **Front Captured**: Show success indicator, offer back capture options
3. **Back Captured**: Automatically proceed to form details

**Visual Specifications**:
- Camera icon: 120dp size with primary color
- Instructions: headlineSmall typography, center-aligned
- Subtitle: bodyLarge typography, onSurfaceVariant color
- Button: Full width with 16dp horizontal padding

**Interface**:
```kotlin
@Composable
private fun CameraCaptureStep(
    cardType: CardType,
    captureState: CaptureState,
    onCameraCapture: (CaptureTarget) -> Unit,
    onSkipBack: () -> Unit,
    modifier: Modifier = Modifier
)

enum class CaptureState {
    AWAITING_FRONT,
    FRONT_CAPTURED,
    BACK_CAPTURED
}
```

### 4. FormDetailsStep

**Purpose**: Comprehensive form for card details with adaptive sections

**Design Decisions**:
- Sections appear with staggered slide-in animations
- OCR cards show: Basic Info, Card Info, Appearance, Additional Info
- Image cards show: Basic Info, Appearance, Additional Info
- All card types have access to gradient customization
- All sections use PremiumCard containers
- Form validates in real-time with visual feedback

**Section Organization**:

**A. OCR Status Card** (OCR cards with extracted data):
```kotlin
@Composable
private fun OCRStatusCard(
    extractedFieldCount: Int,
    cardType: CardType,
    modifier: Modifier = Modifier
)
```
- Background: primaryContainer
- Icon: AutoAwesome (sparkle) icon
- Text: "Text Recognition Complete" with field count
- Elevation: 2dp

**B. Manual Entry Card** (OCR cards without extracted data):
```kotlin
@Composable
private fun ManualEntryCard(
    cardType: CardType,
    modifier: Modifier = Modifier
)
```
- Background: secondaryContainer
- Icon: Edit icon
- Text: "Manual Entry Required"
- Elevation: 2dp

**C. Basic Information Section**:
```kotlin
@Composable
private fun BasicInformationSection(
    cardName: String,
    onCardNameChange: (String) -> Unit,
    selectedCategory: String,
    categories: List<Category>,
    onCategoryChange: (String) -> Unit,
    modifier: Modifier = Modifier
)
```
- Card Name: PremiumTextField with Badge icon
- Category: CategoryDropdown with Category icon
- Spacing: 12dp between fields

**D. Card Information Section** (OCR cards only):
```kotlin
@Composable
private fun CardInformationSection(
    cardNumber: String,
    expiryDate: String,
    cardholderName: String,
    cvv: String,
    hasOCRData: Boolean,
    onCardNumberChange: (String) -> Unit,
    onExpiryDateChange: (String) -> Unit,
    onCardholderNameChange: (String) -> Unit,
    onCvvChange: (String) -> Unit,
    onClearOCRData: () -> Unit,
    modifier: Modifier = Modifier
)
```
- Card Number: PremiumTextField with Payment icon, auto-formatting
- Expiry Date: PremiumTextField with DateRange icon, MM/YY formatting
- Cardholder Name: PremiumTextField with Person icon, uppercase conversion
- CVV: PremiumTextField with Security icon, 3-4 digit validation
- Auto-detected fields show AutoAwesome icon as trailing icon
- "Clear All and Enter Manually" button if OCR data exists

**E. Appearance Section** (All card types):
```kotlin
@Composable
private fun AppearanceSection(
    selectedGradient: CardGradient?,
    cardType: CardType,
    onGradientSelected: (CardGradient) -> Unit,
    modifier: Modifier = Modifier
)
```
- Uses EnhancedColorPicker component
- Shows gradient preview with card type
- Allows custom gradient selection with start color, end color, and direction
- Defaults to card type's predefined gradient
- Available for both OCR and image-only cards
- For OCR cards: gradient used to generate card images
- For image-only cards: gradient stored for default back image generation

**F. Additional Information Section**:
```kotlin
@Composable
private fun AdditionalInformationSection(
    customFields: Map<String, String>,
    onFieldUpdate: (String, String) -> Unit,
    onFieldRemove: (String) -> Unit,
    onFieldAdd: () -> Unit,
    modifier: Modifier = Modifier
)
```
- Custom fields with remove buttons
- "Add Field" button at bottom
- Each field: OutlinedTextField with Close icon trailing

**G. Privacy Notice Card**:
```kotlin
@Composable
private fun PrivacyNoticeCard(
    modifier: Modifier = Modifier
)
```
- Background: surfaceVariant
- Icon: Security icon
- Title: "Privacy and Security"
- Text: Offline-only operation explanation

### 5. StepProgressIndicator

**Purpose**: Visual progress tracking through the wizard

**Design Decisions**:
- Three steps: Type, Capture, Details
- Current step: Filled circle with primary color
- Completed step: Checkmark icon in primary circle
- Future step: Number in muted circle
- Connecting lines change color based on completion

**Visual Specifications**:
- Circle size: 40dp
- Icon size: 20dp (checkmark)
- Line thickness: 1dp
- Spacing: SpaceEvenly arrangement
- Label: labelSmall typography below circle

**Interface**:
```kotlin
@Composable
private fun StepProgressIndicator(
    currentStep: AddCardStep,
    modifier: Modifier = Modifier
)
```

### 6. AddCardBottomBar

**Purpose**: Contextual action buttons that adapt to current step

**Design Decisions**:
- Type Selection: Hidden (selection auto-progresses)
- Camera Capture (pre-front): "Skip Camera" + "Open Camera"
- Camera Capture (post-front): "Capture Back" + "Skip Back"
- Form Details: "Save Card" (full width)
- Buttons use PremiumButton components with haptic feedback

**Visual Specifications**:
- Height: 72dp (48dp button + 12dp padding top/bottom)
- Elevation: 4dp shadow
- Button spacing: 12dp between buttons
- Save button: Primary variant with loading indicator when saving

**Interface**:
```kotlin
@Composable
private fun AddCardBottomBar(
    currentStep: AddCardStep,
    captureState: CaptureState,
    isFormValid: Boolean,
    isLoading: Boolean,
    onOpenCamera: (CaptureTarget) -> Unit,
    onSkipCamera: () -> Unit,
    onSkipBack: () -> Unit,
    onSaveCard: () -> Unit,
    modifier: Modifier = Modifier
)
```

## Data Models

### AddCardUiState

```kotlin
data class AddCardUiState(
    // Card identification
    val cardId: String? = null,
    val isEditMode: Boolean = false,
    
    // Basic information
    val cardName: String = "",
    val cardType: CardType = CardType.Credit,
    val categoryId: String = "personal",
    
    // Image paths
    val frontImagePath: String? = null,
    val backImagePath: String? = null,
    val hasFrontImage: Boolean = false,
    val hasBackImage: Boolean = false,
    
    // OCR data
    val extractedData: Map<String, String> = emptyMap(),
    val hasOCRData: Boolean = false,
    val cardNumber: String = "",
    val expiryDate: String = "",
    val cardholderName: String = "",
    val cvv: String = "",
    
    // Appearance
    val selectedGradient: CardGradient? = null,
    
    // Custom fields
    val customFields: Map<String, String> = emptyMap(),
    
    // UI state
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val canSave: Boolean = false,
    
    // Field errors
    val cardNumberError: String? = null,
    val expiryDateError: String? = null,
    val cardholderNameError: String? = null,
    val cvvError: String? = null
)
```

### AddCardStep

```kotlin
enum class AddCardStep {
    TYPE_SELECTION,
    CAMERA_CAPTURE,
    FORM_DETAILS
}
```

### CaptureState

```kotlin
enum class CaptureState {
    AWAITING_FRONT,      // No images captured yet
    FRONT_CAPTURED,      // Front captured, awaiting back decision
    BACK_CAPTURED,       // Both images captured
    SKIPPED              // Camera capture skipped entirely
}
```

### CardGradient

```kotlin
data class CardGradient(
    val startColor: String,  // Hex color code
    val endColor: String,    // Hex color code
    val direction: GradientDirection = GradientDirection.TopToBottom
)

enum class GradientDirection {
    TopToBottom,
    LeftToRight,
    DiagonalTopLeftToBottomRight,
    DiagonalTopRightToBottomLeft
}
```

## Card Generation Strategy

### For OCR Cards (Credit/Debit)

**Process Flow**:
1. Capture front image → Run OCR → Extract text → Discard image
2. (Optional) Capture back image → Run OCR → Extract additional text → Discard image
3. User selects gradient colors (or uses default)
4. Generate front gradient card with extracted data
5. Generate back gradient card with CVV and magnetic stripe
6. Save generated images to file system
7. Save card with extracted data and gradient info

**Gradient Card Generation**:
```kotlin
class CardGradientGenerator {
    suspend fun generateCreditCardFront(
        cardNumber: String,
        expiryDate: String,
        cardholderName: String,
        gradient: CardGradient,
        cardType: CardType
    ): String // Returns path to generated image
    
    suspend fun generateCreditCardBack(
        cvv: String?,
        gradient: CardGradient,
        cardType: CardType
    ): String // Returns path to generated image
}
```

**Front Card Layout**:
- Gradient background (full card)
- Card type icon (top right, 32dp)
- Card name (top left, titleLarge)
- Card number (bottom, formatted with spaces, titleMedium)
- Expiry date (bottom right, bodyMedium)
- Cardholder name (bottom left, bodyMedium)

**Back Card Layout**:
- Same gradient background
- Magnetic stripe simulation (black bar, 40dp height, top)
- CVV area (white box, bottom right)
- Card type icon (bottom left, watermark style)

### For Image Cards (Others)

**Process Flow**:
1. Capture front image → Save to file system
2. (Optional) Capture back image → Save to file system
3. If no back image: Generate default gradient back
4. Save card with image paths

**Default Back Generation**:
```kotlin
class CardGradientGenerator {
    suspend fun generateDefaultBack(
        cardType: CardType,
        cardName: String,
        gradient: CardGradient
    ): String // Returns path to generated image
}
```

**Default Back Layout**:
- User-selected gradient (or card type's default if not customized)
- Card type icon (center, 80dp, semi-transparent)
- Card name (center bottom, titleMedium)
- "CardVault" watermark (bottom, labelSmall)

## Error Handling

### Validation Errors

**Card Name**:
- Required: "Card name is required"
- Min length: "Card name must be at least 2 characters"

**Category**:
- Required: "Please select a category"

**Images**:
- No front: "Front image is required. Please capture or skip camera for manual entry."

**OCR Card Fields**:
- Card number required: "Card number is required for [Card Type]"
- Card number format: "Card number must be 13-19 digits"
- Expiry required: "Expiry date is required for [Card Type]"
- Expiry format: "Expiry date must be in MM/YY format"
- CVV format: "CVV must be 3-4 digits"

### Processing Errors

**OCR Failure**:
- Display ManualEntryCard
- Allow manual field entry
- Provide "Retry OCR" option if images still available

**Image Save Failure**:
- Error: "Failed to save card images. Please try again."
- Retry option available
- Images remain in temporary location for retry

**Gradient Generation Failure**:
- Error: "Failed to generate card design. Please try again."
- Fallback to default gradient
- Retry option available

## Animation Specifications

### Step Transitions

**Forward Navigation**:
- Duration: 300ms
- Effect: slideInHorizontally(from right) + fadeIn
- Easing: EaseOutCubic

**Backward Navigation**:
- Duration: 300ms
- Effect: slideInHorizontally(from left) + fadeIn
- Easing: EaseOutCubic

### Section Animations

**Slide-In**:
- Base duration: 400ms
- Stagger increment: 100ms per section
- Effect: slideInVertically(from bottom) + fadeIn
- Easing: EaseOutCubic

### Progress Indicator

**Circle Fill**:
- Duration: 200ms
- Effect: Scale from 0.8 to 1.0
- Easing: Spring (DampingRatioMediumBouncy)

**Checkmark Appearance**:
- Duration: 300ms
- Effect: Scale from 0 to 1.0 + fadeIn
- Easing: Spring (DampingRatioMediumBouncy)

### Button Interactions

**Press Effect**:
- Scale: 0.96x
- Duration: 100ms
- Haptic: LongPress type

## Testing Strategy

### Unit Tests (ViewModel)

1. **Step Navigation**
   - Test forward/backward step transitions
   - Test step validation before progression
   - Test capture state management

2. **Form Validation**
   - Test card name validation
   - Test OCR field validation
   - Test image requirement validation
   - Test form validity computation

3. **Data Management**
   - Test OCR data extraction and storage
   - Test custom field add/remove/update
   - Test gradient selection and storage

4. **Save Operations**
   - Test OCR card save with gradient generation
   - Test image card save with actual images
   - Test save with optional back image
   - Test save error handling

### Integration Tests

1. **OCR Processing**
   - Test image capture → OCR → data extraction flow
   - Test OCR failure handling
   - Test manual entry after OCR failure

2. **Gradient Generation**
   - Test gradient card generation for OCR cards
   - Test default back generation for image cards
   - Test gradient customization

3. **Image Management**
   - Test image capture and temporary storage
   - Test image save for image cards
   - Test image cleanup after OCR processing

### UI Tests (Compose)

1. **Step Flow**
   - Test complete flow from type selection to save
   - Test back navigation preserves data
   - Test skip camera flow

2. **Form Interactions**
   - Test field input and formatting
   - Test validation error display
   - Test custom field management

3. **Animations**
   - Test step transition animations
   - Test section slide-in animations
   - Test progress indicator updates

## Performance Considerations

1. **Image Processing**: OCR processing on background thread, max 2 seconds
2. **Gradient Generation**: Async with loading indicator, max 1 second
3. **Form Validation**: Debounced to avoid excessive recomposition
4. **Animation Performance**: Hardware-accelerated, 60fps target
5. **Memory Management**: Cleanup temporary images after processing

## Accessibility

1. **Touch Targets**: All interactive elements minimum 48dp
2. **Content Descriptions**: All icons and images have proper descriptions
3. **Color Contrast**: WCAG AA compliance for all text
4. **Screen Reader**: Proper semantic structure and labels
5. **Focus Management**: Logical focus order in forms

## Implementation Notes

### Existing Components to Leverage

1. **EnhancedComponents**: PremiumCard, PremiumTextField, PremiumButton, AnimatedSectionHeader
2. **EnhancedAnimations**: EnhancedSlideInItem, EnhancedBounceClickable
3. **EnhancedColorPicker**: For gradient selection
4. **CardTypeDropdown**: For type selection (if needed in edit mode)
5. **CategoryDropdown**: For category selection

### New Components to Create

1. **CardTypeGrid**: Grid layout for type selection with gradient previews
2. **CaptureInstructions**: Contextual instructions for camera capture
3. **OCRStatusCard**: Success indicator for OCR completion
4. **ManualEntryCard**: Prompt for manual entry when OCR fails
5. **PrivacyNoticeCard**: Offline-only operation explanation

### Integration Points

1. **CardGradientGenerator**: Utility for generating gradient card images
2. **OCR Processing**: ML Kit integration for text extraction
3. **Image Storage**: File system operations for saving images
4. **AddCardUseCase**: Business logic for card creation
5. **Camera Integration**: CameraX for image capture

This design provides a comprehensive blueprint for implementing the enhanced add card flow with clear specifications, reusable components, and maintainable architecture.
