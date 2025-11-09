# Design Document: Enhanced Card Detail Page

## Overview

The Enhanced Card Detail Page redesign focuses on creating a premium, immersive card viewing and editing experience that leverages the existing enhanced UI components (EnhancedComponents, EnhancedAnimations, EnhancedColorPicker) while maintaining the current functionality. The design emphasizes visual hierarchy, smooth animations, and intuitive interactions to provide users with a polished, professional interface for managing their digital cards.

### Design Goals

1. **Immersive Card Display**: Make the card the hero element with prominent positioning and realistic 3D flip animations
2. **Clear Information Architecture**: Organize card data into logical, scannable sections with consistent visual treatment
3. **Seamless Mode Transitions**: Provide smooth, animated transitions between view and edit modes
4. **Enhanced Editing Experience**: Leverage premium UI components for a delightful editing workflow
5. **Maintain Existing Functionality**: Preserve all current features including sharing, deletion, and custom fields

## Architecture

### Component Hierarchy

```
CardDetailScreen (Main Container)
├── Scaffold
│   ├── CardDetailTopBar
│   │   ├── Navigation (Back button)
│   │   ├── Title (Card name or "Edit Card")
│   │   └── Actions
│   │       ├── View Mode: Share, Edit, Delete
│   │       └── Edit Mode: Cancel, Save
│   └── Content (Scrollable Column)
│       ├── CardDisplaySection
│       │   ├── FlippableCard (Enhanced 3D card)
│       │   └── Instructions (Tap/Long-press hints)
│       ├── View Mode: CardInfoSection
│       │   ├── InfoCard (Basic Information)
│       │   ├── InfoCard (Extracted Information) [OCR cards only]
│       │   └── InfoCard (Additional Information) [if custom fields exist]
│       └── Edit Mode: EditCardSection
│           ├── PremiumCard (Basic Information)
│           │   ├── AnimatedSectionHeader
│           │   ├── PremiumTextField (Card Name)
│           │   ├── CardTypeDropdown
│           │   └── CategoryDropdown
│           ├── PremiumCard (Extracted Data) [OCR cards only]
│           │   ├── AnimatedSectionHeader
│           │   └── ExtractedDataEditor
│           ├── PremiumCard (Custom Fields)
│           │   ├── AnimatedSectionHeader
│           │   └── CustomFieldsEditor
│           ├── PremiumCard (Appearance)
│           │   ├── AnimatedSectionHeader
│           │   └── EnhancedColorPicker
│           └── PremiumCard (Save Reminder)
├── CardDeleteConfirmationDialog
├── CardSharingDialog
└── LoadingOverlay
```

### State Management

The CardDetailViewModel manages all state through StateFlow:

```kotlin
// Core Data
val card: StateFlow<Card?>                    // Current card data
val categories: StateFlow<List<Category>>     // Available categories

// UI State
val uiState: StateFlow<CardDetailUiState>     // Loading, errors, dialogs
val isEditing: StateFlow<Boolean>             // View vs Edit mode
val editedCard: StateFlow<Card?>              // Temporary edit state
val showSharingDialog: StateFlow<Boolean>     // Sharing dialog visibility

// Events
val events: SharedFlow<CardDetailEvent>       // One-time events (saved, deleted, shared)
```

## Components and Interfaces

### 1. CardDetailScreen (Main Composable)

**Purpose**: Root composable that orchestrates the entire card detail experience

**Key Responsibilities**:
- Manage screen-level layout with Scaffold
- Handle navigation events (back, delete)
- Coordinate between view and edit modes
- Display dialogs (delete confirmation, sharing)
- Show loading overlays

**Interface**:
```kotlin
@Composable
fun CardDetailScreen(
    cardId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CardDetailViewModel = hiltViewModel()
)
```

### 2. CardDetailTopBar

**Purpose**: Context-aware top app bar that adapts to view/edit modes

**Design Decisions**:
- **View Mode**: Shows card name as title with share, edit, and delete actions
- **Edit Mode**: Shows "Edit Card" as title with cancel and save actions
- Uses Material 3 TopAppBar with proper elevation and color scheme
- Action buttons use IconButton for consistent touch targets (48dp minimum)

