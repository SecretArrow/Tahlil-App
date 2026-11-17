package my.tahlil.app.util

/**
 * Hamzatul Washal engine — marks the hamzatul-wasl alif (همزة الوصل,
 * Alif Washal) that is SILENT when the reciter CONNECTS the recitation
 * (washal), so the renderer can highlight it in grayish blue #7893B0.
 *
 * Rule-for-rule port of `src/lib/hamza-washal.ts` (web) — companion to
 * [TajwidUtil] (the 7-color tajwid engine) and [WaqafUtil] (the waqaf
 * blue engine). The feature follows the Tajwid toggle automatically
 * (spec requirement 7): it has NO toggle/setting of its own. When
 * tajwid is off, nothing is marked; when tajwid is on, the wasl marking
 * composes with the other colors (see clipping below — wasl has the
 * LOWEST color precedence).
 *
 * ── What Hamzatul Washal is ─────────────────────────────────────────────
 * Hamzatul wasl is the alif written at the head of certain words (Uthmani
 * rasm: bare ا — never أ إ آ ٰ). It is pronounced ONLY when the reader
 * STARTS the recitation with that word (ibtida'), and DROPPED whenever
 * anything is recited before it (washal):
 *
 *  - a preceding word in the recitation flow, or
 *  - a prefix attached to the same word (وَ فَ بِ لِ كَ …), e.g.
 *    وَالْعَصْرِ is read "wal-ʿaṣr" even when the recitation starts
 *    there, بِاسْمِ is read "bis-mi", فَاسْتَقِمْ is read "fas-taqim".
 *
 * Per spec: color ONLY the alif that is actually NOT pronounced in the
 * current reading context. The reading context is the rendered text unit
 * (one ayah, or one clause line of dua text): its FIRST word is an
 * ibtida' starting point, so a word-initial wasl alif there keeps the
 * normal text color; every other wasl alif (preceded by a word or a
 * prefix) is colored.
 *
 * ── Where hamzatul wasl occurs (detection rules) ────────────────────────
 * Detection is rule-based on the Uthmani rasm, NOT generic alif matching
 * (spec requirement 3):
 *
 *  A. The definite article ال: word-initial bare alif followed by the
 *     article lam (any marking: الْحَمْدُ لْ, اللّٰهُ ل bare, الَّذِيْ
 *     shadda), or by a shadda letter (assimilated sun-article per Uthmani
 *     rasm: الرَّحْمٰنِ with lam kept in this app's simplified rasm).
 *
 *  B. Verb/noun wasl: word-initial bare alif followed by a sukun letter —
 *     past tense / imperative / masdar of forms VII–X and quadriliteral
 *     roots, plus the exceptional nouns اِسْمُ اِمْرَأَ اِثْنَانِ (e.g.
 *     اِهْدِنَا اغْفِرْ اِرْحَمْنَا اجْتَمَعْنَا اكْتَسَبَتْ).
 *
 *  C. Wasl alif right after a prefix letter (وَ فَ بِ لِ كَ) inside the
 *     same word, with the same follow-patterns as A/B (e.g. وَاللّٰهُ,
 *     بِالْغَيْبِ, فَاعْلَمْ, وَاغْفِرْ). These are ALWAYS dropped —
 *     even when the word is the first word of the unit.
 *
 * ── What is deliberately NOT marked ─────────────────────────────────────
 *  - Hamzatul qatha': written أ إ آ, or — in this app's rasm convention —
 *    alif carrying dagger-alef/maddah marks (اٰمَنَ اٰتِنَا اٰلِهِ …,
 *    "ā-" is always pronounced). Excluded via the mark blacklist
 *    U+0653 U+0654 U+0655 U+0670 U+06E4 (harakat on the alif are fine).
 *  - Quranic letter initials (الۤمّۤ …): the alif is recited as the
 *    letter name "alif" and never dropped.
 *  - Long-vowel alifs inside words (قَالُوا, سَيِّئَاتِ …): only
 *    word-initial / after-prefix positions are candidates at all.
 *  - A word-initial wasl alif of the FIRST word of the text unit
 *    (ibtida' — the alif IS pronounced when starting there).
 *  - Negation لَا and similar: the alif is not followed by an eligible
 *    pattern (no lam/shadda/sukun letter after it), so it never matches.
 *
 * ── Color precedence (spec requirement 8) ───────────────────────────────
 * Tajwid rule colors > waqaf blue (#1B51D0) > wasl gray-blue (#7893B0).
 * The renderer (ArabicText.kt) clips wasl ranges against tajwid AND waqaf
 * ranges, so the seven tajwid colors and the waqaf marking are never
 * disturbed. In practice the ranges rarely meet: waqaf colors word TAILS
 * while the wasl alif is always the word HEAD.
 *
 * ── Text integrity ──────────────────────────────────────────────────────
 * The engine never mutates the text. It only reports (start, end) ranges
 * that index into the source string, exactly like
 * [TajwidUtil.analyzeTajwid] / [WaqafUtil.analyzeWaqaf] — all harakat,
 * waqaf signs and verse marks are preserved verbatim (spec requirement 5).
 *
 * NOTE on indices: like the TS engine, ranges index into the source string
 * by code units. All Arabic-block characters handled here are BMP, so
 * Kotlin Char (UTF-16) iteration matches the TS `for...of` behavior.
 *
 * NOTE on combining marks: [isCombining] mirrors [WaqafUtil] exactly —
 * U+06DD (ARABIC END OF AYAH, a format char inside the U+06D6–U+06ED
 * band) is excluded so the ayah mark is never glued to the previous
 * letter. (The TS file uses the raw band; behavior is identical for every
 * real text unit because ayah marks are split out by the renderer first.)
 *
 * This object is PURE Kotlin (no Android/Compose imports) so JVM unit
 * tests can run against it.
 */
