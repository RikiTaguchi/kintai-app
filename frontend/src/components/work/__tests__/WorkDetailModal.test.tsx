import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import WorkDetailModal from "@/components/work/WorkDetailModal";
import type { WorkResponse } from "@/types";

function makeWork(overrides: Partial<WorkResponse> = {}): WorkResponse {
  return {
    id: "w-1",
    tutorId: "t-1",
    classroomId: "c-tds",
    classroomName: "戸塚",
    classroomNumber: 22,
    workingDate: "2025-09-22",
    transportationFee: 500,
    dailyAllowance: 0,
    lessonWorkDetail: {
      startTime: "10:00:00",
      endTime: "12:00:00",
      breakMinutes: 30,
      periodCodes: ["M", "K"],
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

describe("WorkDetailModal", () => {
  it("タイトルと勤務日・教室・交通費が描画される", () => {
    render(<WorkDetailModal work={makeWork()} onClose={() => {}} />);
    expect(screen.getByText("勤務詳細")).toBeInTheDocument();
    expect(screen.getByText(/2025\/09\/22/)).toBeInTheDocument();
    expect(screen.getByText("戸塚")).toBeInTheDocument();
    expect(screen.getByText("¥500")).toBeInTheDocument();
  });

  it("showClassroom=false で教室名は表示されず交通費は残る", () => {
    render(<WorkDetailModal work={makeWork()} onClose={() => {}} showClassroom={false} />);
    expect(screen.queryByText("戸塚")).toBeNull();
    expect(screen.getByText("¥500")).toBeInTheDocument();
  });

  it("コマが二次元目は「M（09:10〜10:40）」のような PERIOD_CODES ラベルで表示", () => {
    render(<WorkDetailModal work={makeWork()} onClose={() => {}} />);
    expect(screen.getByText("M（09:10〜10:40）")).toBeInTheDocument();
    expect(screen.getByText("K（10:50〜12:20）")).toBeInTheDocument();
  });

  it("授業業務の開始・終了・休憩がフォーマットされる", () => {
    render(<WorkDetailModal work={makeWork()} onClose={() => {}} />);
    expect(screen.getByText("10:00")).toBeInTheDocument();
    expect(screen.getByText("12:00")).toBeInTheDocument();
    expect(screen.getByText("30分")).toBeInTheDocument();
  });

  it("officeWorkDetail.startTime があるとき事務業務セクションが表示される", () => {
    const work = makeWork({
      officeWorkDetail: { startTime: "08:00:00", endTime: "09:00:00" },
    });
    render(<WorkDetailModal work={work} onClose={() => {}} />);
    expect(screen.getByText("事務業務")).toBeInTheDocument();
    expect(screen.getByText("08:00")).toBeInTheDocument();
  });

  it("officeWorkDetail.startTime が null なら事務業務セクションは出ない", () => {
    render(<WorkDetailModal work={makeWork()} onClose={() => {}} />);
    expect(screen.queryByText("事務業務")).toBeNull();
  });

  it("otherWorkDetail.description があるとき業務内容も描画される", () => {
    const work = makeWork({
      otherWorkDetail: {
        startTime: "13:00:00",
        endTime: "14:00:00",
        breakMinutes: 0,
        description: "掃除当番",
      },
    });
    render(<WorkDetailModal work={work} onClose={() => {}} />);
    expect(screen.getByText("その他業務")).toBeInTheDocument();
    expect(screen.getByText("業務内容")).toBeInTheDocument();
    expect(screen.getByText("掃除当番")).toBeInTheDocument();
  });

  it("teal colorScheme でタイトルサイズとボタン構成が変わる", () => {
    const { container } = render(
      <WorkDetailModal work={makeWork()} onClose={() => {}} colorScheme="teal" />
    );
    expect(container.textContent).toContain("勤務詳細");
    // teal スタイル確認
    const heading = container.querySelector(".text-teal-800");
    expect(heading).not.toBeNull();
  });

  it("onEdit/onCopy/onDelete を透過する（indigo で3ボタン表示）", async () => {
    const user = userEvent.setup({ delay: null });
    const onEdit = vi.fn();
    const onCopy = vi.fn();
    const onDelete = vi.fn();
    render(
      <WorkDetailModal
        work={makeWork()}
        onClose={() => {}}
        onEdit={onEdit}
        onCopy={onCopy}
        onDelete={onDelete}
      />
    );

    await user.click(screen.getByRole("button", { name: "編集" }));
    expect(onEdit).toHaveBeenCalled();
    await user.click(screen.getByRole("button", { name: "他の日付にコピー" }));
    expect(onCopy).toHaveBeenCalled();
    await user.click(screen.getByRole("button", { name: "削除" }));
    expect(onDelete).toHaveBeenCalled();
  });

  it("コピー/編集ボタンが渡されない場合は表示しない", () => {
    render(<WorkDetailModal work={makeWork()} onClose={() => {}} />);
    expect(screen.queryByRole("button", { name: "編集" })).toBeNull();
    expect(screen.queryByRole("button", { name: "他の日付にコピー" })).toBeNull();
    expect(screen.queryByRole("button", { name: "削除" })).toBeNull();
  });

  it("✕ ボタンで onClose される", async () => {
    const user = userEvent.setup({ delay: null });
    const onClose = vi.fn();
    render(<WorkDetailModal work={makeWork()} onClose={onClose} />);
    const closeBtn = screen.getByRole("button", { name: "閉じる" });
    await user.click(closeBtn);
    expect(onClose).toHaveBeenCalled();
  });

  it("backdrop をクリックで onClose される", async () => {
    const user = userEvent.setup({ delay: null });
    const onClose = vi.fn();
    const { container } = render(<WorkDetailModal work={makeWork()} onClose={onClose} />);
    const backdrop = container.querySelector(".absolute.inset-0") as HTMLElement;
    await user.click(backdrop);
    expect(onClose).toHaveBeenCalled();
  });
});
