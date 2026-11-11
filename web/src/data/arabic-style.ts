export type ArabicStyleMode = "standard" | "quran" | "naskhi";

export type ArabicStyleSettings = {
  mode: ArabicStyleMode;
  fontSize: number; // rem
  lineHeight: number; // unitless
  wordSpacing: number; // em
  letterSpacing: number; // em — space BETWEEN letters within a word
  align: "right" | "justify";
};

export const defaultArabicSettings: ArabicStyleSettings = {
  mode: "quran",
  fontSize: 1.75,
  lineHeight: 2.1,
  wordSpacing: 0.05,
  letterSpacing: 0,
  align: "right",
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
    label: "Naskhi Tebal",
    description:
      "Font Naskhi tradisional yang tebal & estetik, dengan struktur huruf horizontal lurus (tanpa ligatur/sambungan rumit), harakat lengkap, dan spasi antarbaris renggang agar setiap huruf & harakatnya jelas. Font: Scheherazade New.",
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
