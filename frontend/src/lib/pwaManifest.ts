import type { MetadataRoute } from "next";

export type PwaRole = "manager" | "tutor";

interface PwaRoleConfig {
  /** アプリ名（ホーム画面アイコンの下やスプラッシュに表示） */
  name: string;
  startUrl: string;
  scope: string;
  themeColor: string;
}

const PWA_ROLE_CONFIGS: Record<PwaRole, PwaRoleConfig> = {
  manager: {
    name: "勤怠管理システム（管理者）",
    startUrl: "/manager/login",
    scope: "/manager/",
    themeColor: "#4f46e5", // indigo-600
  },
  tutor: {
    name: "勤怠管理システム（講師）",
    startUrl: "/tutor/login",
    scope: "/tutor/",
    themeColor: "#0d9488", // teal-600
  },
};

/**
 * ロール別の Web App Manifest を生成する。
 *
 * app/manager/manifest.ts, app/tutor/manifest.ts から呼び出され、
 * Next.js が /manager/manifest.webmanifest 等として配信する。
 * アイコンは public/icons/ 配下の共通画像を参照する。
 */
export function buildManifest(role: PwaRole): MetadataRoute.Manifest {
  const config = PWA_ROLE_CONFIGS[role];
  return {
    name: config.name,
    short_name: "勤怠管理",
    description: "学習塾向け勤怠管理システム",
    start_url: config.startUrl,
    scope: config.scope,
    display: "standalone",
    background_color: "#ffffff",
    theme_color: config.themeColor,
    icons: [
      {
        src: "/icons/icon-192.png",
        sizes: "192x192",
        type: "image/png",
      },
      {
        src: "/icons/icon-512.png",
        sizes: "512x512",
        type: "image/png",
      },
      {
        src: "/icons/icon-512-maskable.png",
        sizes: "512x512",
        type: "image/png",
        purpose: "maskable",
      },
    ],
  };
}
