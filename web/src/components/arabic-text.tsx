"use client";

import * as React from "react";
import { useTheme } from "next-themes";
import { useArabicStyle } from "@/components/arabic-style-provider";
import { useDisplaySettings } from "@/components/display-settings-provider";
import { arabicStyleModes, type ArabicStyleSettings } from "@/data/arabic-style";
import { analyzeTajwid } from "@/lib/tajwid";
import { analyzeWaqaf } from "@/lib/waqaf";
import { analyzeHamzaWashal } from "@/lib/hamza-washal";

/**
 * ArabicText — renders an Arabic string with the user's chosen style settings
 * (font family, font size, line-height, word spacing, letter spacing,
 * alignment) applied consistently. RTL is enforced via dir="rtl".
 *
 * The text content is never modified — only typography/visual presentation.
 *
 * `forceMode` can be used to render with a specific mode (used for the
 * side-by-side comparison view).
 *
 * Ayah marks (۝ followed by Arabic-Indic digits) are detected and rendered
 * as a styled span with the digit overlaid in the center of the ornamental
 * ۝ symbol — mimicking proper Mushaf typography.
 *
 * Each ayat (mark + following text) is wrapped in a `.ayah-line` span so
 * that on mobile (CSS `max-width: 640px`) each verse breaks to a new line
 * for readability; on desktop verses flow inline (Mushaf style).
 *
 * Two optional display toggles further transform the rendering:
 *  - `punctuateArabic`: inserts Arabic commas (،) before "وَ" connectors
 *    to mirror the punctuation already present in the Indonesian
 *    translation. Quran verses (with ۝ or ۩) are never modified.
 *  - `splitArabicLines`: when true (default) Arabic text without ayah
 *    marks is broken into lines at Arabic commas (،) and Latin periods
 *    (.) so each clause gets its own line. When false, the text flows as
 *    a single paragraph and wraps naturally. Ayah-marked verses always
 *    keep their per-verse lines either way.
 *  - `colorizeArabic`: wraps each Arabic word in a colored span with two
 *    alternating shades picked by theme:
 *      - Light theme (dark text)   → #000000 / #011b45
 *      - Dark theme  (light text)  → #ffffff / #d9dadb
 *    Ayah marks (۝X) keep their own styling and are not colorized.
 *    Ignored while `tajwid` is on (rule colors take precedence).
 *  - `tajwid`: when true, tajwid recitation rules detected by
 *    src/lib/tajwid.ts (Izhar, Ikhfa, Idgham, Iqlab, Ghunnah, Qalqalah)
 *    are wrapped in `.tajwid-<rule>` spans colored via globals.css.
 *    Detection is deterministic (pure function of the text), so unlike
 *    colorize it needs no "mounted" gate — SSR and client agree.
 *  - Hamzatul Washal (auto with tajwid): when tajwid is on, the wasl
 *    alifs that stay SILENT in connected recitation (src/lib/hamza-washal.ts)
 *    are wrapped in `.hamza-washal-mark` spans (grayish blue #7893B0).
 *    No separate toggle — the marking follows the tajwid toggle.
 */

// Matches ۝ (U+06DD ARABIC END OF AYAH) followed by one or more Arabic-Indic
// digits (٠-٩). Capturing group lets String.split() include the marks.
const AYAH_MARK_REGEX = /(۝[٠-٩]+)/g;

/**
 * Resolve the actual text-align value for a given Arabic string under the
 * user's chosen alignment setting.
 *
 * "smart" mode picks adaptively based on content:
 *  - Text containing Quran ayah marks (۝ or ۩) → Right. Quran verses are
 *    traditionally right-aligned in Mushafs and never justified.
 *  - Other text (dua, dzikir, shalawat) → Justify. Each clause is already
 *    on its own line (display:block), so justify only stretches clauses that
 *    wrap to multiple visual lines — producing a cleaner block reading.
 *    Short single-line phrases are unaffected by justify (nothing to stretch).
 *
 * The other modes ("right" / "justify" / "center") pass through unchanged.
 */
