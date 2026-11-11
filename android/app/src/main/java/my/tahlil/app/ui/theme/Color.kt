package my.tahlil.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * App color palette — derived from the web app's OKLCH design tokens.
 *
 * The original web tokens use the CSS Color 4 OKLCH notation:
 *   --background: oklch(0.985 0.012 95)  → cream
 *   --foreground: oklch(0.22 0.04 160)   → dark emerald
 *   --primary:    oklch(0.42 0.09 165)    → emerald
 *   --accent:     oklch(0.78 0.13 85)     → gold
 *   ...
 *
 * For Android we precompute the sRGB equivalent via the standard OKLCH → OKLab →
 * linear-sRGB → sRGB (gamma) pipeline. The conversion script is reproduced here
 * for transparency:
 *
 * ```
 * def oklch_to_oklab(L, C, H):
 *     h_rad = math.radians(H)
 *     return (L, C * math.cos(h_rad), C * math.sin(h_rad))
 *
 * def oklab_to_linear_rgb(L, a, b):
 *     l_ = L + 0.3963377774 * a + 0.2158037573 * b
 *     m_ = L - 0.1055613458 * a - 0.0638541728 * b
 *     s_ = L - 0.0894841775 * a - 1.2914855480 * b
 *     l, m, s = l_**3, m_**3, s_**3
 *     return (
 *         +4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s,
 *         -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s,
 *         -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s,
 *     )
 *
 * def linear_to_srgb(c):
 *     return 1.055 * c**(1/2.4) - 0.055 if c > 0.0031308 else 12.92 * c
 * ```
 *
 * All colors are stored as `0xAARRGGBB` Long literals — Compose's `Color(Long)`
 * factory accepts that form directly.
 */

// =============================================================================
// LIGHT THEME — cream background, deep-emerald text, emerald primary, gold accent
// =============================================================================

/** oklch(0.985 0.012 95) — cream background. */
val LightBackground = Color(0xFFFCFAF1)
/** oklch(1 0.006 95) — near-white cream surface for cards. */
val LightSurface = Color(0xFFFFFBF1)
/** oklch(0.985 0.012 95) — slightly darker card variant (alias). */
val LightCard = Color(0xFFFFFBF1)
/** oklch(0.95 0.015 95) — soft cream for muted backgrounds. */
val LightMuted = Color(0xFFF1EFE4)
/** oklch(0.92 0.015 95) — surface variant. */
val LightSurfaceVariant = Color(0xFFE7E5DA)
/** oklch(0.9 0.02 95) — light cream border. */
val LightBorder = Color(0xFFE2DED0)
/** oklch(0.82 0.015 95) — softer border for subtle dividers. */
val LightOutlineVariant = Color(0xFFC7C4B9)
/** oklch(0.60 0.02 95) — neutral outline. */
val LightOutline = Color(0xFF848073)

/** oklch(0.22 0.04 160) — dark emerald foreground text. */
val LightOnBackground = Color(0xFF072014)
/** oklch(0.45 0.025 95) — muted text. */
val LightOnSurfaceVariant = Color(0xFF595546)

/** oklch(0.42 0.09 165) — emerald primary. */
val LightPrimary = Color(0xFF005C41)
/** oklch(0.30 0.08 165) — deep emerald text on primary container. */
val LightOnPrimaryContainer = Color(0xFF003A23)
/** oklch(0.90 0.06 165) — light emerald container. */
val LightPrimaryContainer = Color(0xFFBAEBD4)
/** White-on-emerald has plenty of contrast. */
val LightOnPrimary = Color(0xFFFFFFFF)
/** oklch(0.55 0.10 165) — ring / focus. */
val LightRing = Color(0xFF288463)

/** oklch(0.78 0.13 85) — gold accent / secondary. */
val LightSecondary = Color(0xFFDDB049)
/** oklch(0.92 0.07 85) — light gold container. */
val LightSecondaryContainer = Color(0xFFFAE2B0)
/** oklch(0.30 0.08 85) — deep gold text on light gold container. */
val LightOnSecondaryContainer = Color(0xFF402900)
/** White-on-gold needs extra contrast — use deep gold instead. */
val LightOnSecondary = Color(0xFF2A1B00)

/** oklch(0.55 0.10 195) — teal accent (tertiary). */
val LightTertiary = Color(0xFF008384)
/** oklch(0.88 0.05 195) — light teal container. */
val LightTertiaryContainer = Color(0xFFB2E2E2)
/** oklch(0.28 0.07 195) — deep teal text on light teal container. */
val LightOnTertiaryContainer = Color(0xFF003333)
val LightOnTertiary = Color(0xFFFFFFFF)

