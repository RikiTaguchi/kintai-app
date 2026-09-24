import { buildManifest } from "@/lib/pwaManifest";

/**
 * 講師用 Web App Manifest を JSON で配信する。
 * （Next.js の manifest.ts 規約はルート直下のみのため、サブディレクトリでは
 *   Route Handler として実装。/tutor/manifest.webmanifest で提供される）
 *
 * PWA 運用上、manifest は「ユーザーの最終アクセス URL」に即座に反映されるべきで、
 * Safari/Chrome 両方ともインストール直前に最新を取得する。
 * そのため manifest はキャッシュしない（no-store で毎回最新を配信）。
 * （iOS Safari が以前に取得した他ロール manifest を誤用する事故を防ぐための保険）
 */
export async function GET() {
  return Response.json(buildManifest("tutor"), {
    headers: {
      "Content-Type": "application/manifest+json",
      "Cache-Control": "no-store, no-cache, must-revalidate",
    },
  });
}
