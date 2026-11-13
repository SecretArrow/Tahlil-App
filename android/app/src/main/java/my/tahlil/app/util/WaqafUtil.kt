package my.tahlil.app.util

/**
 * Waqaf engine — detects letters whose READING CHANGES when the reciter
 * stops (waqaf) at a natural pause point, so the renderer can highlight
 * them in blue (#1B51D0).
 *
 * Rule-for-rule port of `src/lib/waqaf.ts` (web) — companion to
 * [TajwidUtil] (the 7-color tajwid engine). Waqaf coloring is a SEPARATE
 * feature with its own display toggle; when both are active, tajwid colors
 * keep their segments and the waqaf blue only fills segments NOT already
 * covered by a tajwid rule (the clipping happens in the UI layer).
 *
 * ── Rules detected (at stopping points only) ────────────────────────────
 *
 * A "stopping point" is the last word of a clause — i.e. the word that
 * immediately precedes a clause delimiter (Arabic comma ، or Latin
 * period .) or the word at the very end of the text. The renderer passes
 * each line to this engine, so clause-final words are analyzed directly.
 * Words whose final letter ALREADY carries a sukun are skipped: stopping
 * does not change their reading (this is why not every final word gets
 * colored — only those that truly meet the kaidah).
 *
 *  1. Waqaf bil Iskan (وقاف بالإسكان)
 *     The final letter carries a movable vowel (fathah/dammah/kasrah, with
 *     or without shadda) or tanwin dammatan/kasratan → when stopping, the
 *     vowel/tanwin is replaced by a sukun. The letter (with all its marks)
 *     is highlighted.
 *
 *  2. Mad Aridh Lissukun (مد عارض للسكون)
 *     The final letter is a natural madd letter — ا / و / ي, either bare
 *     (no marks; the preceding letter carries the matching harakat) or
 *     self-marked (ا with fathah, و with dammah, ي with kasrah, or any
 *     letter carrying a dagger alef ٰ) — at the end of a stop word. When
 *     stopping, the madd is extended to 4–6 harakat. The highlighted range
 *     covers the harakat-bearing anchor letter THROUGH the end of the word
 *     (the mad letter + any silent seat letters), e.g. قَالُوا → لُوا,
 *     فِي → فِي, عِيسَىٰ → ىٰ.
 *
 *  3. Mad Iwadh (مد عوض)
 *     The final letter carries tanwin fathatan (ً). When stopping, the
 *     tanwin is replaced by a pronounced alif. The highlighted range covers
 *     the tanwin letter plus any following silent seat alif, e.g.
 *     حَنِيفًا → فًا, هُدًى → دًى.
 *     EXCEPTION (per classical tajwid, e.g. mad iwadh ta marbutah): words
 *     ending with ta marbutah (ة) carrying fathatan are read with sukun
 *     when stopping (waqaf bil iskan), NOT with an added alif — so ةً is
 *     classified as iskan, not iwadh.
 *
 * ── What is deliberately NOT highlighted ───────────────────────────────
 *  - Final letters already carrying sukun (no reading change on stop).
 *  - Bare final letters with no marks (cannot determine the kaidah).
 *  - Diphthong finals (e.g. أَوْ — waw WITH sukun after fathah is a
 *    consonant, not a madd letter; stopping changes nothing).
 *  - Anything outside the stop words (mid-clause letters keep their
 *    existing tajwid coloring).
 *
 * ── Text integrity ──────────────────────────────────────────────────────
 * The engine never mutates the text. It only reports (start, end, kind)
 * ranges that index into the source string, exactly like
 * [TajwidUtil.analyzeTajwid].
 *
 * NOTE on indices: like the TS engine, ranges index into the source string
 * by code units. All Arabic-block characters handled here are BMP, so
 * Kotlin Char (UTF-16) iteration matches the TS `for...of` behavior.
 *
 * This object is PURE Kotlin (no Android/Compose imports) so JVM unit
 * tests can run against it.
 */
object WaqafUtil {

    enum class WaqafKind { Iskan, MadAridhLissukun, MadIwadh }

    /** Inclusive [start], exclusive [end] — indices into the source string. */
    data class WaqafRange(val start: Int, val end: Int, val kind: WaqafKind)

