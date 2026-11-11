package my.tahlil.app.util

import androidx.compose.ui.graphics.Color

/**
 * Colorize Arabic text word-by-word, alternating between two colors.
 *
 * Port of `colorizeWords()` from `arabic-text.tsx`.
 *
 * The alternation must be CONTINUOUS across multiple invocations (e.g.,
 * across ayah lines or sentence-split lines) so the user sees a continuous
 * A/B/A/B pattern rather than restarting at color A on every line.
 *
 * Pass a shared [Counter] instance to all calls within a single ArabicText
 * render so the running word index persists across lines.
 */
object ColorizeUtil {

    /** Theme-aware color pairs (matches the web app exactly). */
    data class ColorPair(val first: Color, val second: Color)

    val LightColors = ColorPair(
        first = Color(0xFF000000),
        second = Color(0xFF011B45),
    )

    val DarkColors = ColorPair(
        first = Color(0xFFFFFFFF),
        second = Color(0xFFD9DADB),
    )

    /**
     * Mutable counter shared across all colorize calls within one ArabicText
     * render. Each call increments the counter for every word it colors so
     * the alternation continues across lines.
     */
    class Counter {
        var value: Int = 0
        fun reset() { value = 0 }
    }

    /**
     * A token produced by splitting Arabic text on whitespace. Either a
     * whitespace token (preserved verbatim so word spacing is unaffected)
     * or a word token (gets colored).
     */
    sealed class Token {
        data class Whitespace(val text: String) : Token()
        data class Word(val text: String) : Token()
    }

    /**
     * Split [text] into whitespace and word tokens, preserving the
     * whitespace so the original spacing is retained.
     */
    fun tokenize(text: String): List<Token> {
        if (text.isEmpty()) return emptyList()
        val out = mutableListOf<Token>()
        val sb = StringBuilder()
        var inWhitespace = false

        for (c in text) {
            val isWs = c.isWhitespace()
            if (sb.isEmpty()) {
                sb.append(c)
                inWhitespace = isWs
            } else if (isWs == inWhitespace) {
                sb.append(c)
            } else {
                flush(sb, inWhitespace, out)
                sb.append(c)
                inWhitespace = isWs
            }
        }
        if (sb.isNotEmpty()) flush(sb, inWhitespace, out)
        return out
    }

    private fun flush(sb: StringBuilder, wasWhitespace: Boolean, out: MutableList<Token>) {
        val s = sb.toString()
        if (wasWhitespace) out.add(Token.Whitespace(s)) else out.add(Token.Word(s))
        sb.setLength(0)
    }

    /**
     * Pick the next color from [pair] using [counter], incrementing the
     * counter for each word consumed.
     */
    fun nextColor(pair: ColorPair, counter: Counter): Color {
        val c = if (counter.value % 2 == 0) pair.first else pair.second
        counter.value++
        return c
    }
}
