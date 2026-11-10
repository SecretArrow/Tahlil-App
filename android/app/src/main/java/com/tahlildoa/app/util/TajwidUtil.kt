package com.tahlildoa.app.util

import androidx.compose.ui.graphics.Color

/**
 * Tajwid engine — detects recitation rules (hukum bacaan) in fully
 * vocalised Arabic text so the renderer can highlight them with colors.
 *
 * Port of `src/lib/tajwid.ts` (web) — kept rule-for-rule identical:
 *
 * Scope: the 7 core rules taught in Indonesian tahlil/majelis books:
 *   1. Izhar              — nun sukun/tanwin + halqi letters (ء ه ع ح غ خ)
 *                           (incl. izhar syafawi: mim sukun + other letters)
 *   2. Ikhfa              — nun sukun/tanwin + the 15 ikhfa letters
 *                           (incl. ikhfa syafawi: mim sukun + ب)
 *   3. Idgham Bighunnah   — nun sukun/tanwin + ي ن م و
 *                           (incl. idgham syafawi: mim sukun + م)
 *   4. Idgham Bilaghunnah — nun sukun/tanwin + ل ر
 *   5. Iqlab              — nun sukun/tanwin + ب
 *   6. Ghunnah            — نّ / مّ (wajibul ghunnah)
 *   7. Qalqalah           — ق ط ب ج د with sukun
 *
 * The engine is purely character-based and never mutates the text; it only
 * reports (start, end, rule) ranges so the renderer can wrap segments in
 * colored spans. Detection handles combining marks correctly: harakat,
 * shadda, sukun, tanwin, dagger alef etc. are attached to their base
 * letter as "clusters", and rules are evaluated across word boundaries
 * (tanwin / nun sukun at the end of a word applies to the first letter of
 * the next word) but NOT across punctuation (a pause breaks the rule).
 *
 * Not detected (deliberately, to stay accurate): mad rules, lam
 * syamsiyyah/qamariyyah, sifat letters.
 *
 * NOTE on indices: like the TS engine, ranges index into the source string
 * by code units. All Arabic-block characters handled here are BMP, so
 * Kotlin Char (UTF-16) iteration matches the TS `for...of` behavior.
 */
object TajwidUtil {

    enum class Rule { izhar, ikhfa, idghamGhunnah, idghamLaghunnah, iqlab, ghunnah, qalqalah }

    /** Inclusive [start], exclusive [end] — indices into the source string. */
    data class TajwidRange(val start: Int, val end: Int, val rule: Rule)

    /** Ordered legend metadata (also used by the settings legend UI). */
    data class RuleMeta(val rule: Rule, val name: String, val desc: String)

    val RULES_META: List<RuleMeta> = listOf(
        RuleMeta(Rule.izhar, "Izhar", "dibaca jelas"),
        RuleMeta(Rule.ikhfa, "Ikhfa", "dibaca sambar"),
        RuleMeta(Rule.idghamGhunnah, "Idgham Bighunnah", "lebur berdengung"),
        RuleMeta(Rule.idghamLaghunnah, "Idgham Bilaghunnah", "lebur tanpa dengung"),
        RuleMeta(Rule.iqlab, "Iqlab", "nun bertukar mim"),
        RuleMeta(Rule.ghunnah, "Ghunnah", "dengung 2 harakat"),
        RuleMeta(Rule.qalqalah, "Qalqalah", "dibaca memantul"),
    )

    /**
     * Demo text used by the settings legend — contains at least one instance
     * of EVERY rule (identical to the web's PREVIEW_TAJWID_TEXT).
     */
    const val PREVIEW_TAJWID_TEXT =
        "أَنْعَمْتَ عَلَيْهِمْ، مِنْ شَرِّ مَا خَلَقَ، مُحَمَّدٌ رَسُوْلُ اللَّهِ، مِنْ بَعْدِ، مِنْ يَمِيْنِهِمْ، أَجْمَعِيْنَ"

    // ── Colors (mirror `.tajwid-<rule>` in web globals.css) ────────────────

    data class RuleColor(val light: Color, val dark: Color)

    val COLORS: Map<Rule, RuleColor> = mapOf(
        Rule.izhar to RuleColor(Color(0xFFB45309), Color(0xFFFBBF24)),   // amber
        Rule.ikhfa to RuleColor(Color(0xFF047857), Color(0xFF34D399)),   // green
        Rule.idghamGhunnah to RuleColor(Color(0xFF7C3AED), Color(0xFFA78BFA)), // violet
        Rule.idghamLaghunnah to RuleColor(Color(0xFF64748B), Color(0xFF94A3B8)), // slate
        Rule.iqlab to RuleColor(Color(0xFF2563EB), Color(0xFF60A5FA)),   // blue
        Rule.ghunnah to RuleColor(Color(0xFFBE123C), Color(0xFFFB7185)), // rose
        Rule.qalqalah to RuleColor(Color(0xFFEA580C), Color(0xFFFB923C)), // orange
    )

