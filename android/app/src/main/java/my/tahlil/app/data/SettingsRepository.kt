package my.tahlil.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Theme preference — System follows the platform setting, Light/Dark force. */
enum class ThemeMode(val storageKey: String, val label: String) {
    System("system", "Sistem"),
    Light("light", "Terang"),
    Dark("dark", "Gelap");

    companion object {
        fun fromStorageKey(key: String?): ThemeMode =
            values().firstOrNull { it.storageKey == key } ?: System
    }
}

private val Context.tahlilDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tahlil_settings"
)

/**
 * Persistence layer for the user's display & typography settings.
 *
 * Backed by Jetpack DataStore Preferences. Settings are stored as flat keys
 * (rather than one JSON blob) so individual writes are atomic and easy to
 * inspect from Android Studio's Device File Explorer.
 *
 * All values are exposed as cold [Flow]s so the UI recomposes reactively
 * when settings change.
 *
 * In addition to display toggles + typography, this repository also owns the
 * "continue last reading" pointer ([lastReadSectionId]) so the app can
 * restore the user's last opened section on the next launch.
 */
class SettingsRepository(private val context: Context) {

    // --- Display toggles -------------------------------------------------
    private val KEY_ARABIC = booleanPreferencesKey("display_arabic")
    private val KEY_TRANSLIT = booleanPreferencesKey("display_transliteration")
    private val KEY_TRANSLATION = booleanPreferencesKey("display_translation")
    private val KEY_PUNCTUATE = booleanPreferencesKey("display_punctuate_arabic")
    private val KEY_SPLIT_LINES = booleanPreferencesKey("display_split_arabic_lines")
    private val KEY_COLORIZE = booleanPreferencesKey("display_colorize_arabic")
    private val KEY_TAJWID = booleanPreferencesKey("display_tajwid")
    private val KEY_WAQAF_COLOR = booleanPreferencesKey("display_waqaf_color")
    private val KEY_KEEP_SCREEN_ON = booleanPreferencesKey("display_keep_screen_on")

    // --- Auto scroll -------------------------------------------------------
    private val KEY_AUTO_SCROLL_ENABLED = booleanPreferencesKey("auto_scroll_enabled")
    private val KEY_AUTO_SCROLL_MODE = stringPreferencesKey("auto_scroll_mode")
    private val KEY_AUTO_SCROLL_SPEED = floatPreferencesKey("auto_scroll_speed")
    private val KEY_AUTO_SCROLL_PAUSE = intPreferencesKey("auto_scroll_section_pause_sec")

    // --- Auto-scroll FAB position (draggable start/pause button) ----------
    // Mirrors the web's tahlil-autoscroll-fab-pos cookie: the user's drag
    // offset survives app restarts ("posisinya tersimpan jika di pindah2
    // pengguna"). Stored in raw screen pixels, clamped on read by the FAB.
    private val KEY_FAB_OFFSET_X = floatPreferencesKey("auto_scroll_fab_offset_x")
    private val KEY_FAB_OFFSET_Y = floatPreferencesKey("auto_scroll_fab_offset_y")

    // --- Arabic style ----------------------------------------------------
    private val KEY_MODE = stringPreferencesKey("arabic_mode")
    private val KEY_FONT_SIZE = floatPreferencesKey("arabic_font_size")
    private val KEY_LINE_HEIGHT = floatPreferencesKey("arabic_line_height")
    private val KEY_BODY_LINE_HEIGHT = floatPreferencesKey("body_line_height")
    private val KEY_WORD_SPACING = floatPreferencesKey("arabic_word_spacing")
    private val KEY_LETTER_SPACING = floatPreferencesKey("arabic_letter_spacing")
    private val KEY_ALIGN = stringPreferencesKey("arabic_align")

    // --- Theme ------------------------------------------------------------
    private val KEY_THEME = stringPreferencesKey("theme_mode")

    // --- Continue last reading -------------------------------------------
    private val KEY_LAST_READ_SECTION = stringPreferencesKey("last_read_section_id")

    val displayToggles: Flow<DisplayToggles> = context.tahlilDataStore.data.map { p ->
        // autoScrollSpeed legacy/foreign values are clamped into the valid
        // range so a stray write can never produce a runaway scroll speed.
        val clampedSpeed = (p[KEY_AUTO_SCROLL_SPEED] ?: DisplayToggles.Default.autoScrollSpeed)
            .coerceIn(
                DisplayToggles.AUTO_SCROLL_SPEED_RANGE.start,
                DisplayToggles.AUTO_SCROLL_SPEED_RANGE.endInclusive,
            )
        DisplayToggles(
            arabic = p[KEY_ARABIC] ?: true,
            transliteration = p[KEY_TRANSLIT] ?: true,
            translation = p[KEY_TRANSLATION] ?: true,
            punctuateArabic = p[KEY_PUNCTUATE] ?: false,
            splitArabicLines = p[KEY_SPLIT_LINES] ?: true,
            colorizeArabic = p[KEY_COLORIZE] ?: false,
            tajwid = p[KEY_TAJWID] ?: false,
            waqafColor = p[KEY_WAQAF_COLOR] ?: false,
            keepScreenOn = p[KEY_KEEP_SCREEN_ON] ?: false,
            autoScrollEnabled = p[KEY_AUTO_SCROLL_ENABLED]
                ?: DisplayToggles.Default.autoScrollEnabled,
            autoScrollMode = AutoScrollMode.fromStorageKey(p[KEY_AUTO_SCROLL_MODE]),
            autoScrollSpeed = clampedSpeed,
            autoScrollSectionPauseSec = p[KEY_AUTO_SCROLL_PAUSE]
                ?: DisplayToggles.Default.autoScrollSectionPauseSec,
        )
    }

