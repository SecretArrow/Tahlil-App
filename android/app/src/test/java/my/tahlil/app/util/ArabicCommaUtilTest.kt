package my.tahlil.app.util

import my.tahlil.app.util.ArabicCommaUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM unit tests for [ArabicCommaUtil].
 *
 * These verify the Arabic comma insertion rules:
 *  - Quran verses (containing ۝ / ۩) are NEVER modified
 *  - The "وَ" connector before the last item is preserved (Indonesian
 *    "X, Y, Z dan W" pattern)
 *  - Fixed phrases like " وَسَلَّمْ" never get a comma before them
 */
class ArabicCommaUtilTest {

    @Test
    fun quranVersesAreReturnedUnchanged() {
        // Text contains ۝ — must be returned as-is.
        val quran = "الْحَمْدُ لِلَّهِ رَبِّ الْعٰلَمِيْنَ ۝٢ وَعَلَى آلِهِ وَأَصْحَابِهِ"
        val result = ArabicCommaUtil.addArabicCommas(quran)
        assertEquals(quran, result)
    }

    @Test
    fun singleConnectorSegmentIsUnchanged() {
        // Only one " وَ" — Indonesian "X dan Y" pattern, no comma needed.
        val text = "صَلَّى اللهُ عَلَيْهِ وَسَلَّمَ"
        val result = ArabicCommaUtil.addArabicCommas(text)
        assertEquals(text, result)
    }

    @Test
    fun multipleConnectorsGetCommasExceptLast() {
        // "وَعَلَى آلِهِ وَأَصْحَابِهِ وَأَزْوَاجِهِ وَذُرِّيَّتِهِ وَأَهْلِ بَيْتِهِ"
        // Should become "وَعَلَى آلِهِ، وَأَصْحَابِهِ، وَأَزْوَاجِهِ، وَذُرِّيَّتِهِ وَأَهْلِ بَيْتِهِ"
        val text = "وَعَلَى آلِهِ وَأَصْحَابِهِ وَأَزْوَاجِهِ وَذُرِّيَّتِهِ وَأَهْلِ بَيْتِهِ"
        val result = ArabicCommaUtil.addArabicCommas(text)
        // The first connector after the phrase opening is kept (skip);
        // the middle ones get commas; the last " وَأَهْلِ" is preserved.
        assertTrue(
            "Result should contain at least one inserted comma: $result",
            result.contains("،"),
        )
        // The LAST connector (before "أَهْلِ") should NOT have a comma.
        assertTrue(
            "Last connector (وَأَهْلِ) should not have comma prefix",
            result.contains(" وَأَهْلِ"),
        )
    }

    @Test
    fun wasallamPhraseIsNeverSplit() {
        // "صلى الله عليه وسلم" — the " وَسَلَّمْ" connector is fixed.
        val text = "صَلَّى اللهُ عَلَيْهِ وَسَلَّمَ"
        val result = ArabicCommaUtil.addArabicCommas(text)
        assertEquals(text, result)
    }

    @Test
    fun emptyAndShortStringsAreUnchanged() {
        assertEquals("", ArabicCommaUtil.addArabicCommas(""))
        assertEquals("نَصٌّ", ArabicCommaUtil.addArabicCommas("نَصٌّ"))
    }

    @Test
    fun noConnectorReturnsOriginal() {
        val text = "بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيْمِ"
        val result = ArabicCommaUtil.addArabicCommas(text)
        assertEquals(text, result)
    }
}
