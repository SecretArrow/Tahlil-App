package my.tahlil.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import my.tahlil.app.data.TahlilContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sanity checks for the embedded content table (Tahlil Lengkap — 32 sections).
 *
 * These tests guard against silent regressions where the lazy
 * `TahlilContent.sections` initialization fails (e.g. due to a
 * ProGuard-stripped `@Serializable` companion) — which would
 * result in an empty list at runtime and a blank home screen.
 */
@RunWith(AndroidJUnit4::class)
class TahlilContentTest {

    @Test
    fun allThirtyTwoSectionsAreLoaded() {
        assertEquals("Should have 32 sections", 32, TahlilContent.sections.size)
    }

    @Test
    fun firstSectionIsPembukaBasmalahSyahadat() {
        val first = TahlilContent.sections.first()
        assertEquals("pembuka-basmalah-syahadat", first.id)
        assertEquals(1, first.number)
        assertEquals("Pembuka — Basmalah dan Syahadat", first.title)
    }

    @Test
    fun lastSectionIsSalamPenutup() {
        val last = TahlilContent.sections.last()
        assertEquals("salam-penutup", last.id)
        assertEquals(32, last.number)
        assertEquals("Salam Penutup", last.title)
    }

    @Test
    fun allSectionsHaveBlocks() {
        TahlilContent.sections.forEach { section ->
            assertTrue(
                "Section ${section.id} should have blocks",
                section.blocks.isNotEmpty(),
            )
        }
    }

    @Test
    fun allBlocksHaveArabicText() {
        TahlilContent.sections.forEach { section ->
            section.blocks.forEach { block ->
                assertNotNull("Block in ${section.id} should have arabic", block.arabic)
                assertTrue(
                    "Block in ${section.id} should have non-empty arabic",
                    block.arabic!!.isNotEmpty(),
                )
            }
        }
    }

    @Test
    fun byIdReturnsCorrectSection() {
        val section = TahlilContent.byId("tawassul-ahli-kubur")
        assertNotNull(section)
        assertEquals(7, section!!.number)
    }

    @Test
    fun byIdReturnsNullForUnknownId() {
        val section = TahlilContent.byId("nonexistent")
        assertEquals(null, section)
    }

    @Test
    fun sectionNumbersAreSequential() {
        TahlilContent.sections.forEachIndexed { idx, section ->
            assertEquals(
                "Section at index $idx should have number ${idx + 1}",
                idx + 1,
                section.number,
            )
        }
    }

    @Test
    fun sectionIdsAreUnique() {
        val ids = TahlilContent.sections.map { it.id }
        assertEquals(
            "All section ids must be unique",
            ids.size,
            ids.toSet().size,
        )
    }

    @Test
    fun tahlilSectionContainsLaIlahaIllallah() {
        val tahlilSection = TahlilContent.byId("tahlil-la-ilaha-illallah")
        assertNotNull(tahlilSection)
        val arabic = tahlilSection!!.blocks.first().arabic
        assertNotNull(arabic)
        assertTrue(
            "Tahlil section should contain 'la ilaha illallah' in Arabic",
            arabic!!.contains("لَا إِلٰهَ إِلَّا اللّٰهُ"),
        )
    }

    // ------------------------------------------------------------------
    // Content-parity regression guards (web source of truth:
    // web/src/data/tahlil-content.ts — synced by scripts/sync-android-content.ts).
    // ------------------------------------------------------------------

    /**
     * v1.2.5 replaced the sajdah ornament ۩ (U+06E9) with plain periods in
     * the web content. The Android table must never reintroduce it.
     */
    @Test
    fun noSajdahSymbolRemainsInArabicText() {
        TahlilContent.sections.forEach { section ->
            section.blocks.forEach { block ->
                block.arabic?.let { arabic ->
                    assertTrue(
                        "Block in ${section.id} still contains U+06E9 (۩)",
                        !arabic.contains("\u06E9"),
                    )
                }
            }
        }
    }
}