    val arabicStyle: Flow<ArabicStyleSettings> = context.tahlilDataStore.data.map { p ->
        // bodyLineHeight was repurposed from "Latin/translation line-height
        // multiplier (1.0-2.5)" to "gap between Arabic kalimat sentences
        // (em, 0-1.5)". Clamp any legacy stored value into the new range so
        // users updating from an older build don't end up with a 1.55em gap
        // (= ~43dp at default font size — far too large).
        val rawBodyLineHeight = p[KEY_BODY_LINE_HEIGHT] ?: ArabicStyleSettings.Default.bodyLineHeight
        val clampedBodyLineHeight = rawBodyLineHeight.coerceIn(
            ArabicStyleSettings.BODY_LINE_HEIGHT_RANGE.start,
            ArabicStyleSettings.BODY_LINE_HEIGHT_RANGE.endInclusive,
        )
        ArabicStyleSettings(
            mode = ArabicStyleMode.fromStorageKey(p[KEY_MODE]),
            fontSize = p[KEY_FONT_SIZE] ?: ArabicStyleSettings.Default.fontSize,
            lineHeight = p[KEY_LINE_HEIGHT] ?: ArabicStyleSettings.Default.lineHeight,
            bodyLineHeight = clampedBodyLineHeight,
            wordSpacing = p[KEY_WORD_SPACING] ?: ArabicStyleSettings.Default.wordSpacing,
            letterSpacing = p[KEY_LETTER_SPACING] ?: ArabicStyleSettings.Default.letterSpacing,
            align = ArabicAlign.fromStorageKey(p[KEY_ALIGN]),
        )
    }

    val themeMode: Flow<ThemeMode> = context.tahlilDataStore.data.map { p ->
        ThemeMode.fromStorageKey(p[KEY_THEME])
    }

    /**
     * Last section id the user opened — used by the Home screen to show a
     * "Continue reading" card and (optionally) auto-navigate to it on launch.
     */
    val lastReadSectionId: Flow<String?> = context.tahlilDataStore.data.map { p ->
        p[KEY_LAST_READ_SECTION]
    }

    /**
     * Persisted drag offset (px) of the auto-scroll start/pause FAB from
     * its default corner, or `null` when the user has never moved it.
     */
    val autoScrollFabOffset: Flow<Pair<Float, Float>?> = context.tahlilDataStore.data.map { p ->
        val x = p[KEY_FAB_OFFSET_X]
        val y = p[KEY_FAB_OFFSET_Y]
        if (x != null && y != null) x to y else null
    }

    // --- Mutators --------------------------------------------------------
    suspend fun updateDisplayToggles(toggles: DisplayToggles) {
        context.tahlilDataStore.edit { p ->
            p[KEY_ARABIC] = toggles.arabic
            p[KEY_TRANSLIT] = toggles.transliteration
            p[KEY_TRANSLATION] = toggles.translation
            p[KEY_PUNCTUATE] = toggles.punctuateArabic
            p[KEY_SPLIT_LINES] = toggles.splitArabicLines
            p[KEY_COLORIZE] = toggles.colorizeArabic
            p[KEY_TAJWID] = toggles.tajwid
            p[KEY_WAQAF_COLOR] = toggles.waqafColor
            p[KEY_KEEP_SCREEN_ON] = toggles.keepScreenOn
            p[KEY_AUTO_SCROLL_ENABLED] = toggles.autoScrollEnabled
            p[KEY_AUTO_SCROLL_MODE] = toggles.autoScrollMode.storageKey
            p[KEY_AUTO_SCROLL_SPEED] = toggles.autoScrollSpeed
            p[KEY_AUTO_SCROLL_PAUSE] = toggles.autoScrollSectionPauseSec
        }
    }

    suspend fun updateArabicStyle(settings: ArabicStyleSettings) {
        context.tahlilDataStore.edit { p ->
            p[KEY_MODE] = settings.mode.storageKey
            p[KEY_FONT_SIZE] = settings.fontSize
            p[KEY_LINE_HEIGHT] = settings.lineHeight
            p[KEY_BODY_LINE_HEIGHT] = settings.bodyLineHeight
            p[KEY_WORD_SPACING] = settings.wordSpacing
            p[KEY_LETTER_SPACING] = settings.letterSpacing
            p[KEY_ALIGN] = settings.align.storageKey
        }
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.tahlilDataStore.edit { p ->
            p[KEY_THEME] = mode.storageKey
        }
    }

    /**
     * Persist the user's last-read section id. Pass `null` to clear the
     * pointer (used by the "Mulai dari awal" affordance on the Home screen).
     */
    suspend fun setLastReadSectionId(id: String?) {
        context.tahlilDataStore.edit { p ->
            if (id.isNullOrEmpty()) {
                p.remove(KEY_LAST_READ_SECTION)
            } else {
                p[KEY_LAST_READ_SECTION] = id
            }
        }
    }

    /** Persist the auto-scroll FAB's drag offset so it survives restarts. */
    suspend fun setAutoScrollFabOffset(x: Float, y: Float) {
        context.tahlilDataStore.edit { p ->
            p[KEY_FAB_OFFSET_X] = x
            p[KEY_FAB_OFFSET_Y] = y
        }
    }
}
