import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import TemplateDetailModal from "@/components/template/TemplateDetailModal";
import type { TemplateResponse } from "@/types";

function makeTemplate(overrides: Partial<TemplateResponse> = {}): TemplateResponse {
  return {
    id: "tp-1",
    tutorId: "t-1",
    title: "土曜日テンプレ",
    classroomId: "c-tds",
    classroomName: "戸塚",
    classroomNumber: 22,
    transportationFee: 300,
    lessonTemplateDetail: {
      startTime: "10:00:00",
      endTime: "12:00:00",
      breakMinutes: 30,
      periodCodes: ["M", "K"],
    },
    officeTemplateDetail: { startTime: null, endTime: null },
    otherTemplateDetail: {
      startTime: null,
      endTime: null,
      breakMinutes: null,
      description: null,
    },
    ...overrides,
  };
}

describe("TemplateDetailModal", () => {
  it("タイトルと template 名・交通費が描画される", () => {
    render(<TemplateDetailModal template={makeTemplate()} onClose={() => {}} />);
    expect(screen.getByText("テンプレート詳細")).toBeInTheDocument();
    expect(screen.getByText("土曜日テンプレ")).toBeInTheDocument();
    expect(screen.getByText("¥300")).toBeInTheDocument();
  });

  it("コマのラベルが PERIOD_CODES 文字で描画される", () => {
    render(<TemplateDetailModal template={makeTemplate()} onClose={() => {}} />);
    expect(screen.getByText("M（09:10〜10:40）")).toBeInTheDocument();
    expect(screen.getByText("K（10:50〜12:20）")).toBeInTheDocument();
  });

  it("事務 / その他業務は startTime がある場合のみレンダリング", () => {
    const t = makeTemplate({
      officeTemplateDetail: { startTime: "08:00:00", endTime: "09:00:00" },
      otherTemplateDetail: {
        startTime: "13:00:00",
        endTime: "14:00:00",
        breakMinutes: 0,
        description: "掃除",
      },
    });
    render(<TemplateDetailModal template={t} onClose={() => {}} />);
    expect(screen.getByText("事務業務")).toBeInTheDocument();
    expect(screen.getByText("その他業務")).toBeInTheDocument();
    expect(screen.getByText("掃除")).toBeInTheDocument();
  });

  it("事務・その他の startTime が null なら両方レンダリングされない", () => {
    render(<TemplateDetailModal template={makeTemplate()} onClose={() => {}} />);
    expect(screen.queryByText("事務業務")).toBeNull();
    expect(screen.queryByText("その他業務")).toBeNull();
  });

  it("onEdit / onDelete / onClose を透過", async () => {
    const user = userEvent.setup({ delay: null });
    const onEdit = vi.fn();
    const onDelete = vi.fn();
    const onClose = vi.fn();
    render(
      <TemplateDetailModal
        template={makeTemplate()}
        onClose={onClose}
        onEdit={onEdit}
        onDelete={onDelete}
      />
    );

    await user.click(screen.getByRole("button", { name: "編集" }));
    expect(onEdit).toHaveBeenCalled();
    await user.click(screen.getByRole("button", { name: "削除" }));
    expect(onDelete).toHaveBeenCalled();
    await user.click(screen.getByRole("button", { name: "閉じる" }));
    expect(onClose).toHaveBeenCalled();
  });

  it("onEdit/onDelete 未割当ならボタン非表示", () => {
    render(<TemplateDetailModal template={makeTemplate()} onClose={() => {}} />);
    expect(screen.queryByRole("button", { name: "編集" })).toBeNull();
    expect(screen.queryByRole("button", { name: "削除" })).toBeNull();
  });
});
