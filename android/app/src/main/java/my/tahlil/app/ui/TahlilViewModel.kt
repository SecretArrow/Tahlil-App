package my.tahlil.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import my.tahlil.app.data.ArabicStyleSettings
import my.tahlil.app.data.DisplayToggles
import my.tahlil.app.data.SettingsRepository
import my.tahlil.app.data.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Top-level UI state — bundles all reactive settings into one StateFlow so
 * the screens can observe a single source of truth.
 *
 * - [lastReadSectionId]: persisted pointer to the last section the user opened
 *   (used by HomeScreen to show a "Continue reading" card + auto-navigate on
 *   app launch).
 * - [isFullscreen]: session-only fullscreen toggle (NOT persisted — resets to
 *   false each launch). When true, MainActivity hides the system bars via
 *   WindowInsetsControllerCompat.
 */
data class TahlilUiState(
    val toggles: DisplayToggles = DisplayToggles(),
    val style: ArabicStyleSettings = ArabicStyleSettings.Default,
    val themeMode: ThemeMode = ThemeMode.System,
    val isDark: Boolean = false,
    val isFullscreen: Boolean = false,
    val lastReadSectionId: String? = null,
)

/** Internal helper so we can combine >5 flows via a nested combine(). */
private data class PersistedSettings(
    val toggles: DisplayToggles,
    val style: ArabicStyleSettings,
    val themeMode: ThemeMode,
    val lastRead: String?,
    val isDark: Boolean,
)

/**
 * ViewModel for the whole app — owns the [SettingsRepository] and exposes
 * a single [uiState] for screens to observe.
 *
 * Updates are fire-and-forget — the StateFlow is the source of truth and
 * always reflects what's persisted.
 */
class TahlilViewModel(private val repo: SettingsRepository) : ViewModel() {

    // The isDark flag is set by the screen from the system theme resolver,
    // since only Composables can call isSystemInDarkTheme().
    private val _isDark = MutableStateFlow(false)
    val isDark: StateFlow<Boolean> = _isDark.asStateFlow()

    // Session-only fullscreen flag (not persisted) — MainActivity observes
    // this and toggles the system-bars visibility via WindowInsetsControllerCompat.
    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    private val persisted: Flow<PersistedSettings> = combine(
        repo.displayToggles,
        repo.arabicStyle,
        repo.themeMode,
        repo.lastReadSectionId,
        _isDark,
    ) { toggles, style, themeMode, lastRead, isDark ->
        PersistedSettings(toggles, style, themeMode, lastRead, isDark)
    }

    val uiState: StateFlow<TahlilUiState> = combine(
        persisted,
        _isFullscreen,
    ) { p, isFullscreen ->
        TahlilUiState(
            toggles = p.toggles,
            style = p.style,
            themeMode = p.themeMode,
            isDark = p.isDark,
            isFullscreen = isFullscreen,
            lastReadSectionId = p.lastRead,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = TahlilUiState(),
    )

    fun setDark(value: Boolean) { _isDark.value = value }

    /** Toggle (or set) the immersive fullscreen mode for the current session. */
    fun setFullscreen(value: Boolean) { _isFullscreen.value = value }

    /** Convenience: flip the fullscreen flag. */
    fun toggleFullscreen() { _isFullscreen.value = !_isFullscreen.value }

    fun updateToggles(transform: (DisplayToggles) -> DisplayToggles) {
        val next = transform(uiState.value.toggles)
        viewModelScope.launch { repo.updateDisplayToggles(next) }
    }

    fun updateStyle(transform: (ArabicStyleSettings) -> ArabicStyleSettings) {
        val next = transform(uiState.value.style)
        viewModelScope.launch { repo.updateArabicStyle(next) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repo.updateThemeMode(mode) }
    }

    /**
     * Convenience: cycle theme modes for the quick toggle button in the
     * header. System → Light → Dark → System → … so all three modes are
     * reachable from the header without opening Settings.
     */
    fun toggleTheme() {
        val next = when (uiState.value.themeMode) {
            ThemeMode.System -> ThemeMode.Light
            ThemeMode.Light -> ThemeMode.Dark
            ThemeMode.Dark -> ThemeMode.System
        }
        setThemeMode(next)
    }

    /**
     * Persist the last-read section id (called whenever a section is opened).
     * Pass null to clear the pointer ("Mulai dari awal").
     */
    fun setLastReadSectionId(id: String?) {
        viewModelScope.launch { repo.setLastReadSectionId(id) }
    }

    companion object {
        fun factory(repo: SettingsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    TahlilViewModel(repo) as T
            }
    }
}
