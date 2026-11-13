import type { Metadata, Viewport } from "next";
import { Amiri, Amiri_Quran, Plus_Jakarta_Sans, Scheherazade_New } from "next/font/google";
import "./globals.css";
import { Toaster } from "@/components/ui/toaster";
import { ThemeProvider } from "@/components/theme-provider";
import { ArabicStyleProvider } from "@/components/arabic-style-provider";
import { DisplaySettingsProvider } from "@/components/display-settings-provider";
import { getArabicStyleCookie, getDisplayTogglesCookie } from "@/lib/cookies/server";
import { ArabicStyleSettings, defaultArabicSettings } from "@/data/arabic-style";
import {
  DisplayToggles,
  defaultDisplayToggles,
  AUTO_SCROLL_SPEED_MIN,
  AUTO_SCROLL_SPEED_MAX,
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
  title: "Tahlil — Tahlil Lengkap Malam Jumat",
  description:
    "Web Tahlil Lengkap: 32 bagian rangkaian zikir, tahlil, Al-Fatihah, Ayat Kursi, Al-Ikhlas, Al-Falaq, An-Nas, sholawat, dan doa untuk mendoakan para arwah kaum muslimin. Mendukung mode gelap/terang, responsif PC & Android.",
  keywords: [
    "Tahlil",
    "Tahlil Lengkap",
    "Tahlil Malam Jumat",
    "Doa Arwah",
    "Majelis Tahlil",
    "Al-Fatihah",
    "Ayat Kursi",
    "Al-Ikhlas",
    "Al-Falaq",
    "An-Nas",
    "Sholawat",
    "Dzikir",
    "Islam",
  ],
  authors: [{ name: "Tahlil" }],
  icons: {
    // Favicon lokal — tidak ada lagi request ke CDN luar (z-cdn.chatglm.cn).
    // SVG untuk browser modern, PNG sebagai fallback + Apple touch icon.
    icon: [
      { url: "/icon.svg", type: "image/svg+xml" },
      { url: "/icon-192.png", sizes: "192x192", type: "image/png" },
    ],
    apple: [{ url: "/apple-icon.png" }],
  },
  openGraph: {
    title: "Tahlil — Tahlil Lengkap Malam Jumat",
    description:
      "32 bagian rangkaian zikir, tahlil, dan doa untuk mendoakan para arwah kaum muslimin.",
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
      mode: p.mode === "standard" || p.mode === "quran" || p.mode === "naskhi" ? p.mode : "quran",
      fontSize:
        typeof p.fontSize === "number" && p.fontSize >= 1.2 && p.fontSize <= 3.5
          ? p.fontSize
          : defaultArabicSettings.fontSize,
      lineHeight:
        typeof p.lineHeight === "number" && p.lineHeight >= 1.4 && p.lineHeight <= 3.5
          ? p.lineHeight
          : defaultArabicSettings.lineHeight,
      bodyLineHeight:
        typeof p.bodyLineHeight === "number" && p.bodyLineHeight >= 0 && p.bodyLineHeight <= 1.5
          ? p.bodyLineHeight
          : defaultArabicSettings.bodyLineHeight,
      wordSpacing:
        typeof p.wordSpacing === "number" && p.wordSpacing >= 0 && p.wordSpacing <= 0.6
          ? p.wordSpacing
          : defaultArabicSettings.wordSpacing,
      letterSpacing:
        typeof p.letterSpacing === "number" && p.letterSpacing >= 0 && p.letterSpacing <= 0.2
          ? p.letterSpacing
          : defaultArabicSettings.letterSpacing,
      align:
        p.align === "smart" || p.align === "justify" || p.align === "right" || p.align === "center"
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
      transliteration: typeof p.transliteration === "boolean" ? p.transliteration : true,
      translation: typeof p.translation === "boolean" ? p.translation : true,
      punctuateArabic: typeof p.punctuateArabic === "boolean" ? p.punctuateArabic : false,
      splitArabicLines: typeof p.splitArabicLines === "boolean" ? p.splitArabicLines : true,
      colorizeArabic: typeof p.colorizeArabic === "boolean" ? p.colorizeArabic : false,
      tajwid: typeof p.tajwid === "boolean" ? p.tajwid : false,
      autoScrollEnabled: typeof p.autoScrollEnabled === "boolean" ? p.autoScrollEnabled : false,
      autoScrollMode:
        p.autoScrollMode === "continuous" || p.autoScrollMode === "block"
          ? p.autoScrollMode
          : "continuous",
      // Auto-scroll numeric settings — clamped to the valid ranges.
      // Speed range was widened from 0.5–5 to 0.1–25; the clamp uses the
      // new bounds so legacy cookies with a 5.0 value stay valid (no
      // behavior change) and out-of-range values are pinned to the new
      // ceiling of 25.
      autoScrollSpeed:
        typeof p.autoScrollSpeed === "number"
          ? Math.min(AUTO_SCROLL_SPEED_MAX, Math.max(AUTO_SCROLL_SPEED_MIN, p.autoScrollSpeed))
          : defaultDisplayToggles.autoScrollSpeed,
      autoScrollSectionPauseSec:
        typeof p.autoScrollSectionPauseSec === "number" &&
        p.autoScrollSectionPauseSec >= 0 &&
        p.autoScrollSectionPauseSec <= 30
          ? p.autoScrollSectionPauseSec
          : defaultDisplayToggles.autoScrollSectionPauseSec,
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
  // In static-export mode (`BUILD_TARGET=pages`), `cookies()` from
  // `next/headers` throws because there's no request context during
  // static generation. We skip the server-side cookie read entirely —
  // the providers fall back to `getCookie()` from `client.ts` (which
  // reads `document.cookie`) on the client, so settings still persist,
  // just with a brief first-paint flash of defaults. The `<html
  // suppressHydrationWarning>` mask below hides the mismatch.
  //
  // In standalone/server mode, we DO read cookies server-side so SSR
  // matches the first client render — no flash.
  const isStaticExport = process.env.BUILD_TARGET === "pages";

  let initialArabicStyle = defaultArabicSettings;
  let initialDisplayToggles = defaultDisplayToggles;

  if (!isStaticExport) {
    const [arabicStyleRaw, displayTogglesRaw] = await Promise.all([
      getArabicStyleCookie(),
      getDisplayTogglesCookie(),
    ]);
    initialArabicStyle = parseArabicStyle(arabicStyleRaw);
    initialDisplayToggles = parseDisplayToggles(displayTogglesRaw);
  }

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
