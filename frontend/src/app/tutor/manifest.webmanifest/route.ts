import { buildManifest } from "@/lib/pwaManifest";

/**
 * 講師用 Web App Manifest を JSON で配信する。
 * （Next.js の manifest.ts 規約はルート直下のみのため、サブディレクトリでは
 *   Route Handler として実装。/tutor/manifest.webmanifest で提供される）
 */
export async function GET() {
  return Response.json(buildManifest("tutor"), {
    headers: {
      "Content-Type": "application/manifest+json",
    },
  });
}
