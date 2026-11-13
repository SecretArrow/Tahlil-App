package my.tahlil.app.data

import kotlinx.serialization.Serializable

/**
 * Auto-scroll mode — Continuous (smooth pixel-by-pixel) or Block
 * (jump section-by-section). Mirrors the web app's autoScrollMode.
 */
@Serializable
enum class AutoScrollMode(val storageKey: String, val label: String) {
    Continuous("continuous", "Terus-menerus"),
    Block("block", "Per Blok");

    companion object {
        fun fromStorageKey(key: String?): AutoScrollMode =
            values().firstOrNull { it.storageKey == key } ?: Continuous
    }
}

/**
 * Display toggles for the prayer book.
 *
 * - arabic:           Show/hide the Arabic verse text.
 * - transliteration:  Show/hide the Latin transliteration.
 * - translation:      Show/hide the Indonesian translation.
 * - punctuateArabic:  When true, inserts Arabic commas (،) before " وَ"
 *                     connectors — mirroring the punctuation already present
 *                     in the Indonesian translation. Quran verses (with ۝ or
 *                     ۩ marks) are never modified.
 * - splitArabicLines: When true (default), Arabic text WITHOUT ayah marks
 *                     is broken into lines at Arabic commas (،) and Latin
 *                     periods (.) so each clause gets its own line. When
 *                     false, the text flows as a single paragraph and wraps
 *                     naturally. Ayah-marked verses always keep their
 *                     per-verse lines either way.
 * - colorizeArabic:   When true, each Arabic word is rendered in one of two
 *                     alternating shades so consecutive words are visually
 *                     distinguishable. Ignored while [tajwid] is on (rule
 *                     colors take precedence) — same as the web.
 * - tajwid:           When true, tajwid recitation rules detected by
 *                     [my.tahlil.app.util.TajwidUtil.analyzeTajwid]
 *                     (Izhar, Ikhfa, Idgham, Iqlab, Ghunnah, Qalqalah) are
 *                     highlighted with per-rule colors. Default OFF.
 * - keepScreenOn:     When true, the device screen stays awake while the app
 *                     is in the foreground (FLAG_KEEP_SCREEN_ON). Default
 *                     OFF so users don't drain their battery accidentally.
 * - autoScrollEnabled: Master toggle for the auto-scroll (Gulir Otomatis)
 *                     feature. When false (default), the start/pause FAB is
 *                     NOT rendered on the reading screens. When true, a
 *                     small FAB appears at the bottom-right of the reading
 *                     view; tap it to start/stop scrolling, tap the reading
 *                     surface to pause/resume.
 * - autoScrollMode:   Continuous (smooth pixel-by-pixel) or Block
 *                     (jump section-by-section). Mirrors the web's
 *                     `autoScrollMode`. Default Continuous.
 * - autoScrollSpeed:  Dimensionless speed (0.1–25). Higher = faster.
 *                     In Continuous mode the engine derives px/sec from
 *                     this; in Block mode it derives the delay between
 *                     jumps. Default 3.0.
 * - autoScrollSectionPauseSec:
 *                     Seconds the auto-scroll holds at each new section
 *                     card before continuing (0 = no pause; UI offers
 *                     0 / 2 / 5 / 10). Only meaningful in Continuous mode.
 *                     Default 0.
 *
 * All toggles default to true except [punctuateArabic], [colorizeArabic],
 * [tajwid], [keepScreenOn], and [autoScrollEnabled], matching the web app's
 * defaults. [splitArabicLines] defaults to true because per-clause lines are
 * the traditional tahlil book layout.
 */
@Serializable
data class DisplayToggles(
    val arabic: Boolean = true,
    val transliteration: Boolean = true,
    val translation: Boolean = true,
    val punctuateArabic: Boolean = false,
    val splitArabicLines: Boolean = true,
    val colorizeArabic: Boolean = false,
    val tajwid: Boolean = false,
    val keepScreenOn: Boolean = false,
    val autoScrollEnabled: Boolean = false,
    val autoScrollMode: AutoScrollMode = AutoScrollMode.Continuous,
    val autoScrollSpeed: Float = 3.0f,
    val autoScrollSectionPauseSec: Int = 0,
) {
    companion object {
        val Default = DisplayToggles()

        /** Valid range of [autoScrollSpeed] — shared by UI slider & persistence. */
        val AUTO_SCROLL_SPEED_RANGE = 0.1f..25f

        /** Choices (seconds) offered for [autoScrollSectionPauseSec]. */
        val AUTO_SCROLL_PAUSE_CHOICES = intArrayOf(0, 2, 5, 10)
    }
}
