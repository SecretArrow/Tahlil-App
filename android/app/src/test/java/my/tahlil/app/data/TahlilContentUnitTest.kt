package my.tahlil.app.data

import my.tahlil.app.data.HeroInfo
import my.tahlil.app.data.SectionType
import my.tahlil.app.data.TahlilContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM unit tests for the embedded Tahlil content table.
 *
 * These run on the local JVM (no emulator needed) via `./gradlew test`,
 * so they are FAST. They are the primary content-shape regression net —
 * any structural drift in the data file (section count, missing block,
 * missing Arabic / Latin / Indonesian field, broken numbering, duplicate
 * IDs, malformed repeat counts) fails here before the slower instrumented
 * tests even spin up an emulator.
 *
 * The instrumented tests in `androidTest/` re-verify the same content
 * runs in a real Android runtime (catching ProGuard / DataStore issues
 * that JVM tests can't), so this file intentionally does NOT duplicate
 * the AndroidRuntime-specific assertions.
 */
class TahlilContentUnitTest {

    @Test
    fun sections_areLoaded() {
        assertTrue(
            "sections list should not be empty",
            TahlilContent.sections.isNotEmpty(),
        )
    }

    @Test
    fun hasThirtyTwoSections() {
        assertEquals(32, TahlilContent.sections.size)
    }

    @Test
    fun firstSection_isPembukaBasmalahSyahadat() {
        val first = TahlilContent.sections.first()
        assertEquals("pembuka-basmalah-syahadat", first.id)
        assertEquals(1, first.number)
        assertEquals("Pembuka — Basmalah dan Syahadat", first.title)
        assertEquals(SectionType.Pembukaan, first.type)
    }

    @Test
    fun lastSection_isSalamPenutup() {
        val last = TahlilContent.sections.last()
        assertEquals("salam-penutup", last.id)
        assertEquals(32, last.number)
        assertEquals("Salam Penutup", last.title)
        assertEquals(SectionType.Doa, last.type)
    }

    @Test
    fun sectionNumbers_areSequential1to32() {
        TahlilContent.sections.forEachIndexed { idx, s ->
            assertEquals(
                "Section at index $idx (${s.id}) should have number ${idx + 1}",
                idx + 1,
                s.number,
            )
        }
    }

    @Test
    fun sectionIds_areUnique() {
        val ids = TahlilContent.sections.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun allSections_haveNonEmptyTitle() {
        TahlilContent.sections.forEach { s ->
            assertTrue(
                "Section ${s.id} has empty title",
                s.title.isNotBlank(),
            )
        }
    }

    @Test
    fun allSections_haveAtLeastOneBlock() {
        TahlilContent.sections.forEach { s ->
            assertTrue(
                "Section ${s.id} should have at least one block",
                s.blocks.isNotEmpty(),
            )
        }
    }

    @Test
    fun allBlocks_haveNonEmptyTransliteration() {
        TahlilContent.sections.forEach { s ->
            s.blocks.forEach { b ->
                assertTrue(
                    "Block in ${s.id} has empty transliteration",
                    b.transliteration.isNotBlank(),
                )
            }
        }
    }

    @Test
    fun allBlocks_haveNonEmptyTranslation() {
        TahlilContent.sections.forEach { s ->
            s.blocks.forEach { b ->
                assertTrue(
                    "Block in ${s.id} has empty translation",
                    b.translation.isNotBlank(),
                )
            }
        }
    }

    @Test
    fun allBlocks_haveArabicText() {
        TahlilContent.sections.forEach { s ->
            s.blocks.forEach { b ->
                assertNotNull("Block in ${s.id} should have arabic", b.arabic)
                assertTrue(
                    "Block in ${s.id} should have non-empty arabic",
                    b.arabic!!.isNotEmpty(),
                )
            }
        }
    }

    @Test
    fun repeatValues_arePositiveInts() {
        TahlilContent.sections.forEach { s ->
            s.blocks.forEach { b ->
                b.repeat?.let { r ->
                    assertTrue(
                        "Block in ${s.id} has invalid repeat=$r",
                        r > 0,
                    )
                }
            }
        }
    }

    @Test
    fun byId_returnsCorrectSection() {
        val section = TahlilContent.byId("surah-al-fatihah")
        assertNotNull(section)
        assertEquals(4, section!!.number)
        assertEquals(SectionType.Surah, section.type)
    }

    @Test
    fun byId_returnsNullForUnknownId() {
        assertNull(TahlilContent.byId("nonexistent-id"))
    }

    @Test
    fun indexOf_returnsPositionOrMinusOne() {
        assertEquals(0, TahlilContent.indexOf("pembuka-basmalah-syahadat"))
        assertEquals(31, TahlilContent.indexOf("salam-penutup"))
        assertEquals(-1, TahlilContent.indexOf("does-not-exist"))
    }

    @Test
    fun tahlilSection_containsLaIlahaIllallah() {
        val tahlil = TahlilContent.byId("tahlil-la-ilaha-illallah")
        assertNotNull(tahlil)
        val arabic = tahlil!!.blocks.first().arabic
        assertNotNull(arabic)
        assertTrue(
            "Tahlil section must contain 'la ilaha illallah' in Arabic",
            arabic!!.contains("لَا إِلٰهَ إِلَّا اللّٰهُ"),
        )
        // The Tahlil is typically repeated ~100 times.
        assertEquals(
            "Tahlil section should be repeated 100 times",
            100,
            tahlil.blocks.first().repeat,
        )
    }

    @Test
    fun irhamnaSection_isRepeated7Times() {
        val irhamna = TahlilContent.byId("irhamna-ya-arhamar-rahimin")
        assertNotNull(irhamna)
        assertEquals(
            "Irhamna should be repeated 7 times",
            7,
            irhamna!!.blocks.first().repeat,
        )
    }

    @Test
    fun alIkhlasSection_isRepeated3Times() {
        val ikhlas = TahlilContent.byId("surah-al-ikhlas")
        assertNotNull(ikhlas)
        assertEquals(
            "Al-Ikhlas should be repeated 3 times",
            3,
            ikhlas!!.blocks.first().repeat,
        )
    }

    @Test
    fun sectionTypes_areValidEnumValues() {
        TahlilContent.sections.forEach { s ->
            assertNotNull(
                "Section ${s.id} has invalid type",
                s.type,
            )
            // Enum is exhaustive — no need to assert value set.
        }
    }

    @Test
    fun heroInfo_hasNonEmptyFields() {
        assertTrue(HeroInfo.title.isNotBlank())
        // subtitle is intentionally allowed to be empty — the hero can
        // work with just a title + bismillah + description (no subtitle).
        assertTrue(HeroInfo.description.isNotBlank())
        assertTrue(HeroInfo.arabicBismillah.isNotBlank())
        assertTrue(HeroInfo.source.isNotBlank())
    }

    @Test
    fun heroInfo_arabicBismillah_isArabicText() {
        // Bismillah starts with the Arabic letter 'ب' (U+0628).
        assertTrue(
            "Hero bismillah should be Arabic text",
            HeroInfo.arabicBismillah.contains("\u0628"),
        )
    }

    @Test
    fun alFatihah_isReferencedFromMultipleSections() {
        // Section 4 is the main Al-Fatihah; section 26 references it
        // as the closing Fatihah before the final doa.
        val s4 = TahlilContent.byId("surah-al-fatihah")
        val s26 = TahlilContent.byId("al-fatihah-pengantar-doa-penutup")
        assertNotNull(s4)
        assertNotNull(s26)
        // The pengantar doa section's 2nd block should reproduce the
        // full Al-Fatihah text (mirrors the docx).
        assertTrue(
            "Pengantar doa section should carry the Al-Fatihah text",
            s26!!.blocks.size >= 2,
        )
        assertEquals(
            "Al-Fatihah should appear as both section 4 and inside section 26",
            s4!!.blocks.first().arabic,
            s26.blocks[1].arabic,
        )
    }

    @Test
    fun noSajdahSymbolRemainsInArabicText() {
        // U+06E9 (۩) was historically a sajdah mark; the modern content
        // uses plain separators. This guards against accidental reintroduction.
        TahlilContent.sections.forEach { s ->
            s.blocks.forEach { b ->
                b.arabic?.let { a ->
                    assertFalse(
                        "Block in ${s.id} still contains U+06E9 (۩)",
                        a.contains("\u06E9"),
                    )
                }
            }
        }
    }

    @Test
    fun noPlaceholderTextRemainsInContent() {
        // Guards against accidental `TODO`, `XXX`, `FIXME`, `[...]` placeholders
        // left in the user-facing content (a real risk when content is
        // generated by a script).
        val forbidden = listOf("TODO", "FIXME", "XXX", "[...]", "【", "〚")
        TahlilContent.sections.forEach { s ->
            forbidden.forEach { token ->
                assertFalse(
                    "Section ${s.id} title contains '$token'",
                    s.title.contains(token),
                )
                s.subtitle?.let { sub ->
                    assertFalse(
                        "Section ${s.id} subtitle contains '$token'",
                        sub.contains(token),
                    )
                }
                s.blocks.forEach { b ->
                    b.translation.split("\n").forEach { line ->
                        assertFalse(
                            "Section ${s.id} translation contains '$token'",
                            line.contains(token),
                        )
                    }
                }
            }
        }
    }
}
