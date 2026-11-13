"use client";

import * as React from "react";
import {
  AUTO_SCROLL_SPEED_MAX,
  AUTO_SCROLL_SPEED_MIN,
  DisplayToggles,
  defaultDisplayToggles,
} from "@/data/display-settings";
import { COOKIE_KEYS, getCookie, setCookie } from "@/lib/cookies/client";

type Ctx = {
  toggles: DisplayToggles;
  setToggle: (key: keyof DisplayToggles, value: boolean) => void;
  /** Update a numeric setting (autoScrollSpeed / autoScrollSectionPauseSec). */
  setNumber: <K extends "autoScrollSpeed" | "autoScrollSectionPauseSec">(
    key: K,
    value: DisplayToggles[K],
  ) => void;
  /** Update the auto-scroll mode (continuous / block). */
  setAutoScrollMode: (mode: "continuous" | "block") => void;
  setAll: (next: DisplayToggles) => void;
  reset: () => void;
};

const DisplaySettingsContext = React.createContext<Ctx | null>(null);

function parseStored(raw: string | null): DisplayToggles {
  if (!raw) return defaultDisplayToggles;
  try {
    const parsed = JSON.parse(raw) as Partial<DisplayToggles>;
    return {
      arabic: typeof parsed.arabic === "boolean" ? parsed.arabic : true,
      transliteration: typeof parsed.transliteration === "boolean" ? parsed.transliteration : true,
      translation: typeof parsed.translation === "boolean" ? parsed.translation : true,
      punctuateArabic: typeof parsed.punctuateArabic === "boolean" ? parsed.punctuateArabic : false,
      splitArabicLines:
        typeof parsed.splitArabicLines === "boolean" ? parsed.splitArabicLines : true,
      colorizeArabic: typeof parsed.colorizeArabic === "boolean" ? parsed.colorizeArabic : false,
      tajwid: typeof parsed.tajwid === "boolean" ? parsed.tajwid : false,
      autoScrollEnabled:
        typeof parsed.autoScrollEnabled === "boolean" ? parsed.autoScrollEnabled : false,
      autoScrollMode:
        parsed.autoScrollMode === "continuous" || parsed.autoScrollMode === "block"
          ? parsed.autoScrollMode
          : "continuous",
      // Auto-scroll numeric settings — clamp into the valid ranges so a
      // hand-edited cookie can never produce a runaway scroll speed.
      autoScrollSpeed:
        typeof parsed.autoScrollSpeed === "number"
          ? Math.min(AUTO_SCROLL_SPEED_MAX, Math.max(AUTO_SCROLL_SPEED_MIN, parsed.autoScrollSpeed))
          : defaultDisplayToggles.autoScrollSpeed,
      autoScrollSectionPauseSec:
        typeof parsed.autoScrollSectionPauseSec === "number" &&
        parsed.autoScrollSectionPauseSec >= 0 &&
        parsed.autoScrollSectionPauseSec <= 30
          ? parsed.autoScrollSectionPauseSec
          : defaultDisplayToggles.autoScrollSectionPauseSec,
    };
  } catch {
    return defaultDisplayToggles;
  }
}

const SERVER_INITIAL: DisplayToggles | null =
  typeof window !== "undefined" ? parseStored(getCookie(COOKIE_KEYS.displayToggles)) : null;

export function DisplaySettingsProvider({
  children,
  initialToggles,
}: {
  children: React.ReactNode;
  initialToggles?: DisplayToggles;
}) {
  const [toggles, setToggles] = React.useState<DisplayToggles>(
    () => initialToggles ?? SERVER_INITIAL ?? defaultDisplayToggles,
  );

  const persist = React.useCallback((next: DisplayToggles) => {
    const value = JSON.stringify(next);
    setCookie(COOKIE_KEYS.displayToggles, value);
    if (typeof fetch !== "undefined") {
      void fetch("/api/settings", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ key: "displayToggles", value }),
        keepalive: true,
      }).catch(() => {});
    }
  }, []);

  const update = React.useCallback(
    (next: DisplayToggles) => {
      setToggles(next);
      persist(next);
    },
    [persist],
  );

  const value = React.useMemo<Ctx>(
    () => ({
      toggles,
      setToggle: (key, val) => update({ ...toggles, [key]: val } as DisplayToggles),
      setNumber: (key, val) => update({ ...toggles, [key]: val } as DisplayToggles),
      setAutoScrollMode: (mode) => update({ ...toggles, autoScrollMode: mode }),
      setAll: (next) => update(next),
      reset: () => update(defaultDisplayToggles),
    }),
    [toggles, update],
  );

  return (
    <DisplaySettingsContext.Provider value={value}>{children}</DisplaySettingsContext.Provider>
  );
}

export function useDisplaySettings() {
  const ctx = React.useContext(DisplaySettingsContext);
  if (!ctx) {
    throw new Error("useDisplaySettings must be used within DisplaySettingsProvider");
  }
  return ctx;
}
