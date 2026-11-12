package my.tahlil.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import my.tahlil.app.data.ArabicAlign
import my.tahlil.app.data.ArabicStyleMode
import my.tahlil.app.data.ArabicStyleSettings
import my.tahlil.app.data.DisplayToggles
import my.tahlil.app.util.ArabicCommaUtil
import my.tahlil.app.util.AyahMarkUtil
import my.tahlil.app.util.ColorizeUtil
import my.tahlil.app.util.TajwidUtil

/**
 * ArabicText — renders an Arabic string with the user's chosen style settings
 * (font mode, size, line-height, word spacing, letter spacing, alignment)
 * applied consistently. RTL is enforced via [TextDirection.Rtl].
 *
 * The text content is never modified — only typography/visual presentation,
 * EXCEPT when [toggles.punctuateArabic] is on, in which case Arabic commas
 * are inserted before " وَ" connectors (see [ArabicCommaUtil.addArabicCommas]).
 *
 * Ayah marks (۝ followed by Arabic-Indic digits) are detected and rendered
 * as a styled circle with the digit overlaid in the center — mimicking
 * proper Mushaf typography. Each ayah (verse + mark) is wrapped in its own
 * line (Column with spacedBy) so each verse gets its own row for
 * readability.
 *
 * For Arabic text WITHOUT ayah marks (e.g., long dua text), split by Arabic
 * comma (،) and Latin period (.) so each clause also gets its own line —
 * unless [DisplayToggles.splitArabicLines] is off, in which case the text
 * flows as one paragraph and wraps naturally.
 *
 * If [toggles.colorizeArabic] is on, each non-whitespace word is wrapped in
 * a colored span with alternating shades picked by theme:
 *  - Light theme (dark text)   → #000000 / #011b45
 *  - Dark theme  (light text)  → #ffffff / #d9dadb
 * The alternation is CONTINUOUS across all lines (shared counter).
 *
 * If [toggles.tajwid] is on, tajwid recitation rules (Izhar, Ikhfa, Idgham,
 * Iqlab, Ghunnah, Qalqalah — detected by [TajwidUtil.analyzeTajwid]) are
 * wrapped in per-rule colored spans instead. Tajwid takes PRECEDENCE over
 * word colorization (same behavior as the web).
 *
 * Port of `ArabicText` from `arabic-text.tsx`.
 *
 * ALIGNMENT NOTE (bug fix): each rendered line is a standalone [Text] inside
 * a [Column]. A Text that wraps its content gives [TextAlign] nothing to
 * align against — the line would sit at the Column's start (LEFT edge) no
 * matter what [TextAlign] says, which is exactly why short Arabic clauses
 * appeared left-aligned. Every line therefore gets `Modifier.fillMaxWidth()`
 * so `TextAlign.Right` / `TextAlign.Justify` has the full width to align
 * against — mirroring the web, where `direction: rtl` + `text-align: right`
 * applies to a full-width block element.
 */
@Composable
fun ArabicText(
    text: String,
    style: ArabicStyleSettings,
    toggles: DisplayToggles,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    suffix: (@Composable () -> Unit)? = null,
) {
    val fontFamily = remember(style.mode) {
        when (style.mode) {
            ArabicStyleMode.Standard -> FontFamily.Default
            ArabicStyleMode.Quran, ArabicStyleMode.Naskhi -> FontFamily.Serif
        }
    }

    val textAlign = when (style.align) {
        ArabicAlign.Right -> TextAlign.Right
        ArabicAlign.Justify -> TextAlign.Justify
        ArabicAlign.Center -> TextAlign.Center
    }

    // Apply Arabic comma transformation if the toggle is on.
    // (No-op for Quran verses — handled internally.)
    val renderedText = remember(text, toggles.punctuateArabic) {
        if (toggles.punctuateArabic) ArabicCommaUtil.addArabicCommas(text) else text
    }

    // Pick color pair based on theme.
    val colors = if (toggles.colorizeArabic) {
        if (isDark) ColorizeUtil.DarkColors else ColorizeUtil.LightColors
    } else {
        null
    }

    // Tajwid takes precedence over word colorization (same as the web):
    // while it is on, the colorize pair is ignored entirely.
    val tajwidOn = toggles.tajwid
    val effectiveColors = if (tajwidOn) null else colors

    // Shared counter for colorize alternation — reset each render.
    val counter = remember(text, toggles, isDark, style) { ColorizeUtil.Counter() }
    val baseTextStyle = TextStyle(
        fontFamily = fontFamily,
        fontSize = (style.fontSize * 16f).sp,  // rem → sp (1rem ≈ 16sp)
        lineHeight = (style.fontSize * 16f * style.lineHeight).sp,
        letterSpacing = (style.letterSpacing * 16f).sp,  // em → sp
        textAlign = textAlign,
        textDirection = TextDirection.Rtl,
        color = MaterialTheme.colorScheme.onSurface,
    )

    val lines = remember(renderedText, effectiveColors, counter, toggles.splitArabicLines, tajwidOn, isDark) {
        buildLines(renderedText, effectiveColors, counter, toggles.splitArabicLines, tajwidOn, isDark)
    }

    SelectionContainer {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            // End = right in this (LTR) composition context — keeps the
            // suffix (repeat badge) and any wrap-content child on the
            // Arabic side of the block.
            horizontalAlignment = Alignment.End,
        ) {
            lines.forEach { line ->
                ArabicLineRenderer(
                    line = line,
                    baseStyle = baseTextStyle,
                    isDark = isDark,
                )
            }
            suffix?.invoke()
        }
    }
}

