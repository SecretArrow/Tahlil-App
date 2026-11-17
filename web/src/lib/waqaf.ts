/**
 * Waqaf engine — detects letters whose READING CHANGES when the reciter
 * stops (waqaf) at a natural pause point, so the renderer can highlight
 * them in blue (#1B51D0).
 *
 * Companion to `src/lib/tajwid.ts` (the 7-color tajwid engine). Waqaf
 * coloring is a SEPARATE feature with its own display toggle; when both
 * are active, tajwid colors keep their segments and the waqaf blue only
 * fills segments NOT already covered by a tajwid rule (no color clashes).
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
 * ranges that index into the source string, exactly like analyzeTajwid().
 */

export type WaqafKind = "waqaf-iskan" | "mad-aridh-lissukun" | "mad-iwadh";

export type WaqafRange = {
  /** Inclusive start index into the source string. */
  start: number;
  /** Exclusive end index into the source string. */
  end: number;
  kind: WaqafKind;
};

/** Ordered legend metadata (used by the settings panel UI). */
export const WAQAF_RULES_META: {
  kind: WaqafKind;
  name: string;
  desc: string;
}[] = [
  {
    kind: "waqaf-iskan",
    name: "Waqaf bil Iskan",
    desc: "harokat/tanwin akhir kata diganti sukun saat berhenti",
  },
  {
    kind: "mad-aridh-lissukun",
    name: "Mad Aridh Lissukun",
    desc: "huruf mad akhir kata memanjang 4–6 harakat saat berhenti",
  },
  {
    kind: "mad-iwadh",
    name: "Mad Iwadh",
    desc: "tanwin fath dibaca alif (2 harakat) saat berhenti",
  },
];

/**
 * Demo text for the settings waqaf legend — three one-word clauses, each
 * exercising one waqaf kind at its stopping point:
 *  - قَالُوا،      → mad aridh lissukun (لُوا — madd extends when stopping)
 *  - هُدًى،         → mad iwadh (دًى — tanwin fath read as alif)
 *  - مُحَمَّدٌ رَسُوْلُ اللَّهِ، → waqaf bil iskan (هِ — vowel replaced by sukun)
 * Only the STOP words (before each ، and at the text end) get colored;
 * mid-clause words like مُحَمَّدٌ stay uncolored (per spec: not every
 * final letter is highlighted).
 */
export const PREVIEW_WAQAF_TEXT = "قَالُوا، هُدًى، مُحَمَّدٌ رَسُوْلُ اللَّهِ،";

// ── Character classes (identical to tajwid.ts) ───────────────────────────

const SUKUN_RE = /[\u0652\u06E1]/;
const TANWIN_RE = /[\u064B-\u064D]/;
const SHADDA = "\u0651";

const COMBINING_RE = /[\u064B-\u0655\u0670\u06D6-\u06DC\u06DE-\u06ED]/;
const ARABIC_LETTER_RE = /[\u0621-\u063A\u0641-\u064A\u0671-\u06D3]/;

const FATHAH = "\u064E";
const DAMMAH = "\u064F";
const KASRAH = "\u0650";
const FATHATAN = "\u064B";
const DAMMATAN = "\u064C";
const KASRATAN = "\u064D";
const DAGGER_ALEF = "\u0670";

/** Ta marbutah — mad iwadh does NOT apply to it (reads sukun when stopped). */
const TA_MARBUTAH = "\u0629";

/** Arabic comma and Latin period — clause delimiters = stopping points. */
const DELIMITER_RE = /[،.]/;

// ── Clusters ─────────────────────────────────────────────────────────────

type Cluster = {
  start: number;
  end: number;
  base: string;
  marks: string;
};

function buildClusters(text: string): Cluster[] {
  const clusters: Cluster[] = [];
  let pos = 0;
  for (const ch of text) {
    const last = clusters[clusters.length - 1];
    if (last && COMBINING_RE.test(ch)) {
      last.marks += ch;
      last.end = pos + ch.length;
    } else {
      clusters.push({ start: pos, end: pos + ch.length, base: ch, marks: "" });
    }
    pos += ch.length;
  }
  return clusters;
}

const isLetter = (c: Cluster): boolean => ARABIC_LETTER_RE.test(c.base);

