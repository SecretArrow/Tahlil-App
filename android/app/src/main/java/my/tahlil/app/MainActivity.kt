package my.tahlil.app

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import my.tahlil.app.data.ThemeMode
import my.tahlil.app.ui.TahlilViewModel
import my.tahlil.app.ui.navigation.TahlilNavigation
import my.tahlil.app.ui.theme.DarkBackground
import my.tahlil.app.ui.theme.LightBackground
import my.tahlil.app.ui.theme.TahlilDoaTheme

/**
 * MainActivity — hosts the Compose UI.
 *
 * Everything on the launch path is defensive — the app has a history of
 * force-closing on real devices while passing debug-build instrumented
 * tests, so no subsystem is allowed to take the whole process down.
 *
 * Removed [androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen]
 * — it requires a `Theme.SplashScreen` parent with `windowSplashScreenBackground`
 * + `postSplashScreenTheme` attributes, which our theme doesn't provide. The
 * crash this causes on real devices happens BEFORE `super.onCreate()` (outside
 * any try/catch), making it unrecoverable. We don't need a custom splash —
 * the framework already shows a default window background from
 * `android:windowBackground` in the theme.
 *
 * Cross-cutting behaviors driven from here:
 *  - **Keep screen on**: when `toggles.keepScreenOn` is true, the
 *    `FLAG_KEEP_SCREEN_ON` window flag is added so the screen never sleeps
 *    while the app is in the foreground. Cleared on dispose.
 *  - **Fullscreen (immersive)**: when [TahlilViewModel.isFullscreen] is true,
 *    the system bars are hidden with `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`.
 *  - **System bar appearance**: light vs. dark icons based on resolved theme.
 *  All three are guarded by try/catch — some OEM ROMs throw from insets
 *  controllers.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            setContent {
                MainContent()
            }
        } catch (e: Exception) {
            // Last-resort: log + toast + graceful finish instead of a
            // force-close. The global TahlilCrashHandler installed in
            // TahlilDoaApp.onCreate catches anything that slips past this.
            Log.e(TAG, "Failed to set content", e)
            Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    @Composable
    private fun MainContent() {
        val app = remember { (application as TahlilDoaApp) }

        // ViewModel creation is guarded — a DataStore/repository construction
        // failure here shows up as a logged error and a fallback screen, not a
        // force-close. The try/catch must live INSIDE the remember {} — the
        // Compose compiler rejects try/catch around composable invocations.
        val factory = remember {
            try {
                TahlilViewModel.factory(app.settingsRepository)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create ViewModel factory", e)
                null
            }
        }
        if (factory == null) {
            TahlilDoaTheme {
                ErrorFallback()
            }
            return
        }

        val viewModel: TahlilViewModel = viewModel(factory = factory)
        // collectAsStateWithLifecycle: stops collecting the StateFlow when the
        // Composable isn't at least STARTED — saves battery when the app is
        // backgrounded. Recommended by the jetpack-compose-performance skill
        // (Category 5: Effects, lifecycle, and threading) for any UI state
        // Flow collected on Android.
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        // Compute the actual dark flag from the user's preference + system.
        val systemDark = isSystemInDarkTheme()
        val isDark = when (uiState.themeMode) {
            ThemeMode.System -> systemDark
            ThemeMode.Light -> false
            ThemeMode.Dark -> true
        }

        // Push the resolved flag into the ViewModel so ArabicText can read it.
        LaunchedEffect(isDark) {
            viewModel.setDark(isDark)
        }

        // --- Keep-screen-on window flag ------------------------------------
        val window = this.window
        DisposableEffect(uiState.toggles.keepScreenOn) {
            try {
                if (uiState.toggles.keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            } catch (e: Exception) {
                Log.e(TAG, "keepScreenOn failed", e)
            }
            onDispose {
                try {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } catch (_: Exception) {}
            }
        }

        // --- Fullscreen (immersive) + system bar appearance ----------------
        val view = LocalView.current
        DisposableEffect(uiState.isFullscreen, isDark) {
            try {
                val controller = WindowInsetsControllerCompat(window, view)
                if (uiState.isFullscreen) {
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    controller.show(WindowInsetsCompat.Type.systemBars())
                }
                // Light/dark status bar icons based on theme
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            } catch (e: Exception) {
                Log.e(TAG, "WindowInsets failed", e)
            }
            onDispose {
                try {
                    val controller = WindowInsetsControllerCompat(window, view)
                    controller.show(WindowInsetsCompat.Type.systemBars())
                } catch (_: Exception) {}
            }
        }

        // --- Window background follows the RESOLVED in-app theme -----------
        // The XML theme (values-night) picks its window background from the
        // SYSTEM dark mode, but the app manages light/dark in-app
        // (ThemeMode). With system=dark + app=light the old window
        // background stayed near-black, so every gap between the light
        // cards showed a dark "hole". Painting the resolved theme color on
        // the DecorView (and a full-size root Box below) makes the whole
        // window — status-bar area included — match the active theme in
        // every combination of system vs. in-app mode.
        DisposableEffect(isDark) {
            try {
                window.setBackgroundDrawable(
                    android.graphics.drawable.ColorDrawable(
                        (if (isDark) DarkBackground else LightBackground).toArgb()
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "window background sync failed", e)
            }
            onDispose { }
        }

        TahlilDoaTheme(themeMode = uiState.themeMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                TahlilNavigation(
                    toggles = uiState.toggles,
                    style = uiState.style,
                    themeMode = uiState.themeMode,
                    isDark = isDark,
                    isFullscreen = uiState.isFullscreen,
                    lastReadSectionId = uiState.lastReadSectionId,
                    onTogglesChange = { newToggles -> viewModel.updateToggles { newToggles } },
                    onStyleChange = { newStyle -> viewModel.updateStyle { newStyle } },
                    onThemeModeChange = { mode -> viewModel.setThemeMode(mode) },
                    onToggleThemeClick = { viewModel.toggleTheme() },
                    onToggleFullscreen = { viewModel.toggleFullscreen() },
                    onSetLastReadSection = { id -> viewModel.setLastReadSectionId(id) },
                    onClearLastReadSection = { viewModel.setLastReadSectionId(null) },
                )
            }
        }
    }

    /**
     * Minimal fallback UI shown when the ViewModel (and therefore the
     * settings repository) cannot be constructed — renders instead of a
     * force-close so the user sees a readable message.
     */
    @Composable
    private fun ErrorFallback() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Terjadi kesalahan saat memuat aplikasi.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Silakan tutup dan buka kembali aplikasi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
