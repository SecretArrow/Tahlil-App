package my.tahlil.app.util

import my.tahlil.app.util.HamzaWashalUtil.HamzaWashalRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM unit tests for [HamzaWashalUtil] — mirror ALL 26 cases of the
 * web validation script (`scripts/test-hamza-washal.mjs`, all PASS there)
 * so the Kotlin port is proven rule-identical to
 * `web/src/lib/hamza-washal.ts`. Arabic literals are byte-identical to
 * the mjs script (same code points, harakat included).
 *
 * Like the mjs `test()` helper, every case asserts:
 *  - the NUMBER of ranges,
 *  - each range expanded to its full word token (whitespace-delimited) and
 *    in the same order — i.e. ranges sit on the right words/positions,
 *  - the colored slice (start/end indexes) is exactly the alif cluster.
 */
class HamzaWashalUtilTest {

    /** Expand a range to its full word token (whitespace-delimited). */
    private fun wordOf(text: String, r: HamzaWashalRange): String {
        var s = r.start
        var e = r.end
        while (s > 0 && !text[s - 1].isWhitespace()) s--
        while (e < text.length && !text[e].isWhitespace()) e++
        return text.substring(s, e)
    }

    /** The slice that actually gets colored (must be the alif cluster). */
    private fun sliceOf(text: String, r: HamzaWashalRange): String =
        text.substring(r.start, r.end)

    /**
     * Every colored slice must be the alif cluster itself — a bare ا
     * (optionally with plain harakat) or the dedicated ٱ. Mirrors the mjs
     * check `/^ا[ً-ْ]*$|^ٱ$/`.
     */
    private fun isAlifSlice(slice: String): Boolean {
        if (slice == "ٱ") return true
        if (!slice.startsWith("ا")) return false
        return slice.drop(1).all { it in 'ً'..'ْ' }
    }

    /** Mirror of the mjs `test()` helper: words, order, slices, bounds. */
    private fun expectWords(text: String, vararg expectedWords: String) {
        val ranges = HamzaWashalUtil.analyzeHamzaWashal(text)
        val got = ranges.map { wordOf(text, it) }
        assertEquals("words for \"$text\"", expectedWords.toList(), got)
        for (r in ranges) {
            val slice = sliceOf(text, r)
            assertTrue("slice \"$slice\" must be the alif cluster for \"$text\"", isAlifSlice(slice))
            assertTrue("range $r out of bounds for \"$text\"", r.start >= 0 && r.end <= text.length && r.start < r.end)
        }
    }

    // ── 26 mirrored cases (scripts/test-hamza-washal.mjs) ─────────────────

    @Test
    // 1. Bismillah — 3 article words colored, بِسْمِ has no alif.
    fun bismillah() {
        expectWords("بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ", "اللّٰهِ", "الرَّحْمٰنِ", "الرَّحِيْمِ")
    }

    @Test
    // 2. Ayah-initial article = ibtida' → NOT colored.
    fun ayahInitialArticleIbtida() {
        expectWords("الْحَمْدُ لِلّٰهِ رَبِّ الْعٰلَمِيْنَ", "الْعٰلَمِيْنَ")
    }

    @Test
    // 3. Prefix wasl is dropped even when the unit STARTS there.
    fun prefixWalAsrFirstWord() {
        expectWords("وَالْعَصْرِ", "وَالْعَصْرِ")
    }

    @Test
    // 3. Prefix wasl is dropped even when the unit STARTS there.
    fun prefixFaLamFirstWord() {
        expectWords("فَاعْلَمْ أَنَّهُ", "فَاعْلَمْ")
    }

    @Test
    // 4. Qatha' أ never colored; article words after a preceding word colored.
    fun syahadatQathaVsWasl() {
        expectWords("أَشْهَدُ أَنْ لَا إِلٰهَ إِلَّا اللّٰهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا رَسُوْلُ اللّٰهِ", "اللّٰهُ", "اللّٰهِ")
    }

    @Test
    // 5. First word Allahumma = ibtida' → nothing colored.
    fun allahummaIbtida() {
        expectWords("اَللّٰهُمَّ صَلِّ عَلٰى مُحَمَّدٍ")
    }

    @Test
    // 6. Fatihah 6 — verb wasl first word normal, article words colored.
    fun ihdinaClause() {
        expectWords("اِهْدِنَا الصِّرَاطَ الْمُسْتَقِيْمَ،", "الصِّرَاطَ", "الْمُسْتَقِيْمَ،")
    }

