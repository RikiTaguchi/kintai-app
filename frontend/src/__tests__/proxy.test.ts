import { describe, expect, it, vi } from "vitest";
import { config as proxyConfig } from "@/proxy";

// next/server は Edge 依存を持つため、テストでは NextResponse/NextRequest を
// 手務扱いの最小実装でモックする。proxy.ts のロジックは response の
// "呼び出し方" のみを確認できれば足りるので、NextResponse 本体は差し替える。
vi.mock("next/server", () => {
  return {
    NextResponse: {
      next() {
        return { __kind: "next" };
      },
      redirect(url: URL) {
        return { __kind: "redirect", url: String(url) };
      },
    },
  };
});

import proxy from "@/proxy";

function makeRequest(
  pathname: string,
  cookies: Record<string, string> = {},
  url = "http://localhost"
): never {
  return {
    nextUrl: { pathname },
    url: url + pathname,
    cookies: {
      has: (k: string) => k in cookies,
      get: (k: string) => (cookies[k] != null ? { value: cookies[k] } : undefined),
    },
  } as never;
}

describe("proxy middleware", () => {
  describe("マネージ系", () => {
    it("未ログインで /manager/login は許可", () => {
      const res = proxy(makeRequest("/manager/login"));
      expect(res).toEqual({ __kind: "next" });
    });

    it("未ログインで /manager/register も許可", () => {
      const res = proxy(makeRequest("/manager/register"));
      expect(res).toEqual({ __kind: "next" });
    });

    it("JSESSIONID がないとリダイレクト → /manager/login", () => {
      const res = proxy(makeRequest("/manager/tutors"));
      expect(res).toEqual({
        __kind: "redirect",
        url: "http://localhost/manager/login",
      });
    });

    it("JSESSIONID + user_role=MANAGER → 許可", () => {
      const res = proxy(
        makeRequest("/manager/tutors", { JSESSIONID: "abc", user_role: "MANAGER" })
      );
      expect(res).toEqual({ __kind: "next" });
    });

    it("JSESSIONID + user_role=TUTOR → 自身のロールのホーム /tutor にリダイレクト", () => {
      // ロール不一致の場合は管理側 /login への誘導ではなく、
      // 自身のロールに合った画面へ戻す UX 設計
      const res = proxy(
        makeRequest("/manager/tutors", { JSESSIONID: "abc", user_role: "TUTOR" })
      );
      expect(res).toEqual({
        __kind: "redirect",
        url: "http://localhost/tutor",
      });
    });

    it("JSESSIONID なし（user_role はある）→ リダイレクト（未ログイン判定が優先）", () => {
      const res = proxy(makeRequest("/manager/tutors", { user_role: "MANAGER" }));
      expect(res).toEqual({
        __kind: "redirect",
        url: "http://localhost/manager/login",
      });
    });
  });

  describe("チューター系", () => {
    it("未ログインで /tutor/login は許可", () => {
      const res = proxy(makeRequest("/tutor/login"));
      expect(res).toEqual({ __kind: "next" });
    });

    it("JSESSIONID + user_role=TUTOR → 許可", () => {
      const res = proxy(
        makeRequest("/tutor/works", { JSESSIONID: "abc", user_role: "TUTOR" })
      );
      expect(res).toEqual({ __kind: "next" });
    });

    it("JSESSIONID + user_role=MANAGER → 自身のロールのホーム /manager にリダイレクト", () => {
      const res = proxy(
        makeRequest("/tutor/works", { JSESSIONID: "abc", user_role: "MANAGER" })
      );
      expect(res).toEqual({
        __kind: "redirect",
        url: "http://localhost/manager",
      });
    });

    it("user_role 欠落 → リダイレクト", () => {
      const res = proxy(makeRequest("/tutor/works", { JSESSIONID: "abc" }));
      expect(res).toEqual({
        __kind: "redirect",
        url: "http://localhost/tutor/login",
      });
    });
  });

  describe("その他", () => {
    it("matcher 外のパス（.css, .png 等）は素通し", () => {
      const res = proxy(makeRequest("/_next/static/chunk.js"));
      expect(res).toEqual({ __kind: "next" });
    });

    it("ルート / は素通し", () => {
      const res = proxy(makeRequest("/"));
      expect(res).toEqual({ __kind: "next" });
    });
  });

  describe("manifest 除外（リグレッション監視）", () => {
    // PWA manifest は未認証で取得できる必要がある（Android は Cookie なしで fetch する）。
    // matcher が manifest.webmanifest を弾かなくなった場合、PWA 追加が壊れる静かな退行を
    // 防ぐため、matcher の設定に除外条件が残っていることを継続的に検証する。
    it("matcher が manifest.webmanifest を除外条件に含めている", () => {
      const matcher = proxyConfig.matcher as string[];
      // 両ロールとも manifest 除外条件（否定先読み）を含む matcher であること
      const hasExclusionFor = (rolePath: "manager" | "tutor") =>
        matcher.some(
          (p) =>
            p.startsWith(`/${rolePath}/`) &&
            p.includes("manifest\\.webmanifest")
        );
      expect(hasExclusionFor("manager")).toBe(true);
      expect(hasExclusionFor("tutor")).toBe(true);
    });
  });
});