**Interface**:
```kotlin
@Composable
private fun CardDetailTopBar(
    card: Card,
    isEditing: Boolean,
    onNavigateBack: () -> Unit,
    onStartEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onDeleteCard: () -> Unit,
    onShowSharingDialog: () -> Unit,
    modifier: Modifier = Modifier
)
```

### 3. CardDisplaySection

**Purpose**: Prominent card display with flip animation and sharing options

**Design Decisions**:
- Card takes full width with 220dp minimum height for visibility
- Uses FlippableCard component (existing) for 3D flip animation
- Displays tap-to-flip and long-press-to-share instructions below card
- Shows quick share buttons overlay on the card (front side only)
- Instructions use subtle styling (bodySmall, onSurfaceVariant color)

**Visual Specifications**:
- Card height: 220dp
- Card aspect ratio: 1.6:1 (standard credit card ratio)
- Corner radius: 16dp
- Elevation: 8dp
- Padding around card: 16dp
- Spacing between card and instructions: 16dp

**Interface**:
```kotlin
@Composable
private fun CardDisplaySection(
    card: Card,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onShare: (CardSharingOption) -> Unit,
    onShowSharingDialog: () -> Unit,
    modifier: Modifier = Modifier
)
```

### 4. CardInfoSection (View Mode)

**Purpose**: Display card information in organized, scannable sections

**Design Decisions**:
- Uses InfoCard containers for each information category
- Sections: Basic Information, Extracted Information (OCR only), Additional Information (custom fields)
- Each section has an icon and title in the header
- Information displayed as label-value pairs in rows
- Sensitive data (card number, CVV, PIN, password) is masked
- Empty sections are hidden

**Visual Specifications**:
- Card elevation: 2dp
- Card padding: 16dp
- Spacing between sections: 16dp
- Spacing between info rows: 8dp
- Label color: onSurfaceVariant
- Value color: onSurface (for emphasis)

**Interface**:
```kotlin
@Composable
private fun CardInfoSection(
    card: Card,
    modifier: Modifier = Modifier
)

@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
)

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
)
```

### 5. EditCardSection (Edit Mode)

**Purpose**: Comprehensive editing interface with premium components

**Design Decisions**:
- Uses PremiumCard containers for visual consistency
- Each section uses AnimatedSectionHeader with icon, title, and subtitle
- Sections appear with staggered slide-in animations (EnhancedSlideInItem)
- All editing controls use enhanced components (PremiumTextField, dropdowns, color picker)
- Save reminder card at the bottom with info icon and message

**Sections**:
1. **Basic Information**: Name, Type, Category
2. **Card Information** (OCR cards only): Extracted data fields
3. **Additional Information**: Custom fields with add/remove
4. **Appearance**: Color picker with multi-palette selection
5. **Save Reminder**: Informational card prompting user to save

**Visual Specifications**:
- Card elevation: 2dp
- Card padding: 20dp
- Spacing between sections: 16dp
- Spacing between fields: 12dp
- Animation stagger delay: 100ms per section
- Save reminder uses primaryContainer background

**Interface**:
```kotlin
@Composable
private fun EditCardSection(
    card: Card,
    categories: List<Category>,
    onUpdateName: (String) -> Unit,
    onUpdateCategory: (String) -> Unit,
    onUpdateCardType: (CardType) -> Unit,
    onUpdateExtractedData: (String, String) -> Unit,
    onUpdateCustomField: (String, String) -> Unit,
    onAddCustomField: (String) -> Unit,
    onRemoveCustomField: (String) -> Unit,
    onUpdateColor: (String) -> Unit,
    modifier: Modifier = Modifier
)
```

### 6. FlippableCard (Existing Component)

**Purpose**: 3D card display with flip animation

**Design Decisions** (No changes to existing component):
- Maintains current 3D flip animation with 600ms duration
- Shows front/back based on rotation state
- Displays actual images for non-OCR cards
- Shows gradient with card data for OCR cards
- Includes share buttons overlay on front side
- Supports tap to flip and long-press for sharing dialog

### 7. Enhanced Components Integration