/** oklch(0.10 0 0) — near-black scrim for modal overlays. */
val LightScrim = Color(0xFF030303)

/** Error tones — kept consistent with the rest of the palette (deep red). */
val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)

// =============================================================================
// DARK THEME — deep forest green background, cream text, bright emerald, gold
// =============================================================================

/** oklch(0.16 0.02 165) — deep forest green background. */
val DarkBackground = Color(0xFF05100B)
/** oklch(0.20 0.025 165) — deep green card surface. */
val DarkSurface = Color(0xFF0A1A13)
/** oklch(0.20 0.025 165) — alias. */
val DarkCard = Color(0xFF0A1A13)
/** oklch(0.25 0.02 165) — muted green. */
val DarkMuted = Color(0xFF19251F)
/** oklch(0.25 0.02 165) — surface variant. */
val DarkSurfaceVariant = Color(0xFF19251F)
/** oklch(1 0 0 / 10%) — 10% white border. */
val DarkBorder = Color(0x1AFFFFFF)
/** oklch(0.40 0.02 165) — softer border. */
val DarkOutlineVariant = Color(0xFF3E4B45)
/** oklch(0.70 0.015 95) — light outline. */
val DarkOutline = Color(0xFFA19E94)

/** oklch(0.95 0.015 95) — cream foreground text. */
val DarkOnBackground = Color(0xFFF1EFE4)
/** oklch(0.78 0.025 95) — muted text. */
val DarkOnSurfaceVariant = Color(0xFFBCB8A6)

/** oklch(0.65 0.12 165) — bright emerald primary. */
val DarkPrimary = Color(0xFF32A67D)
/** oklch(0.90 0.06 165) — cream-emerald text on dark primary container. */
val DarkOnPrimaryContainer = Color(0xFFBAEBD4)
/** oklch(0.28 0.07 165) — dark emerald container. */
val DarkPrimaryContainer = Color(0xFF003320)
/** oklch(0.16 0.02 165) — deep background text on primary. */
val DarkOnPrimary = Color(0xFF05100B)
/** oklch(0.60 0.10 165) — ring / focus. */
val DarkRing = Color(0xFF3A9371)

/** oklch(0.72 0.13 85) — gold accent / secondary. */
val DarkSecondary = Color(0xFFCA9D33)
/** oklch(0.30 0.07 85) — deep gold container (dark variant). */
val DarkSecondaryContainer = Color(0xFF3E2A00)
val DarkOnSecondaryContainer = Color(0xFFFAE2B0)
/** oklch(0.16 0.02 165) — deep bg text on gold. */
val DarkOnSecondary = Color(0xFF05100B)

/** oklch(0.60 0.10 195) — bright teal accent (tertiary). */
val DarkTertiary = Color(0xFF0F9293)
/** oklch(0.28 0.07 195) — dark teal container. */
val DarkTertiaryContainer = Color(0xFF003333)
/** oklch(0.88 0.05 195) — light teal text. */
val DarkOnTertiaryContainer = Color(0xFFB2E2E2)
val DarkOnTertiary = Color(0xFF05100B)

/** oklch(0.0 0 0) — pure black scrim. */
val DarkScrim = Color(0xFF000000)

/** Error tones — kept consistent with the rest of the palette. */
val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

// =============================================================================
// DECORATIVE / BRAND COLORS (independent of light/dark)
// =============================================================================

/** Emerald gradient start (deep emerald) — used for the launcher icon. */
val BrandEmeraldDeep = Color(0xFF065F46)
/** Emerald gradient mid (emerald-500). */
val BrandEmerald = Color(0xFF10B981)
/** Teal gradient end — used for the launcher icon. */
val BrandTeal = Color(0xFF0D9488)
/** Cream / off-white for the launcher foreground rub-el-hizb star. */
val BrandCream = Color(0xFFFEF3C7)
/** Gold for the small star above the crescent. */
val BrandGold = Color(0xFFF59E0B)

/** Used by the ArabicText colorize utility — kept here for one source of truth. */
val ArabicColorizeA = Color(0xFF1A1C19)
val ArabicColorizeB = Color(0xFF3A3F36)
val ArabicColorizeADark = Color(0xFFE8E3D6)
val ArabicColorizeBDark = Color(0xFFC9C3B0)

/** Ayah mark (۝) background tint. */
val AyahMarkBgLight = Color(0xFF005C41)
val AyahMarkBgDark = Color(0xFF32A67D)
