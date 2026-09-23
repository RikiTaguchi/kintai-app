import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";

import ManagerLayout from "@/components/layout/ManagerLayout";
import TutorLayout from "@/components/layout/TutorLayout";
import { AuthProvider } from "@/context/AuthContext";

// usePathname を差し替える（次_router会依赖）
function mockPathname(pathname: string) {
  vi.doMock("next/navigation", () => ({
    usePathname: () => pathname,
    useRouter: () => ({ push: () => {}, replace: () => {} }),
  }));
}

// Next.js は Link の href を child の a タグにたどる対応（vi.mock は per-test 必要）
vi.mock("next/navigation", () => ({
  usePathname: () => "/",
  useRouter: () => ({ push: () => {}, replace: () => {} }),
}));

function renderWithAuth(
  ui: React.ReactNode,
  user?: { loginId: string; classroomName: string } | null
) {
  // AuthProvider は sessionStorage.user_info から hydrate する
  if (user) {
    sessionStorage.setItem(
      "user_info",
      JSON.stringify({
        id: "u-1",
        loginId: user.loginId,
        classroomId: "c",
        classroomName: user.classroomName,
        classroomNumber: 22,
        firstName: "Taro",
        lastName: "Yamada",
        role: "ROLE_MANAGER",
      })
    );
  }
  return render(<AuthProvider>{ui}</AuthProvider>);
}

describe("ManagerLayout", () => {
  it("サイドバーの3つのナビリンクを描画する", async () => {
    renderWithAuth(<ManagerLayout>x</ManagerLayout>, {
      loginId: "manager1",
      classroomName: "戸塚",
    });
    // render直後は mobileMenuOpen=false だからサイドバー has1 個、
    // mobileMenu 展開後は計 2 個の los 同じラベルが DOM に存在する
    expect((await screen.findAllByText("ダッシュボード")).length).toBeGreaterThanOrEqual(1);
    expect((await screen.findAllByText("講師管理")).length).toBeGreaterThan(0);
    expect((await screen.findAllByText("勤務管理")).length).toBeGreaterThan(0);
  });

  it("ユーザー名と教室名を表示する", async () => {
    renderWithAuth(<ManagerLayout>x</ManagerLayout>, {
      loginId: "manager1",
      classroomName: "戸塚",
    });
    // AuthContext の hydrate は useEffect 経由 → findBy で待機
    expect((await screen.findAllByText(/Yamada Taro/)).length).toBeGreaterThan(0);
    expect((await screen.findAllByText(/戸塚教室/)).length).toBeGreaterThan(0);
  });

  it("ログアウトボタンと「管理者情報」「使い方を確認」リンクを含む", async () => {
    renderWithAuth(<ManagerLayout>x</ManagerLayout>, {
      loginId: "m",
      classroomName: "戸塚",
    });
    expect((await screen.findAllByText("ログアウト")).length).toBeGreaterThan(0);
    expect((await screen.findAllByText("管理者情報")).length).toBeGreaterThan(0);
    expect((await screen.findAllByText("使い方を確認")).length).toBeGreaterThan(0);
  });
});

describe("TutorLayout", () => {
  it("下部タブの4項目を描画する", () => {
    renderWithAuth(<TutorLayout>x</TutorLayout>);
    expect(screen.getByText("勤務")).toBeInTheDocument();
    expect(screen.getByText("テンプレート")).toBeInTheDocument();
    expect(screen.getByText("給与")).toBeInTheDocument();
    expect(screen.getByText("マイページ")).toBeInTheDocument();
  });

  it("タイトル「勤怠管理システム」をヘッダに表示する", () => {
    renderWithAuth(<TutorLayout>x</TutorLayout>);
    expect(screen.getByText("勤怠管理システム")).toBeInTheDocument();
  });
});