function resolveTextAlign(
  align: ArabicStyleSettings["align"],
  text: string,
): "right" | "justify" | "center" {
  if (align !== "smart") return align;
  if (text.includes("۝") || text.includes("۩")) return "right";
  return "justify";
}

/** Color pairs for the colorizeArabic feature, indexed by resolved theme. */
const COLORIZE_COLORS = {
  light: ["#000000", "#011b45"], // dark text on light bg
  dark: ["#ffffff", "#d9dadb"], // light text on dark bg
} as const;

/**
 * Insert Arabic commas (،) before " وَ" connectors so that the Arabic
 * text mirrors the comma punctuation that already exists in the
 * Indonesian translation. For example, "وَعَلَى آلِهِ وَأَصْحَابِهِ
 * وَأَزْوَاجِهِ وَذُرِّيَّتِهِ وَأَهْلِ بَيْتِهِ" (which renders as a
 * single long unbroken run) becomes "وَعَلَى آلِهِ، وَأَصْحَابِهِ،
 * وَأَزْوَاجِهِ، وَذُرِّيَّتِهِ وَأَهْلِ بَيْتِهِ" — matching the
 * translation "keluarga, sahabat, istri, anak cucu dan Ahli Baitnya".
 *
 * Rules:
 *  - Quran verses (text containing ۝ or ۩) are returned unchanged.
 *  - The text is split into segments by existing major punctuation
 *    (Arabic comma ، and Latin period .).
 *  - Within each segment, the following " وَ" connectors are skipped:
 *      a) Connectors that are part of a fixed phrase (e.g., " وَسَلَّمْ"
 *         in "صلى الله عليه وسلم" — never split).
 *      b) The connector immediately after a phrase (it introduces the
 *         first list item, e.g., " وَعَلَى آلِهِ" — translation has no
 *         comma before "keluarga").
 *      c) The LAST connector in the segment (it's the "and" before the
 *         final list item — matches "X, Y, Z dan W" pattern).
 *  - All other " وَ" connectors in the segment get a "، " prefix.
 *  - Single-connector segments (no list) are left unchanged.
 */
const PHRASE_PATTERNS = [" وَسَلَّمْ", " وَسَلِّمْ"];

function addArabicCommas(text: string): string {
  // Skip Quran verses — they must never be modified.
  if (text.includes("۝") || text.includes("۩")) return text;

  // Find all " وَ" connector positions in the text.
  const positions: number[] = [];
  let searchPos = 0;
  while (true) {
    const idx = text.indexOf(" وَ", searchPos);
    if (idx === -1) break;
    positions.push(idx);
    searchPos = idx + 3; // Move past " وَ"
  }

  if (positions.length < 2) return text;

  // Find segment boundaries (، or .) so we can group connectors per segment.
  const segBoundaries: number[] = [];
  for (let i = 0; i < text.length; i++) {
    if (text[i] === "،" || text[i] === ".") {
      segBoundaries.push(i);
    }
  }
  const getSegIdx = (pos: number): number => {
    let idx = 0;
    while (idx < segBoundaries.length && segBoundaries[idx] < pos) idx++;
    return idx;
  };

  // Group connector positions by segment index.
  const segPositions = new Map<number, number[]>();
  positions.forEach((pos, i) => {
    const segIdx = getSegIdx(pos);
    if (!segPositions.has(segIdx)) segPositions.set(segIdx, []);
    segPositions.get(segIdx)!.push(i);
  });

  // Build the skip set — positions where we will NOT add a comma.
  const skipSet = new Set<number>();
  segPositions.forEach((posIndices) => {
    if (posIndices.length < 2) {
      // Single connector in segment — don't add comma.
      posIndices.forEach((i) => skipSet.add(i));
      return;
    }

    // Find phrase-pattern positions in this segment.
    const phraseIndices: number[] = [];
    posIndices.forEach((i) => {
      const pos = positions[i];
      const after = text.substring(pos);
      if (PHRASE_PATTERNS.some((p) => after.startsWith(p))) {
        phraseIndices.push(i);
      }
    });

    // Skip phrase positions AND the connector immediately after them
    // (the after-phrase connector introduces the first list item).
    phraseIndices.forEach((i) => {
      skipSet.add(i);
      const idxInSeg = posIndices.indexOf(i);
      if (idxInSeg + 1 < posIndices.length) {
        skipSet.add(posIndices[idxInSeg + 1]);
      }
    });

    // Always skip the LAST connector in the segment.
    skipSet.add(posIndices[posIndices.length - 1]);
  });

  // Build the result string by walking through positions.
  let result = "";
  let lastEnd = 0;
  positions.forEach((pos, i) => {
    result += text.substring(lastEnd, pos);
    result += skipSet.has(i) ? " وَ" : "، وَ";
    lastEnd = pos + 3; // After " وَ"
  });
  result += text.substring(lastEnd);

  return result;
}