    /** Ordered legend metadata (also used by the settings legend UI). */
    data class RuleMeta(val kind: WaqafKind, val name: String, val desc: String)

    val RULES_META: List<RuleMeta> = listOf(
        RuleMeta(WaqafKind.Iskan, "Waqaf bil Iskan", "harokat/tanwin akhir kata diganti sukun saat berhenti"),
        RuleMeta(WaqafKind.MadAridhLissukun, "Mad Aridh Lissukun", "huruf mad akhir kata memanjang 4–6 harakat saat berhenti"),
        RuleMeta(WaqafKind.MadIwadh, "Mad Iwadh", "tanwin fath dibaca alif (2 harakat) saat berhenti"),
    )

    /**
     * Demo text for the settings waqaf legend — three one-word clauses, each
     * exercising one waqaf kind at its stopping point (identical to the
     * web's PREVIEW_WAQAF_TEXT):
     *  - قَالُوا،      → mad aridh lissukun (لُوا — madd extends when stopping)
     *  - هُدًى،         → mad iwadh (دًى — tanwin fath read as alif)
     *  - مُحَمَّدٌ رَسُوْلُ اللَّهِ، → waqaf bil iskan (هِ — vowel replaced by sukun)
     * Only the STOP words (before each ، and at the text end) get colored;
     * mid-clause words like مُحَمَّدٌ stay uncolored (per spec: not every
     * final letter is highlighted).
     */
    const val PREVIEW_WAQAF_TEXT = "قَالُوا، هُدًى، مُحَمَّدٌ رَسُوْلُ اللَّهِ،"

    // ── Character classes (identical to TajwidUtil) ─────────────────────────

    /** Sukun (U+0652) or small sukun variant (U+06E1). */
    private fun isSukun(c: Char) = c == 'ْ' || c == '\u06E1'

    /**
     * Combining marks that belong to the preceding letter. U+06DD (ARABIC
     * END OF AYAH, a format char inside the U+06D6–U+06ED band) is excluded
     * — it must never be treated as a mark of the previous letter.
     */
    private fun isCombining(c: Char): Boolean =
        c in '\u064B'..'\u0655' || c == '\u0670' ||
            (c in '\u06D6'..'\u06DC') || (c in '\u06DE'..'\u06ED')

    /** Arabic letter (hamza .. yeh, plus extended ranges). */
    private fun isArabicLetter(c: Char): Boolean =
        c in '\u0621'..'\u063A' || c in '\u0641'..'\u064A' || c in '\u0671'..'\u06D3'

    private const val FATHAH = '\u064E'      // َ
    private const val DAMMAH = '\u064F'      // ُ
    private const val KASRAH = '\u0650'      // ِ
    private const val FATHATAN = '\u064B'    // ً
    private const val DAMMATAN = '\u064C'    // ٌ
    private const val KASRATAN = '\u064D'    // ٍ
    private const val SHADDA = '\u0651'      // ّ
    private const val DAGGER_ALEF = '\u0670' // ٰ

    /** Ta marbutah — mad iwadh does NOT apply to it (reads sukun when stopped). */
    private const val TA_MARBUTAH = 'ة'

    /** Arabic comma and Latin period — clause delimiters = stopping points. */
    private fun isDelimiter(c: Char) = c == '،' || c == '.'

    // ── Clusters ─────────────────────────────────────────────────────────────

    private data class Cluster(
        val start: Int,      // index of the base character in the source string
        var end: Int,        // exclusive end (base + all combining marks)
        val base: Char,
        var marks: String,
    )

    private fun buildClusters(text: String): List<Cluster> {
        val clusters = mutableListOf<Cluster>()
        var pos = 0
        for (ch in text) {
            val last = clusters.lastOrNull()
            if (last != null && isCombining(ch)) {
                last.marks += ch
                last.end = pos + 1
            } else {
                clusters.add(Cluster(start = pos, end = pos + 1, base = ch, marks = ""))
            }
            pos++
        }
        return clusters
    }

    private fun isLetter(c: Cluster) = isArabicLetter(c.base)

    /** Mark analysis for one cluster. */
    private data class Marks(
        val hasFathah: Boolean,
        val hasDammah: Boolean,
        val hasKasrah: Boolean,
        val hasFathatan: Boolean,
        val hasDammatan: Boolean,
        val hasKasratan: Boolean,
        val hasSukun: Boolean,
        val hasShadda: Boolean,
        val hasDaggerAlef: Boolean,
    )

