import { cookies } from "next/headers";

/**
 * Server-side cookie reader for user preferences.
 * These cookies are set client-side via /api/settings (see api route).
 * All cookies use a stable name and 1-year maxAge.
 */

export const COOKIE_KEYS = {
  theme: "tahlil-theme",
  arabicStyle: "tahlil-arabic-style-v1",
  displayToggles: "tahlil-display-toggles-v1",
} as const;

const COOKIE_MAX_AGE = 60 * 60 * 24 * 365; // 1 year

export async function getCookie<T>(
  name: string,
  parse: (raw: string) => T | null,
): Promise<T | null> {
  const store = await cookies();
  const raw = store.get(name)?.value;
  if (!raw) return null;
  try {
    return parse(raw);
  } catch {
    return null;
  }
}

export async function getArabicStyleCookie(): Promise<string | null> {
  const store = await cookies();
  return store.get(COOKIE_KEYS.arabicStyle)?.value ?? null;
}

export async function getDisplayTogglesCookie(): Promise<string | null> {
  const store = await cookies();
  return store.get(COOKIE_KEYS.displayToggles)?.value ?? null;
}

export async function getThemeCookie(): Promise<string | null> {
  const store = await cookies();
  return store.get(COOKIE_KEYS.theme)?.value ?? null;
}

export { COOKIE_MAX_AGE };