/** Render a single ayah mark (۝X) as an ornamental circle with digit. */
function AyahMark({ digit }: { digit: string }) {
  return (
    <span className="ayah-mark" dir="rtl" aria-label={`Ayah ${digit}`}>
      <span className="ayah-mark-ornament" aria-hidden="true">
        ۝
      </span>
      <span className="ayah-mark-digit">{digit}</span>
    </span>
  );
}

/** Inline style for each line — display:block + margin to ensure each
    ayah/clause is on its own line (CSS class sometimes gets overridden
    by Tailwind v4 layer system, so inline style is more reliable).
    The bottom margin is the user-adjustable "Jarak Antar Kalimat" slider
    (ArabicStyleSettings.bodyLineHeight, in em) — it adds breathing room
    BETWEEN Arabic kalimat/sentences (ayah lines & comma-split clause
    lines). The lineHeight setting (within a single line) is separate. */
function makeLineStyle(gapEm: number): React.CSSProperties {
  return {
    display: "block",
    marginBottom: `${gapEm}em`,
  };
}

/**
 * Wrap each non-whitespace token of `text` in a colored span, alternating
 * between `colors[0]` and `colors[1]`. Whitespace tokens are preserved
 * verbatim so word spacing is unaffected.
 *
 * `counterRef` is a mutable ref holding the running word index — this lets
 * the alternation CONTINUE across multiple invocations (e.g., across ayah
 * lines or sentence-split lines) so the user sees a continuous A/B/A/B
 * pattern rather than restarting at color A on every line.
 */
function colorizeWords(
  text: string,
  colors: readonly [string, string],
  counterRef: { current: number },
  keyPrefix: string,
): React.ReactNode[] {
  if (!text) return [text];
  // Split on whitespace but keep the separators so spacing is preserved.
  const tokens = text.split(/(\s+)/);
  const out: React.ReactNode[] = [];
  for (let i = 0; i < tokens.length; i++) {
    const tok = tokens[i];
    if (tok === "") continue;
    if (/^\s+$/.test(tok)) {
      out.push(tok);
      continue;
    }
    const color = colors[counterRef.current % 2];
    counterRef.current += 1;
    out.push(
      <span key={`${keyPrefix}-w${i}`} style={{ color }}>
        {tok}
      </span>,
    );
  }
  return out;
}

/** If `colors` is non-null, colorize `text` word-by-word; otherwise return
 *  the raw text node. The counter is shared across all calls within one
 *  ArabicText render so the alternation is continuous. */
function maybeColorize(
  text: string,
  colors: readonly [string, string] | null,
  counterRef: { current: number },
  keyPrefix: string,
): React.ReactNode {
  if (!colors) return text;
  return colorizeWords(text, colors, counterRef, keyPrefix);
}

type SegKind = "tajwid" | "waqaf" | "hamza";

type Seg = { start: number; end: number; kind: SegKind; rule?: string };