    @Test
    // 7. Verb wasl (alif+sukun) mid-text.
    fun verbWaslMid() {
        expectWords("رَبَّنَا اغْفِرْ لِنَا", "اغْفِرْ")
    }

    @Test
    // 7. Verb wasl (alif+sukun) mid-text.
    fun verbWaslWithPrefix() {
        expectWords("وَاغْفِرْ لِنَا", "وَاغْفِرْ")
    }

    @Test
    // 7. Verb wasl (alif+sukun) mid-text.
    fun verbWaslIrhamnaMid() {
        expectWords("رَبَّنَا اِرْحَمْنَا اَللّٰهُمَّ", "اِرْحَمْنَا", "اَللّٰهُمَّ")
    }

    @Test
    // 8. Qatha' written as alif+dagger (اٰ) in this app's rasm → excluded.
    fun daggerAlifQatha() {
        expectWords("اٰمَنَ الرَّسُوْلُ", "الرَّسُوْلُ")
    }

    @Test
    // 8. Qatha' written as alif+dagger (اٰ) in this app's rasm → excluded.
    fun daggerAlifQathaAtina() {
        expectWords("رَبَّنَا اٰتِنَا فِي الدُّنْيَا", "الدُّنْيَا")
    }

    @Test
    // 9. Quranic letter initials — alif recited as letter name.
    fun quranicInitialAlM() {
        expectWords("الۤمّۤ")
    }

    @Test
    // 10. Leading whitespace/punctuation must not shift the ibtida' word.
    fun leadingWhitespace() {
        expectWords(" اِهْدِنَا الصِّرَاطَ", "الصِّرَاطَ")
    }

    @Test
    // 10. Leading whitespace/punctuation must not shift the ibtida' word.
    fun leadingDelimiter() {
        expectWords("، اِهْدِنَا الصِّرَاطَ", "الصِّرَاطَ")
    }

    @Test
    // 11. No false candidates: لَا / كَمَا / فِي / قَالُوا.
    fun laNegation() {
        expectWords("لَا")
    }

    @Test
    // 11. No false candidates: لَا / كَمَا / فِي / قَالُوا.
    fun kama() {
        expectWords("كَمَا الَّذِيْنَ", "الَّذِيْنَ")
    }

    @Test
    // 11. No false candidates: لَا / كَمَا / فِي / قَالُوا.
    fun fi() {
        expectWords("فِي الدُّنْيَا", "الدُّنْيَا")
    }

    @Test
    // 11. No false candidates: لَا / كَمَا / فِي / قَالُوا.
    fun qaluLongVowels() {
        expectWords("قَالُوا اَللّٰهُمَّ", "اَللّٰهُمَّ")
    }

    @Test
    // 12. Multi-ayah chunk (each verse keeps ibtida' at its own first word).
    fun twoVerseChunks() {
        expectWords("الْحَمْدُ لِلّٰهِ رَبِّ الْعٰلَمِيْنَ الرَّحْمٰنِ الرَّحِيْمِ", "الْعٰلَمِيْنَ", "الرَّحْمٰنِ", "الرَّحِيْمِ")
    }

    @Test
    // 13. Word attached to trailing delimiter still detected.
    fun trailingDelimiter() {
        expectWords("بِسْمِ اللّٰهِ،", "اللّٰهِ،")
    }

    @Test
    // 14. Dedicated ALEF WASLA character (definitive wasl).
    fun alefWaslaCharMid() {
        expectWords("بِسْمِ ٱللّٰهِ", "ٱللّٰهِ")
    }

    @Test
    // 15. Mid-text prefix article.
    fun prefixMidText() {
        expectWords("صَلِّ وَالْمُؤْمِنِيْنَ", "وَالْمُؤْمِنِيْنَ")
    }

    @Test
    // 16. Empty / no-alif text.
    fun emptyText() {
        expectWords("")
    }

    @Test
    // 16. Empty / no-alif text.
    fun noAlifWords() {
        expectWords("مُحَمَّدٌ رَسُوْلُ")
    }

    @Test
    // 17. Assimilated sun-article (shadda letter after alif, Uthmani rasm).
    fun sunArticleViaShaddaFollow() {
        expectWords("يَوْمَ اَلْاٰخِرَةِ", "اَلْاٰخِرَةِ")
    }

    // ── Extra data checks (real content text) ─────────────────────────────

