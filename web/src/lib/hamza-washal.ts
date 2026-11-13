/**
 * Hamzatul Washal engine — marks the hamzatul-wasl alif (همزة الوصل,
 * Alif Washal) that is SILENT when the reciter CONNECTS the recitation
 * (washal), so the renderer can highlight it in grayish blue #7893B0.
 *
 * Companion to `src/lib/tajwid.ts` (7-color tajwid engine) and
 * `src/lib/waqaf.ts` (waqaf blue engine). This feature follows the
 * tajwid toggle automatically (spec requirement 7): when tajwid is off,
 * nothing is marked; when tajwid is on, the wasl marking composes with
 * the other colors (see clipping below).
 *
 * ── What Hamzatul Washal is ─────────────────────────────────────────────
 * Hamzatul wasl is the alif written at the head of certain words (Uthmani
 * rasm: bare ا — never أ إ آ ٰ). It is pronounced ONLY when the reader
 * STARTS the recitation with that word (ibtida'), and DROPPED whenever
 * anything is recited before it (washal):
 *
 *   - a preceding word in the recitation flow, or
 *   - a prefix attached to the same word (وَ فَ بِ لِ كَ …), e.g.
 *     وَالْعَصْرِ is read "wal-ʿaṣr" even when the recitation starts
 *     there, بِاسْمِ is read "bis-mi", فَاسْتَقِمْ is read "fas-taqim".
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
 *     بِالْغَيْبِ, فَاعْلَمْ, وَاغْفِرْ). These are ALWAYS dropped.
 *
 * ── What is deliberately NOT marked ─────────────────────────────────────
 *  - Hamzatul qatha': written أ إ آ, or — in this app's rasm convention —
 *    alif carrying dagger-alef/maddah marks (اٰمَنَ اٰتِنَا اٰلِهِ …,
 *    "ā-" is always pronounced). Excluded via the mark blacklist
 *    U+0653 U+0654 U+0655 U+0670 U+06E4.
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
 * The renderer clips wasl ranges against tajwid AND waqaf ranges, so the
 * seven tajwid colors and the waqaf marking are never disturbed. In
 * practice the ranges rarely meet: waqaf colors word TAILS while the
 * wasl alif is always the word HEAD.
 *
 * ── Text integrity ──────────────────────────────────────────────────────
 * The engine never mutates the text. It only reports (start, end) ranges
 * that index into the source string, exactly like analyzeTajwid() /
 * analyzeWaqaf() — all harakat, waqaf signs and verse marks are preserved
 * verbatim (spec requirement 5).
 */

export type HamzaWashalRange = {
  /** Inclusive start index into the source string. */
  start: number;
  /** Exclusive end index into the source string. */
  end: number;
};

/** Legend metadata for the settings panels. */
export const HAMZA_WASHAL_META = {
  name: "Hamzatul Washal",
  desc: "alif wasal yang tidak dilafalkan saat menyambung bacaan (aktif bersama Tajwid)",
  /** Spec color — grayish blue. */
  colorLight: "#7893B0",
  /** Lightened same-hue variant for the dark theme. */
  colorDark: "#A8BED4",
};

/**
 * Demo text for the settings legend — a clause that exercises both
 * contexts at once:
 *  - اِهْدِنَا → wasl alif of the FIRST word = ibtida' → normal color.
 *  - الصِّرَاطَ، الْمُسْتَقِيْمَ → wasl alifs after a preceding word →
 *    colored (silent in connected reading).
 * Mirrors Surat al-Fātiḥah 1:6.
 */
export const PREVIEW_HAMZA_WASHAL_TEXT = "اِهْدِنَا الصِّرَاطَ الْمُسْتَقِيْمَ،";

// ── Character classes ────────────────────────────────────────────────────

const ALIF = "\u0627";
/** Dedicated ALEF WASLA character — definitive hamzatul wasl if present. */
const ALEF_WASLA = "\u0671";
const LAM = "\u0644";

const SUKUN_RE = /[\u0652\u06E1]/;
const SHADDA = "\u0651";

const COMBINING_RE = /[\u064B-\u0655\u0670\u06D6-\u06ED]/;
const ARABIC_LETTER_RE = /[\u0621-\u063A\u0641-\u064A\u0671-\u06D3]/;

/** Marks that turn a bare alif into hamzatul QATHA' (never washal):
 *  maddah U+0653, hamza above/below U+0654/0655, dagger alef U+0670
 *  (this app writes qatha' "ā-" as اٰ), and U+06E4 (Quranic initials). */
const QATHA_MARKS_RE = /[\u0653\u0654\u0655\u0670\u06E4]/;

/** Prefix letters that attach before a wasl alif inside the same word. */
const PREFIX_BASES = new Set(["\u0648", "\u0641", "\u0628", "\u0644", "\u0643"]);

// ── Clusters (identical structure to waqaf.ts) ───────────────────────────

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

/**
 * A bare wasl-form alif: plain ا (optionally with harakat like اِ اَ —
 * harakat do not affect the wasl identity) or the dedicated ٱ. Rejects
 * every qatha'-shaped alif (أ إ آ اٰ) via the mark blacklist.
 */
