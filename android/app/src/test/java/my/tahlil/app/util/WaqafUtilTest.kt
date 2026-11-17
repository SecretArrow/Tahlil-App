package my.tahlil.app.util

import my.tahlil.app.util.WaqafUtil.WaqafKind
import my.tahlil.app.util.WaqafUtil.WaqafRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM unit tests for [WaqafUtil] — mirror the 26 cases of the web
 * validation script (`scripts/test-waqaf.mjs`, all PASS there) so the
 * Kotlin port is proven rule-identical to `web/src/lib/waqaf.ts`.
 */
class WaqafUtilTest {

    /** Assert the text yields exactly ONE range of [kind] covering [segment]. */
    private fun expectSingle(text: String, kind: WaqafKind, segment: String) {
        val ranges = WaqafUtil.analyzeWaqaf(text)
        assertEquals("range count for \"$text\"", 1, ranges.size)
        val r = ranges[0]
        assertEquals("kind for \"$text\"", kind, r.kind)
        assertEquals("segment for \"$text\"", segment, text.substring(r.start, r.end))
    }

    /** Assert the text yields NO waqaf ranges. */
    private fun expectEmpty(text: String) {
        val ranges = WaqafUtil.analyzeWaqaf(text)
        assertTrue("expected no ranges for \"$text\", got $ranges", ranges.isEmpty())
    }

    // ── Mad Aridh Lissukun ────────────────────────────────────────────────

    @Test
    fun aridh_qaluwa_chainOfTwoSeats() {
        expectSingle("قَالُوا", WaqafKind.MadAridhLissukun, "لُوا")
    }

    @Test
    fun aridh_fi_selfMarkedYa() {
        expectSingle("فِي", WaqafKind.MadAridhLissukun, "فِي")
    }

    @Test
    fun aridh_isa_daggerAlef() {
        expectSingle("عِيسَىٰ", WaqafKind.MadAridhLissukun, "ىٰ")
    }

    @Test
    fun aridh_kama_singleSeat() {
        expectSingle("كَمَا", WaqafKind.MadAridhLissukun, "مَا")
    }

    @Test
    fun aridh_dunya_afterSukunAnchor() {
        expectSingle("دُنْيَا", WaqafKind.MadAridhLissukun, "يَا")
    }

    @Test
    fun aridh_hatta_shaddaAnchor() {
        expectSingle("حَتَّى", WaqafKind.MadAridhLissukun, "تَّى")
    }

    @Test
    fun aridh_yad_uw() {
        expectSingle("يَدْعُو", WaqafKind.MadAridhLissukun, "عُو")
    }

    @Test
    fun aridh_ala() {
        expectSingle("عَلَى", WaqafKind.MadAridhLissukun, "لَى")
    }

    // ── Mad Iwadh ─────────────────────────────────────────────────────────

    @Test
    fun iwadh_hudan() {
        expectSingle("هُدًى", WaqafKind.MadIwadh, "دًى")
    }

    @Test
    fun iwadh_hanifan_seatAlif() {
        expectSingle("حَنِيفًا", WaqafKind.MadIwadh, "فًا")
    }

    @Test
    fun iwadh_shayin_hamza() {
        expectSingle("شَيْءً", WaqafKind.MadIwadh, "ءً")
    }

    // ── Mad Iwadh EXCEPTION: ta marbutah reads sukun (iskan), not alif ────

    @Test
    fun iwadhException_laanatan_isTaMarbutah() {
        expectSingle("لَعَنَةً", WaqafKind.Iskan, "ةً")
    }

    // ── Waqaf bil Iskan ───────────────────────────────────────────────────

    @Test
    fun iskan_khalaqa() {
        expectSingle("خَلَقَ", WaqafKind.Iskan, "قَ")
    }

    @Test
    fun iskan_tanwinDamm_muhammadun() {
        expectSingle("مُحَمَّدٌ", WaqafKind.Iskan, "دٌ")
    }

    @Test
    fun iskan_tanwinKasr_kitabin() {
        expectSingle("كِتَابٍ", WaqafKind.Iskan, "بٍ")
    }

    @Test
    fun iskan_shadda_thumma() {
        expectSingle("ثُمَّ", WaqafKind.Iskan, "مَّ")
    }

    @Test
    fun iskan_allahi() {
        expectSingle("اللَّهِ", WaqafKind.Iskan, "هِ")
    }

