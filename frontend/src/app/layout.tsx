import type { Metadata, Viewport } from "next";
import "./globals.css";
import { AuthProvider } from "@/context/AuthContext";
import { ToastProvider } from "@/context/ToastContext";

export const metadata: Metadata = {
  title: "勤怠管理システム",
  description: "勤怠管理システム",
  // ロールごとに /manager/manifest.webmanifest, /tutor/manifest.webmanifest を
  // 自動配信するため、ここでは manifest を指定しない（重複 link を避ける）
  icons: {
    // public/icons/ 配下の画像を参照
    icon: [
      { url: "/icons/favicon-32.png", sizes: "32x32", type: "image/png" },
      { url: "/icons/icon-192.png", sizes: "192x192", type: "image/png" },
      { url: "/icons/icon-512.png", sizes: "512x512", type: "image/png" },
    ],
    apple: [
      {
        url: "/icons/apple-touch-icon.png",
        sizes: "180x180",
        type: "image/png",
      },
    ],
  },
};

export const viewport: Viewport = {
  // PWA（ホーム画面追加後のアプリ利用）でピンチ/ダブルタップズームを無効化
  width: "device-width",
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
  // ブラウザのアドレスバー等のUI色（ライト/ダークで出し分け）
  themeColor: [
    { media: "(prefers-color-scheme: light)", color: "#ffffff" },
    { media: "(prefers-color-scheme: dark)", color: "#111827" },
  ],
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ja" className="h-full antialiased">
      <body className="min-h-full flex flex-col">
        <AuthProvider>
          <ToastProvider>{children}</ToastProvider>
        </AuthProvider>
      </body>
    </html>
  );
}