/** Push `range` (kind) into `segs`, clipped against the sorted & disjoint
 *  `blockers` intervals — the blocked parts are already covered by a
 *  higher-precedence marking (tajwid rule color or waqaf blue), so only
 *  the uncovered sub-intervals are emitted. */
function pushClipped(
  segs: Seg[],
  range: { start: number; end: number },
  kind: SegKind,
  blockers: { start: number; end: number }[],
) {
  let s = range.start;
  for (const b of blockers) {
    if (b.end <= s) continue; // entirely before
    if (b.start >= range.end) break; // entirely after
    if (b.start > s) {
      segs.push({ start: s, end: Math.min(b.start, range.end), kind });
    }
    s = Math.max(s, b.end);
    if (s >= range.end) break;
  }
  if (s < range.end) segs.push({ start: s, end: range.end, kind });
}

/**
 * Wrap tajwid rule segments (detected by analyzeTajwid), waqaf segments
 * (detected by analyzeWaqaf, when the waqafColor toggle is on) AND silent
 * hamzatul-wasl alif segments (detected by analyzeHamzaWashal, auto-on
 * with tajwid) in colored spans. All ranges are already sorted &
 * non-overlapping WITHIN each set; precedence is resolved by clipping:
 *
 *  - Tajwid ranges take the HIGHEST precedence: any overlap between a
 *    waqaf range and a tajwid range is clipped away from the waqaf side.
 *  - Hamzatul-wasl ranges take the LOWEST precedence: they are clipped
 *    against BOTH tajwid and waqaf ranges (the seven tajwid colors and
 *    the waqaf blue #1B51D0 are never disturbed — spec requirement 8).
 *  - Waqaf segments get the `.waqaf-mark` class (blue #1B51D0, lightened
 *    in dark theme); hamza segments get `.hamza-washal-mark` (grayish
 *    blue #7893B0, lightened in dark theme).
 */
function renderTajwidAndWaqafText(
  text: string,
  keyPrefix: string,
  waqafOn: boolean,
  hamzaOn: boolean,
): React.ReactNode {
  const tajwidRanges = analyzeTajwid(text);
  const waqafRanges = waqafOn ? analyzeWaqaf(text) : [];
  const hamzaRanges = hamzaOn ? analyzeHamzaWashal(text) : [];

  const segs: Seg[] = tajwidRanges.map((r) => ({
    start: r.start,
    end: r.end,
    kind: "tajwid" as const,
    rule: r.rule,
  }));

  for (const w of waqafRanges) {
    pushClipped(segs, w, "waqaf", tajwidRanges);
  }

  // Hamzatul washal loses to both tajwid and waqaf — combine them into
  // one sorted blocker list (both sets are individually sorted & disjoint).
  if (hamzaRanges.length > 0) {
    const blockers = [
      ...tajwidRanges.map((r) => ({ start: r.start, end: r.end })),
      ...waqafRanges.map((r) => ({ start: r.start, end: r.end })),
    ].sort((a, b) => a.start - b.start);
    for (const h of hamzaRanges) {
      pushClipped(segs, h, "hamza", blockers);
    }
  }

  segs.sort((a, b) => a.start - b.start);

  const nodes: React.ReactNode[] = [];
  let cursor = 0;
  segs.forEach((seg, i) => {
    if (seg.start > cursor) nodes.push(text.slice(cursor, seg.start));
    if (seg.kind === "tajwid") {
      nodes.push(
        <span key={`${keyPrefix}-tw-${i}`} className={`tajwid-${seg.rule}`}>
          {text.slice(seg.start, seg.end)}
        </span>,
      );
    } else if (seg.kind === "waqaf") {
      nodes.push(
        <span key={`${keyPrefix}-wq-${i}`} className="waqaf-mark">
          {text.slice(seg.start, seg.end)}
        </span>,
      );
    } else {
      nodes.push(
        <span key={`${keyPrefix}-hw-${i}`} className="hamza-washal-mark">
          {text.slice(seg.start, seg.end)}
        </span>,
      );
    }
    cursor = seg.end;
  });
  if (cursor < text.length) nodes.push(text.slice(cursor));
  return nodes;
}

