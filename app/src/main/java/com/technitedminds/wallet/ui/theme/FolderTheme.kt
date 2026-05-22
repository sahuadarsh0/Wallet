package com.technitedminds.wallet.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Curated palette options for folder tile gradients.
 *
 * The first option, [VIBRANT], reproduces the original color scheme used by
 * CardVault before themes were introduced — it is intentionally preserved
 * unchanged so existing users see no visual regression when they upgrade.
 *
 * The remaining 7 themes are designed to feel professional, "industry
 * standard" and grown-up rather than playful: think enterprise dashboards,
 * banking apps and editorial design rather than candy-bright kid friendly UI.
 *
 * Each theme exposes:
 *  - a stable [palette] of 6 gradient pairs used to color category folders
 *    deterministically based on the category id / name hash.
 *  - dedicated [allCardsGradient] and [uncategorizedGradient] so the two
 *    "system" folders look intentional inside every theme.
 *  - a light [accent] color used as the screen gradient tint and for the
 *    optional [BackgroundPattern] overlay so the whole surface feels cohesive.
 */
enum class FolderTheme(
    val displayName: String,
    val description: String,
    val accent: Color,
    val allCardsGradient: List<Color>,
    val uncategorizedGradient: List<Color>,
    val palette: List<List<Color>>,
) {
    /**
     * Original, vivid CardVault palette. Preserved exactly so existing users
     * keep their familiar look when they upgrade.
     */
    VIBRANT(
        displayName = "Vibrant",
        description = "Original CardVault colors",
        accent = Color(0xFF6366F1),
        allCardsGradient = listOf(Color(0xFF4F46E5), Color(0xFF9333EA)),
        uncategorizedGradient = listOf(Color(0xFF64748B), Color(0xFF334155)),
        palette = listOf(
            listOf(Color(0xFF1976D2), Color(0xFF115293)),
            listOf(Color(0xFF7C3AED), Color(0xFF5B21B6)),
            listOf(Color(0xFFEC4899), Color(0xFFBE185D)),
            listOf(Color(0xFFF59E0B), Color(0xFFB45309)),
            listOf(Color(0xFF10B981), Color(0xFF047857)),
            listOf(Color(0xFF06B6D4), Color(0xFF0E7490)),
        ),
    ),

    /**
     * Boardroom palette. Each gradient runs from a bright brand mid-tone at
     * the top-left down to a deep saturated shadow at the bottom-right —
     * the same tonal range you see on Stripe / Revolut / Linear marketing
     * tiles, which is what makes those tiles feel premium instead of flat.
     */
    EXECUTIVE(
        displayName = "Executive",
        description = "Cobalt, indigo & graphite",
        accent = Color(0xFF3B82F6),
        allCardsGradient = listOf(Color(0xFF60A5FA), Color(0xFF1E3A8A)),
        uncategorizedGradient = listOf(Color(0xFF94A3B8), Color(0xFF1E293B)),
        palette = listOf(
            listOf(Color(0xFF60A5FA), Color(0xFF1E3A8A)), // cobalt
            listOf(Color(0xFF818CF8), Color(0xFF312E81)), // indigo
            listOf(Color(0xFF38BDF8), Color(0xFF075985)), // azure
            listOf(Color(0xFF94A3B8), Color(0xFF1E293B)), // slate
            listOf(Color(0xFF67E8F9), Color(0xFF155E75)), // ice → teal-ink
            listOf(Color(0xFF64748B), Color(0xFF0F172A)), // graphite
        ),
    ),

    /** Electric blue + violet — modern fintech dashboard at night. */
    MIDNIGHT(
        displayName = "Midnight",
        description = "Electric blue & violet",
        accent = Color(0xFF6366F1),
        allCardsGradient = listOf(Color(0xFF818CF8), Color(0xFF1E1B4B)),
        uncategorizedGradient = listOf(Color(0xFF94A3B8), Color(0xFF0F172A)),
        palette = listOf(
            listOf(Color(0xFF818CF8), Color(0xFF1E1B4B)),
            listOf(Color(0xFFA78BFA), Color(0xFF4C1D95)),
            listOf(Color(0xFF60A5FA), Color(0xFF1E3A8A)),
            listOf(Color(0xFFC4B5FD), Color(0xFF5B21B6)),
            listOf(Color(0xFF38BDF8), Color(0xFF1E40AF)),
            listOf(Color(0xFF818CF8), Color(0xFF312E81)),
        ),
    ),

    /**
     * Neutral by design but with a clear tonal step from light slate down
     * to ink, plus warm/blue undertones in the shadows so it reads
     * editorial rather than washed-out.
     */
    MONOCHROME(
        displayName = "Monochrome",
        description = "Warm slate & ink",
        accent = Color(0xFF94A3B8),
        allCardsGradient = listOf(Color(0xFF94A3B8), Color(0xFF1E293B)),
        uncategorizedGradient = listOf(Color(0xFF9CA3AF), Color(0xFF111827)),
        palette = listOf(
            listOf(Color(0xFF94A3B8), Color(0xFF1E293B)), // slate
            listOf(Color(0xFFA1A1AA), Color(0xFF27272A)), // zinc
            listOf(Color(0xFF9CA3AF), Color(0xFF111827)), // gray
            listOf(Color(0xFFA8A29E), Color(0xFF1C1917)), // stone
            listOf(Color(0xFF6B7280), Color(0xFF030712)), // ink
            listOf(Color(0xFFCBD5E1), Color(0xFF334155)), // pearl
        ),
    ),

    /**
     * Warm earth — terracotta, saffron, olive, copper. Tops are bright and
     * sun-warmed, shadows go deep so the tiles actually have falloff.
     */
    EARTH(
        displayName = "Earth",
        description = "Terracotta, saffron & clay",
        accent = Color(0xFFEA580C),
        allCardsGradient = listOf(Color(0xFFFB923C), Color(0xFF7C2D12)),
        uncategorizedGradient = listOf(Color(0xFFA8A29E), Color(0xFF1C1917)),
        palette = listOf(
            listOf(Color(0xFFFB923C), Color(0xFF7C2D12)), // terracotta
            listOf(Color(0xFFFBBF24), Color(0xFF92400E)), // amber → coffee
            listOf(Color(0xFFFACC15), Color(0xFF854D0E)), // saffron → toffee
            listOf(Color(0xFFA3E635), Color(0xFF3F6212)), // olive
            listOf(Color(0xFFF97316), Color(0xFF7C2D12)), // copper
            listOf(Color(0xFFD6D3D1), Color(0xFF44403C)), // bone → stone
        ),
    ),

    /**
     * Aqua → deep ocean. Bright cyan / sky tops descend into saturated
     * indigo and teal-ink shadows. Reads like premium banking marketing.
     */
    OCEAN(
        displayName = "Ocean",
        description = "Aqua, teal & deep blue",
        accent = Color(0xFF06B6D4),
        allCardsGradient = listOf(Color(0xFF22D3EE), Color(0xFF0C4A6E)),
        uncategorizedGradient = listOf(Color(0xFF94A3B8), Color(0xFF1E293B)),
        palette = listOf(
            listOf(Color(0xFF22D3EE), Color(0xFF0C4A6E)), // aqua
            listOf(Color(0xFF2DD4BF), Color(0xFF115E59)), // teal
            listOf(Color(0xFF38BDF8), Color(0xFF075985)), // sky
            listOf(Color(0xFF60A5FA), Color(0xFF1E3A8A)), // azure → sapphire
            listOf(Color(0xFF5EEAD4), Color(0xFF134E4A)), // mint → deep teal
            listOf(Color(0xFF67E8F9), Color(0xFF155E75)), // ice → ocean ink
        ),
    ),

    /**
     * Emerald + lime tops landing on pine / spruce shadows. Each tile reads
     * like its own forest layer instead of a uniform dark green.
     */
    FOREST(
        displayName = "Forest",
        description = "Emerald, lime & pine",
        accent = Color(0xFF10B981),
        allCardsGradient = listOf(Color(0xFF34D399), Color(0xFF064E3B)),
        uncategorizedGradient = listOf(Color(0xFF78716C), Color(0xFF1C1917)),
        palette = listOf(
            listOf(Color(0xFF34D399), Color(0xFF065F46)), // emerald
            listOf(Color(0xFF4ADE80), Color(0xFF166534)), // grass
            listOf(Color(0xFF2DD4BF), Color(0xFF115E59)), // teal-pine
            listOf(Color(0xFFA3E635), Color(0xFF3F6212)), // lime → moss
            listOf(Color(0xFF6EE7B7), Color(0xFF064E3B)), // jade
            listOf(Color(0xFF14B8A6), Color(0xFF134E4A)), // spruce
        ),
    ),

    /**
     * Heritage / editorial — bright rose, raspberry, mauve, plum tops over
     * deep wine and burgundy shadows. Warm without ever being childish.
     */
    ROSEWOOD(
        displayName = "Rosewood",
        description = "Rose, claret & plum",
        accent = Color(0xFFE11D48),
        allCardsGradient = listOf(Color(0xFFFB7185), Color(0xFF881337)),
        uncategorizedGradient = listOf(Color(0xFFA8A29E), Color(0xFF1C1917)),
        palette = listOf(
            listOf(Color(0xFFFB7185), Color(0xFF881337)), // rose
            listOf(Color(0xFFF472B6), Color(0xFF9D174D)), // raspberry
            listOf(Color(0xFFC084FC), Color(0xFF6B21A8)), // mauve
            listOf(Color(0xFFFDA4AF), Color(0xFF9F1239)), // blush
            listOf(Color(0xFFD8B4FE), Color(0xFF581C87)), // lilac → plum
            listOf(Color(0xFFFCA5A5), Color(0xFF7F1D1D)), // coral → mahogany
        ),
    );

    /**
     * Pick a deterministic gradient pair from [palette] given a stable string
     * seed (typically the category id). Same seed always returns the same
     * gradient inside a theme so opening a folder a second time looks
     * identical.
     */
    fun gradientFor(seed: String): List<Color> {
        if (palette.isEmpty()) return allCardsGradient
        // String.hashCode() can be negative and `%` in Kotlin/Java preserves
        // the sign of the dividend, so use floorMod to guarantee a
        // non-negative index regardless of the seed.
        val index = Math.floorMod(seed.hashCode(), palette.size)
        return palette[index]
    }

    companion object {
        fun fromName(name: String?): FolderTheme = entries.firstOrNull { it.name == name } ?: VIBRANT
    }
}

/**
 * Optional decorative pattern rendered as a low-opacity overlay on top of the
 * screen gradient. Patterns are designed to be subtle so they read as texture
 * rather than decoration.
 */
enum class BackgroundPattern(val displayName: String, val description: String) {
    NONE("None", "Plain gradient backdrop"),
    DOTS("Dots", "Soft dot grid"),
    GRID("Grid", "Crisp pinstripe grid"),
    TOPO("Topographic", "Concentric contour lines");

    companion object {
        fun fromName(name: String?): BackgroundPattern =
            entries.firstOrNull { it.name == name } ?: NONE
    }
}

/** Selected folder theme — defaults to VIBRANT to preserve existing behavior. */
val LocalFolderTheme = compositionLocalOf { FolderTheme.VIBRANT }

/** Selected background pattern — defaults to NONE to preserve existing behavior. */
val LocalBackgroundPattern = staticCompositionLocalOf { BackgroundPattern.NONE }
