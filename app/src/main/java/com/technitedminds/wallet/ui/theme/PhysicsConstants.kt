package com.technitedminds.wallet.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

/**
 * Centralized spring animation specs used across the entire app.
 * Inspired by physics-based motion design for premium wallet UX.
 */
object WalletSpring {
    /** High overshoot, playful bounce — splash entrance, FAB appearance, chip selection */
    fun <T> bouncy() = spring<T>(dampingRatio = 0.55f, stiffness = 200f)

    /** Moderate bounce, responsive — button press, card press, navigation indicator */
    fun <T> snappy() = spring<T>(dampingRatio = 0.75f, stiffness = 400f)

    /** Smooth deceleration, minimal overshoot — section entrance, search expand */
    fun <T> gentle() = spring<T>(dampingRatio = 0.85f, stiffness = 160f)

    /** Card-specific — magnetic drag snap, 3D flip settle */
    fun <T> card() = spring<T>(dampingRatio = 0.65f, stiffness = 420f)

    /** Stiff with no bounce — instant micro-interactions */
    fun <T> stiff() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh,
    )
}

/**
 * Timing constants for non-spring animations (stagger delays, durations).
 */
object WalletTiming {
    /** Per-item stagger delay for list/grid entrances */
    const val STAGGER_DELAY_MS = 50

    /** Glass morphism fade duration */
    const val GLASS_FADE_MS = 300

    /** Card flip duration (for tween fallback) */
    const val CARD_FLIP_MS = 300

    /** Splash screen total display time */
    const val SPLASH_DISPLAY_MS = 1800L

    /** Section entrance stagger interval */
    const val SECTION_STAGGER_MS = 80
}