/**
 * One line of Arabic text — either a verse ending with an ayah mark, or a
 * plain clause (sentence-split line).
 */
private sealed class Line {
    data class Ayah(val text: AnnotatedString, val digits: String) : Line()
    data class Sentence(val text: AnnotatedString) : Line()
    data class Trailing(val text: AnnotatedString) : Line()
}

@Composable
private fun ArabicLineRenderer(line: Line, baseStyle: TextStyle, isDark: Boolean) {
    when (line) {
        is Line.Ayah -> {
            // Render in an RTL layout direction so the verse text appears on the
            // right (start of RTL reading direction) and the ayah mark appears on
            // the left (end of the verse — matching Mushaf convention).
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalLayoutDirection provides
                    androidx.compose.ui.unit.LayoutDirection.Rtl
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier,
                ) {
                    if (line.text.isNotEmpty()) {
                        Text(
                            text = line.text,
                            style = baseStyle,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    AyahMark(digits = line.digits, isDark = isDark)
                }
            }
        }
        is Line.Sentence -> Text(
            text = line.text,
            style = baseStyle,
            // fillMaxWidth is REQUIRED for TextAlign.Right/Justify to take
            // effect — a wrap-content Text has nothing to align against.
            modifier = Modifier.fillMaxWidth(),
        )
        is Line.Trailing -> Text(
            text = line.text,
            style = baseStyle,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Render a single ayah mark (۝X) as an ornamental circle with the digit
 * centered inside. Matches the web app's `.ayah-mark` styling — a soft
 * circular ornament with the digit overlaid.
 */
@Composable
private fun AyahMark(digits: String, isDark: Boolean) {
    if (digits.isEmpty()) return
    val accentColor = MaterialTheme.colorScheme.primary
    val digitColor = MaterialTheme.colorScheme.onSurface
    val isMultiDigit = digits.length > 1
    val size = if (isMultiDigit) 28.dp else 22.dp

    Box(
        modifier = Modifier
            .size(size)
            .background(accentColor.copy(alpha = 0.12f), CircleShape)
            .border(1.5.dp, accentColor.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        // The decorative ۝ ornament sits behind the digit, faded.
        Text(
            text = "۝",
            color = accentColor.copy(alpha = 0.35f),
            style = TextStyle(
                fontSize = (size.value * 0.95f).sp,
                fontFamily = FontFamily.Serif,
                textDirection = TextDirection.Rtl,
            ),
            modifier = Modifier.alpha(0.6f),
        )
        // The digit centered.
        Text(
            text = digits,
            color = digitColor,
            style = TextStyle(
                fontSize = (size.value * 0.42f).sp,
                fontFamily = FontFamily.Serif,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                textDirection = TextDirection.Rtl,
            ),
        )
    }
}

/**
 * Build the list of [Line]s for the given [text].
 *
 * If the text contains ayah marks (۝X), split by ayah marks — each verse
 * ends with its mark on the same line.
 * Otherwise split by sentence delimiters (، or .) — each clause ends with
 * its delimiter on its own line.
 *
 * When [splitSentences] is false, the comma/period line-breaking is
 * disabled — text without ayah marks renders as ONE flowing paragraph
 * (wrapping naturally by width) and text within ayah verses is NOT broken
 * per clause either. Ayah-marked verses still get their own line because
 * that is verse structure, not comma formatting.
 *
 * Each non-whitespace word within a line is colored alternately if
 * [colors] is non-null. The [counter] is shared across all lines.
 */
private fun buildLines(
    text: String,
    colors: ColorizeUtil.ColorPair?,
    counter: ColorizeUtil.Counter,
    splitSentences: Boolean,
    tajwidOn: Boolean,
    isDark: Boolean,
): List<Line> {
    val ayahMarks = AyahMarkUtil.findMarks(text)
    val out = mutableListOf<Line>()

    if (ayahMarks.isEmpty()) {
        if (!splitSentences) {
            if (text.isNotBlank()) {
                out.add(Line.Sentence(colorizeLine(text.trim(), colors, counter, tajwidOn, isDark)))
            }
            return out
        }
        // No ayah marks — split by sentence.
        AyahMarkUtil.splitBySentence(text).forEach { sentenceLine ->
            out.add(Line.Sentence(colorizeLine(sentenceLine.text, colors, counter, tajwidOn, isDark)))
        }
        return out
    }

    var cursor = 0
    for (mark in ayahMarks) {
        val verse = text.substring(cursor, mark.start)
        if (!splitSentences) {
            // Keep the whole verse on one line — comma splitting off.
            out.add(Line.Ayah(colorizeLine(verse, colors, counter, tajwidOn, isDark), mark.digits))
        } else {
            // Try sentence-splitting within the verse for dua-style text mixed
            // with ayah marks. If splitBySentence yields one fragment, colorize
            // and emit as a verse. If multiple, emit each as a sentence then a
            // final verse with the mark.
            val sentences = AyahMarkUtil.splitBySentence(verse)
            if (sentences.size <= 1) {
                out.add(Line.Ayah(colorizeLine(verse, colors, counter, tajwidOn, isDark), mark.digits))
            } else {
                // Emit each sentence, then attach the mark to the LAST fragment.
                sentences.forEachIndexed { i, s ->
                    if (i == sentences.lastIndex) {
                        out.add(Line.Ayah(colorizeLine(s.text, colors, counter, tajwidOn, isDark), mark.digits))
                    } else {
                        out.add(Line.Sentence(colorizeLine(s.text, colors, counter, tajwidOn, isDark)))
                    }
                }
            }
        }
        cursor = mark.endExclusive
    }

    // Trailing text after the last mark.
    if (cursor < text.length) {
        val trailing = text.substring(cursor)
        if (!splitSentences) {
            if (trailing.isNotBlank()) {
                out.add(Line.Trailing(colorizeLine(trailing.trim(), colors, counter, tajwidOn, isDark)))
            }
        } else {
            AyahMarkUtil.splitBySentence(trailing).forEach { s ->
                out.add(Line.Trailing(colorizeLine(s.text, colors, counter, tajwidOn, isDark)))
            }
        }
    }
    return out
}

/**
 * Colorize a single line of text word-by-word if [colors] is non-null,
 * otherwise return a plain [AnnotatedString].
 *
 * When [tajwidOn] is true, tajwid rule segments get per-rule colors and
 * word colorization is skipped entirely (rule colors take precedence —
 * matching the web's `decorateText` entry point).
 *
 * Whitespace tokens are preserved verbatim so word spacing is unaffected.
 */
private fun colorizeLine(
    text: String,
    colors: ColorizeUtil.ColorPair?,
    counter: ColorizeUtil.Counter,
    tajwidOn: Boolean,
    isDark: Boolean,
): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")
    if (tajwidOn) return tajwidLine(text, isDark)
    if (colors == null) return AnnotatedString(text)

    return buildAnnotatedString {
        val tokens = ColorizeUtil.tokenize(text)
        for (token in tokens) {
            when (token) {
                is ColorizeUtil.Token.Whitespace -> append(token.text)
                is ColorizeUtil.Token.Word -> {
                    val color = ColorizeUtil.nextColor(colors, counter)
                    withStyle(SpanStyle(color = color)) { append(token.text) }
                }
            }
        }
    }
}

/**
 * Wrap tajwid rule segments (detected by [TajwidUtil.analyzeTajwid]) in
 * colored spans — the Compose counterpart of the web's `renderTajwidText`.
 * Ranges are already sorted & overlap-resolved by the engine, so this is
 * a simple linear walk.
 */
private fun tajwidLine(text: String, isDark: Boolean): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")
    val ranges = TajwidUtil.analyzeTajwid(text)
    if (ranges.isEmpty()) return AnnotatedString(text)
    return buildAnnotatedString {
        var cursor = 0
        for (r in ranges) {
            if (r.start > cursor) append(text.substring(cursor, r.start))
            withStyle(SpanStyle(color = TajwidUtil.colorFor(r.rule, isDark))) {
                append(text.substring(r.start, r.end))
            }
            cursor = r.end
        }
        if (cursor < text.length) append(text.substring(cursor))
    }
}
