/**
 * Tajwid engine — detects recitation rules (hukum bacaan) in fully
 * vocalised Arabic text so the renderer can highlight them with colors.
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
 * colored <span>s. Detection handles combining marks correctly: harakat,
 * shadda, sukun, tanwin, dagger alef etc. are attached to their base
 * letter as "clusters", and rules are evaluated across word boundaries
 * (tanwin / nun sukun at the end of a word applies to the first letter of
 * the next word) but NOT across punctuation (a pause breaks the rule).
 *
 * Not detected (deliberately, to stay accurate): mad rules, lam
 * syamsiyyah/qamariyyah, sifat letters.
 */

export type TajwidRule =
  | "izhar"
  | "ikhfa"
  | "idgham-ghunnah"
  | "idgham-laghunnah"
  | "iqlab"
  | "ghunnah"
  | "qalqalah";

export type TajwidRange = {
  /** Inclusive start index into the source string. */
  start: number;
  /** Exclusive end index into the source string. */
  end: number;
  rule: TajwidRule;
};

/** Ordered legend metadata (also used by the settings panel UI). */
export const TAJWID_RULES_META: {
  rule: TajwidRule;
  name: string;
  desc: string;
}[] = [
  { rule: "izhar", name: "Izhar", desc: "dibaca jelas" },
  { rule: "ikhfa", name: "Ikhfa", desc: "dibaca sambar" },
  {
    rule: "idgham-ghunnah",
    name: "Idgham Bighunnah",
    desc: "lebur berdengung",
  },
  {
    rule: "idgham-laghunnah",
    name: "Idgham Bilaghunnah",
    desc: "lebur tanpa dengung",
  },
  { rule: "iqlab", name: "Iqlab", desc: "nun bertukar mim" },
  { rule: "ghunnah", name: "Ghunnah", desc: "dengung 2 harakat" },
  { rule: "qalqalah", name: "Qalqalah", desc: "dibaca memantul" },
];

/**
 * Demo text used by the settings panels — contains at least one instance
 * of EVERY rule so the legend colors can be verified at a glance:
 *  - أَنْعَمْتَ  → izhar (نْ+ع) & izhar syafawi (مْ+ت)
 *  - مِنْ شَرِّ  → ikhfa (نْ+ش)
 *  - مُحَمَّدٌ رَسُوْلُ → ghunnah (مّ) & idgham bilaghunnah (دٌ+ر)
 *  - مِنْ بَعْدِ → iqlab (نْ+ب)
 *  - مِنْ يَمِيْنِهِمْ → idgham bighunnah (نْ+ي)
 *  - أَجْمَعِيْنَ → qalqalah (جْ)
 */
export const PREVIEW_TAJWID_TEXT =
  "أَنْعَمْتَ عَلَيْهِمْ، مِنْ شَرِّ مَا خَلَقَ، مُحَمَّدٌ رَسُوْلُ اللَّهِ، مِنْ بَعْدِ، مِنْ يَمِيْنِهِمْ، أَجْمَعِيْنَ";

// ── Character classes ────────────────────────────────────────────────────

/** Sukun (U+0652) or small sukun variant (U+06E1). */
const SUKUN_RE = /[\u0652\u06E1]/;
/** Tanwin marks: fathatan (U+064B), dammatan (U+064C), kasratan (U+064D). */
const TANWIN_RE = /[\u064B-\u064D]/;
const SHADDA = "\u0651";

/**
 * Combining marks that belong to the preceding letter. U+06DD (ARABIC END
 * OF AYAH, a format char inside the U+06D6–U+06ED band) is excluded — it
 * must never be treated as a mark of the previous letter.
 */
const COMBINING_RE = /[\u064B-\u0655\u0670\u06D6-\u06DC\u06DE-\u06ED]/;

/** Arabic letter (hamza .. yeh, plus extended ranges). */
const ARABIC_LETTER_RE = /[\u0621-\u063A\u0641-\u064A\u0671-\u06D3]/;

