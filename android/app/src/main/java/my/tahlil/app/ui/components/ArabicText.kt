package my.tahlil.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import my.tahlil.app.data.ArabicAlign
import my.tahlil.app.data.ArabicStyleMode
import my.tahlil.app.data.ArabicStyleSettings
import my.tahlil.app.data.DisplayToggles
import my.tahlil.app.util.ArabicCommaUtil
import my.tahlil.app.util.AyahMarkUtil
import my.tahlil.app.util.ColorizeUtil
import my.tahlil.app.util.TajwidUtil
import my.tahlil.app.util.WaqafUtil

/**
 * Waqaf highlight blue — the exact brand blue used by the web's
 * `.waqaf-mark` class in light theme.
 */
internal val WAQAF_BLUE_LIGHT = Color(0xFF1B51D0)

/** Waqaf highlight blue — lightened in dark theme for contrast (same as web). */
internal val WAQAF_BLUE_DARK = Color(0xFF7B9DFF)

/**
 * Waqaf blue for the active theme — shared by the text highlighting in
 * [ArabicText] and the settings legend swatch so both always match.
 */
internal fun waqafColorFor(isDark: Boolean): Color =
    if (isDark) WAQAF_BLUE_DARK else WAQAF_BLUE_LIGHT

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
 * as a circled digit INLINE at the end of the verse text — the badge flows
 * with the text via Compose `inlineContent`, so it always hugs the last
 * word of the verse exactly like the web's `.ayah-mark` span (mimicking
 * proper Mushaf typography). Each ayah (verse + mark) is wrapped in its
 * own line (Column with spacedBy) so each verse gets its own row for
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
 * If [toggles.waqafColor] is on, waqaf segments (letters whose reading
 * changes when the reciter stops — detected by [WaqafUtil.analyzeWaqaf])
 * are filled with blue ([WAQAF_BLUE_LIGHT] / [WAQAF_BLUE_DARK]). It composes
 * with tajwid: tajwid ranges keep their colors and the waqaf blue only fills
 * the sub-segments NOT already covered by a tajwid rule (same behavior as
 * the web's `renderTajwidAndWaqafText`). Either toggle being on disables
 * word colorization.
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

    // Apply Arabic comma transformation if the toggle is on.
    // (No-op for Quran verses — handled internally.)
    val renderedText = remember(text, toggles.punctuateArabic) {
        if (toggles.punctuateArabic) ArabicCommaUtil.addArabicCommas(text) else text
    }

    // Resolve the actual text alignment. "Smart" picks adaptively based on
    // content: Quran verses (with ۝/۩ ayah marks) → Right (Mushaf
    // convention — Quran is never justified in Mushafs); other text
    // (dua, dzikir, shalawat) → Justify for a cleaner block reading.
    // Short single-line phrases are unaffected by justify (no wrap to stretch).
    val textAlign = when (style.align) {
        ArabicAlign.Smart -> if (renderedText.contains("۝") || renderedText.contains("۩")) {
            TextAlign.Right
        } else {
            TextAlign.Justify
        }
        ArabicAlign.Right -> TextAlign.Right
        ArabicAlign.Justify -> TextAlign.Justify
        ArabicAlign.Center -> TextAlign.Center
    }

    // Pick color pair based on theme.
    val colors = if (toggles.colorizeArabic) {
        if (isDark) ColorizeUtil.DarkColors else ColorizeUtil.LightColors
    } else {
        null
    }

    // Tajwid takes precedence over word colorization (same as the web):
    // while it is on, the colorize pair is ignored entirely. The waqaf
    // toggle composes with tajwid instead (its blue only fills segments
    // not already covered by a tajwid rule).
    val tajwidOn = toggles.tajwid
    val waqafOn = toggles.waqafColor
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

    val lines = remember(
        renderedText, effectiveColors, counter, toggles.splitArabicLines, tajwidOn, waqafOn, isDark,
    ) {
        buildLines(
            renderedText, effectiveColors, counter, toggles.splitArabicLines, tajwidOn, waqafOn, isDark,
        )
    }

    SelectionContainer {
        Column(
            modifier = modifier,
            // The user-adjustable "Jarak Antar Kalimat" slider
            // (style.bodyLineHeight, in em) controls the breathing room
            // BETWEEN Arabic kalimat/sentences (ayah lines & comma-split
            // clause lines). 1em = fontSize in sp (1rem ≈ 16sp), so we
            // convert em → dp via LocalDensity.
            verticalArrangement = Arrangement.spacedBy(
                with(androidx.compose.ui.platform.LocalDensity.current) {
                    (style.fontSize * 16f * style.bodyLineHeight).sp.toDp()
                }
            ),
            // End = right in this (LTR) composition context — keeps the
            // suffix (repeat badge) and any wrap-content child on the
            // Arabic side of the block.
            horizontalAlignment = Alignment.End,
        ) {
            lines.forEach { line ->
                ArabicLineRenderer(
                    line = line,
                    baseStyle = baseTextStyle,
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

/** Inline-content id used to embed the ayah mark inside the verse Text. */
private const val AYAH_MARK_INLINE_ID = "ayah-mark"

@Composable
private fun ArabicLineRenderer(line: Line, baseStyle: TextStyle) {
    when (line) {
        is Line.Ayah -> {
            // The ayah mark is embedded as INLINE CONTENT at the end of the
            // verse text: the circled digit becomes part of the text flow and
            // always sits immediately after (RTL: to the left of) the last
            // word — never pinned to the margin. Matches the web, where the
            // .ayah-mark span flows inline inside the .ayah-line block.
            val annotated = remember(line) {
                buildAnnotatedString {
                    append(line.text)
                    appendInlineContent(AYAH_MARK_INLINE_ID, "[${line.digits}]")
                }
            }
            // Placeholder box sized in em so the badge scales with the
            // user's font-size setting. Width grows for multi-digit marks
            // (e.g. ۝٢٨٦). Vertical: centered on the text line.
            val markWidthEm = 0.675f + 0.25f * (line.digits.length - 1).coerceAtLeast(0)
            Text(
                text = annotated,
                style = baseStyle,
                // fillMaxWidth is REQUIRED for TextAlign.Right/Justify to
                // take effect — same reason as the Sentence branch.
                modifier = Modifier.fillMaxWidth(),
                inlineContent = mapOf(
                    AYAH_MARK_INLINE_ID to InlineTextContent(
                        Placeholder(
                            width = markWidthEm.em,
                            height = 0.675f.em,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                        )
                    ) {
                        AyahMark(
                            digits = line.digits,
                            baseFontSize = baseStyle.fontSize,
                        )
                    },
                ),
            )
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
 * centered inside. Matches the web app's `.ayah-mark` styling — a circular
 * ornament with the digit overlaid (the raw ۝ glyph is NOT drawn; the
 * circle is vector-drawn so it renders identically on every device font).
 *
 * The composable FILLS the inline-content placeholder box, which is sized
 * in em relative to the verse font size — so the badge scales together
 * with the user's font-size setting.
 */
@Composable
private fun AyahMark(digits: String, baseFontSize: TextUnit) {
    if (digits.isEmpty()) return
    val accentColor = MaterialTheme.colorScheme.primary
    val digitColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(accentColor.copy(alpha = 0.18f), CircleShape)
            .border(1.dp, accentColor.copy(alpha = 0.80f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        // The digit centered — same 0.275em proportion as the web's
        // .ayah-mark-digit (halved from 0.55em per user request:
        // "lingkaran angka ayat ukurannya kecilkan 50%").
        Text(
            text = digits,
            color = digitColor,
            style = TextStyle(
                fontSize = (baseFontSize.value * 0.275f).sp,
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
 *
 * [tajwidOn] / [waqafOn] switch to the combined tajwid+waqaf renderer
 * ([tajwidAndWaqafLine]) — rule colors take precedence, the waqaf blue only
 * fills uncovered sub-segments.
 */
private fun buildLines(
    text: String,
    colors: ColorizeUtil.ColorPair?,
    counter: ColorizeUtil.Counter,
    splitSentences: Boolean,
    tajwidOn: Boolean,
    waqafOn: Boolean,
    isDark: Boolean,
): List<Line> {
    val ayahMarks = AyahMarkUtil.findMarks(text)
    val out = mutableListOf<Line>()

    if (ayahMarks.isEmpty()) {
        if (!splitSentences) {
            if (text.isNotBlank()) {
                out.add(Line.Sentence(colorizeLine(text.trim(), colors, counter, tajwidOn, waqafOn, isDark)))
            }
            return out
        }
        // No ayah marks — split by sentence.
        AyahMarkUtil.splitBySentence(text).forEach { sentenceLine ->
            out.add(Line.Sentence(colorizeLine(sentenceLine.text, colors, counter, tajwidOn, waqafOn, isDark)))
        }
        return out
    }

    var cursor = 0
    for (mark in ayahMarks) {
        val verse = text.substring(cursor, mark.start)
        if (!splitSentences) {
            // Keep the whole verse on one line — comma splitting off.
            out.add(Line.Ayah(colorizeLine(verse, colors, counter, tajwidOn, waqafOn, isDark), mark.digits))
        } else {
            // Try sentence-splitting within the verse for dua-style text mixed
            // with ayah marks. If splitBySentence yields one fragment, colorize
            // and emit as a verse. If multiple, emit each as a sentence then a
            // final verse with the mark.
            val sentences = AyahMarkUtil.splitBySentence(verse)
            if (sentences.size <= 1) {
                out.add(Line.Ayah(colorizeLine(verse, colors, counter, tajwidOn, waqafOn, isDark), mark.digits))
            } else {
                // Emit each sentence, then attach the mark to the LAST fragment.
                sentences.forEachIndexed { i, s ->
                    if (i == sentences.lastIndex) {
                        out.add(Line.Ayah(colorizeLine(s.text, colors, counter, tajwidOn, waqafOn, isDark), mark.digits))
                    } else {
                        out.add(Line.Sentence(colorizeLine(s.text, colors, counter, tajwidOn, waqafOn, isDark)))
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
                out.add(Line.Trailing(colorizeLine(trailing.trim(), colors, counter, tajwidOn, waqafOn, isDark)))
            }
        } else {
            AyahMarkUtil.splitBySentence(trailing).forEach { s ->
                out.add(Line.Trailing(colorizeLine(s.text, colors, counter, tajwidOn, waqafOn, isDark)))
            }
        }
    }
    return out
}

/**
 * Colorize a single line of text word-by-word if [colors] is non-null,
 * otherwise return a plain [AnnotatedString].
 *
 * When [tajwidOn] or [waqafOn] is true, the combined tajwid+waqaf renderer
 * is used instead and word colorization is skipped entirely (rule colors
 * take precedence — matching the web's `decorateText` entry point).
 *
 * Whitespace tokens are preserved verbatim so word spacing is unaffected.
 */
private fun colorizeLine(
    text: String,
    colors: ColorizeUtil.ColorPair?,
    counter: ColorizeUtil.Counter,
    tajwidOn: Boolean,
    waqafOn: Boolean,
    isDark: Boolean,
): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")
    if (tajwidOn || waqafOn) return tajwidAndWaqafLine(text, isDark, waqafOn)
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
 * One highlighted segment in the combined tajwid+waqaf render — either a
 * tajwid rule span or a waqaf (blue) span.
 */
private sealed class HighlightSeg {
    abstract val start: Int
    abstract val end: Int
    data class Tajwid(
        override val start: Int,
        override val end: Int,
        val rule: TajwidUtil.Rule,
    ) : HighlightSeg()
    data class Waqaf(override val start: Int, override val end: Int) : HighlightSeg()
}

/**
 * Wrap tajwid rule segments (detected by [TajwidUtil.analyzeTajwid]) AND
 * waqaf segments (detected by [WaqafUtil.analyzeWaqaf], when [waqafOn] is
 * true) in colored spans — the Compose counterpart of the web's
 * `renderTajwidAndWaqafText`. All ranges are already sorted &
 * non-overlapping WITHIN each set, so the merge is a simple walk:
 *
 *  - Tajwid ranges take PRECEDENCE: any overlap between a waqaf range and
 *    a tajwid range is clipped away from the waqaf side, so the seven
 *    tajwid colors are never disturbed.
 *  - Waqaf segments get the theme-aware waqaf blue
 *    ([WAQAF_BLUE_LIGHT] / [WAQAF_BLUE_DARK]).
 */
private fun tajwidAndWaqafLine(text: String, isDark: Boolean, waqafOn: Boolean): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")
    val tajwidRanges = TajwidUtil.analyzeTajwid(text)
    val waqafRanges = if (waqafOn) WaqafUtil.analyzeWaqaf(text) else emptyList()
    if (tajwidRanges.isEmpty() && waqafRanges.isEmpty()) return AnnotatedString(text)

    val segs = mutableListOf<HighlightSeg>()
    tajwidRanges.forEach { r ->
        segs.add(HighlightSeg.Tajwid(r.start, r.end, r.rule))
    }

    // Subtract tajwid-covered intervals from each waqaf range. Tajwid ranges
    // are sorted & disjoint, so a linear clip per waqaf range suffices.
    for (w in waqafRanges) {
        var s = w.start
        for (t in tajwidRanges) {
            if (t.end <= s) continue // entirely before
            if (t.start >= w.end) break // entirely after
            if (t.start > s) {
                segs.add(HighlightSeg.Waqaf(s, minOf(t.start, w.end)))
            }
            s = maxOf(s, t.end)
            if (s >= w.end) break
        }
        if (s < w.end) segs.add(HighlightSeg.Waqaf(s, w.end))
    }

    segs.sortBy { it.start }

    return buildAnnotatedString {
        var cursor = 0
        for (seg in segs) {
            if (seg.start > cursor) append(text.substring(cursor, seg.start))
            when (seg) {
                is HighlightSeg.Tajwid -> withStyle(
                    SpanStyle(color = TajwidUtil.colorFor(seg.rule, isDark))
                ) {
                    append(text.substring(seg.start, seg.end))
                }
                is HighlightSeg.Waqaf -> withStyle(
                    SpanStyle(color = waqafColorFor(isDark))
                ) {
                    append(text.substring(seg.start, seg.end))
                }
            }
            cursor = seg.end
        }
        if (cursor < text.length) append(text.substring(cursor))
    }
}
