/**
 * Build information — baked into the web bundle at CI build time.
 *
 * CI (`.github/workflows/ci.yml`, job web-test) overwrites this file right
 * before `next build` with the real values taken from:
 *   - version  → android/app/build.gradle.kts `versionName`
 *   - commit   → git short SHA of the built commit
 *   - builtAt  → UTC timestamp of the build
 *
 * This committed version is only a local-dev fallback.
 *
 * Surfaced in two places so anyone can verify which build a server is
 * actually serving:
 *   - GET /api/version      → JSON (version, commit, builtAt)
 *   - footer version chip   → v{version}
 */
export const BUILD_INFO = {
  app: "Tahlil-Doa Web",
  version: "1.2.2",
  commit: "local-dev",
  builtAt: "unknown",
} as const;
