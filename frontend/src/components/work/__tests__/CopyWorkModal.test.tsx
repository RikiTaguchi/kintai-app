import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

import CopyWorkModal from "@/components/work/CopyWorkModal";
import { getWorks, registerWork } from "@/lib/api";
import type { WorkResponse } from "@/types";

// useAuth/useToast は AuthContext/ToastContext を直接使わず、簡易フックで代替
const mockAddToast = vi.fn();
vi.mock("@/context/AuthContext", () => ({
  useAuth: () => ({
    user: { id: "tutor-1", loginId: "t1", role: "ROLE_TUTOR", classroomName: "戸塚" },
  }),
}));
vi.mock("@/context/ToastContext", () => ({
  useToast: () => ({ addToast: mockAddToast }),
}));
vi.mock("@/lib/api", () => ({
  getWorks: vi.fn(),
  registerWork: vi.fn(),
}));

// jsdom では scrollIntoView 等は不要だが、CopyWorkModal は useEffect で getWorks を呼ぶ
// ため、全テスト間で dish しないよう beforeEach で clearAllMocks

function makeWork(overrides: Partial<WorkResponse> = {}): WorkResponse {
  return {
    id: "w-1",
    tutorId: "tutor-1",
    classroomId: "c-1",
    classroomName: "戸塚",
    classroomNumber: 22,
    workingDate: "2025-09-22",
    transportationFee: 500,
    dailyAllowance: 0,
    lessonWorkDetail: {
      startTime: "10:00:00",
      endTime: "12:00:00",
      breakMinutes: 30,
      periodCodes: ["M"],
    },
    officeWorkDetail: { startTime: null, endTime: null },
    otherWorkDetail: {
      startTime: null,
      endTime: null,
      breakMinutes: null,
      description: null,
    },
    ...overrides,
  };
}

beforeEach(() => {
  vi.clearAllMocks();
});

