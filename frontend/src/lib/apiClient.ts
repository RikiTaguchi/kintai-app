function getCsrfToken(): string | null {
  if (typeof document === "undefined") return null;
  const match = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/);
  return match ? decodeURIComponent(match[1]) : null;
}

async function ensureCsrfToken(): Promise<string | null> {
  let token = getCsrfToken();
  if (!token) {
    await fetch("/api/csrf");
    token = getCsrfToken();
  }
  return token;
}

export async function apiClient<T>(input: RequestInfo | URL, init?: RequestInit): Promise<T> {
  const method = (init?.method ?? "GET").toUpperCase();
  const isMutating = ["POST", "PUT", "DELETE", "PATCH"].includes(method);

  const csrfHeaders: Record<string, string> = {};
  if (isMutating) {
    const token = await ensureCsrfToken();
    if (token) csrfHeaders["X-XSRF-TOKEN"] = token;
  }

  const res = await fetch(input, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...csrfHeaders,
      ...init?.headers,
    },
  });

  if (res.status === 401) {
    if (
      typeof window !== "undefined" &&
      !window.location.pathname.endsWith("/login") &&
      !window.location.pathname.startsWith("/manager/register")
    ) {
      const isManager = window.location.pathname.startsWith("/manager");
      sessionStorage.removeItem("user_info");
      window.location.href = isManager ? "/manager/login" : "/tutor/login";
    }
    throw new Error("認証エラーが発生しました。再ログインしてください。");
  }

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.error || "予期せぬエラーが発生しました。");
  }

  if (res.status === 204) return undefined as T;

  return res.json();
}
