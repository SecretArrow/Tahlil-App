export type DisplayToggles = {
  arabic: boolean;
  transliteration: boolean;
  translation: boolean;
  /**
   * When true, the Arabic text is post-processed to insert Arabic commas
   * (،) at clause/list boundaries — mirroring the punctuation that already
   * exists in the Indonesian translation. Off by default so the Arabic
   * renders as authored. Quran verses (with ۝ or ۩ marks) are never
   * modified.
   */
  punctuateArabic: boolean;
  /**
   * When true (default), Arabic text WITHOUT ayah marks is broken into
   * lines at Arabic commas (،) and Latin periods (.) so each clause gets
   * its own line — the traditional tahlil book layout. When false, the
   * text flows as a single paragraph and wraps naturally. Ayah-marked
   * verses always keep their per-verse lines either way.
   */
  splitArabicLines: boolean;
  /**
   * When true, each Arabic word is wrapped in a colored span, alternating
   * between two shades so consecutive words are visually distinguishable.
   * The two shades are picked based on the active theme:
   *   - Light theme (dark text)   → #000000 and #011b45
   *   - Dark theme  (light text)  → #ffffff and #d9dadb
   * Off by default.
   */
  colorizeArabic: boolean;
};

export const defaultDisplayToggles: DisplayToggles = {
  arabic: true,
  transliteration: true,
  translation: true,
  punctuateArabic: false,
  splitArabicLines: true,
  colorizeArabic: false,
};
