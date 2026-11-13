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
   * Waqaf coloring — when true, letters whose reading CHANGES when the
   * reciter stops (waqaf bil iskan, mad aridh lissukun, mad iwadh) are
   * highlighted in blue (#1B51D0 light theme / #7B9DFF dark) at natural
   * stopping points (clause delimiters, verse ends, text end). Detected
   * by src/lib/waqaf.ts. Independent of `tajwid` — when both are on, the
   * waqaf blue only fills segments NOT already colored by a tajwid rule.
   * The original text is never modified.
   */
  waqafColor: boolean;
  /**
   * Master toggle for the auto-scroll (Gulir Otomatis) feature. When
   * false (default), the play/pause FAB is NOT rendered on the reading
   * page. When true, a small FAB appears at the right edge of the
   * reading view; tap it to start/stop scrolling, tap the reading
   * surface to pause/resume.
   */
  autoScrollEnabled: boolean;
  /**
   * Auto-scroll mode:
   *  - "continuous": the page scrolls smoothly pixel-by-pixel at a
   *    fixed rate derived from `autoScrollSpeed`.
   *  - "block":      the page jumps one section card at a time
   *    (`[data-autoscroll-block]`), with a delay derived from
   *    `autoScrollSpeed`.
   */
  autoScrollMode: "continuous" | "block";
  /**
   * Dimensionless auto-scroll speed (0.1 – 25). Higher = faster.
   *  - In "continuous" mode: px/sec = 12 + speed * 18 (so 0.1 ≈ 14 px/s,
   *    25 ≈ 462 px/s).
   *  - In "block" mode:      ms between jumps = clamp(7000 / speed, 700,
   *    30000) (so 0.1 = 70s between jumps, 25 = 280ms).
   */
  autoScrollSpeed: number;
  /**
   * Seconds the auto-scroll holds when a new section card scrolls into
   * view before continuing (0 = no pause; UI offers 0 / 2 / 5 / 10).
   * Only meaningful in "continuous" mode.
   */
  autoScrollSectionPauseSec: number;
};

export const AUTO_SCROLL_SPEED_MIN = 0.1;
export const AUTO_SCROLL_SPEED_MAX = 25;
export const AUTO_SCROLL_SPEED_STEP = 0.1;
export const AUTO_SCROLL_PAUSE_CHOICES = [0, 2, 5, 10] as const;

export const defaultDisplayToggles: DisplayToggles = {
  arabic: true,
  transliteration: true,
  translation: true,
  punctuateArabic: false,
  splitArabicLines: true,
  colorizeArabic: false,
  tajwid: false,
  waqafColor: false,
  autoScrollEnabled: false,
  autoScrollMode: "continuous",
  autoScrollSpeed: 3,
  autoScrollSectionPauseSec: 0,
};