    @Test
    fun emptyText_returnsEmptyList() {
        assertTrue(HamzaWashalUtil.analyzeHamzaWashal("").isEmpty())
        assertEquals(0, HamzaWashalUtil.analyzeHamzaWashal("").size)
    }

    @Test
    fun realData_bismillah_threeRanges_correctPositions() {
        val text = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ"
        val ranges = HamzaWashalUtil.analyzeHamzaWashal(text)
        assertEquals(3, ranges.size)
        // The three article words, in reading order.
        assertEquals("اللّٰهِ", wordOf(text, ranges[0]))
        assertEquals("الرَّحْمٰنِ", wordOf(text, ranges[1]))
        assertEquals("الرَّحِيْمِ", wordOf(text, ranges[2]))
        // NO range on بِسْمِ — every range sits after the first word.
        val endOfBismi = text.indexOf(' ')
        assertTrue(ranges.all { it.start > endOfBismi })
        // Exact alif positions (range start = the alif of each word) and
        // bare-alif slices (only the alif cluster gets colored).
        assertEquals(text.indexOf("اللّٰهِ"), ranges[0].start)
        assertEquals(text.indexOf("الرَّحْمٰنِ"), ranges[1].start)
        assertEquals(text.indexOf("الرَّحِيْمِ"), ranges[2].start)
        ranges.forEach { assertEquals("ا", sliceOf(text, it)) }
    }

    @Test
    fun realData_alhamdulillah_singleRangeOnAlAlamin() {
        val text = "الْحَمْدُ لِلّٰهِ رَبِّ الْعٰلَمِيْنَ"
        val ranges = HamzaWashalUtil.analyzeHamzaWashal(text)
        assertEquals(1, ranges.size)
        // الْعٰلَمِيْنَ ONLY — الْحَمْدُ is the ibtida' first word, and
        // لِلّٰهِ / رَبِّ carry no wasl alif at all.
        assertEquals("الْعٰلَمِيْنَ", wordOf(text, ranges[0]))
        assertEquals(text.indexOf("الْعٰلَمِيْنَ"), ranges[0].start)
        assertEquals("ا", sliceOf(text, ranges[0]))
        assertTrue(ranges[0].start > text.indexOf("رَبِّ"))
    }

    // ── Preview / legend metadata ─────────────────────────────────────────

    @Test
    fun previewText_exercisesIbtidaAndWashal() {
        assertEquals("اِهْدِنَا الصِّرَاطَ الْمُسْتَقِيْمَ،", HamzaWashalUtil.PREVIEW_TEXT)
        val text = HamzaWashalUtil.PREVIEW_TEXT
        val ranges = HamzaWashalUtil.analyzeHamzaWashal(text)
        assertEquals(2, ranges.size)
        // اِهْدِنَا (first word = ibtida') stays normal; the two article
        // words after it are colored.
        assertEquals("الصِّرَاطَ", wordOf(text, ranges[0]))
        assertEquals("الْمُسْتَقِيْمَ،", wordOf(text, ranges[1]))
        assertTrue(ranges.all { it.start > text.indexOf(' ') })
    }

    @Test
    fun meta_matchesWebLegend() {
        assertEquals("Hamzatul Washal", HamzaWashalUtil.META.name)
        assertEquals(
            "alif wasal tidak dibaca saat menyambung bacaan (aktif bersama Tajwid)",
            HamzaWashalUtil.META.desc,
        )
        assertEquals(0xFF7893B0L, HamzaWashalUtil.META.colorLight)
        assertEquals(0xFFA8BED4L, HamzaWashalUtil.META.colorDark)
    }

    // ── Text integrity ────────────────────────────────────────────────────

    @Test
    fun rangesSortedNonOverlapping_stayInBounds() {
        val text = "أَشْهَدُ أَنْ لَا إِلٰهَ إِلَّا اللّٰهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا رَسُوْلُ اللّٰهِ"
        val ranges: List<HamzaWashalRange> = HamzaWashalUtil.analyzeHamzaWashal(text)
        assertEquals(2, ranges.size)
        // Sorted by start and non-overlapping.
        assertEquals(ranges.map { it.start }.sorted(), ranges.map { it.start })
        for (i in 1 until ranges.size) {
            assertTrue(ranges[i - 1].end <= ranges[i].start)
        }
        for (r in ranges) {
            assertTrue(r.start >= 0)
            assertTrue(r.end <= text.length)
            assertTrue(r.start < r.end)
        }
    }
}