describe("CopyWorkModal", () => {
  it("表示直後は source の年月が表示され、getWorks で同月の勤務を取得する", async () => {
    (getWorks as unknown as ReturnType<typeof vi.fn>).mockResolvedValue([]);
    render(
      <CopyWorkModal work={makeWork()} onClose={() => {}} onCopied={() => {}} />
    );
    expect(screen.getByText("2025年9月")).toBeInTheDocument();
    await waitFor(() =>
      expect(getWorks).toHaveBeenCalledWith("tutor-1", 2025, 9)
    );
  });

  it("登録済み日付は disabled でクリック不可", async () => {
    // 2025-09-22 を登録済みとする
    (getWorks as unknown as ReturnType<typeof vi.fn>).mockResolvedValue([
      makeWork({ workingDate: "2025-09-22" }),
    ]);
    render(
      <CopyWorkModal work={makeWork()} onClose={() => {}} onCopied={() => {}} />
    );

    const day22 = await screen.findByRole("button", { name: "22" });
    await waitFor(() => expect(getWorks).toHaveBeenCalled());
    expect(day22).toBeDisabled();
  });

  it("日付を選択すると「N日選択中」が表示される", async () => {
    const user = userEvent.setup({ delay: null });
    (getWorks as unknown as ReturnType<typeof vi.fn>).mockResolvedValue([]);
    render(
      <CopyWorkModal work={makeWork()} onClose={() => {}} onCopied={() => {}} />
    );
    await screen.findByText("2025年9月");
    await waitFor(() => expect(getWorks).toHaveBeenCalled());

    await user.click(screen.getByRole("button", { name: "10" }));
    expect(screen.getByText("1日選択中")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "11" }));
    expect(screen.getByText("2日選択中")).toBeInTheDocument();
  });

  it("コピー成功：registerWork が選択分だけ呼ばれ success トーストが出る", async () => {
    const user = userEvent.setup({ delay: null });
    const onCopied = vi.fn();
    (getWorks as unknown as ReturnType<typeof vi.fn>).mockResolvedValue([]);
    (registerWork as unknown as ReturnType<typeof vi.fn>).mockResolvedValue({});
    render(
      <CopyWorkModal
        work={makeWork()}
        onClose={() => {}}
        onCopied={onCopied}
      />
    );
    await screen.findByText("2025年9月");
    await waitFor(() => expect(getWorks).toHaveBeenCalled());

    await user.click(screen.getByRole("button", { name: "10" }));
    await user.click(screen.getByRole("button", { name: "11" }));
    await user.click(screen.getByRole("button", { name: "コピーする" }));

    await waitFor(() => expect(registerWork).toHaveBeenCalledTimes(2));
    expect(mockAddToast).toHaveBeenCalledWith(
      "2件の勤務情報をコピーしました",
      "success"
    );
    await waitFor(() => expect(onCopied).toHaveBeenCalled());
  });

  it("全件失敗：error トーストが出る", async () => {
    const user = userEvent.setup({ delay: null });
    (getWorks as unknown as ReturnType<typeof vi.fn>).mockResolvedValue([]);
    (registerWork as unknown as ReturnType<typeof vi.fn>).mockRejectedValue(
      new Error("conflict")
    );
    render(
      <CopyWorkModal work={makeWork()} onClose={() => {}} onCopied={() => {}} />
    );
    await screen.findByText("2025年9月");
    await waitFor(() => expect(getWorks).toHaveBeenCalled());

    await user.click(screen.getByRole("button", { name: "10" }));
    await user.click(screen.getByRole("button", { name: "コピーする" }));

    await waitFor(() =>
      expect(mockAddToast).toHaveBeenCalledWith(
        expect.stringContaining("コピーに失敗しました"),
        "error"
      )
    );
  });

  it("部分失敗：成功件数と失敗件数を含むエラートースト", async () => {
    const user = userEvent.setup({ delay: null });
    (getWorks as unknown as ReturnType<typeof vi.fn>).mockResolvedValue([]);
    (registerWork as unknown as ReturnType<typeof vi.fn>)
      .mockResolvedValueOnce({})
      .mockRejectedValueOnce(new Error("fail"));
    render(
      <CopyWorkModal work={makeWork()} onClose={() => {}} onCopied={() => {}} />
    );
    await screen.findByText("2025年9月");
    await waitFor(() => expect(getWorks).toHaveBeenCalled());

    await user.click(screen.getByRole("button", { name: "10" }));
    await user.click(screen.getByRole("button", { name: "11" }));
    await user.click(screen.getByRole("button", { name: "コピーする" }));

    await waitFor(() =>
      expect(mockAddToast).toHaveBeenCalledWith(
        expect.stringMatching(/1件コピー完了、1件失敗しました/),
        "error"
      )
    );
  });

  it("月ナビゲーターで翌月に移動すると getWorks が再取得される", async () => {
    const user = userEvent.setup({ delay: null });
    (getWorks as unknown as ReturnType<typeof vi.fn>).mockResolvedValue([]);
    render(
      <CopyWorkModal work={makeWork()} onClose={() => {}} onCopied={() => {}} />
    );
    await screen.findByText("2025年9月");
    await waitFor(() => expect(getWorks).toHaveBeenCalledWith("tutor-1", 2025, 9));

    // 次の月へ（aria未設定だがsvgボタンなのでテキストで引く）
    const nextBtn = screen.getAllByRole("button").find((b) =>
      b.querySelector("svg path[d*='M9 5l7 7-7 7']")
    )!;
    await user.click(nextBtn);
    await screen.findByText("2025年10月");
    await waitFor(() =>
      expect(getWorks).toHaveBeenCalledWith("tutor-1", 2025, 10)
    );
  });

  it("tutorId prop が渡された場合はそれを使用する（管理者用）", async () => {
    (getWorks as unknown as ReturnType<typeof vi.fn>).mockResolvedValue([]);
    render(
      <CopyWorkModal
        work={makeWork()}
        tutorId="tutor-other"
        onClose={() => {}}
        onCopied={() => {}}
      />
    );
    await waitFor(() =>
      expect(getWorks).toHaveBeenCalledWith("tutor-other", 2025, 9)
    );
  });

  it("キャンセルボタンで onClose が呼ばれる", async () => {
    const user = userEvent.setup({ delay: null });
    const onClose = vi.fn();
    (getWorks as unknown as ReturnType<typeof vi.fn>).mockResolvedValue([]);
    render(
      <CopyWorkModal work={makeWork()} onClose={onClose} onCopied={() => {}} />
    );
    await screen.findByText("2025年9月");
    await user.click(screen.getByRole("button", { name: "キャンセル" }));
    expect(onClose).toHaveBeenCalled();
  });
});
