import { buildManifest } from "@/lib/pwaManifest";

/**
 * 管理者用 Web App Manifest を JSON で配信する。
 * （Next.js の manifest.ts 規約はルート直下のみのため、サブディレクトリでは
 *   Route Handler として実装。/manager/manifest.webmanifest で提供される）
 */
export async function GET() {
  return Response.json(buildManifest("manager"), {
    headers: {
      "Content-Type": "application/manifest+json",
    },
  });
}
