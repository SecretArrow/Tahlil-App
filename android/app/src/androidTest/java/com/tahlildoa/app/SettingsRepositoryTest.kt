package com.tahlildoa.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tahlildoa.app.data.SettingsRepository
import com.tahlildoa.app.data.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for [SettingsRepository].
 *
 * These run on a real device or emulator so they exercise the
 * actual DataStore Preferences implementation — including its
 * on-disk file. This is the layer most likely to fail silently
 * when ProGuard strips DataStore internals.
 *
 * Note: these tests share the application's DataStore file. We
 * accept the side effects because the test runner creates a fresh
 * test instance of the app on each run.
 */
@RunWith(AndroidJUnit4::class)
class SettingsRepositoryTest {

    private lateinit var repo: SettingsRepository

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        repo = SettingsRepository(context)
    }

    @After
    fun tearDown() {
        // Best-effort clean-up: reset the toggles we mutated so the
        // next test starts from defaults.
        runBlocking {
            try {
                repo.updateDisplayToggles(repo.displayToggles.first().copy(keepScreenOn = false))
                repo.setLastReadSectionId(null)
            } catch (_: Throwable) {
                // Swallow — clean-up is best-effort.
            }
        }
    }

    @Test
    fun defaultDisplayTogglesAreCorrect() = runBlocking {
        val toggles = repo.displayToggles.first()
        assertEquals(true, toggles.arabic)
        assertEquals(true, toggles.transliteration)
        assertEquals(true, toggles.translation)
        assertEquals(false, toggles.punctuateArabic)
        assertEquals(true, toggles.splitArabicLines)
        assertEquals(false, toggles.colorizeArabic)
        assertEquals(false, toggles.keepScreenOn)
    }

    @Test
    fun defaultThemeIsSystem() = runBlocking {
        val mode = repo.themeMode.first()
        assertEquals(ThemeMode.System, mode)
    }

    @Test
    fun updatingDisplayTogglesPersists() = runBlocking {
        val updated = repo.displayToggles.first().copy(keepScreenOn = true)
        repo.updateDisplayToggles(updated)
        val reloaded = repo.displayToggles.first()
        assertEquals(true, reloaded.keepScreenOn)
    }

    @Test
    fun lastReadSectionIdPersistsAndClears() = runBlocking {
        repo.setLastReadSectionId("tahlil-ahli-kubur")
        assertEquals("tahlil-ahli-kubur", repo.lastReadSectionId.first())

        repo.setLastReadSectionId(null)
        assertEquals(null, repo.lastReadSectionId.first())
    }
}
