import type { Metadata, Viewport } from "next";
import { Amiri, Amiri_Quran, Plus_Jakarta_Sans, Scheherazade_New } from "next/font/google";
import "./globals.css";
import { Toaster } from "@/components/ui/toaster";
import { ThemeProvider } from "@/components/theme-provider";
import { ArabicStyleProvider } from "@/components/arabic-style-provider";
import { DisplaySettingsProvider } from "@/components/display-settings-provider";
import {
  getArabicStyleCookie,
  getDisplayTogglesCookie,
} from "@/lib/cookies/server";
import {
  ArabicStyleSettings,
  defaultArabicSettings,
} from "@/data/arabic-style";
import {
  DisplayToggles,
  defaultDisplayToggles,
} from "@/data/display-settings";

const amiri = Amiri({
  variable: "--font-arabic",
  subsets: ["arabic", "latin"],
  weight: ["400", "700"],
  display: "swap",
});

const amiriQuran = Amiri_Quran({
  variable: "--font-arabic-quran",
  subsets: ["arabic"],
  weight: "400",
  display: "swap",
});

const scheherazade = Scheherazade_New({
  variable: "--font-arabic-naskh",
  subsets: ["arabic"],
  weight: ["400", "700"],
  display: "swap",
});

const jakarta = Plus_Jakarta_Sans({
  variable: "--font-sans",
  subsets: ["latin"],
  display: "swap",
});

export const metadata: Metadata = {
  title: "Tahlil Singkat — Majelis Tahlil & Doa",
  description:
    "Web Tahlil Singkat: rangkaian zikir, tahlil, Al-Fatihah, Ayat Kursi, dan doa untuk mendoakan para arwah kaum muslimin. Mendukung mode gelap/terang, responsif PC & Android.",
  keywords: [
    "Tahlil",
    "Tahlil Singkat",
    "Doa Arwah",
    "Majelis Tahlil",
    "Al-Fatihah",
    "Ayat Kursi",
    "Dzikir",
    "Islam",
    "Salik Channel",
  ],
  authors: [{ name: "Salik Channel" }],
  icons: {
    icon: "https://z-cdn.chatglm.cn/z-ai/static/logo.svg",
  },
  openGraph: {
    title: "Tahlil Singkat — Majelis Tahlil & Doa",
    description:
      "Rangkaian zikir, tahlil, dan doa untuk mendoakan para arwah kaum muslimin.",
    type: "website",
  },
};

export const viewport: Viewport = {
  themeColor: [
    { media: "(prefers-color-scheme: light)", color: "#faf7f0" },
    { media: "(prefers-color-scheme: dark)", color: "#0a1612" },
  ],
  width: "device-width",
  initialScale: 1,
  maximumScale: 5,
};

function parseArabicStyle(raw: string | null): ArabicStyleSettings {
  if (!raw) return defaultArabicSettings;
  try {
    const p = JSON.parse(raw) as Partial<ArabicStyleSettings>;
    return {
      mode:
        p.mode === "standard" || p.mode === "quran" || p.mode === "naskhi"
          ? p.mode
          : "quran",
      fontSize:
        typeof p.fontSize === "number" && p.fontSize >= 1.2 && p.fontSize <= 3.5
          ? p.fontSize
          : defaultArabicSettings.fontSize,
      lineHeight:
        typeof p.lineHeight === "number" &&
        p.lineHeight >= 1.4 &&
        p.lineHeight <= 3.5
          ? p.lineHeight
          : defaultArabicSettings.lineHeight,
      wordSpacing:
        typeof p.wordSpacing === "number" &&
        p.wordSpacing >= 0 &&
        p.wordSpacing <= 0.6
          ? p.wordSpacing
          : defaultArabicSettings.wordSpacing,
      letterSpacing:
        typeof p.letterSpacing === "number" &&
        p.letterSpacing >= 0 &&
        p.letterSpacing <= 0.2
          ? p.letterSpacing
          : defaultArabicSettings.letterSpacing,
      align:
        p.align === "justify" || p.align === "right"
          ? p.align
          : defaultArabicSettings.align,
    };
  } catch {
    return defaultArabicSettings;
  }
}

function parseDisplayToggles(raw: string | null): DisplayToggles {
  if (!raw) return defaultDisplayToggles;
  try {
    const p = JSON.parse(raw) as Partial<DisplayToggles>;
    return {
      arabic: typeof p.arabic === "boolean" ? p.arabic : true,
      transliteration:
        typeof p.transliteration === "boolean" ? p.transliteration : true,
      translation:
        typeof p.translation === "boolean" ? p.translation : true,
      punctuateArabic:
        typeof p.punctuateArabic === "boolean" ? p.punctuateArabic : false,
      splitArabicLines:
        typeof p.splitArabicLines === "boolean" ? p.splitArabicLines : true,
      colorizeArabic:
        typeof p.colorizeArabic === "boolean" ? p.colorizeArabic : false,
    };
  } catch {
    return defaultDisplayToggles;
  }
}

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  // Read user preferences from cookies on the server so SSR matches
  // the first client render — no hydration mismatch, no flash of defaults.
  const [arabicStyleRaw, displayTogglesRaw] = await Promise.all([
    getArabicStyleCookie(),
    getDisplayTogglesCookie(),
  ]);

  const initialArabicStyle = parseArabicStyle(arabicStyleRaw);
  const initialDisplayToggles = parseDisplayToggles(displayTogglesRaw);

  return (
    <html lang="id" suppressHydrationWarning>
      <body
        className={`${amiri.variable} ${amiriQuran.variable} ${scheherazade.variable} ${jakarta.variable} font-sans antialiased bg-background text-foreground`}
      >
        <ThemeProvider
          attribute="class"
          defaultTheme="system"
          enableSystem
          disableTransitionOnChange={false}
          cookieName="tahlil-theme"
          storage="cookie"
        >
          <ArabicStyleProvider initialSettings={initialArabicStyle}>
            <DisplaySettingsProvider initialToggles={initialDisplayToggles}>
              {children}
              <Toaster />
            </DisplaySettingsProvider>
          </ArabicStyleProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}
