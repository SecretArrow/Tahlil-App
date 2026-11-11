package my.tahlil.app.util

/**
 * Parses ۝X ayah marks from Arabic text.
 *
 * Matches ۝ (U+06DD ARABIC END OF AYAH) followed by one or more
 * Arabic-Indic digits (٠-٩). The mark sits at the END of the verse it
 * closes, matching standard Mushaf convention.
 *
 * Port of the `AYAH_MARK_REGEX` parsing logic from `arabic-text.tsx`.
 */
object AyahMarkUtil {

    private val ARABIC_DIGITS = setOf(
        '٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'
    )

    /** End-of-ayah marker ۝ (U+06DD). */
    const val AYAH_SYMBOL = '۝'

    /** Sajda marker ۩ (U+06E9) — treated as a non-ayah verse terminator. */
    const val SAJDA_SYMBOL = '۩'

    /** Connector + ayah symbol — what we search for. */
    private const val AYAH_MARK_PREFIX = AYAH_SYMBOL.toString()

    /**
     * A single match — the start index of the full mark in the original
     * string, the length of the mark, and the digit string (e.g. "١").
     */
    data class Match(val start: Int, val length: Int, val digits: String) {
        val endExclusive: Int get() = start + length
    }

    /**
     * Scan [text] for ۝X marks in order. The mark always starts with ۝
     * followed by one or more Arabic-Indic digits.
     */
    fun findMarks(text: String): List<Match> {
        val out = mutableListOf<Match>()
        var i = 0
        val n = text.length
        while (i < n) {
            if (text[i] == AYAH_SYMBOL) {
                // Collect following digits
                var j = i + 1
                while (j < n && text[j] in ARABIC_DIGITS) j++
                if (j > i + 1) {
                    // At least one digit — emit a match.
                    out.add(Match(start = i, length = j - i, digits = text.substring(i + 1, j)))
                    i = j
                } else {
                    // Bare ۝ without digits — treat as a single-char mark.
                    out.add(Match(start = i, length = 1, digits = ""))
                    i = j
                }
            } else {
                i++
            }
        }
        return out
    }

    /**
     * Split [text] into a list of (verseText, mark?) pairs where each pair
     * represents one ayah line. The mark (if present) closes the verse.
     *
     * Used by `ArabicText` to render each ayah on its own line.
     */
    data class AyahLine(val text: String, val mark: Match?)

    fun splitByAyahMarks(text: String): List<AyahLine> {
        val marks = findMarks(text)
        if (marks.isEmpty()) return listOf(AyahLine(text, null))

        val out = mutableListOf<AyahLine>()
        var cursor = 0
        for (m in marks) {
            val verse = text.substring(cursor, m.start)
            out.add(AyahLine(verse, m))
            cursor = m.endExclusive
        }
        if (cursor < text.length) {
            out.add(AyahLine(text.substring(cursor), null))
        }
        return out
    }

    /**
     * Split a non-ayah string by Arabic comma (،) and Latin period (.),
     * preserving the delimiter at the END of each line (per user request:
     * "berikan tanda di akhir kalimatnya").
     *
     * Port of `splitBySentence()` from `arabic-text.tsx`.
     */
    data class SentenceLine(val text: String)

    fun splitBySentence(text: String): List<SentenceLine> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return emptyList()

        val out = mutableListOf<SentenceLine>()
        val current = StringBuilder()
        for (c in trimmed) {
            current.append(c)
            if (c == '،' || c == '.') {
                out.add(SentenceLine(current.toString()))
                current.setLength(0)
            }
        }
        if (current.toString().trim().isNotEmpty()) {
            out.add(SentenceLine(current.toString()))
        }
        return out
    }

    /**
     * Convert Arabic-Indic digit string to Latin digits for accessibility
     * (e.g. for contentDescription "Ayah 1"). Returns the original string
     * if no digits are present.
     */
    fun digitsToLatin(arabicDigits: String): String {
        if (arabicDigits.isEmpty()) return ""
        val sb = StringBuilder(arabicDigits.length)
        for (c in arabicDigits) {
            val idx = ARABIC_DIGITS.toList().indexOf(c)
            if (idx >= 0) sb.append(idx) else sb.append(c)
        }
        return sb.toString()
    }
}