    private fun analyzeMarks(c: Cluster): Marks {
        val marks = c.marks
        return Marks(
            hasFathah = marks.contains(FATHAH),
            hasDammah = marks.contains(DAMMAH),
            hasKasrah = marks.contains(KASRAH),
            hasFathatan = marks.contains(FATHATAN),
            hasDammatan = marks.contains(DAMMATAN),
            hasKasratan = marks.contains(KASRATAN),
            hasSukun = marks.isNotEmpty() && marks.any { isSukun(it) },
            hasShadda = marks.contains(SHADDA),
            hasDaggerAlef = marks.contains(DAGGER_ALEF),
        )
    }

    /**
     * A "bare madd seat" — a madd letter (ا / و / ي) carrying NO marks at
     * all. These letters are silent connectors: they extend the harakat
     * carried by the preceding letter (the anchor). Walking backward over a
     * chain of such letters (plus a trailing tanwin seat) lets us find the
     * anchor precisely.
     */
    private fun isBareMaddSeat(c: Cluster): Boolean =
        isLetter(c) && c.marks.isEmpty() &&
            (c.base == 'ا' || c.base == 'و' || c.base == 'ي' || c.base == 'ى')

    /**
     * Classify the stop word that ends at cluster index [from] (the last
     * letter cluster of the word — the caller passes the index of the letter
     * immediately before a delimiter, or the final letter of the text).
     *
     * Returns a [WaqafRange], or null when stopping does not change the
     * reading (final letter already sukun / bare with no determinable kaidah).
     */
    private fun classifyStopWord(clusters: List<Cluster>, from: Int): WaqafRange? {
        // Walk back over whitespace between the delimiter and the word.
        var i = from
        while (i >= 0 && clusters[i].base.isWhitespace()) i--
        if (i < 0 || !isLetter(clusters[i])) return null

        // ── Determine the anchor ─────────────────────────────────────────────
        // The anchor is the harakat/tanwin-bearing letter at the head of the
        // word's tail. If the LAST cluster itself is a bare madd seat (ا/و/ي
        // with no marks), the anchor sits before the run of trailing bare
        // seats — e.g. قَالُوا → anchor لُ, seats [و, ا]; دُنْيَا → anchor يَ,
        // seat [ا]. The walk NEVER crosses a word boundary and only consumes
        // MARKLESS madd letters (a sukun-carrying و breaks the chain: أَوْ is
        // a diphthong, not a madd).
        val lastIsBareSeat = isBareMaddSeat(clusters[i])
        val chainStart: Int // first index of the bare-seat run (if any)
        val anchorIdx: Int
        if (lastIsBareSeat) {
            var j = i
            while (j - 1 >= 0 && !clusters[j - 1].base.isWhitespace() && isBareMaddSeat(clusters[j - 1])) {
                j--
            }
            chainStart = j
            anchorIdx = j - 1
            if (anchorIdx < 0 || !isLetter(clusters[anchorIdx]) || clusters[anchorIdx].base.isWhitespace()) {
                // The whole word is bare letters — kaidah undeterminable.
                return null
            }
        } else {
            // The final cluster carries marks — it IS the anchor; no chain.
            chainStart = i
            anchorIdx = i
        }

        val anchor = clusters[anchorIdx]
        val m = analyzeMarks(anchor)
        val chainEnd = clusters[i].end // range always extends to the word's last cluster
        val anchorOnly = anchor.end
        // A trailing bare-seat run exists whenever the LAST cluster is itself a
        // bare madd seat — even when the run is a single letter (e.g. كَمَا's ا).
        val hasChain = lastIsBareSeat

        // 1) Mad Iwadh — tanwin fathatan (ً) → alif, EXCEPT ta marbutah (ةً),
        //    which reads sukun (waqaf bil iskan) per classical tajwid.
        if (m.hasFathatan) {
            return if (anchor.base == TA_MARBUTAH) {
                WaqafRange(anchor.start, anchorOnly, WaqafKind.Iskan)
            } else {
                WaqafRange(anchor.start, chainEnd, WaqafKind.MadIwadh)
            }
        }

        // 2) Mad Aridh Lissukun — the anchor carries a madd-feeding harakat and
        //    the trailing bare seats are the matching madd letter(s).
        if (hasChain) {
            val chainBases = (chainStart..i).map { clusters[it].base }
            val chainAllE = chainBases.all { it == 'ا' || it == 'ى' }
            val chainAllW = chainBases.all { it == 'و' || it == 'ا' }
            val chainAllY = chainBases.all { it == 'ي' }
            // Anchor must carry the matching harakat — a sukun anchor means the
            // trailing letters are consonants/diphthongs, NOT madd (e.g. أَوْ).
            if (m.hasFathah && chainAllE) {
                return WaqafRange(anchor.start, chainEnd, WaqafKind.MadAridhLissukun)
            }
            if (m.hasDammah && chainAllW) {
                return WaqafRange(anchor.start, chainEnd, WaqafKind.MadAridhLissukun)
            }
            if (m.hasKasrah && chainAllY) {
                return WaqafRange(anchor.start, chainEnd, WaqafKind.MadAridhLissukun)
            }
            // Unusual structure — stay conservative (avoid wrong coloring).
            return null
        }

        // No chain — the anchor IS the final cluster.
        // Dagger alef (ٰ) marks a madd ā → mad aridh (e.g. عِيسَىٰ).
        if (m.hasDaggerAlef && !m.hasSukun && !m.hasFathatan && !m.hasDammatan && !m.hasKasratan) {
            return WaqafRange(anchor.start, anchorOnly, WaqafKind.MadAridhLissukun)
        }

        // Self-marked madd letters (اَ / وُ / يِ) — the letter is both the
        // harakat carrier and the madd letter → mad aridh when stopped.
        if ((anchor.base == 'ا' || anchor.base == 'ى') && m.hasFathah ||
            (anchor.base == 'و' && m.hasDammah) ||
            (anchor.base == 'ي' && m.hasKasrah)
        ) {
            return WaqafRange(anchor.start, anchorOnly, WaqafKind.MadAridhLissukun)
        }

        // 3) Waqaf bil Iskan — movable vowel (± shadda) replaced by sukun.
        if ((m.hasFathah || m.hasDammah || m.hasKasrah) && !m.hasSukun) {
            return WaqafRange(anchor.start, anchorOnly, WaqafKind.Iskan)
        }

        // Tanwin dammatan / kasratan → sukun when stopping.
        if (m.hasDammatan || m.hasKasratan) {
            return WaqafRange(anchor.start, anchorOnly, WaqafKind.Iskan)
        }

        // 4) No reading change (already sukun, or undeterminable) → no highlight.
        return null
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Analyze [text] and return sorted, non-overlapping waqaf ranges.
     *
     * Stopping points: the word immediately before every clause delimiter
     * (، or .) AND the final word of the text (after stripping trailing
     * delimiters/whitespace). Only words whose reading actually changes when
     * stopping produce a range.
     */
    fun analyzeWaqaf(text: String): List<WaqafRange> {
        if (text.isEmpty()) return emptyList()
        val clusters = buildClusters(text)
        if (clusters.isEmpty()) return emptyList()

        val raw = mutableListOf<WaqafRange>()
        val seenStarts = mutableSetOf<Int>()

        fun add(r: WaqafRange?) {
            if (r == null) return
            if (!seenStarts.add(r.start)) return
            raw.add(r)
        }

        // 1) The word before every clause delimiter (، / .).
        for (i in clusters.indices) {
            val c = clusters[i]
            if (isLetter(c) || c.base.isWhitespace()) continue
            if (isDelimiter(c.base)) {
                add(classifyStopWord(clusters, i - 1))
            }
        }

        // 2) The final word — strip trailing whitespace + delimiters first so
        //    "… خَلَقَ،" reports خَلَقَ exactly once (dedupe handles the overlap
        //    with the delimiter scan above).
        var end = clusters.size - 1
        while (end >= 0 && (clusters[end].base.isWhitespace() || isDelimiter(clusters[end].base))) {
            end--
        }
        if (end >= 0) {
            add(classifyStopWord(clusters, end))
        }

        // Sort by start; ranges from distinct stop words never overlap, so a
        // simple sort suffices.
        raw.sortBy { it.start }
        return raw
    }
}
