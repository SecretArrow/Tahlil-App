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
   * Off by default. Ignored while `tajwid` is on (the rule colors take
   * precedence — combining both would produce unreadable rainbow text).
   */
  colorizeArabic: boolean;
  /**
   * When true, tajwid recitation rules in the Arabic text are highlighted
   * with colors (Izhar, Ikhfa, Idgham, Iqlab, Ghunnah, Qalqalah — see
   * src/lib/tajwid.ts). Off by default; detection is purely visual — the
   * text content itself is never modified. While active, the word
   * colorize toggle is temporarily ignored.
   */
  tajwid: boolean;
  /**
   * Auto-scroll speed multiplier for the "Gulir Otomatis" feature
   * (0.5–5.0). 1.0 ≈ 55 px/s — a comfortable reading pace. The feature
   * is started/stopped from the bottom-left FAB; tapping the page pauses
   * it, tapping again resumes.
   */
  autoScrollSpeed: number;
  /**
   * Seconds the auto-scroll holds when a new section card scrolls into
   * view before continuing (0 = no pause; UI offers 0 / 2 / 5 / 10).
   */
  autoScrollSectionPauseSec: number;
};

export const AUTO_SCROLL_SPEED_MIN = 0.5;
export const AUTO_SCROLL_SPEED_MAX = 5;
export const AUTO_SCROLL_PAUSE_CHOICES = [0, 2, 5, 10] as const;

export const defaultDisplayToggles: DisplayToggles = {
  arabic: true,
  transliteration: true,
  translation: true,
  punctuateArabic: false,
  splitArabicLines: true,
  colorizeArabic: false,
  tajwid: false,
  autoScrollSpeed: 1,
  autoScrollSectionPauseSec: 0,
};