object HamzaWashalUtil {

    /** Inclusive [start], exclusive [end] — indices into the source string. */
    data class HamzaWashalRange(val start: Int, val end: Int)

    /**
     * Legend metadata for the settings panels — mirrors
     * `HAMZA_WASHAL_META` in `hamza-washal.ts`. Colors are ARGB hex
     * (Compose-style) so the UI layer can build [androidx.compose.ui.graphics.Color]
     * values from them directly.
     */
    data class Meta(
        val name: String,
        val desc: String,
        /** Spec color — grayish blue (light theme). */
        val colorLight: Long,
        /** Lightened same-hue variant for the dark theme. */
        val colorDark: Long,
    )

    val META = Meta(
        name = "Hamzatul Washal",
        desc = "alif wasal tidak dibaca saat menyambung bacaan (aktif bersama Tajwid)",
        colorLight = 0xFF7893B0L,
        colorDark = 0xFFA8BED4L,
    )

    /**
     * Demo text for the settings legend — a clause that exercises both
     * contexts at once (mirrors `PREVIEW_HAMZA_WASHAL_TEXT` on the web,
     * Surat al-Fātiḥah 1:6):
     *  - اِهْدِنَا → wasl alif of the FIRST word = ibtida' → normal color.
     *  - الصِّرَاطَ الْمُسْتَقِيْمَ، → wasl alifs after a preceding word →
     *    colored (silent in connected reading).
     */
    const val PREVIEW_TEXT = "اِهْدِنَا الصِّرَاطَ الْمُسْتَقِيْمَ،"

    // ── Character classes ────────────────────────────────────────────────────

    private const val ALIF = '\u0627'        // ا
    private const val ALEF_WASLA = '\u0671'  // ٱ — definitive hamzatul wasl
    private const val LAM = '\u0644'         // ل
    private const val SHADDA = '\u0651'      // ّ

    /** Sukun (U+0652) or small sukun variant (U+06E1). */
    private fun isSukun(c: Char) = c == '\u0652' || c == '\u06E1'

    /**
     * Combining marks that belong to the preceding letter. U+06DD (ARABIC
     * END OF AYAH, a format char inside the U+06D6–U+06ED band) is excluded
     * — it must never be treated as a mark of the previous letter.
     * Identical to [WaqafUtil]'s cluster builder.
     */
    private fun isCombining(c: Char): Boolean =
        c in '\u064B'..'\u0655' || c == '\u0670' ||
            (c in '\u06D6'..'\u06DC') || (c in '\u06DE'..'\u06ED')

    /** Arabic letter (hamza .. yeh, plus extended ranges). */
    private fun isArabicLetter(c: Char): Boolean =
        c in '\u0621'..'\u063A' || c in '\u0641'..'\u064A' || c in '\u0671'..'\u06D3'

    /**
     * Marks that turn a bare alif into hamzatul QATHA' (never washal):
     * maddah U+0653, hamza above/below U+0654/0655, dagger alef U+0670
     * (this app writes qatha' "ā-" as اٰ), and U+06E4 (Quranic initials).
     */
    private fun isQathaMark(c: Char): Boolean =
        c == '\u0653' || c == '\u0654' || c == '\u0655' || c == '\u0670' || c == '\u06E4'

    /** Prefix letters that attach before a wasl alif inside the same word. */
    private val PREFIX_BASES = setOf('\u0648', '\u0641', '\u0628', '\u0644', '\u0643')

    /** Final bases of the 3-letter Quranic letter initials (ا + ل + X). */
    private val INITIAL_LAST_BASES = setOf('م', 'ر', 'ص', 'ق', 'ك', 'ل')

    // ── Clusters (identical structure to WaqafUtil) ──────────────────────────

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

    /**
     * A bare wasl-form alif: plain ا (optionally with harakat like اِ اَ —
     * harakat do not affect the wasl identity) or the dedicated ٱ. Rejects
     * every qatha'-shaped alif (أ إ آ اٰ) via the mark blacklist.
     */
    private fun isWashalAlifShape(c: Cluster): Boolean {
        if (c.base == ALEF_WASLA) return true
        if (c.base != ALIF) return false
        return c.marks.none { isQathaMark(it) }
    }