**Components Used**:
- **PremiumCard**: All section containers in edit mode
- **PremiumTextField**: Card name input with icon and validation
- **AnimatedSectionHeader**: Section headers with icon, title, subtitle
- **EnhancedSlideInItem**: Staggered animations for sections
- **EnhancedColorPicker**: Multi-palette color selection with preview
- **CardTypeDropdown**: Type selection with icons (existing)
- **CategoryDropdown**: Category selection with colors (existing)
- **ExtractedDataEditor**: OCR field editing (existing)
- **CustomFieldsEditor**: Custom field management (existing)

## Data Models

### Card Model (Existing)

```kotlin
data class Card(
    val id: String,
    val name: String,
    val type: CardType,
    val categoryId: String,
    val frontImagePath: String,
    val backImagePath: String,
    val extractedData: Map<String, String>,
    val customFields: Map<String, String>,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun getDisplayColor(): String
    fun getCardNumber(): String?
    fun getCVV(): String?
    fun withCustomColor(colorHex: String): Card
}
```

### CardDetailUiState

```kotlin
data class CardDetailUiState(
    val isLoading: Boolean = false,
    val isCardFlipped: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val error: String? = null
)
```

### CardDetailEvent

```kotlin
sealed class CardDetailEvent {
    object CardSaved : CardDetailEvent()
    object CardDeleted : CardDetailEvent()
    object ShareSuccess : CardDetailEvent()
}
```

## Error Handling

### Error Scenarios

1. **Card Not Found**
   - Display: Centered loading indicator with "Loading card details" text
   - Action: Show error message if card fails to load after timeout
   - Recovery: Provide back navigation option

2. **Save Failure**
   - Display: Error message in uiState.error
   - Action: Keep edit mode active with user's changes preserved
   - Recovery: User can retry save or cancel to discard changes

3. **Delete Failure**
   - Display: Error message in uiState.error
   - Action: Keep card detail screen open
   - Recovery: User can retry delete or navigate back

4. **Share Failure**
   - Display: Error message from CardSharingResult.Error
   - Action: Close sharing dialog, keep card detail screen open
   - Recovery: User can retry sharing with different options

### Loading States

1. **Initial Load**: Full-screen loading indicator with "Loading card details"
2. **Save Operation**: LoadingOverlay with "Processing" text
3. **Delete Operation**: LoadingOverlay with "Processing" text
4. **Share Operation**: LoadingOverlay with "Processing" text

### Validation

1. **Card Name**: Must not be empty (validated in ViewModel before save)
2. **Category**: Must be a valid category ID from available categories
3. **Card Type**: Must be a valid CardType enum value
4. **Custom Field Names**: Must not be empty when adding new fields

## Testing Strategy

### Unit Tests (ViewModel)

1. **State Management**
   - Test initial state loading
   - Test edit mode activation/cancellation
   - Test card flip toggle
   - Test dialog visibility toggles

2. **Data Updates**
   - Test card name update
   - Test category update
   - Test card type update
   - Test extracted data updates
   - Test custom field add/update/remove
   - Test color update

3. **Operations**
   - Test save card success
   - Test save card failure
   - Test delete card success
   - Test delete card failure
   - Test share card with different options

4. **Event Emission**
   - Test CardSaved event on successful save
   - Test CardDeleted event on successful delete
   - Test ShareSuccess event on successful share

### UI Tests (Compose)

1. **Screen Display**
   - Test card information displays correctly
   - Test sections show/hide based on card type
   - Test sensitive data masking

2. **Mode Transitions**
   - Test edit mode activation
   - Test edit mode cancellation
   - Test save button enables/disables correctly

3. **Interactions**
   - Test card flip on tap
   - Test sharing dialog on long-press
   - Test delete confirmation dialog
   - Test navigation back

4. **Animations**
   - Test section slide-in animations
   - Test card flip animation smoothness
   - Test mode transition animations

### Integration Tests

1. **End-to-End Flows**
   - Test view → edit → save → view flow
   - Test view → edit → cancel → view flow
   - Test view → delete → navigate back flow
   - Test view → share → success flow

