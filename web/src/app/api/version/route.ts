import { BUILD_INFO } from "@/data/build-info";

export const dynamic = "force-static";

/**
 * GET /api/version
 *
 * Deployment verification endpoint. Open `<site>/api/version` in a browser:
 * if `version` is not the latest release, the server is running a stale
 * build (old deploy, failed restart, or CDN cache) — regardless of what
 * the page itself looks like.
 */
export function GET() {
  return Response.json(BUILD_INFO);
}
