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
};

export const viewport: Viewport = {
  // 管理者テーマ（indigo-600）でブラウザUIを着色
  themeColor: [{ color: "#4f46e5" }],
};

export default function Layout({ children }: { children: ReactNode }) {
  return <ManagerChrome>{children}</ManagerChrome>;
}
