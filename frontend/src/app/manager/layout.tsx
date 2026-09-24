import type { Metadata, Viewport } from "next";
import { ReactNode } from "react";
import ManagerChrome from "./ManagerChrome";

export const metadata: Metadata = {
  // 管理者用 PWA マニフェスト（Route Handler で配信）を紐付ける
  manifest: "/manager/manifest.webmanifest",
  appleWebApp: {
    // ホーム画面追加時にブラウザUIを非表示（スタンドアロン）にする
    capable: true,
    statusBarStyle: "default",
    title: "勤怠管理（管理者）",
  },
  // 旧 iOS Safari が参照する apple-mobile-web-app-capable。
  // Next.js は appleWebApp.capable から mobile-web-app-capable のみ生成するため、
  // レガシー名も other で明示的に出す（iOS 16 系でも仕様上有効）。
  other: {
    "apple-mobile-web-app-capable": "yes",
  },
};

export const viewport: Viewport = {
  // PWA（ホーム画面追加後のアプリ利用）でピンチ/ダブルタップズームを無効化
  width: "device-width",
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
  // 管理者テーマ（indigo-600）でブラウザUIを着色
  themeColor: [{ color: "#4f46e5" }],
};

export default function Layout({ children }: { children: ReactNode }) {
  return <ManagerChrome>{children}</ManagerChrome>;
}