/** Single text-decoration entry point used by every render path: tajwid
 *  highlighting takes precedence over the alternating word colorize, and
 *  waqaf blue + hamzatul-wasl gray-blue compose with tajwid (clipped to
 *  non-overlapping segments). */
function decorateText(
  text: string,
  colors: readonly [string, string] | null,
  counterRef: { current: number },
  keyPrefix: string,
  tajwid: boolean,
  waqaf = false,
  hamza = false,
): React.ReactNode {
  if (tajwid || waqaf || hamza) return renderTajwidAndWaqafText(text, keyPrefix, waqaf, hamza);
  return maybeColorize(text, colors, counterRef, keyPrefix);
}

/**
 * Parse an Arabic string and return an array of React nodes where:
 *  - each ayah (text + ۝X mark) is wrapped together in an `.ayah-line` span,
 *    with the ayah number mark placed at the END of the verse text —
 *    matching standard Mushaf convention (number marks the end of the verse)
 *  - each `.ayah-line` is `display: block` on ALL viewports so every verse
 *    gets its own line — easier to read & memorise.
 *
 * For Arabic text WITHOUT ayah marks (e.g., long dua text), split by Arabic
 * comma (،) and Latin period (.) so each clause also gets its own line.
 *
 * If `colors` is provided, each non-whitespace word is wrapped in a colored
 * span with alternating shades. The `counterRef` is shared across all
 * recursive calls so the alternation is continuous.
 */
function renderArabicWithAyahMarks(
  text: string,
  keyPrefix: string,
  colors: readonly [string, string] | null = null,
  counterRef: { current: number } = { current: 0 },
  splitSentences: boolean = true,
  tajwid: boolean = false,
  gapEm: number = 0.25,
  waqaf: boolean = false,
  hamza: boolean = false,
): React.ReactNode[] {
  // Hard line breaks: a "\n" in the source data forces a line boundary
  // (used by the surah blocks — the Bismillah ends with a period and its
  // own line before the first ayah, per user request). Each segment is
  // rendered through the same pipeline independently, so the resulting
  // `.ayah-line` blocks already stack vertically and the tajwid/waqaf/
  // hamza analyzers see per-segment text exactly like any other line —
  // all indices stay consistent.
  if (text.includes("\n")) {
    const out: React.ReactNode[] = [];
    text.split("\n").forEach((chunk, ci) => {
      out.push(
        ...renderArabicWithAyahMarks(
          chunk,
          `${keyPrefix}-br${ci}`,
          colors,
          counterRef,
          splitSentences,
          tajwid,
          gapEm,
          waqaf,
          hamza,
        ),
      );
    });
    return out;
  }

  const lineStyle = makeLineStyle(gapEm);
  const matches = Array.from(text.matchAll(AYAH_MARK_REGEX));
  if (matches.length === 0) {
    // No ayah marks — this is likely a long dua text.
    if (!splitSentences) {
      // Comma/period line-breaking disabled — render as ONE flowing
      // paragraph (wraps naturally by width).
      const trimmed = text.trim();
      if (!trimmed) return [];
      return [
        <span key={`${keyPrefix}-0`} className="ayah-line" style={lineStyle}>
          {decorateText(trimmed, colors, counterRef, keyPrefix, tajwid, waqaf, hamza)}
        </span>,
      ];
    }
    // Split by Arabic comma (،) or Latin period (.) to break into lines.
    return splitBySentence(text, keyPrefix, colors, counterRef, tajwid, gapEm, waqaf, hamza);
  }

  const nodes: React.ReactNode[] = [];
  let lineKey = 0;
  let cursor = 0;

  matches.forEach((m, i) => {
    const full = m[0]; // e.g., ۝١
    const digit = full.slice(1); // e.g., ١
    const markStart = m.index ?? 0;

    // Text BEFORE this mark (the verse that this mark ends)
    const textBefore = text.substring(cursor, markStart);

    // Wrap verse text + mark together — mark at the END of the verse.
    // The verse text is colorized word-by-word if colors is set; the
    // AyahMark ornament keeps its own CSS-driven styling.
    nodes.push(
      <span key={`${keyPrefix}-${lineKey++}`} className="ayah-line" style={lineStyle}>
        {decorateText(textBefore, colors, counterRef, `${keyPrefix}-${i}`, tajwid, waqaf, hamza)}
        <AyahMark digit={digit} />
      </span>,
    );

    cursor = markStart + full.length;
  });

  // After the last mark, there may be trailing text (e.g., final verse text
  // without a closing mark, or whitespace) — render it as a trailing line.
  if (cursor < text.length) {
    const trailing = text.substring(cursor);
    if (!splitSentences) {
      // Comma splitting off — trailing text as one flowing line.
      const trimmed = trailing.trim();
      if (trimmed) {
        nodes.push(
          <span key={`${keyPrefix}-trail-0`} className="ayah-line" style={lineStyle}>
            {decorateText(trimmed, colors, counterRef, `${keyPrefix}-trail`, tajwid, waqaf, hamza)}
          </span>,
        );
      }
    } else {
      // Also split trailing by sentence for dua-style text
      nodes.push(
        ...splitBySentence(
          trailing,
          `${keyPrefix}-trail`,
          colors,
          counterRef,
          tajwid,
          gapEm,
          waqaf,
          hamza,
        ),
      );
    }
  }

  return nodes;
}