function isWashalAlifShape(c: Cluster): boolean {
  if (c.base === ALEF_WASLA) return true;
  if (c.base !== ALIF) return false;
  return !QATHA_MARKS_RE.test(c.marks);
}

/**
 * The letter pattern that must follow a wasl alif:
 *  - the article lam (الْ اللّٰهُ الَّذِيْ — lam with any marking, or
 *    even bare when the vocalizer omitted its sukun),
 *  - a shadda letter (assimilated sun-article per Uthmani rasm), or
 *  - a sukun letter (form VII–X verb/noun wasl: اِهْدِنَا اغْفِرْ …).
 */
function isWashalFollowLetter(c: Cluster): boolean {
  if (!isLetter(c)) return false;
  if (c.base === LAM) return true;
  if (c.marks.includes(SHADDA)) return true;
  return SUKUN_RE.test(c.marks);
}

/**
 * Quranic letter initials (الۤمّۤ …): the alif is recited as the letter
 * name and never drops. These are exactly the short ا + ل + one-letter
 * words of the Uthmani initials family.
 */
function isQuranicInitial(clusters: Cluster[], from: number, to: number): boolean {
  if (to - from !== 3) return false;
  const [a, b, c] = clusters.slice(from, to).map((cl) => cl.base);
  return a === ALIF && b === LAM && ["م", "ر", "ص", "ق", "ك", "ل", "م"].includes(c);
}

/**
 * Analyze the word that starts at cluster index `wordStart` (the caller
 * guarantees the alif candidate has already been shape-checked). Returns
 * the range of the SILENT wasl alif cluster, or null when:
 *  - the follow-letter pattern does not confirm hamzatul wasl,
 *  - the word is a Quranic letter initial,
 *  - the alif is pronounced (word-initial wasl alif of the first word —
 *    ibtida').
 *
 * ` isFirstWord` tells whether this word is the first word of the text
 * unit (the ibtida' starting point of the current reading context).
 */
function classifyWord(
  clusters: Cluster[],
  wordStart: number,
  alifIdx: number,
  isFirstWord: boolean,
): HamzaWashalRange | null {
  const afterPrefix = alifIdx > wordStart; // وَ/فَ/بِ/لِ/كَ before the alif
  const follow = clusters[alifIdx + 1];

  // The follow pattern must confirm hamzatul wasl (article lam / shadda
  // sun-letter / sukun letter). Without it — not a wasl alif, e.g. the
  // ا of لَا which is a long vowel seat.
  if (!follow || !isWashalFollowLetter(follow)) return null;

  // Word boundaries for the Quranic-initial guard: from wordStart to the
  // next whitespace cluster.
  let wordEnd = wordStart;
  while (wordEnd < clusters.length && clusters[wordEnd].base.trim() !== "") wordEnd++;
  if (isQuranicInitial(clusters, wordStart, wordEnd)) return null;

  // Context: the alif is pronounced only when NOTHING is recited before
  // it — i.e. word-initial wasl alif of the FIRST word of the unit
  // (ibtida'). An after-prefix alif is always dropped, even there.
  if (isFirstWord && !afterPrefix) return null;

  const alif = clusters[alifIdx];
  return { start: alif.start, end: alif.end };
}

// ── Public API ───────────────────────────────────────────────────────────

/**
 * Analyze `text` (one reading unit: an ayah or a clause line) and return
 * sorted, non-overlapping ranges of the wasl alifs that are SILENT in
 * connected reading — the ones to color #7893B0.
 */
export function analyzeHamzaWashal(text: string): HamzaWashalRange[] {
  if (!text) return [];
  const clusters = buildClusters(text);
  if (clusters.length === 0) return [];

  const ranges: HamzaWashalRange[] = [];
  const seenStarts = new Set<number>();

  // Tokenize into words (maximal runs of non-whitespace clusters) while
  // tracking each word's cluster start. Only words containing at least
  // one Arabic letter count toward the word index (punctuation-only
  // tokens like ، . are skipped).
  let wordStart = -1;
  let wordIndex = -1; // 0-based index of the current letter-bearing word
  for (let i = 0; i <= clusters.length; i++) {
    const c = clusters[i];
    const isBoundary = i === clusters.length || (c && c.base.trim() === "");
    if (isBoundary) {
      // Close the current word, if any.
      if (wordStart >= 0) {
        for (let j = wordStart; j < i; j++) {
          const cl = clusters[j];
          const isCandidatePos =
            (j === wordStart ||
              (j === wordStart + 1 && PREFIX_BASES.has(clusters[wordStart].base))) &&
            isWashalAlifShape(cl) &&
            // no duplicate scan of the same cluster
            !seenStarts.has(cl.start);
          if (!isCandidatePos) continue;
          const r = classifyWord(clusters, wordStart, j, wordIndex === 0);
          if (r) {
            ranges.push(r);
            seenStarts.add(r.start);
          }
        }
        wordStart = -1;
      }
      continue;
    }
    if (wordStart < 0) {
      wordStart = i;
      if (isLetter(c)) wordIndex += 1;
    }
  }

  ranges.sort((a, b) => a.start - b.start);
  return ranges;
}
