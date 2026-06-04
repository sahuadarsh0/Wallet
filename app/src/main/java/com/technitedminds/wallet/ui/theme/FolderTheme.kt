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
     * Boardroom palette — Stripe / Linear / Mercury blues. Refined: dropped
     * the pale "ice" highlights for deeper royal/sapphire mid-tones so every
     * tile reads like a trading-desk chip rather than a sky.
     */
    EXECUTIVE(
        displayName = "Executive",
        description = "Sapphire, indigo & graphite",
        accent = Color(0xFF2563EB),
        allCardsGradient = listOf(Color(0xFF3B82F6), Color(0xFF1E3A8A)),
        uncategorizedGradient = listOf(Color(0xFF64748B), Color(0xFF0F172A)),
        palette = listOf(
            listOf(Color(0xFF3B82F6), Color(0xFF1E3A8A)), // royal
            listOf(Color(0xFF6366F1), Color(0xFF312E81)), // indigo ink
            listOf(Color(0xFF0EA5E9), Color(0xFF075985)), // deep azure
            listOf(Color(0xFF475569), Color(0xFF0F172A)), // slate ink
            listOf(Color(0xFF0891B2), Color(0xFF164E63)), // steel teal
            listOf(Color(0xFF1E40AF), Color(0xFF1E1B4B)), // sapphire deep
        ),
    ),

    /**
     * Aubergine / heliotrope nightfall. Pulled away from the prior neon
     * lavender highlights toward saturated jewel-tone violets that bottom
     * out in true purple ink — feels like luxury watch packaging at night.
     */
    MIDNIGHT(
        displayName = "Midnight",
        description = "Violet, indigo & aubergine",
        accent = Color(0xFF6D28D9),
        allCardsGradient = listOf(Color(0xFF7C3AED), Color(0xFF2E1065)),
        uncategorizedGradient = listOf(Color(0xFF64748B), Color(0xFF0F172A)),
        palette = listOf(
            listOf(Color(0xFF7C3AED), Color(0xFF2E1065)), // violet
            listOf(Color(0xFF6366F1), Color(0xFF1E1B4B)), // indigo
            listOf(Color(0xFF8B5CF6), Color(0xFF4C1D95)), // heliotrope
            listOf(Color(0xFF4F46E5), Color(0xFF1E1B4B)), // deep indigo
            listOf(Color(0xFFA855F7), Color(0xFF581C87)), // orchid
            listOf(Color(0xFF5B21B6), Color(0xFF1E1B4B)), // royal purple ink
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
        accent = Color(0xFF78716C),
        allCardsGradient = listOf(Color(0xFF94A3B8), Color(0xFF1E293B)),
        uncategorizedGradient = listOf(Color(0xFF9CA3AF), Color(0xFF111827)),
        palette = listOf(
            listOf(Color(0xFF94A3B8), Color(0xFF1E293B)), // slate
            listOf(Color(0xFFA1A1AA), Color(0xFF27272A)), // zinc
            listOf(Color(0xFF9CA3AF), Color(0xFF111827)), // gray
            listOf(Color(0xFFA8A29E), Color(0xFF1C1917)), // stone
            listOf(Color(0xFF6B7280), Color(0xFF030712)), // ink
            listOf(Color(0xFF78716C), Color(0xFF292524)), // taupe
        ),
    ),

    /**
     * Warm earth — refined to dusty/sun-baked tones. Replaced bright lemon
     * yellows and lime greens with burnt amber, rust, bronze and olive that
     * land in true coffee/charcoal shadows. Editorial, not crayon.
     */
    EARTH(
        displayName = "Earth",
        description = "Walnut, clay & espresso",
        accent = Color(0xFF78350F),
        allCardsGradient = listOf(Color(0xFFB45309), Color(0xFF451A03)),
        uncategorizedGradient = listOf(Color(0xFF78716C), Color(0xFF1C1917)),
        palette = listOf(
            listOf(Color(0xFF92400E), Color(0xFF451A03)), // walnut
            listOf(Color(0xFFB45309), Color(0xFF7C2D12)), // clay
            listOf(Color(0xFF78350F), Color(0xFF292524)), // espresso
            listOf(Color(0xFF9A3412), Color(0xFF431407)), // sienna ink
            listOf(Color(0xFF854D0E), Color(0xFF422006)), // tobacco
            listOf(Color(0xFF44403C), Color(0xFF1C1917)), // charcoal stone
        ),
    ),

    /**
     * Deep ocean — pulled the highlights from pop-cyan and mint toward
     * cyan/teal/sapphire mid-tones that bottom out in trench-blue. The
     * tiles feel like they were dipped, not painted.
     */
    OCEAN(
        displayName = "Ocean",
        description = "Cyan, teal & sapphire",
        accent = Color(0xFF0891B2),
        allCardsGradient = listOf(Color(0xFF06B6D4), Color(0xFF164E63)),
        uncategorizedGradient = listOf(Color(0xFF64748B), Color(0xFF0F172A)),
        palette = listOf(
            listOf(Color(0xFF06B6D4), Color(0xFF164E63)), // cyan → deep teal
            listOf(Color(0xFF0D9488), Color(0xFF134E4A)), // teal
            listOf(Color(0xFF0EA5E9), Color(0xFF075985)), // sky → deep azure
            listOf(Color(0xFF1E40AF), Color(0xFF1E1B4B)), // sapphire
            listOf(Color(0xFF14B8A6), Color(0xFF115E59)), // jade-teal
            listOf(Color(0xFF0E7490), Color(0xFF083344)), // ocean ink
        ),
    ),

    /**
     * Pine forest — dropped the spring-lime / mint highlights for sage,
     * moss and pine mid-tones that resolve into spruce / forest-floor
     * shadows. National-park guidebook, not lawn fertilizer.
     */
    FOREST(
        displayName = "Forest",
        description = "Pine, sage & moss",
        accent = Color(0xFF15803D),
        allCardsGradient = listOf(Color(0xFF16A34A), Color(0xFF064E3B)),
        uncategorizedGradient = listOf(Color(0xFF78716C), Color(0xFF1C1917)),
        palette = listOf(
            listOf(Color(0xFF16A34A), Color(0xFF064E3B)), // emerald
            listOf(Color(0xFF15803D), Color(0xFF14532D)), // forest
            listOf(Color(0xFF65A30D), Color(0xFF365314)), // moss
            listOf(Color(0xFF0D9488), Color(0xFF134E4A)), // pine teal
            listOf(Color(0xFF4D7C0F), Color(0xFF1A2E05)), // deep moss
            listOf(Color(0xFF166534), Color(0xFF052E16)), // spruce
        ),
    ),

    /**
     * Heritage / editorial — pulled from blush-pink and lilac highlights
     * toward claret, burgundy, mulberry and oxblood. Reads like vintage
     * leather binding rather than birthday card.
     */
    ROSEWOOD(
        displayName = "Rosewood",
        description = "Bordeaux, oxblood & port",
        accent = Color(0xFF7F1D1D),
        allCardsGradient = listOf(Color(0xFF9F1239), Color(0xFF450A0A)),
        uncategorizedGradient = listOf(Color(0xFF78716C), Color(0xFF1C1917)),
        palette = listOf(
            listOf(Color(0xFF9F1239), Color(0xFF450A0A)), // bordeaux
            listOf(Color(0xFF881337), Color(0xFF4C0519)), // oxblood
            listOf(Color(0xFF7F1D1D), Color(0xFF450A0A)), // port
            listOf(Color(0xFFB91C1C), Color(0xFF7F1D1D)), // ruby leather
            listOf(Color(0xFF991B1B), Color(0xFF450A0A)), // claret deep
            listOf(Color(0xFF6B1D1D), Color(0xFF1C0303)), // mahogany ink
        ),
    ),

    /**
     * Pure neutral graphite — ink-on-ink without slate cools or stone warms.
     * Reads like printed editorial typography on premium card stock.
     */
    GRAPHITE(
        displayName = "Graphite",
        description = "Onyx, graphite & ink",
        accent = Color(0xFF3F3F46),
        allCardsGradient = listOf(Color(0xFF52525B), Color(0xFF09090B)),
        uncategorizedGradient = listOf(Color(0xFF71717A), Color(0xFF18181B)),
        palette = listOf(
            listOf(Color(0xFF52525B), Color(0xFF09090B)), // onyx
            listOf(Color(0xFF3F3F46), Color(0xFF18181B)), // graphite
            listOf(Color(0xFF27272A), Color(0xFF000000)), // pure ink
            listOf(Color(0xFF44403C), Color(0xFF0C0A09)), // basalt
            listOf(Color(0xFF374151), Color(0xFF030712)), // gunmetal
            listOf(Color(0xFF1F2937), Color(0xFF030712)), // tar
        ),
    ),

    /**
     * Cognac/champagne — refined warm metallics. Antique gold, brass, brandy.
     * Reads like vintage spirits packaging or hardcover gilt edges.
     */
    CHAMPAGNE(
        displayName = "Champagne",
        description = "Cognac, brass & antique gold",
        accent = Color(0xFF92400E),
        allCardsGradient = listOf(Color(0xFFA16207), Color(0xFF422006)),
        uncategorizedGradient = listOf(Color(0xFF78716C), Color(0xFF1C1917)),
        palette = listOf(
            listOf(Color(0xFFA16207), Color(0xFF422006)), // antique gold
            listOf(Color(0xFF854D0E), Color(0xFF422006)), // brass
            listOf(Color(0xFF92400E), Color(0xFF451A03)), // cognac
            listOf(Color(0xFF713F12), Color(0xFF1C1917)), // bronze ink
            listOf(Color(0xFFB45309), Color(0xFF7C2D12)), // brandy
            listOf(Color(0xFF78350F), Color(0xFF292524)), // dark amber
        ),
    ),

    /**
     * Twilight — premium teal/navy crossover that lives between Ocean and
     * Executive. Petrol blue, deep teal, midnight navy. Aman / Aesop / Apple
     * Watch dial vibe.
     */
    TWILIGHT(
        displayName = "Twilight",
        description = "Petrol, teal & midnight navy",
        accent = Color(0xFF0F4C5C),
        allCardsGradient = listOf(Color(0xFF155E75), Color(0xFF0C2E3D)),
        uncategorizedGradient = listOf(Color(0xFF475569), Color(0xFF0F172A)),
        palette = listOf(
            listOf(Color(0xFF155E75), Color(0xFF0C2E3D)), // petrol
            listOf(Color(0xFF134E4A), Color(0xFF042F2E)), // deep teal
            listOf(Color(0xFF1E3A8A), Color(0xFF0F172A)), // midnight navy
            listOf(Color(0xFF164E63), Color(0xFF083344)), // peacock ink
            listOf(Color(0xFF115E59), Color(0xFF052E26)), // verdigris
            listOf(Color(0xFF1E40AF), Color(0xFF1E1B4B)), // sapphire night
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

/**
 * How the folder tile is rendered. [GRADIENT] is the lit-corner-to-corner
 * default with radial specular and vignette. [FLAT] paints a single solid
 * fill (the gradient's deep tone) for an editorial / Aesop / Mercury card
 * feel — no gloss, no falloff, just the color.
 */
enum class FolderStyle(val displayName: String, val description: String) {
    GRADIENT("Gradient", "Lit corner-to-corner with soft specular"),
    FLAT("Flat", "Single solid color, no gloss");

    companion object {
        fun fromName(name: String?): FolderStyle =
            entries.firstOrNull { it.name == name } ?: GRADIENT
    }
}

/** Selected folder theme — defaults to VIBRANT to preserve existing behavior. */
val LocalFolderTheme = compositionLocalOf { FolderTheme.VIBRANT }

/** Selected background pattern — defaults to NONE to preserve existing behavior. */
val LocalBackgroundPattern = staticCompositionLocalOf { BackgroundPattern.NONE }

/** Selected folder style — defaults to GRADIENT to preserve existing behavior. */
val LocalFolderStyle = staticCompositionLocalOf { FolderStyle.GRADIENT }
