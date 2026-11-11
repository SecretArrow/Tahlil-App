package my.tahlil.app.util

import my.tahlil.app.util.AyahMarkUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM unit tests for [AyahMarkUtil].
 *
 * These run on the local JVM (no emulator) and verify the core text
 * parsing logic for ۝X ayah marks.
 */
class AyahMarkUtilTest {

    @Test
    fun findMarks_returnsEmptyForPlainText() {
        val marks = AyahMarkUtil.findMarks("بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيْمِ")
        assertTrue("Should find no marks in plain text", marks.isEmpty())
    }

    @Test
    fun findMarks_detectsSingleAyahMark() {
        // Al-Fatihah ayah 1 — ayah mark ۝١
        val text = "بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيْمِ ۝١"
        val marks = AyahMarkUtil.findMarks(text)
        assertEquals(1, marks.size)
        assertEquals("١", marks[0].digits)
    }

    @Test
    fun findMarks_detectsMultipleAyahMarks() {
        val text = "آيَةٌ ۝١ آيَةٌ ۝٢ آيَةٌ ۝٣"
        val marks = AyahMarkUtil.findMarks(text)
        assertEquals(3, marks.size)
        assertEquals("١", marks[0].digits)
        assertEquals("٢", marks[1].digits)
        assertEquals("٣", marks[2].digits)
    }

    @Test
    fun findMarks_handlesMultiDigitNumbers() {
        // For very long surahs (e.g., Al-Baqarah has 286 ayat).
        val text = "آيَةٌ ۝٢٨٦"
        val marks = AyahMarkUtil.findMarks(text)
        assertEquals(1, marks.size)
        assertEquals("٢٨٦", marks[0].digits)
    }

    @Test
    fun findMarks_handlesBareSymbolWithoutDigits() {
        val text = "آيَةٌ ۝ نِصْفُ آيَة"
        val marks = AyahMarkUtil.findMarks(text)
        assertEquals(1, marks.size)
        assertEquals("", marks[0].digits)
    }

    @Test
    fun splitByAyahMarks_returnsSingleLineIfNoMarks() {
        val text = "نَصٌّ بِلَا عَلَامَةِ آيَة"
        val lines = AyahMarkUtil.splitByAyahMarks(text)
        assertEquals(1, lines.size)
        assertEquals(text, lines[0].text)
        assertEquals(null, lines[0].mark)
    }

    @Test
    fun splitByAyahMarks_splitsOnEachMark() {
        val text = "آيَةٌ أُولَى ۝١ آيَةٌ ثَانِيَة ۝٢"
        val lines = AyahMarkUtil.splitByAyahMarks(text)
        assertEquals(2, lines.size)
        assertEquals("آيَةٌ أُولَى ", lines[0].text)
        assertEquals("١", lines[0].mark?.digits)
        assertEquals(" آيَةٌ ثَانِيَة", lines[1].text)
        assertEquals("٢", lines[1].mark?.digits)
    }

    @Test
    fun splitBySentence_returnsEmptyForEmpty() {
        val lines = AyahMarkUtil.splitBySentence("   ")
        assertTrue(lines.isEmpty())
    }

    @Test
    fun splitBySentence_splitsOnArabicComma() {
        val text = "جُمْلَةٌ أُولَى، جُمْلَةٌ ثَانِيَة"
        val lines = AyahMarkUtil.splitBySentence(text)
        assertEquals(2, lines.size)
        // The delimiter is preserved at the END of each line per spec.
        assertEquals("جُمْلَةٌ أُولَى،", lines[0].text)
        assertEquals(" جُمْلَةٌ ثَانِيَة", lines[1].text)
    }

    @Test
    fun splitBySentence_splitsOnPeriod() {
        val text = "First. Second. Third."
        val lines = AyahMarkUtil.splitBySentence(text)
        assertEquals(3, lines.size)
    }

    @Test
    fun digitsToLatin_convertsArabicIndicDigits() {
        assertEquals("0", AyahMarkUtil.digitsToLatin("٠"))
        assertEquals("1", AyahMarkUtil.digitsToLatin("١"))
        assertEquals("9", AyahMarkUtil.digitsToLatin("٩"))
        assertEquals("255", AyahMarkUtil.digitsToLatin("٢٥٥"))
        assertEquals("286", AyahMarkUtil.digitsToLatin("٢٨٦"))
    }

    @Test
    fun digitsToLatin_passesThroughNonDigits() {
        // Non-digit characters are preserved verbatim.
        assertEquals("abc", AyahMarkUtil.digitsToLatin("abc"))
        assertEquals("", AyahMarkUtil.digitsToLatin(""))
    }
}