2. **Data Persistence**
   - Test edited data persists after save
   - Test edited data discarded after cancel
   - Test card deleted from database

## Animation Specifications

### Card Flip Animation

- **Duration**: 600ms
- **Easing**: EaseInOutCubic
- **Rotation**: 0° to 180° on Y-axis
- **Camera Distance**: 12f * density for proper perspective
- **Hover Effect**: 1.02x scale on press with 200ms duration

### Mode Transition Animation

- **Duration**: 300ms
- **Effect**: Fade in/out with crossfade
- **Easing**: Linear

### Section Slide-In Animation

- **Base Duration**: 400ms
- **Stagger Increment**: 100ms per section
- **Initial Offset**: Full height from bottom
- **Easing**: EaseOutCubic
- **Combined Effects**: slideInVertically + fadeIn

### Button/Card Press Animation

- **Scale**: 0.95x to 0.98x (depending on component)
- **Spring**: DampingRatioMediumBouncy, StiffnessHigh
- **Haptic Feedback**: LongPress type on all interactive elements

## Accessibility Considerations

1. **Touch Targets**: All interactive elements minimum 48dp
2. **Content Descriptions**: All icons have proper contentDescription
3. **Color Contrast**: Follows Material 3 color system for WCAG AA compliance
4. **Text Scaling**: All text uses Material 3 typography scale
5. **Focus Management**: Proper focus order in edit mode
6. **Screen Reader**: Semantic structure with proper labels

## Performance Considerations

1. **Lazy Loading**: Card data loaded on-demand via Flow
2. **State Hoisting**: Minimal recomposition with proper state management
3. **Animation Performance**: Hardware-accelerated animations with graphicsLayer
4. **Image Loading**: Coil for efficient image caching and loading
5. **Memory Management**: Proper cleanup of resources in ViewModel

## Design Tokens

### Spacing

- Extra Small: 4dp
- Small: 8dp
- Medium: 12dp
- Large: 16dp
- Extra Large: 20dp

### Corner Radius

- Small: 8dp
- Medium: 12dp
- Large: 16dp
- Extra Large: 20dp

### Elevation

- Default: 2dp
- Medium: 4dp
- High: 8dp
- Floating: 12dp

### Animation Durations

- Fast: 200ms
- Normal: 300ms
- Slow: 600ms
- Stagger: 100ms increment

## Implementation Notes

### Existing Components to Preserve

1. **FlippableCard**: Keep current implementation with 3D flip
2. **CardSharingDialog**: Maintain existing sharing functionality
3. **CardSharingManager**: No changes to sharing logic
4. **CardDeleteConfirmationDialog**: Keep current dialog
5. **ExtractedDataEditor**: Preserve OCR field editing
6. **CustomFieldsEditor**: Maintain custom field management
7. **CardTypeDropdown**: Keep type selection dropdown
8. **CategoryDropdown**: Preserve category selection

### New Integrations

1. **EnhancedSlideInItem**: Add staggered animations to edit sections
2. **AnimatedSectionHeader**: Replace plain text headers in edit mode
3. **PremiumCard**: Wrap all edit sections for consistent styling
4. **PremiumTextField**: Use for card name input
5. **EnhancedColorPicker**: Already integrated, ensure proper usage

### Code Organization

- Keep all composables in CardDetailScreen.kt
- Maintain private composables for internal components
- Use existing utility functions (formatDate, formatFieldName, maskSensitiveData)
- Follow existing naming conventions and code style

## Migration Strategy

### Phase 1: Visual Enhancements (Non-Breaking)

1. Replace edit section containers with PremiumCard
2. Add AnimatedSectionHeader to each edit section
3. Wrap sections with EnhancedSlideInItem for animations
4. Update card display section spacing and sizing

### Phase 2: Component Integration

1. Replace card name TextField with PremiumTextField
2. Ensure EnhancedColorPicker is properly integrated
3. Add save reminder card at bottom of edit mode
4. Update spacing and padding to match design tokens

### Phase 3: Polish and Testing

1. Fine-tune animation timings and easing
2. Test all interactions and transitions
3. Verify accessibility compliance
4. Performance testing and optimization
