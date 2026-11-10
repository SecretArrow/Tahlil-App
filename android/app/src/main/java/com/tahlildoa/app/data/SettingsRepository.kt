package com.tahlildoa.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
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
    private val KEY_KEEP_SCREEN_ON = booleanPreferencesKey("display_keep_screen_on")

    // --- Arabic style ----------------------------------------------------
    private val KEY_MODE = stringPreferencesKey("arabic_mode")
    private val KEY_FONT_SIZE = floatPreferencesKey("arabic_font_size")
    private val KEY_LINE_HEIGHT = floatPreferencesKey("arabic_line_height")
    private val KEY_WORD_SPACING = floatPreferencesKey("arabic_word_spacing")
    private val KEY_LETTER_SPACING = floatPreferencesKey("arabic_letter_spacing")
    private val KEY_ALIGN = stringPreferencesKey("arabic_align")

    // --- Theme ------------------------------------------------------------
    private val KEY_THEME = stringPreferencesKey("theme_mode")

    // --- Continue last reading -------------------------------------------
    private val KEY_LAST_READ_SECTION = stringPreferencesKey("last_read_section_id")

    val displayToggles: Flow<DisplayToggles> = context.tahlilDataStore.data.map { p ->
        DisplayToggles(
            arabic = p[KEY_ARABIC] ?: true,
            transliteration = p[KEY_TRANSLIT] ?: true,
            translation = p[KEY_TRANSLATION] ?: true,
            punctuateArabic = p[KEY_PUNCTUATE] ?: false,
            splitArabicLines = p[KEY_SPLIT_LINES] ?: true,
            colorizeArabic = p[KEY_COLORIZE] ?: false,
            tajwid = p[KEY_TAJWID] ?: false,
            keepScreenOn = p[KEY_KEEP_SCREEN_ON] ?: false,
        )
    }

    val arabicStyle: Flow<ArabicStyleSettings> = context.tahlilDataStore.data.map { p ->
        ArabicStyleSettings(
            mode = ArabicStyleMode.fromStorageKey(p[KEY_MODE]),
            fontSize = p[KEY_FONT_SIZE] ?: ArabicStyleSettings.Default.fontSize,
            lineHeight = p[KEY_LINE_HEIGHT] ?: ArabicStyleSettings.Default.lineHeight,
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
            p[KEY_KEEP_SCREEN_ON] = toggles.keepScreenOn
        }
    }

    suspend fun updateArabicStyle(settings: ArabicStyleSettings) {
        context.tahlilDataStore.edit { p ->
            p[KEY_MODE] = settings.mode.storageKey
            p[KEY_FONT_SIZE] = settings.fontSize
            p[KEY_LINE_HEIGHT] = settings.lineHeight
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
}
