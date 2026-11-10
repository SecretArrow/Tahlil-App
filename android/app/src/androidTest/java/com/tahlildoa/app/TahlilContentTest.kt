package com.tahlildoa.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tahlildoa.app.data.TahlilContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sanity checks for the embedded content table.
 *
 * These tests guard against silent regressions where the lazy
 * `TahlilContent.sections` initialization fails (e.g. due to a
 * ProGuard-stripped `@Serializable` companion) — which would
 * result in an empty list at runtime and a blank home screen.
 */
@RunWith(AndroidJUnit4::class)
class TahlilContentTest {

    @Test
    fun allElevenSectionsAreLoaded() {
        assertEquals("Should have 11 sections", 11, TahlilContent.sections.size)
    }

    @Test
    fun firstSectionIsPembukaan() {
        val first = TahlilContent.sections.first()
        assertEquals("pembukaan", first.id)
        assertEquals(1, first.number)
        assertEquals("Pembukaan", first.title)
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
        val section = TahlilContent.byId("tahlil-ahli-kubur")
        assertNotNull(section)
        assertEquals(3, section!!.number)
    }

    @Test
    fun byIdReturnsNullForUnknownId() {
        val section = TahlilContent.byId("nonexistent")
        assertEquals(null, section)
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

    /**
     * v1.2.4 unified سيدنا to the kasra form (سَيِّدِنَا) everywhere,
     * matching Indonesian convention and the Latin transliteration
     * "Sayyidinaa". The damma variant (سَيِّدُنَا) must not return.
     */
    @Test
    fun sayyidinaaIsAlwaysSpelledWithKasra() {
        val dammaVariant = "سَيِّدُنَا"
        TahlilContent.sections.forEach { section ->
            section.blocks.forEach { block ->
                block.arabic?.let { arabic ->
                    assertTrue(
                        "Block in ${section.id} uses the damma sayyidinaa variant",
                        !arabic.contains(dammaVariant),
                    )
                }
            }
        }
    }
}
