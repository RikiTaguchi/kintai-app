import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

/**
 * Edge middleware: セッション cookie の有無と user_role に応じたアクセス制御。
 *
 * - /manager/* は MANAGER ロール専用
 * - /tutor/* は TUTOR ロール専用
 * - 未ログイン（JSESSIONID なし）→ アクセス先ロールの /login へ
 * - ロール不一致 → 自分のロールのホーム (/manager or /tutor) へ
 *   （誤ったロールの管理画面に誤導させない UX 設計）
 */
export default function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;

  const hasSession = request.cookies.has("JSESSIONID");
  const userRole = request.cookies.get("user_role")?.value;

  if (pathname.startsWith("/manager")) {
    if (pathname === "/manager/login" || pathname === "/manager/register") {
      return NextResponse.next();
    }
    if (!hasSession) {
      return NextResponse.redirect(new URL("/manager/login", request.url));
    }
    if (userRole !== "MANAGER") {
      // 自身のロールのホームへ戻す（未ログイン時は上でTUTOR側 /login へ誘導）
      const home = userRole === "TUTOR" ? "/tutor" : "/manager/login";
      return NextResponse.redirect(new URL(home, request.url));
    }
  }

  if (pathname.startsWith("/tutor")) {
    if (pathname === "/tutor/login") {
      return NextResponse.next();
    }
    if (!hasSession) {
      return NextResponse.redirect(new URL("/tutor/login", request.url));
    }
    if (userRole !== "TUTOR") {
      const home = userRole === "MANAGER" ? "/manager" : "/tutor/login";
      return NextResponse.redirect(new URL(home, request.url));
    }
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    // PWA の manifest.webmanifest は認証チェックから除外（Android が未認証で取得するため）
    "/manager/:path((?!manifest\\.webmanifest$).*)",
    "/tutor/:path((?!manifest\\.webmanifest$).*)",
  ],
};
