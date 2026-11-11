import { NextRequest, NextResponse } from "next/server";

export const runtime = "nodejs";

const ALLOWED_KEYS = {
  arabicStyle: "tahlil-arabic-style-v1",
  displayToggles: "tahlil-display-toggles-v1",
} as const;

type Body = {
  key: keyof typeof ALLOWED_KEYS;
  value: string;
};

const COOKIE_MAX_AGE = 60 * 60 * 24 * 365; // 1 year

/**
 * Persist user preference (ArabicStyle or DisplayToggles) to an HTTP-only
 * cookie. The body must be JSON: { key, value } where key is one of
 * ALLOWED_KEYS. Returns 200 on success, 400 on validation error.
 *
 * Theme is handled by next-themes (it sets its own cookie) — we don't
 * touch it here.
 */
export async function POST(req: NextRequest) {
  let body: Body;
  try {
    body = (await req.json()) as Body;
  } catch {
    return NextResponse.json({ error: "Invalid JSON body" }, { status: 400 });
  }

  if (!body || typeof body.key !== "string" || typeof body.value !== "string") {
    return NextResponse.json({ error: "Missing key or value" }, { status: 400 });
  }

  const cookieName = ALLOWED_KEYS[body.key];
  if (!cookieName) {
    return NextResponse.json(
      { error: `Unknown key: ${body.key}. Allowed: ${Object.keys(ALLOWED_KEYS).join(", ")}` },
      { status: 400 },
    );
  }

  // Sanity check: cap value size to prevent abuse
  if (body.value.length > 2048) {
    return NextResponse.json({ error: "Value too large" }, { status: 413 });
  }

  const res = NextResponse.json({ ok: true });
  res.cookies.set({
    name: cookieName,
    value: body.value,
    path: "/",
    maxAge: COOKIE_MAX_AGE,
    sameSite: "lax",
    // Not HttpOnly so the client can also read it for instant UI updates
    // before the server round-trip completes.
    httpOnly: false,
  });
  return res;
}