    /**
     * The letter pattern that must follow a wasl alif:
     *  - the article lam (الْ اللّٰهُ الَّذِيْ — lam with any marking, or
     *    even bare when the vocalizer omitted its sukun),
     *  - a shadda letter (assimilated sun-article per Uthmani rasm), or
     *  - a sukun letter (form VII–X verb/noun wasl: اِهْدِنَا اغْفِرْ …).
     */
    private fun isWashalFollowLetter(c: Cluster): Boolean {
        if (!isLetter(c)) return false
        if (c.base == LAM) return true
        if (c.marks.contains(SHADDA)) return true
        return c.marks.any { isSukun(it) }
    }

    /**
     * Quranic letter initials (الۤمّۤ …): the alif is recited as the letter
     * name and never drops. These are exactly the short ا + ل + one-letter
     * words of the Uthmani initials family.
     */
    private fun isQuranicInitial(clusters: List<Cluster>, from: Int, to: Int): Boolean {
        if (to - from != 3) return false
        val a = clusters[from].base
        val b = clusters[from + 1].base
        val c = clusters[from + 2].base
        return a == ALIF && b == LAM && c in INITIAL_LAST_BASES
    }

    /**
     * Analyze the word that starts at cluster index [wordStart] (the caller
     * guarantees the alif candidate has already been shape-checked). Returns
     * the range of the SILENT wasl alif cluster, or null when:
     *  - the follow-letter pattern does not confirm hamzatul wasl,
     *  - the word is a Quranic letter initial,
     *  - the alif is pronounced (word-initial wasl alif of the first word —
     *    ibtida').
     *
     * [isFirstWord] tells whether this word is the first word of the text
     * unit (the ibtida' starting point of the current reading context).
     */
    private fun classifyWord(
        clusters: List<Cluster>,
        wordStart: Int,
        alifIdx: Int,
        isFirstWord: Boolean,
    ): HamzaWashalRange? {
        val afterPrefix = alifIdx > wordStart // وَ/فَ/بِ/لِ/كَ before the alif
        val follow = clusters.getOrNull(alifIdx + 1) ?: return null

        // The follow pattern must confirm hamzatul wasl (article lam / shadda
        // sun-letter / sukun letter). Without it — not a wasl alif, e.g. the
        // ا of لَا which is a long-vowel seat.
        if (!isWashalFollowLetter(follow)) return null

        // Word boundaries for the Quranic-initial guard: from wordStart to the
        // next whitespace cluster.
        var wordEnd = wordStart
        while (wordEnd < clusters.size && !clusters[wordEnd].base.isWhitespace()) wordEnd++
        if (isQuranicInitial(clusters, wordStart, wordEnd)) return null

        // Context: the alif is pronounced only when NOTHING is recited before
        // it — i.e. word-initial wasl alif of the FIRST word of the unit
        // (ibtida'). An after-prefix alif is always dropped, even there.
        if (isFirstWord && !afterPrefix) return null

        val alif = clusters[alifIdx]
        return HamzaWashalRange(alif.start, alif.end)
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Analyze [text] (one reading unit: an ayah or a clause line) and return
     * sorted, non-overlapping ranges of the wasl alifs that are SILENT in
     * connected reading — the ones to color #7893B0.
     *
     * Word context: the first word that contains an Arabic letter (index 0;
     * punctuation-only tokens like ، are skipped, leading whitespace and
     * delimiters ignored) is the ibtida' starting point — its word-initial
     * wasl alif keeps the normal color.
     */
    fun analyzeHamzaWashal(text: String): List<HamzaWashalRange> {
        if (text.isEmpty()) return emptyList()
        val clusters = buildClusters(text)
        if (clusters.isEmpty()) return emptyList()

        val ranges = mutableListOf<HamzaWashalRange>()
        val seenStarts = mutableSetOf<Int>()

        // Tokenize into words (maximal runs of non-whitespace clusters) while
        // tracking each word's cluster start. Only words containing at least
        // one Arabic letter count toward the word index (punctuation-only
        // tokens like ، . are skipped).
        var wordStart = -1
        var wordIndex = -1 // 0-based index of the current letter-bearing word
        for (i in 0..clusters.size) {
            val c = clusters.getOrNull(i)
            val isBoundary = c == null || c.base.isWhitespace()
            if (isBoundary) {
                // Close the current word, if any.
                if (wordStart >= 0) {
                    for (j in wordStart until i) {
                        val cl = clusters[j]
                        val isCandidatePos =
                            (j == wordStart ||
                                (j == wordStart + 1 && clusters[wordStart].base in PREFIX_BASES)) &&
                                isWashalAlifShape(cl) &&
                                // no duplicate scan of the same cluster
                                cl.start !in seenStarts
                        if (!isCandidatePos) continue
                        val r = classifyWord(clusters, wordStart, j, wordIndex == 0)
                        if (r != null) {
                            ranges.add(r)
                            seenStarts.add(r.start)
                        }
                    }
                    wordStart = -1
                }
                continue
            }
            if (wordStart < 0) {
                wordStart = i
                if (isLetter(c!!)) wordIndex += 1
            }
        }

        ranges.sortBy { it.start }
        return ranges
    }
}
