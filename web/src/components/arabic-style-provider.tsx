"use client";

import * as React from "react";
import {
  ArabicStyleSettings,
  ArabicStyleMode,
  defaultArabicSettings,
  arabicStyleModes,
} from "@/data/arabic-style";
import { COOKIE_KEYS, getCookie, setCookie } from "@/lib/cookies/client";

type Ctx = {
  settings: ArabicStyleSettings;
  setMode: (mode: ArabicStyleMode) => void;
  setFontSize: (v: number) => void;
  setLineHeight: (v: number) => void;
  setBodyLineHeight: (v: number) => void;
  setWordSpacing: (v: number) => void;
  setLetterSpacing: (v: number) => void;
  setAlign: (a: ArabicStyleSettings["align"]) => void;
  reset: () => void;
};

const ArabicStyleContext = React.createContext<Ctx | null>(null);

function parseStored(raw: string | null): ArabicStyleSettings {
  if (!raw) return defaultArabicSettings;
  try {
    const parsed = JSON.parse(raw) as Partial<ArabicStyleSettings>;
    return {
      mode:
        parsed.mode === "standard" || parsed.mode === "quran" || parsed.mode === "naskhi"
          ? parsed.mode
          : "quran",
      fontSize:
        typeof parsed.fontSize === "number" && parsed.fontSize >= 1.2 && parsed.fontSize <= 3.5
          ? parsed.fontSize
          : defaultArabicSettings.fontSize,
      lineHeight:
        typeof parsed.lineHeight === "number" &&
        parsed.lineHeight >= 1.4 &&
        parsed.lineHeight <= 3.5
          ? parsed.lineHeight
          : defaultArabicSettings.lineHeight,
      bodyLineHeight:
        typeof parsed.bodyLineHeight === "number" &&
        parsed.bodyLineHeight >= 0 &&
        parsed.bodyLineHeight <= 1.5
          ? parsed.bodyLineHeight
          : defaultArabicSettings.bodyLineHeight,
      wordSpacing:
        typeof parsed.wordSpacing === "number" &&
        parsed.wordSpacing >= 0 &&
        parsed.wordSpacing <= 0.6
          ? parsed.wordSpacing
          : defaultArabicSettings.wordSpacing,
      letterSpacing:
        typeof parsed.letterSpacing === "number" &&
        parsed.letterSpacing >= 0 &&
        parsed.letterSpacing <= 0.2
          ? parsed.letterSpacing
          : defaultArabicSettings.letterSpacing,
      align:
        parsed.align === "smart" ||
        parsed.align === "justify" ||
        parsed.align === "right" ||
        parsed.align === "center"
          ? parsed.align
          : defaultArabicSettings.align,
    };
  } catch {
    return defaultArabicSettings;
  }
}

// Initial settings passed from the server (read from cookie in layout.tsx).
// When provided, this is used as the initial useState value so SSR and
// first client render agree — no hydration mismatch, no flicker.
const SERVER_INITIAL: ArabicStyleSettings | null =
  typeof window !== "undefined" ? parseStored(getCookie(COOKIE_KEYS.arabicStyle)) : null;

export function ArabicStyleProvider({
  children,
  initialSettings,
}: {
  children: React.ReactNode;
  initialSettings?: ArabicStyleSettings;
}) {
  // Prefer server-passed initial (cookie-based); fall back to client cookie;
  // fall back to defaults. SSR renders with `initialSettings` so the markup
  // matches the very first client render.
  const [settings, setSettings] = React.useState<ArabicStyleSettings>(
    () => initialSettings ?? SERVER_INITIAL ?? defaultArabicSettings,
  );

  // Write to cookie every time settings change.
  const persist = React.useCallback((next: ArabicStyleSettings) => {
    const value = JSON.stringify(next);
    // 1. Immediate local cookie write (no network round-trip)
    setCookie(COOKIE_KEYS.arabicStyle, value);
    // 2. Fire-and-forget POST to /api/settings so the server-side cookie
    //    is also updated (next reload SSR sees the new value).
    if (typeof fetch !== "undefined") {
      void fetch("/api/settings", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          key: "arabicStyle",
          value,
        }),
        keepalive: true,
      }).catch(() => {
        /* silent — local cookie already covers us */
      });
    }
  }, []);

  const update = React.useCallback(
    (partial: Partial<ArabicStyleSettings>) => {
      setSettings((prev) => {
        const next = { ...prev, ...partial };
        persist(next);
        return next;
      });
    },
    [persist],
  );

  // Cross-tab sync via the `storage` event (covers localStorage writes from
  // other tabs — we don't use localStorage here, but we keep this so a future
  // migration is painless). Cookies are tab-scoped, so cross-tab sync relies
  // on the storage event not firing — which is fine.
  React.useEffect(() => {
    const onStorage = (e: StorageEvent) => {
      if (e.key === COOKIE_KEYS.arabicStyle && e.newValue) {
        setSettings(parseStored(e.newValue));
      }
    };
    window.addEventListener("storage", onStorage);
    return () => window.removeEventListener("storage", onStorage);
  }, []);

  const value = React.useMemo<Ctx>(
    () => ({
      settings,
      setMode: (mode) => {
        const suggested = arabicStyleModes[mode].suggested;
        if (suggested) {
          update({ mode, ...suggested });
        } else {
          update({ mode });
        }
      },
      setFontSize: (fontSize) => update({ fontSize }),
      setLineHeight: (lineHeight) => update({ lineHeight }),
      setBodyLineHeight: (bodyLineHeight) => update({ bodyLineHeight }),
      setWordSpacing: (wordSpacing) => update({ wordSpacing }),
      setLetterSpacing: (letterSpacing) => update({ letterSpacing }),
      setAlign: (align) => update({ align }),
      reset: () => update(defaultArabicSettings),
    }),
    [settings, update],
  );

  return <ArabicStyleContext.Provider value={value}>{children}</ArabicStyleContext.Provider>;
}

export function useArabicStyle() {
  const ctx = React.useContext(ArabicStyleContext);
  if (!ctx) {
    throw new Error("useArabicStyle must be used within ArabicStyleProvider");
  }
  return ctx;
}
