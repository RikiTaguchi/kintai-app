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
};

export const viewport: Viewport = {
  // 講師テーマ（teal-600）でブラウザUIを着色
  themeColor: [{ color: "#0d9488" }],
};

export default function Layout({ children }: { children: ReactNode }) {
  return <TutorChrome>{children}</TutorChrome>;
}
