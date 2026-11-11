package my.tahlil.app.util

/**
 * Port of `addArabicCommas()` from the web app (`arabic-text.tsx`).
 *
 * Inserts Arabic commas (،) before " وَ" connectors so that the Arabic
 * text mirrors the comma punctuation that already exists in the
 * Indonesian translation. For example, "وَعَلَى آلِهِ وَأَصْحَابِهِ
 * وَأَزْوَاجِهِ وَذُرِّيَّتِهِ وَأَهْلِ بَيْتِهِ" becomes
 * "وَعَلَى آلِهِ، وَأَصْحَابِهِ، وَأَزْوَاجِهِ، وَذُرِّيَّتِهِ وَأَهْلِ بَيْتِهِ".
 *
 * Rules (from the web app source):
 *  - Quran verses (text containing ۝ or ۩) are returned unchanged.
 *  - The text is split into segments by existing major punctuation
 *    (Arabic comma ، and Latin period .).
 *  - Within each segment, the following " وَ" connectors are skipped:
 *      a) Connectors that are part of a fixed phrase (e.g., " وَسَلَّمْ"
 *         in "صلى الله عليه وسلم" — never split).
 *      b) The connector immediately after a phrase (it introduces the
 *         first list item).
 *      c) The LAST connector in the segment (it's the "and" before the
 *         final list item — matches "X, Y, Z dan W" pattern).
 *  - All other " وَ" connectors in the segment get a "، " prefix.
 *  - Single-connector segments (no list) are left unchanged.
 */
object ArabicCommaUtil {

    /** Fixed phrases whose leading " وَ" should never be split. */
    private val PHRASE_PATTERNS = listOf(" وَسَلَّمْ", " وَسَلِّمْ")

    /** The connector we look for: a space + وَ (waw + fatha). */
    private const val CONNECTOR = " وَ"
    private const val CONNECTOR_LEN = CONNECTOR.length

    /** Skip Quran verses — they must never be modified. */
    private fun isQuranVerse(text: String): Boolean =
        text.contains('۝') || text.contains('۩')

    /**
     * Find all " وَ" connector positions in [text]. Returns a list of
     * start indices (the index of the leading space).
     */
    private fun findConnectorPositions(text: String): List<Int> {
        val out = mutableListOf<Int>()
        var searchPos = 0
        while (true) {
            val idx = text.indexOf(CONNECTOR, searchPos)
            if (idx == -1) break
            out.add(idx)
            searchPos = idx + CONNECTOR_LEN
        }
        return out
    }

    /** Find indices of segment boundaries (positions of ، or .). */
    private fun findSegmentBoundaries(text: String): List<Int> {
        val out = mutableListOf<Int>()
        for (i in text.indices) {
            val c = text[i]
            if (c == '،' || c == '.') out.add(i)
        }
        return out
    }

    /** Map a connector position to its segment index. */
    private fun segmentIndexOf(boundaries: List<Int>, pos: Int): Int {
        var idx = 0
        while (idx < boundaries.size && boundaries[idx] < pos) idx++
        return idx
    }

    fun addArabicCommas(text: String): String {
        if (isQuranVerse(text)) return text

        val positions = findConnectorPositions(text)
        if (positions.size < 2) return text

        val segBoundaries = findSegmentBoundaries(text)

        // Group connector indices by segment index.
        // Map<segIdx, List<connectorPosIndex>>
        val segPositions = LinkedHashMap<Int, MutableList<Int>>()
        positions.forEachIndexed { i, pos ->
            val segIdx = segmentIndexOf(segBoundaries, pos)
            segPositions.getOrPut(segIdx) { mutableListOf() }.add(i)
        }

        // Build the skip set — positions where we will NOT add a comma.
        val skipSet = HashSet<Int>()
        segPositions.values.forEach { posIndices ->
            if (posIndices.size < 2) {
                // Single connector in segment — don't add comma.
                posIndices.forEach { skipSet.add(it) }
                return@forEach
            }

            // Find phrase-pattern positions in this segment.
            val phraseIndices = mutableListOf<Int>()
            posIndices.forEach { i ->
                val pos = positions[i]
                val after = text.substring(pos)
                if (PHRASE_PATTERNS.any { after.startsWith(it) }) {
                    phraseIndices.add(i)
                }
            }

            // Skip phrase positions AND the connector immediately after them.
            phraseIndices.forEach { i ->
                skipSet.add(i)
                val idxInSeg = posIndices.indexOf(i)
                if (idxInSeg + 1 < posIndices.size) {
                    skipSet.add(posIndices[idxInSeg + 1])
                }
            }

            // Always skip the LAST connector in the segment.
            skipSet.add(posIndices.last())
        }

        // Build the result string by walking through positions.
        val sb = StringBuilder(text.length + 16)
        var lastEnd = 0
        positions.forEachIndexed { i, pos ->
            sb.append(text, lastEnd, pos)
            sb.append(if (skipSet.contains(i)) CONNECTOR else "،$CONNECTOR")
            lastEnd = pos + CONNECTOR_LEN
        }
        if (lastEnd < text.length) {
            sb.append(text, lastEnd, text.length)
        }
        return sb.toString()
    }
}