/** Mark analysis for one cluster. */
type Marks = {
  hasFathah: boolean;
  hasDammah: boolean;
  hasKasrah: boolean;
  hasFathatan: boolean;
  hasDammatan: boolean;
  hasKasratan: boolean;
  hasSukun: boolean;
  hasShadda: boolean;
  hasDaggerAlef: boolean;
};

function analyzeMarks(c: Cluster): Marks {
  const has = (ch: string) => c.marks.includes(ch);
  return {
    hasFathah: has(FATHAH),
    hasDammah: has(DAMMAH),
    hasKasrah: has(KASRAH),
    hasFathatan: has(FATHATAN),
    hasDammatan: has(DAMMATAN),
    hasKasratan: has(KASRATAN),
    hasSukun: c.marks.length > 0 && SUKUN_RE.test(c.marks),
    hasShadda: has(SHADDA),
    hasDaggerAlef: has(DAGGER_ALEF),
  };
}

/**
 * A "bare madd seat" — a madd letter (ا / و / ي) carrying NO marks at all.
 * These letters are silent connectors: they extend the harakat carried by
 * the preceding letter (the anchor). Walking backward over a chain of such
 * letters (plus a trailing tanwin seat) lets us find the anchor precisely.
 */
function isBareMaddSeat(c: Cluster): boolean {
  if (!isLetter(c)) return false;
  if (c.marks !== "") return false;
  return c.base === "ا" || c.base === "و" || c.base === "ي" || c.base === "ى";
}

/**
 * Classify the stop word that ends at cluster index `from` (the last
 * letter cluster of the word — the caller passes the index of the letter
 * immediately before a delimiter, or the final letter of the text).
 *
 * Returns a WaqafRange, or null when stopping does not change the reading
 * (final letter already sukun / bare with no determinable kaidah).
 */
function classifyStopWord(clusters: Cluster[], from: number): WaqafRange | null {
  // Walk back over whitespace between the delimiter and the word.
  let i = from;
  while (i >= 0 && clusters[i].base.trim() === "") i--;
  if (i < 0 || !isLetter(clusters[i])) return null;

  // ── Determine the anchor ─────────────────────────────────────────────
  // The anchor is the harakat/tanwin-bearing letter at the head of the
  // word's tail. If the LAST cluster itself is a bare madd seat (ا/و/ي
  // with no marks), the anchor sits before the run of trailing bare
  // seats — e.g. قَالُوا → anchor لُ, seats [و, ا]; دُنْيَا → anchor يَ,
  // seat [ا]. The walk NEVER crosses a word boundary and only consumes
  // MARKLESS madd letters (a sukun-carrying و breaks the chain: أَوْ is
  // a diphthong, not a madd).
  let anchorIdx: number;
  let chainStart: number; // first index of the bare-seat run (if any)
  const lastIsBareSeat = isBareMaddSeat(clusters[i]);
  if (lastIsBareSeat) {
    let j = i;
    while (j - 1 >= 0 && !/\s/.test(clusters[j - 1].base) && isBareMaddSeat(clusters[j - 1])) {
      j--;
    }
    chainStart = j;
    anchorIdx = j - 1;
    if (anchorIdx < 0 || !isLetter(clusters[anchorIdx]) || /\s/.test(clusters[anchorIdx].base)) {
      // The whole word is bare letters — kaidah undeterminable.
      return null;
    }
  } else {
    // The final cluster carries marks — it IS the anchor; no chain.
    chainStart = i;
    anchorIdx = i;
  }

  const anchor = clusters[anchorIdx];
  const m = analyzeMarks(anchor);
  const chainEnd = clusters[i].end; // range always extends to the word's last cluster
  const anchorOnly = anchor.end;
  // A trailing bare-seat run exists whenever the LAST cluster is itself a
  // bare madd seat — even when the run is a single letter (e.g. كَمَا's ا).
  const hasChain = lastIsBareSeat;

  // 1) Mad Iwadh — tanwin fathatan (ً) → alif, EXCEPT ta marbutah (ةً),
  //    which reads sukun (waqaf bil iskan) per classical tajwid.
  if (m.hasFathatan) {
    if (anchor.base === TA_MARBUTAH) {
      return { start: anchor.start, end: anchorOnly, kind: "waqaf-iskan" };
    }
    return { start: anchor.start, end: chainEnd, kind: "mad-iwadh" };
  }

  // 2) Mad Aridh Lissukun — the anchor carries a madd-feeding harakat and
  //    the trailing bare seats are the matching madd letter(s).
  if (hasChain) {
    const chainBases = clusters.slice(chainStart, i + 1).map((c) => c.base);
    const chainAllE = chainBases.every((b) => b === "ا" || b === "ى");
    const chainAllW = chainBases.every((b) => b === "و" || b === "ا");
    const chainAllY = chainBases.every((b) => b === "ي");
    // Anchor must carry the matching harakat — a sukun anchor means the
    // trailing letters are consonants/diphthongs, NOT madd (e.g. أَوْ).
    if (m.hasFathah && chainAllE) {
      return { start: anchor.start, end: chainEnd, kind: "mad-aridh-lissukun" };
    }
    if (m.hasDammah && chainAllW) {
      return { start: anchor.start, end: chainEnd, kind: "mad-aridh-lissukun" };
    }
    if (m.hasKasrah && chainAllY) {
      return { start: anchor.start, end: chainEnd, kind: "mad-aridh-lissukun" };
    }
    // Unusual structure — stay conservative (avoid wrong coloring).
    return null;
  }

  // No chain — the anchor IS the final cluster.
  // Dagger alef (ٰ) marks a madd ā → mad aridh (e.g. عِيسَىٰ).
  if (m.hasDaggerAlef && !m.hasSukun && !m.hasFathatan && !m.hasDammatan && !m.hasKasratan) {
    return { start: anchor.start, end: anchorOnly, kind: "mad-aridh-lissukun" };
  }

  // Self-marked madd letters (اَ / وُ / يِ) — the letter is both the
  // harakat carrier and the madd letter → mad aridh when stopped.
  if (
    ((anchor.base === "ا" || anchor.base === "ى") && m.hasFathah) ||
    (anchor.base === "و" && m.hasDammah) ||
    (anchor.base === "ي" && m.hasKasrah)
  ) {
    return { start: anchor.start, end: anchorOnly, kind: "mad-aridh-lissukun" };
  }

  // 3) Waqaf bil Iskan — movable vowel (± shadda) replaced by sukun.
  if ((m.hasFathah || m.hasDammah || m.hasKasrah) && !m.hasSukun) {
    return { start: anchor.start, end: anchorOnly, kind: "waqaf-iskan" };
  }

  // Tanwin dammatan / kasratan → sukun when stopping.
  if (m.hasDammatan || m.hasKasratan) {
    return { start: anchor.start, end: anchorOnly, kind: "waqaf-iskan" };
  }

  // 4) No reading change (already sukun, or undeterminable) → no highlight.
  return null;
}

