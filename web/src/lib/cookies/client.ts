/**
 * Client-side cookie helper — minimal, framework-agnostic.
 * Cookies are used (instead of localStorage) so the server-side render
 * can read the same state and avoid hydration flicker.
 */

export const COOKIE_KEYS = {
  theme: "tahlil-theme",
  arabicStyle: "tahlil-arabic-style-v1",
  displayToggles: "tahlil-display-toggles-v1",
} as const;

const COOKIE_MAX_AGE = 60 * 60 * 24 * 365; // 1 year

export function setCookie(name: string, value: string): void {
  if (typeof document === "undefined") return;
  // Encoded value, path=/ so it's available app-wide, SameSite=Lax for safety
  const encoded = encodeURIComponent(value);
  document.cookie = `${name}=${encoded}; path=/; max-age=${COOKIE_MAX_AGE}; SameSite=Lax`;
}

export function getCookie(name: string): string | null {
  if (typeof document === "undefined") return null;
  const match = document.cookie.split("; ").find((c) => c.startsWith(`${name}=`));
  if (!match) return null;
  const value = match.slice(name.length + 1);
  try {
    return decodeURIComponent(value);
  } catch {
    return value;
  }
}

export function deleteCookie(name: string): void {
  if (typeof document === "undefined") return;
  document.cookie = `${name}=; path=/; max-age=0; SameSite=Lax`;
}
