import { act, render, renderHook, screen, waitFor } from "@testing-library/react";
import React from "react";
import { beforeEach, describe, expect, it, vi } from "vitest";

import { AuthProvider, useAuth, type ManagerUser, type TutorUser } from "@/context/AuthContext";

const MANAGER_DATA: ManagerUser = {
  id: "m-1",
  loginId: "manager1",
  classroomId: "c-tds",
  classroomName: "戸塚",
  classroomNumber: 22,
  firstName: "太郎",
  lastName: "山田",
  role: "ROLE_MANAGER",
};

const TUTOR_DATA: TutorUser = {
  id: "t-1",
  loginId: "tutor1",
  classroomId: "c-tds",
  classroomName: "戸塚",
  classroomNumber: 22,
  firstName: "花子",
  lastName: "佐藤",
  tutorNumber: 1,
  terminated: false,
  terminationDate: null,
  role: "ROLE_TUTOR",
};

function Consumer() {
  const { user, isLoading, login, logout } = useAuth();
  return React.createElement("div", null, [
    React.createElement("span", { key: "l", "data-testid": "loading" }, String(isLoading)),
    React.createElement("span", { key: "u", "data-testid": "user" }, user ? user.loginId : "null"),
    React.createElement("button", {
      key: "login",
      onClick: () =>
        login(
          {
            id: TUTOR_DATA.id,
            loginId: TUTOR_DATA.loginId,
            classroomId: TUTOR_DATA.classroomId,
            classroomName: TUTOR_DATA.classroomName,
            classroomNumber: TUTOR_DATA.classroomNumber,
            firstName: TUTOR_DATA.firstName,
            lastName: TUTOR_DATA.lastName,
            tutorNumber: TUTOR_DATA.tutorNumber,
            terminated: TUTOR_DATA.terminated,
            terminationDate: TUTOR_DATA.terminationDate,
          },
          "ROLE_TUTOR"
        ),
      children: "login",
    }),
    React.createElement("button", {
      key: "logout",
      onClick: () => void logout(),
      children: "logout",
    }),
  ]);
}

const wrapper = ({ children }: { children: React.ReactNode }) =>
  React.createElement(AuthProvider, null, children);

beforeEach(() => {
  sessionStorage.clear();
});

describe("AuthContext", () => {
  it("sessionStorage に user_info がない初期状態は user=null, isLoading 経由で false に遷移する", async () => {
    render(React.createElement(AuthProvider, null, React.createElement(Consumer)));
    // mount 直後は useEffect 前後で isLoading の遷移があるため、最終的に false に落ち着く
    await waitFor(() => expect(screen.getByTestId("loading").textContent).toBe("false"));
    expect(screen.getByTestId("user").textContent).toBe("null");
  });

  it("hydrate: sessionStorage の有効な JSON から user を復元する", async () => {
    sessionStorage.setItem("user_info", JSON.stringify(TUTOR_DATA));
    render(React.createElement(AuthProvider, null, React.createElement(Consumer)));
    await waitFor(() => expect(screen.getByTestId("loading").textContent).toBe("false"));
    expect(screen.getByTestId("user").textContent).toBe("tutor1");
  });

  it("hydrate 失敗: 破損 JSON は取り除かれ user は null", async () => {
    sessionStorage.setItem("user_info", "{broken");
    render(React.createElement(AuthProvider, null, React.createElement(Consumer)));
    await waitFor(() => expect(screen.getByTestId("loading").textContent).toBe("false"));
    expect(screen.getByTestId("user").textContent).toBe("null");
    expect(sessionStorage.getItem("user_info")).toBeNull();
  });

  it("login() で user がセットされ sessionStorage も更新される", async () => {
    render(React.createElement(AuthProvider, null, React.createElement(Consumer)));
    await waitFor(() => expect(screen.getByTestId("loading").textContent).toBe("false"));

    act(() => screen.getByRole("button", { name: "login" }).click());

    await waitFor(() => expect(screen.getByTestId("user").textContent).toBe("tutor1"));
    const stored = sessionStorage.getItem("user_info");
    expect(stored).not.toBeNull();
    expect(JSON.parse(String(stored)).role).toBe("ROLE_TUTOR");
  });

  it("login(role='ROLE_MANAGER') が user.role を MANAGER にする", async () => {
    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    act(() => result.current.login(
      { id: "m-9", loginId: "m9", classroomId: "c", classroomName: "戸塚",
        classroomNumber: 22, firstName: "t", lastName: "y" },
      "ROLE_MANAGER"
    ));

    expect(result.current.user?.role).toBe("ROLE_MANAGER");
    expect(JSON.parse(String(sessionStorage.getItem("user_info"))).role).toBe("ROLE_MANAGER");
  });

  it("logout() で user が null になり sessionStorage も空になる", async () => {
    sessionStorage.setItem("user_info", JSON.stringify(TUTOR_DATA));
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(new Response(null, { status: 204 }))
    );

    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.isLoading).toBe(false));
    expect(result.current.user?.loginId).toBe("tutor1");

    await act(() => result.current.logout());

    expect(result.current.user).toBeNull();
    expect(sessionStorage.getItem("user_info")).toBeNull();
  });

  it("logout() は user.role が MANAGER なら /api/managers/logout を叩く", async () => {
    // CSRF トークンが既に設定されていれば /api/csrf は叩かれず、そのまま本リクエストが飛ぶ
    document.cookie = "XSRF-TOKEN=preset";
    sessionStorage.setItem("user_info", JSON.stringify(MANAGER_DATA));
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 204 }));
    vi.stubGlobal("fetch", fetchMock);

    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    await act(() => result.current.logout());

    const [url] = fetchMock.mock.calls[0] as [string];
    expect(url).toBe("/api/managers/logout");
  });

  it("logout() で API が失敗してもローカル状態はクリアされる", async () => {
    sessionStorage.setItem("user_info", JSON.stringify(TUTOR_DATA));
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new Error("network")));

    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    await act(() => result.current.logout());

    expect(result.current.user).toBeNull();
    expect(sessionStorage.getItem("user_info")).toBeNull();
  });

  it("Provider 外で useAuth を使うと例外を投げる", () => {
    // vitest は console.error でエラーを流すので、test のあいだ簡黙にする
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
    expect(() => renderHook(() => useAuth())).toThrow("useAuth must be used within an AuthProvider");
    consoleSpy.mockRestore();
  });
});