    // ── NO highlight (no reading change on stop) ──────────────────────────

    @Test
    fun skip_alayhim_sukunFinal() {
        expectEmpty("عَلَيْهِمْ")
    }

    @Test
    fun skip_aw_diphthong() {
        expectEmpty("أَوْ")
    }

    @Test
    fun skip_min_sukunFinal() {
        expectEmpty("مِنْ")
    }

    @Test
    fun skip_emptyText() {
        expectEmpty("")
    }

    // ── Multi-clause text (the renderer's per-line strings) ───────────────

    @Test
    fun multi_threeClauses_threeKinds() {
        val text = "قَالُوا، هُدًى، مُحَمَّدٌ رَسُوْلُ اللَّهِ،"
        val ranges = WaqafUtil.analyzeWaqaf(text)
        assertEquals(3, ranges.size)

        val (r1, r2, r3) = ranges
        assertEquals(WaqafKind.MadAridhLissukun, r1.kind)
        assertEquals("لُوا", text.substring(r1.start, r1.end))
        assertEquals(WaqafKind.MadIwadh, r2.kind)
        assertEquals("دًى", text.substring(r2.start, r2.end))
        assertEquals(WaqafKind.Iskan, r3.kind)
        assertEquals("هِ", text.substring(r3.start, r3.end))
    }

    @Test
    fun trailingDelimiter_dedupeByStart() {
        // "خَلَقَ،" — the word before the ، AND the final word are the same
        // word: dedupe by start index must report it exactly once.
        expectSingle("خَلَقَ،", WaqafKind.Iskan, "قَ")
    }

    @Test
    fun fullClause_onlyFinalWordColored() {
        // Mid-clause words (مِنْ / شَرِّ / مَا) are NOT stop words — only the
        // word before the delimiter produces a range.
        expectSingle("مِنْ شَرِّ مَا خَلَقَ،", WaqafKind.Iskan, "قَ")
    }

    @Test
    fun sukunFinalClause_uncolored() {
        expectEmpty("أَنْعَمْتَ عَلَيْهِمْ،")
    }

    @Test
    fun skip_latinText() {
        expectEmpty("Bismillah")
    }

    // ── Metadata used by the settings legend UI ───────────────────────────

    @Test
    fun rulesMeta_matchesWebLegend() {
        assertEquals(3, WaqafUtil.RULES_META.size)
        val meta = WaqafUtil.RULES_META
        assertEquals(WaqafKind.Iskan, meta[0].kind)
        assertEquals("Waqaf bil Iskan", meta[0].name)
        assertEquals(
            "harokat/tanwin akhir kata diganti sukun saat berhenti",
            meta[0].desc,
        )
        assertEquals(WaqafKind.MadAridhLissukun, meta[1].kind)
        assertEquals("Mad Aridh Lissukun", meta[1].name)
        assertEquals(
            "huruf mad akhir kata memanjang 4–6 harakat saat berhenti",
            meta[1].desc,
        )
        assertEquals(WaqafKind.MadIwadh, meta[2].kind)
        assertEquals("Mad Iwadh", meta[2].name)
        assertEquals(
            "tanwin fath dibaca alif (2 harakat) saat berhenti",
            meta[2].desc,
        )
    }

    @Test
    fun previewText_isThreeClauses() {
        assertEquals("قَالُوا، هُدًى، مُحَمَّدٌ رَسُوْلُ اللَّهِ،", WaqafUtil.PREVIEW_WAQAF_TEXT)
        // The preview itself must yield exactly the three kaidah ranges.
        assertEquals(3, WaqafUtil.analyzeWaqaf(WaqafUtil.PREVIEW_WAQAF_TEXT).size)
    }

    // ── Text integrity ────────────────────────────────────────────────────

    @Test
    fun rangesNeverMutateText_andStayInBounds() {
        val text = "قَالُوا، هُدًى، مُحَمَّدٌ رَسُوْلُ اللَّهِ،"
        val ranges: List<WaqafRange> = WaqafUtil.analyzeWaqaf(text)
        for (r in ranges) {
            assertTrue(r.start >= 0)
            assertTrue(r.end <= text.length)
            assertTrue(r.start < r.end)
        }
        // Ranges are sorted by start.
        assertEquals(
            ranges.map { it.start }.sorted(),
            ranges.map { it.start },
        )
    }
}
