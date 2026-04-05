---
name: android-accessibility
description: Expert checklist and prompts for auditing and fixing Android accessibility issues in Jetpack Compose. CardVault-specific guidance for card images, animations, and glassmorphic UI.
---

# Android Accessibility Checklist

## Instructions

Analyze the provided component or screen for the following accessibility aspects.

### 1. Content Descriptions
*   **Check**: Do `Image`, `Icon`, and `AsyncImage` composables have a meaningful `contentDescription`?
*   **Decorative**: If an image is purely decorative, use `contentDescription = null`.
*   **Actionable**: If an element is clickable, the description should describe the *action* (e.g., "View card details"), not the icon (e.g., "Arrow").
*   **Card images**: Front/back card images should describe the card (e.g., "Front of Visa credit card").
*   **Sensitive data**: NEVER include card numbers, CVV, or PINs in content descriptions.

### 2. Touch Target Size
*   **Standard**: Minimum **48x48dp** for all interactive elements.
*   **Fix**: Use `MinTouchTargetSize` or wrap in `Box` with appropriate padding if the visual icon is smaller.
*   **Card items**: Card list items and category chips must meet touch target requirements.

### 3. Color Contrast
*   **Standard**: WCAG AA requires **4.5:1** for normal text and **3.0:1** for large text/icons.
*   **Glassmorphic UI**: Verify text over frosted-glass backgrounds meets contrast requirements in both light and dark themes.
*   **Card gradients**: Ensure text overlaid on card gradient backgrounds is readable.
*   **Tool**: Verify colors against backgrounds using contrast logic.

### 4. Focus & Semantics
*   **Focus Order**: Ensure keyboard/screen-reader focus moves logically (e.g., Top-Start to Bottom-End).
*   **Grouping**: Use `Modifier.semantics(mergeDescendants = true)` for complex items (card list items with image, name, and type).
*   **State descriptions**: Use `stateDescription` to describe custom states (e.g., "Selected", "Checked", "Card flipped to back").
*   **3D flip animation**: Announce card flip state change to screen readers.

### 5. Headings
*   **Traversal**: Mark `AnimatedSectionHeader` and section titles with `Modifier.semantics { heading() }` to allow screen reader users to jump between sections.

### 6. Animations
*   **Reduce motion**: Respect `AccessibilityManager.isEnabled` — reduce or disable animations for users who prefer reduced motion.
*   **FlippableCard**: Provide non-animated alternative for viewing card back.
*   **Step progress**: Announce step changes to screen readers.

## Example Prompts for Agent Usage
*   "Analyze the content description of this card image. Is it appropriate?"
*   "Check if the touch target size of this bottom navigation item is at least 48dp."
*   "Does the card flip animation announce the state change to TalkBack?"
*   "Is the text on this card gradient readable with proper contrast?"