// ── Public API ───────────────────────────────────────────────────────────

/**
 * Analyze `text` and return sorted, non-overlapping waqaf ranges.
 *
 * Stopping points: the word immediately before every clause delimiter
 * (، or .) AND the final word of the text (after stripping trailing
 * delimiters/whitespace). Only words whose reading actually changes when
 * stopping produce a range.
 */
export function analyzeWaqaf(text: string): WaqafRange[] {
  if (!text) return [];
  const clusters = buildClusters(text);
  if (clusters.length === 0) return [];

  const raw: WaqafRange[] = [];
  const seenStarts = new Set<number>();

  const add = (r: WaqafRange | null) => {
    if (!r) return;
    if (seenStarts.has(r.start)) return;
    seenStarts.add(r.start);
    raw.push(r);
  };

  // 1) The word before every clause delimiter (، / .).
  for (let i = 0; i < clusters.length; i++) {
    const c = clusters[i];
    if (isLetter(c) || c.base.trim() === "") continue;
    if (DELIMITER_RE.test(c.base)) {
      add(classifyStopWord(clusters, i - 1));
    }
  }

  // 2) The final word — strip trailing whitespace + delimiters first so
  //    "… خَلَقَ،" reports خَلَقَ exactly once (dedupe handles the overlap
  //    with the delimiter scan above).
  let end = clusters.length - 1;
  while (end >= 0 && (clusters[end].base.trim() === "" || DELIMITER_RE.test(clusters[end].base))) {
    end--;
  }
  if (end >= 0) {
    add(classifyStopWord(clusters, end));
  }

  // Sort by start; ranges from distinct stop words never overlap, so a
  // simple sort suffices.
  raw.sort((a, b) => a.start - b.start);
  return raw;
}
