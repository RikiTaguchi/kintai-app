import type { Metadata, Viewport } from "next";
import { ReactNode } from "react";
import TutorChrome from "./TutorChrome";

export const metadata: Metadata = {
  // 講師用 PWA マニフェスト（Route Handler で配信）を紐付ける
  manifest: "/tutor/manifest.webmanifest",
  appleWebApp: {
    // ホーム画面追加時にブラウザUIを非表示（スタンドアロン）にする
    capable: true,
    statusBarStyle: "default",
    title: "勤怠管理（講師）",
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
  // 講師テーマ（teal-600）でブラウザUIを着色
  themeColor: [{ color: "#0d9488" }],
};

export default function Layout({ children }: { children: ReactNode }) {
  return <TutorChrome>{children}</TutorChrome>;
}
