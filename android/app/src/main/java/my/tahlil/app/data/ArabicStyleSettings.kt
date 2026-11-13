package my.tahlil.app.data

import kotlinx.serialization.Serializable

/**
 * Arabic font style mode — matches the three modes available in the web app:
 *  - Standard:  Clean, conventional harakat (Amiri-style).
 *  - Quran:      Utsmani mushaf style (Amiri Quran).
 *  - Naskhi:     Traditional Naskhi (Scheherazade New) — bold and spacious.
 */
@Serializable
enum class ArabicStyleMode(val storageKey: String, val label: String, val description: String) {
    Standard(
        storageKey = "standard",
        label = "Arab Standar",
        description = "Tampilan sederhana, bersih, dan harakat konvensional. Readability tinggi untuk teks sehari-hari."
    ),
    Quran(
        storageKey = "quran",
        label = "Gaya Mushaf Al-Qur'an (Utsmani)",
        description = "Karakter Naskhi khas mushaf, bentuk sambungan huruf, ligatur, harakat, dan tanda baca Al-Qur'an yang lebih autentik."
    ),
    Naskhi(
        storageKey = "naskhi",
        label = "Naskh Tradisional",
        description = "Font Naskh tradisional (Scheherazade New), dengan struktur huruf horizontal lurus tanpa ligatur Utsmani, harakat lengkap, dan spasi antarbaris renggang."
    );

    companion object {
        fun fromStorageKey(key: String?): ArabicStyleMode =
            values().firstOrNull { it.storageKey == key } ?: Quran
    }
}

/** Alignment for Arabic text — right-aligned (Mushaf style), justified, or centered. */
@Serializable
enum class ArabicAlign(val storageKey: String, val label: String) {
    Right("right", "Kanan"),
    Justify("justify", "Rata Kanan-Kiri"),
    Center("center", "Tengah");

    companion object {
        fun fromStorageKey(key: String?): ArabicAlign =
            values().firstOrNull { it.storageKey == key } ?: Right
    }
}

/**
 * Full Arabic typography settings — font family/mode plus numerical layout
 * knobs that the user controls with sliders on the Settings screen.
 *
 * Besides the Arabic-text knobs, this also carries [bodyLineHeight]: the
 * line-spacing multiplier for the LATIN transliteration + Indonesian
 * translation ("kalimat") text shown under each Arabic verse. It lives here
 * (not in DisplayToggles) because every screen that renders reading text
 * already receives [ArabicStyleSettings] — no extra threading needed.
 */
@Serializable
data class ArabicStyleSettings(
    val mode: ArabicStyleMode = ArabicStyleMode.Quran,
    val fontSize: Float = 1.75f,        // rem (mapped to sp via scale)
    val lineHeight: Float = 2.1f,       // unitless multiplier (Arabic text)
    val bodyLineHeight: Float = 1.55f,  // unitless multiplier (Latin + terjemahan)
    val wordSpacing: Float = 0.05f,     // em
    val letterSpacing: Float = 0f,      // em
    val align: ArabicAlign = ArabicAlign.Right,
) {
    companion object {
        val Default = ArabicStyleSettings()

        // Slider ranges — kept here so the UI and persistence use the same bounds.
        val FONT_SIZE_RANGE = 1.0f..3.0f
        val LINE_HEIGHT_RANGE = 1.4f..3.5f
        val BODY_LINE_HEIGHT_RANGE = 1.0f..2.5f
        val WORD_SPACING_RANGE = 0f..0.5f
        val LETTER_SPACING_RANGE = 0f..0.1f

        /** Suggested defaults when the user switches to Naskhi mode. */
        val NaskhiSuggested = ArabicStyleSettings(
            mode = ArabicStyleMode.Naskhi,
            fontSize = 2.0f,
            lineHeight = 2.6f,
            wordSpacing = 0.18f,
            letterSpacing = 0.03f,
            align = ArabicAlign.Right,
        )

        /**
         * Base font size (sp) of the Latin/translation body text — Material3's
         * `bodyMedium`. [bodyLineHeight] multiplies this to produce the final
         * TextUnit line height, so 1.55 ≈ the previous fixed 22.sp (14 × 1.55 ≈ 21.7).
         */
        const val BODY_FONT_SIZE_SP = 14f
    }
}
