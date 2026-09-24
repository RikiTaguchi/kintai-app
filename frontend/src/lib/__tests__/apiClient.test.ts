import { describe, expect, it, vi } from "vitest";

import { apiClient } from "@/lib/apiClient";

// ---- fetch モック helpers ----------------------------------------------------

function mockJsonResponse({
  status = 200,
  body = {},
}: { status?: number; body?: unknown } = {}) {
  return new Response(body === null ? null : JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

function setHref(href: string) {
  window.history.replaceState({}, "", href);
}

// jsdom では window.location の setter をモックできない
// （configurable: false かつ Object.defineProperty 不可）。
// そのため production コードの `location.href = ...` による遷移は
// jsdom では何も起きない。ここでは「location.href に代入されているか」を
// localStorage がユーザー情報消去に使われている点と合わせて観測する
// ことで、「401 → 遷移するブランチに入ったこと」を間接的に検証する。

// -----------------------------------------------------------------------------

describe("apiClient", () => {
  describe("CSRF ヘッダ", () => {
    it("GET では CSRF トークンを要求・付与しない", async () => {
      const fetchMock = vi.fn().mockResolvedValue(mockJsonResponse({ body: [] }));
      vi.stubGlobal("fetch", fetchMock);
      document.cookie = "XSRF-TOKEN=existing-token";

      await apiClient("/api/test");

      expect(fetchMock).toHaveBeenCalledTimes(1);
      const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit];
      expect(url).toBe("/api/test");
      const headers = init.headers as Record<string, string>;
      expect(headers["X-XSRF-TOKEN"]).toBeUndefined();
    });

    it("POST で既存クッキーがあればそれをヘッダに付ける（/api/csrf は叩かない）", async () => {
      const fetchMock = vi.fn().mockResolvedValue(mockJsonResponse({}));
      vi.stubGlobal("fetch", fetchMock);
      document.cookie = "XSRF-TOKEN=my-token";

      await apiClient("/api/test", { method: "POST" });

      expect(fetchMock).toHaveBeenCalledTimes(1);
      const [, init] = fetchMock.mock.calls[0] as [string, RequestInit];
      const headers = init.headers as Record<string, string>;
      expect(headers["X-XSRF-TOKEN"]).toBe("my-token");
    });

    it("POST でクッキーが無いとき一度だけ /api/csrf を叩く", async () => {
      const fetchMock = vi
        .fn()
        .mockResolvedValueOnce(mockJsonResponse({ body: { token: "x" } })) // /api/csrf
        .mockResolvedValueOnce(mockJsonResponse({})); // 本リクエスト
      vi.stubGlobal("fetch", fetchMock);
      window.document.cookie = ""; // hostname localhost で空に（jsdom）
      // jsdom 上の document.cookie は HttpOnly 持ちではないため、ここでは呼ばれた回数のみ確認
      document.cookie = "XSRF-TOKEN="; // 空にする
      document.cookie = "XSRF-TOKEN=deleted; expires=Thu, 01 Jan 1970 00:00:00 GMT";

      await apiClient("/api/test", { method: "POST" });

      // /api/csrf → 本リクエスト の順で2回
      expect(fetchMock).toHaveBeenCalledTimes(2);
      const [firstUrl] = fetchMock.mock.calls[0] as [string, RequestInit];
      expect(firstUrl).toBe("/api/csrf");
    });

    it("PUT / DELETE / PATCH も CSRF を付与する", async () => {
      const fetchMock = vi.fn().mockImplementation(() =>
        Promise.resolve(mockJsonResponse({}))
      );
      vi.stubGlobal("fetch", fetchMock);
      document.cookie = "XSRF-TOKEN=tk";

      for (const method of ["PUT", "DELETE", "PATCH"] as const) {
        fetchMock.mockClear();
        await apiClient("/api/test", { method });
        const [, init] = fetchMock.mock.calls[0] as [string, RequestInit];
        const headers = init.headers as Record<string, string>;
        expect(headers["X-XSRF-TOKEN"]).toBe("tk");
      }
    });
  });

  describe("Content-Type", () => {
    it("application/json をデフォルトで付与する", async () => {
      const fetchMock = vi.fn().mockResolvedValue(mockJsonResponse({}));
      vi.stubGlobal("fetch", fetchMock);
      document.cookie = "XSRF-TOKEN=tk";

      await apiClient("/api/test");
      const [, init] = fetchMock.mock.calls[0] as [string, RequestInit];
      const headers = init.headers as Record<string, string>;
      expect(headers["Content-Type"]).toBe("application/json");
    });

    it("呼び出し側の headers がデフォルトを上書きする", async () => {
      const fetchMock = vi.fn().mockResolvedValue(mockJsonResponse({}));
      vi.stubGlobal("fetch", fetchMock);
      document.cookie = "XSRF-TOKEN=tk";

      await apiClient("/api/test", { headers: { "X-Custom": "yes" } });
      const [, init] = fetchMock.mock.calls[0] as [string, RequestInit];
      const headers = init.headers as Record<string, string>;
      expect(headers["X-Custom"]).toBe("yes");
      expect(headers["Content-Type"]).toBe("application/json");
    });
  });

  describe("正常系", () => {
    it("200 + JSON → パース結果を返す", async () => {
      vi.stubGlobal("fetch", vi.fn().mockResolvedValue(mockJsonResponse({ body: { id: 1 } })));
      const result = await apiClient<{ id: number }>("/api/test");
      expect(result).toEqual({ id: 1 });
    });

    it("204 No Content → undefined", async () => {
      vi.stubGlobal(
        "fetch",
        vi.fn().mockResolvedValue(new Response(null, { status: 204 }))
      );
      const result = await apiClient<undefined>("/api/test");
      expect(result).toBeUndefined();
    });
  });

  describe("エラーレスポンス", () => {
    it("400 + error フィールド → そのメッセージで throw", async () => {
      vi.stubGlobal(
        "fetch",
        vi.fn().mockResolvedValue(mockJsonResponse({ status: 400, body: { error: "入力が不正です" } }))
      );
      await expect(apiClient("/api/test")).rejects.toThrow("入力が不正です");
    });

    it("エラー用 body が parse できなければデフォルトメッセージで throw", async () => {
      vi.stubGlobal(
        "fetch",
        vi
          .fn()
          .mockResolvedValue(
            new Response("not json", { status: 500, headers: { "Content-Type": "text/plain" } })
          )
      );
      await expect(apiClient("/api/test")).rejects.toThrow("予期せぬエラーが発生しました。");
    });
  });

  describe("401 ハンドリング", () => {
    // production は `location.href = "/manager/login"` または `"/tutor/login"` を
    // 代入する。jsdom では location の setter をモックできないため、
    // 遷移 URL 自体は確認できないが、代わりに：
    //   ・認証エラー必須メッセージが投げられること
    //   ・localStorage の user_info が削除されること（遷移分岐に入った証）
    // の2点で各パターンを固定する。遷移 URL の選択ロジック（/manager/* と
    // その他の判定）は E2E/手動で確認要（本ユニットテストでは検証不能）。

    it("/tutor/* 以外のパスで 401 → 認証エラーで再ログインを促す（user_info 削除）", async () => {
      localStorage.setItem("user_info", JSON.stringify({ id: "u-1" }));
      vi.stubGlobal(
        "fetch",
        vi.fn().mockResolvedValue(mockJsonResponse({ status: 401 }))
      );
      setHref("/manager/works/t-1");

      await expect(apiClient("/api/test")).rejects.toThrow(
        "認証エラーが発生しました。再ログインしてください。"
      );
      expect(localStorage.getItem("user_info")).toBeNull();
    });

    it("/tutor/* のパスで 401 → 認証エラーで再ログインを促す（user_info 削除）", async () => {
      localStorage.setItem("user_info", JSON.stringify({ id: "u-1" }));
      vi.stubGlobal(
        "fetch",
        vi.fn().mockResolvedValue(mockJsonResponse({ status: 401 }))
      );
      setHref("/tutor/works");

      await expect(apiClient("/api/test")).rejects.toThrow(
        "認証エラーが発生しました。再ログインしてください。"
      );
      expect(localStorage.getItem("user_info")).toBeNull();
    });

    it("既に /login に居るとき 401 → user_info は削除されない（遷移もしない分岐）", async () => {
      localStorage.setItem("user_info", JSON.stringify({ id: "u-1" }));
      vi.stubGlobal(
        "fetch",
        vi.fn().mockResolvedValue(mockJsonResponse({ status: 401 }))
      );
      setHref("/manager/login");

      await expect(apiClient("/api/test")).rejects.toThrow(
        "認証エラーが発生しました。再ログインしてください。"
      );
      // production: 遷移をしないので localStorage も消されない
      expect(localStorage.getItem("user_info")).not.toBeNull();
    });

    it("/manager/register で 401 → user_info は削除されない", async () => {
      localStorage.setItem("user_info", JSON.stringify({ id: "u-1" }));
      vi.stubGlobal(
        "fetch",
        vi.fn().mockResolvedValue(mockJsonResponse({ status: 401 }))
      );
      setHref("/manager/register");

      await expect(apiClient("/api/test")).rejects.toThrow(
        "認証エラーが発生しました。再ログインしてください。"
      );
      expect(localStorage.getItem("user_info")).not.toBeNull();
    });

    it("401 レスポンスはパスに関わらず必ず認証エラーメッセージを投げる", async () => {
      vi.stubGlobal(
        "fetch",
        vi.fn().mockResolvedValue(mockJsonResponse({ status: 401 }))
      );
      setHref("/manager/works");

      await expect(apiClient("/api/test")).rejects.toThrow(
        "認証エラーが発生しました。再ログインしてください。"
      );
    });
  });
});
