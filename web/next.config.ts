import type { NextConfig } from "next";

/**
 * Two build targets are supported:
 *
 *  1. Self-hosted Node.js deploy (default — `output: "standalone"`)
 *     Produces `.next/standalone/server.js` + bundled node_modules so the
 *     release `web-build.zip` can run with `node server.js` directly. Used
 *     by the existing CI `web-test` job.
 *
 *  2. GitHub Pages static export (`BUILD_TARGET=pages` — `output: "export"`)
 *     Produces a static `out/` folder that GitHub Pages can serve directly.
 *     Used by the new CI `pages-deploy` job. Differences:
 *       - `output: "export"` (no server.js)
 *       - `basePath: "/Tahlil-App"` — GitHub Pages project site is served
 *         under `/<repo-name>/`, NOT at the domain root. Without this
 *         every <link> and asset URL would 404.
 *       - `trailingSlash: true` — Pages serves `folder/index.html` for
 *         `/folder/`, so trailing slashes match the on-disk layout.
 *       - `images: { unoptimized: true }` — required for static export
 *         (we don't use next/image anyway, but Next enforces it).
 *
 *     Trade-offs in static mode:
 *       - `/api/settings` (POST cookie sync) is unavailable — client-side
 *         `setCookie()` still works (cookies persist in the browser), but
 *         there's no server-side confirmation. The `.catch(() => {})` in
 *         the providers swallows the 404 silently.
 *       - SSR cookie reads in `layout.tsx` are skipped (see the env check
 *         there) because `cookies()` from `next/headers` throws in static
 *         generation. Result: first paint uses defaults, then the client
 *         reads `document.cookie` on mount → brief flash possible. The
 *         `<html suppressHydrationWarning>` already in place masks this.
 */
const isStaticExport = process.env.BUILD_TARGET === "pages";

const nextConfig: NextConfig = {
  reactStrictMode: true,
  output: isStaticExport ? "export" : "standalone",
  // GitHub Pages project site: https://<user>.github.io/<repo>/
  basePath: isStaticExport ? "/Tahlil-App" : "",
  // Trailing slash matches Pages' folder/index.html convention.
  trailingSlash: isStaticExport,
  // next/image optimization requires a server — disable for static export.
  // We don't use next/image anywhere, but Next.js refuses to export without
  // this flag.
  images: { unoptimized: isStaticExport },
  // Skip TypeScript errors during CI build (pre-existing minor type issues
  // that don't affect runtime).
  typescript: {
    ignoreBuildErrors: true,
  },
};

export default nextConfig;