    fun colorFor(rule: Rule, isDark: Boolean): Color =
        (COLORS[rule] ?: RuleColor(Color(0xFFB45309), Color(0xFFFBBF24))).let { if (isDark) it.dark else it.light }

    // ── Character classes ───────────────────────────────────────────────────

    /** Sukun (U+0652) or small sukun variant (U+06E1). */
    private fun isSukun(c: Char) = c == 'ْ' || c == '\u06E1'

    /** Tanwin marks: fathatan (U+064B), dammatan (U+064C), kasratan (U+064D). */
    private fun isTanwin(c: Char) = c in '\u064B'..'\u064D'

    private const val SHADDA = '\u0651'

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

    private const val IZHAR_LETTERS = "ءهعحغخ"
    private const val IDGHAM_GHUNNAH_LETTERS = "ينمو"
    private const val IDGHAM_LAGHUNNAH_LETTERS = "لر"
    private const val QALQALAH_LETTERS = "قطبجد"
    private const val IQLAB_LETTER = 'ب'
    private const val IKHFA_LETTERS = "تثجدذزسشصضطظفقك"

    // ── Clusters ────────────────────────────────────────────────────────────

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
     * Index of the letter cluster that immediately follows cluster [from],
     * skipping whitespace (rules apply across word boundaries). Returns -1
     * if any other character (punctuation, ayah mark, digit, Latin, …)
     * intervenes — a pause breaks the rule.
     */
    private fun nextLetterIdx(clusters: List<Cluster>, from: Int): Int {
        for (j in from + 1 until clusters.size) {
            val c = clusters[j]
            if (isLetter(c)) return j
            if (c.base.isWhitespace()) continue // word boundary — keep looking
            return -1 // punctuation / symbol — rule broken
        }
        return -1
    }

    private fun classifyNunRule(base: Char): Rule? = when {
        IZHAR_LETTERS.contains(base) -> Rule.izhar
        IDGHAM_GHUNNAH_LETTERS.contains(base) -> Rule.idghamGhunnah
        IDGHAM_LAGHUNNAH_LETTERS.contains(base) -> Rule.idghamLaghunnah
        base == IQLAB_LETTER -> Rule.iqlab
        IKHFA_LETTERS.contains(base) -> Rule.ikhfa
        else -> null
    }

    // ── Public API ──────────────────────────────────────────────────────────

    /**
     * Analyze [text] and return sorted, non-overlapping tajwid ranges
     * (possibly empty — texts without the covered rules simply return []).
     */
    fun analyzeTajwid(text: String): List<TajwidRange> {
        if (text.isEmpty()) return emptyList()
        val clusters = buildClusters(text)
        val raw = mutableListOf<TajwidRange>()

        for (i in clusters.indices) {
            val c = clusters[i]
            if (!isLetter(c)) continue

            val hasSukun = c.marks.any { isSukun(it) }
            val hasShadda = c.marks.contains(SHADDA)
            val hasTanwin = c.marks.any { isTanwin(it) }

            // 1) Ghunnah — نّ / مّ (the letter itself is highlighted).
            if (hasShadda && (c.base == 'ن' || c.base == 'م')) {
                raw.add(TajwidRange(c.start, c.end, Rule.ghunnah))
            }

            // 2) Qalqalah — ق ط ب ج د carrying a sukun.
            if (hasSukun && QALQALAH_LETTERS.contains(c.base)) {
                raw.add(TajwidRange(c.start, c.end, Rule.qalqalah))
            }

            // 3) Nun sakin / tanwin — rule partner is the next letter.
            val nunSakin = c.base == 'ن' && hasSukun && !hasShadda
            if (nunSakin || hasTanwin) {
                val j = nextLetterIdx(clusters, i)
                if (j != -1) {
                    val rule = classifyNunRule(clusters[j].base)
                    if (rule != null) {
                        raw.add(TajwidRange(c.start, clusters[j].end, rule))
                    }
                }
            }

            // 4) Mim sakin — syafawi rules (ikhfa / idgham / izhar syafawi).
            val mimSakin = c.base == 'م' && hasSukun && !hasShadda
            if (mimSakin) {
                val j = nextLetterIdx(clusters, i)
                if (j != -1) {
                    val rule = when (clusters[j].base) {
                        'ب' -> Rule.ikhfa
                        'م' -> Rule.idghamGhunnah
                        else -> Rule.izhar
                    }
                    raw.add(TajwidRange(c.start, clusters[j].end, rule))
                }
            }
        }

        // Sort by start, then by longer range first; resolve overlaps by
        // trimming the later range so both partial rules stay visible.
        raw.sortWith(compareBy({ it.start }, { -it.end }))
        val out = mutableListOf<TajwidRange>()
        var cursor = 0
        for (r in raw) {
            val s = maxOf(r.start, cursor)
            if (s >= r.end) continue // fully swallowed by an earlier range
            out.add(TajwidRange(s, r.end, r.rule))
            cursor = r.end
        }
        return out
    }
}