/**
 * Split a string by Arabic comma (،) and Latin period (.) into lines.
 * Each line is wrapped in an `.ayah-line` span (display: block).
 * The delimiter is preserved at the END of each line (per user request:
 * "berikan tanda di akhir kalimatnya").
 *
 * If `colors` is provided, each word is wrapped in a colored span with
 * alternating shades (counter shared with the caller).
 */
function splitBySentence(
  text: string,
  keyPrefix: string,
  colors: readonly [string, string] | null = null,
  counterRef: { current: number } = { current: 0 },
  tajwid: boolean = false,
  gapEm: number = 0.25,
  waqaf: boolean = false,
  hamza: boolean = false,
): React.ReactNode[] {
  const lineStyle = makeLineStyle(gapEm);
  // Trim leading/trailing whitespace
  const trimmed = text.trim();
  if (!trimmed) return [<React.Fragment key={`${keyPrefix}-0`} />];

  // Split by Arabic comma or Latin period, KEEPING the delimiter
  // The regex captures the delimiter so split() includes it in the result
  const parts = trimmed.split(/(،|\.)/);
  const lines: React.ReactNode[] = [];
  let current = "";
  let keyIdx = 0;

  for (let i = 0; i < parts.length; i++) {
    const part = parts[i];
    current += part;
    // If this part is a delimiter, finish the current line
    if (part === "،" || part === ".") {
      lines.push(
        <span key={`${keyPrefix}-${keyIdx++}`} className="ayah-line" style={lineStyle}>
          {decorateText(
            current,
            colors,
            counterRef,
            `${keyPrefix}-${keyIdx}`,
            tajwid,
            waqaf,
            hamza,
          )}
        </span>,
      );
      current = "";
    }
  }
  // Trailing text without delimiter
  if (current.trim()) {
    lines.push(
      <span key={`${keyPrefix}-${keyIdx++}`} className="ayah-line" style={lineStyle}>
        {decorateText(current, colors, counterRef, `${keyPrefix}-${keyIdx}`, tajwid, waqaf, hamza)}
      </span>,
    );
  }

  return lines;
}

type Props = {
  text: string;
  forceMode?: "standard" | "quran" | "naskhi";
  forceSettings?: {
    fontSize: number;
    lineHeight: number;
    wordSpacing: number;
    letterSpacing?: number;
    align: ArabicStyleSettings["align"];
  };
  className?: string;
  lang?: string;
  /** Optional node to render after the text (e.g. repeat counter) */
  suffix?: React.ReactNode;
} & Omit<React.HTMLAttributes<HTMLParagraphElement>, "children">;

