import type { MetadataRoute } from "next";

export type PwaRole = "manager" | "tutor";

interface PwaRoleConfig {
  /** アプリ名（ホーム画面アイコンの下やスプラッシュに表示） */
  name: string;
  /** 短いアプリ名（アイコン下の表示用） */
  shortName: string;
  /** manifest の id に使うロール固有パス（クエリなしで一意にする） */
  id: string;
  /** ホーム画面起動時に開く URL */
  startUrl: string;
  scope: string;
  themeColor: string;
}

const PWA_ROLE_CONFIGS: Record<PwaRole, PwaRoleConfig> = {
  manager: {
    name: "勤怠管理システム（管理者）",
    shortName: "勤怠管理(管理者)",
    id: "/manager/",
    // middleware が /manager/login へリダイレクトする（未認証時）
    startUrl: "/manager/",
    scope: "/manager/",
    themeColor: "#4f46e5", // indigo-600
  },
  tutor: {
    name: "勤怠管理システム（講師）",
    shortName: "勤怠管理(講師)",
    id: "/tutor/",
    // middleware が /tutor/login へリダイレクトする（未認証時）
    startUrl: "/tutor/",
    scope: "/tutor/",
    themeColor: "#0d9488", // teal-600
  },
};

/**
 * ロール別の Web App Manifest を生成する。
 *
 * app/manager/manifest.webmanifest/route.ts, app/tutor/manifest.webmanifest/route.ts
 * から呼び出され、Route Handler が /manager/manifest.webmanifest 等で配信する。
 * `DisplayModeOverride` は Next.js の generated route types に存在しない場合があるため
 * ローカル型で定義する。
 */
type DisplayModeOverride = "fullscreen" | "standalone" | "minimal-ui" | "browser";

export function buildManifest(role: PwaRole): MetadataRoute.Manifest {
  const config = PWA_ROLE_CONFIGS[role];
  return {
    // id をロールで分離し、同一オリジンに管理者版・講師版を2つの別アプリとして
    // インストールできるようにする（Chrome は id をキーにインストール済みアプリを識別する）
    id: config.id,
    start_url: config.startUrl,
    // クエリでホスト側（Safari）から起動したことを検知できるようにする
    // （standalone でない場合にアプリ内リダイレクトする判定材料）
    name: config.name,
    short_name: config.shortName,
    description: "学習塾向け勤怠管理システム",
    display_override: ["standalone"] as DisplayModeOverride[],
    display: "standalone",
    scope: config.scope,
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
