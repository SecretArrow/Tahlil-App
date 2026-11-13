export type ArabicStyleMode = "standard" | "quran" | "naskhi";

export type ArabicStyleSettings = {
  mode: ArabicStyleMode;
  fontSize: number; // rem
  lineHeight: number; // unitless — Arabic text line-height (within a single ayah/clause line)
  /**
   * Extra gap (em) BETWEEN Arabic sentences (ayah lines / comma-split
   * clause lines). This is the user-adjustable "Jarak Antar Kalimat"
   * slider in Pengaturan Teks Arab → Tipografi. It controls the
   * breathing room between one Arabic kalimat and the next, NOT the
   * line-height of the Latin transliteration / Indonesian translation
   * (those use their own sensible default line-height).
   */
  bodyLineHeight: number; // em — extra gap between Arabic kalimat lines
  wordSpacing: number; // em
  letterSpacing: number; // em — space BETWEEN letters within a word
  /**
   * Text alignment for the Arabic block.
   *  - "smart"     : Adaptive — Quran verses (with ۝/۩) are right-aligned
   *                  (Mushaf convention), other text (dua, dzikir) is
   *                  justified for a cleaner block reading. Short
   *                  single-line phrases are unaffected by justify.
   *  - "right"     : Always right-aligned (default Arabic reading direction).
   *  - "justify"   : Always justified (both edges neat).
   *  - "center"    : Always centered.
   */
  align: "smart" | "right" | "justify" | "center";
};

export const defaultArabicSettings: ArabicStyleSettings = {
  mode: "quran",
  fontSize: 1.75,
  lineHeight: 2.1,
  bodyLineHeight: 0.3,
  wordSpacing: 0.05,
  letterSpacing: 0,
  align: "smart",
};

export const arabicStyleModes: Record<
  ArabicStyleMode,
  {
    label: string;
    description: string;
    fontFamily: string;
    cssClass: string;
    /** Suggested defaults when user switches to this mode (only applied if they haven't customized) */
    suggested?: Partial<ArabicStyleSettings>;
  }
> = {
  standard: {
    label: "Arab Standar",
    description:
      "Tampilan sederhana, bersih, dan harakat konvensional. Font: Amiri — readability tinggi untuk teks sehari-hari.",
    fontFamily: "var(--font-arabic), 'Amiri', 'Scheherazade New', serif",
    cssClass: "font-arabic",
  },
  quran: {
    label: "Gaya Mushaf Al-Qur'an (Utsmani)",
    description:
      "Karakter Naskhi khas mushaf, bentuk sambungan huruf, ligatur, harakat, dan tanda baca Al-Qur'an yang lebih autentik. Font: Amiri Quran.",
    fontFamily: "var(--font-arabic-quran), 'Amiri Quran', 'Scheherazade New', serif",
    cssClass: "font-arabic-quran",
  },
  naskhi: {
    label: "Naskh Tradisional",
    description:
      "Font Naskh tradisional (Scheherazade New), dengan struktur huruf horizontal lurus tanpa ligatur Utsmani, harakat lengkap, dan spasi antarbaris renggang agar setiap huruf & harakatnya jelas.",
    fontFamily: "var(--font-arabic-naskh), 'Scheherazade New', 'Amiri', serif",
    cssClass: "font-arabic-naskhi",
    suggested: {
      fontSize: 2.0,
      lineHeight: 2.6,
      wordSpacing: 0.18,
      letterSpacing: 0.03,
      align: "right",
    },
  },
};