export function ArabicText({
  text,
  forceMode,
  forceSettings,
  className = "",
  lang = "ar",
  suffix,
  ...rest
}: Props) {
  const { settings } = useArabicStyle();
  const { toggles } = useDisplaySettings();
  const { resolvedTheme } = useTheme();

  // Colorize only after mount to avoid SSR/CSR hydration mismatch —
  // next-themes resolves the actual theme (light/dark) on the client.
  const [mounted, setMounted] = React.useState(false);
  React.useEffect(() => setMounted(true), []);

  const mode = forceMode ?? settings.mode;
  const s = forceSettings ?? settings;
  const cfg = arabicStyleModes[mode];

  // Apply Arabic comma transformation if the punctuateArabic toggle is ON.
  // The transform is a no-op for Quran verses (texts containing ۝ or ۩).
  const renderedText = toggles.punctuateArabic ? addArabicCommas(text) : text;

  // Tajwid highlighting — deterministic pure function of the text, so it
  // renders identically on server and client (no mounted gate needed,
  // unlike the theme-dependent colorize). Skipped in the compare view.
  const tajwidOn = toggles.tajwid && !forceMode;
  // Waqaf coloring — its own display toggle; composes with tajwid (the
  // waqaf blue only fills segments not already colored by a tajwid rule).
  // Also skipped in the compare view.
  const waqafOn = toggles.waqafColor && !forceMode;
  // Hamzatul Washal marking — follows the TAJWID toggle automatically
  // (spec requirement 7): active when tajwid is on, off when it is off.
  // The gray-blue fills segments not already covered by tajwid or waqaf.
  const hamzaOn = tajwidOn;

  // Pick color pair based on resolved theme. Only applied after mount to
  // avoid SSR mismatch (resolvedTheme is undefined on first render).
  // Tajwid wins over colorize — combining both would be unreadable.
  const wantColors = mounted && toggles.colorizeArabic && !forceMode && !tajwidOn; // skip in compare view
  const colors: readonly [string, string] | null = wantColors
    ? resolvedTheme === "dark"
      ? COLORIZE_COLORS.dark
      : COLORIZE_COLORS.light
    : null;

  // Per-render counter — shared across all line splits so the A/B alternation
  // is continuous across ayah lines, sentence splits, and trailing text.
  const counterRef = React.useRef(0);
  // Reset on each render so the alternation restarts at color[0] when the
  // user toggles or the text changes.
  counterRef.current = 0;

  const style: React.CSSProperties = {
    fontFamily: cfg.fontFamily,
    fontSize: `${s.fontSize}rem`,
    lineHeight: s.lineHeight,
    wordSpacing: `${s.wordSpacing}em`,
    letterSpacing: `${s.letterSpacing ?? 0}em`,
    // Resolve "smart" alignment based on this block's text content so each
    // block gets the appropriate alignment (Quran verses → right, dua/dzikir
    // → justify). Pass through right/justify/center unchanged.
    textAlign: resolveTextAlign(s.align, renderedText),
    direction: "rtl",
    unicodeBidi: "isolate",
  };

  return (
    <p
      dir="rtl"
      lang={lang}
      className={`arabic-text ${cfg.cssClass} ${className}`}
      style={style}
      {...rest}
    >
      {renderArabicWithAyahMarks(
        renderedText,
        "t",
        colors,
        counterRef,
        toggles.splitArabicLines,
        tajwidOn,
        // Always use the user's bodyLineHeight (gap between Arabic kalimat
        // lines). When `forceSettings` is set (the compare-view branch), we
        // intentionally fall back to the user's gap value because the compare
        // view is about font/size/spacing comparison, not about the sentence
        // gap knob — and forceSettings's type doesn't carry bodyLineHeight.
        settings.bodyLineHeight,
        waqafOn,
        hamzaOn,
      )}
      {suffix}
    </p>
  );
}