const IZHAR_LETTERS = "ءهعحغخ";
const IDGHAM_GHUNNAH_LETTERS = "ينمو";
const IDGHAM_LAGHUNNAH_LETTERS = "لر";
const QALQALAH_LETTERS = "قطبجد";
const IQLAB_LETTER = "ب";
const IKHFA_LETTERS = "تثجدذزسشصضطظفقك";

// ── Clusters ─────────────────────────────────────────────────────────────

type Cluster = {
  /** Index of the base character in the source string. */
  start: number;
  /** Exclusive end index (base + all combining marks). */
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
 * Index of the letter cluster that immediately follows cluster `from`,
 * skipping whitespace (rules apply across word boundaries). Returns -1 if
 * any other character (punctuation, ayah mark, digit, Latin, …) intervenes
 * — a pause breaks the rule.
 */
function nextLetterIdx(clusters: Cluster[], from: number): number {
  for (let j = from + 1; j < clusters.length; j++) {
    const c = clusters[j];
    if (isLetter(c)) return j;
    if (/\s/.test(c.base)) continue; // word boundary — keep looking
    return -1; // punctuation / symbol — rule broken
  }
  return -1;
}

function classifyNunRule(base: string): TajwidRule | null {
  if (IZHAR_LETTERS.includes(base)) return "izhar";
  if (IDGHAM_GHUNNAH_LETTERS.includes(base)) return "idgham-ghunnah";
  if (IDGHAM_LAGHUNNAH_LETTERS.includes(base)) return "idgham-laghunnah";
  if (base === IQLAB_LETTER) return "iqlab";
  if (IKHFA_LETTERS.includes(base)) return "ikhfa";
  return null;
}

// ── Public API ───────────────────────────────────────────────────────────

/**
 * Analyze `text` and return sorted, non-overlapping tajwid ranges
 * (possibly empty — texts without the covered rules simply return []).
 */
export function analyzeTajwid(text: string): TajwidRange[] {
  if (!text) return [];
  const clusters = buildClusters(text);
  const raw: TajwidRange[] = [];

  for (let i = 0; i < clusters.length; i++) {
    const c = clusters[i];
    if (!isLetter(c)) continue;

    const hasSukun = SUKUN_RE.test(c.marks);
    const hasShadda = c.marks.includes(SHADDA);
    const hasTanwin = TANWIN_RE.test(c.marks);

    // 1) Ghunnah — نّ / مّ (the letter itself is highlighted).
    if (hasShadda && (c.base === "ن" || c.base === "م")) {
      raw.push({ start: c.start, end: c.end, rule: "ghunnah" });
    }

    // 2) Qalqalah — ق ط ب ج د carrying a sukun.
    if (hasSukun && QALQALAH_LETTERS.includes(c.base)) {
      raw.push({ start: c.start, end: c.end, rule: "qalqalah" });
    }

    // 3) Nun sakin / tanwin — rule partner is the next letter.
    const nunSakin = c.base === "ن" && hasSukun && !hasShadda;
    if (nunSakin || hasTanwin) {
      const j = nextLetterIdx(clusters, i);
      if (j !== -1) {
        const rule = classifyNunRule(clusters[j].base);
        if (rule) {
          raw.push({ start: c.start, end: clusters[j].end, rule });
        }
      }
    }

    // 4) Mim sakin — syafawi rules (ikhfa / idgham / izhar syafawi).
    const mimSakin = c.base === "م" && hasSukun && !hasShadda;
    if (mimSakin) {
      const j = nextLetterIdx(clusters, i);
      if (j !== -1) {
        const nb = clusters[j].base;
        const rule: TajwidRule =
          nb === "ب" ? "ikhfa" : nb === "م" ? "idgham-ghunnah" : "izhar";
        raw.push({ start: c.start, end: clusters[j].end, rule });
      }
    }
  }

  // Sort by start, then by longer range first; resolve overlaps by
  // trimming the later range so both partial rules stay visible.
  raw.sort((a, b) => a.start - b.start || b.end - a.end);
  const out: TajwidRange[] = [];
  let cursor = 0;
  for (const r of raw) {
    const s = Math.max(r.start, cursor);
    if (s >= r.end) continue; // fully swallowed by an earlier range
    out.push({ start: s, end: r.end, rule: r.rule });
    cursor = r.end;
  }
  return out;
}
