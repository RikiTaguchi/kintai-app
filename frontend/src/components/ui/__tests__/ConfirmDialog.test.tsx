import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import ConfirmDialog from "@/components/ui/ConfirmDialog";

const baseProps = {
  isOpen: true,
  title: "削除の確認",
  message: "本当に削除しますか？",
  onConfirm: () => {},
  onCancel: () => {},
};

describe("ConfirmDialog", () => {
  it("isOpen=false で何もレンダリングしない", () => {
    render(<ConfirmDialog {...baseProps} isOpen={false} />);
    expect(screen.queryByRole("dialog")).toBeNull();
    expect(screen.queryByText("削除の確認")).toBeNull();
  });

  it("title と message を表示する", () => {
    render(<ConfirmDialog {...baseProps} />);
    expect(screen.getByText("削除の確認")).toBeInTheDocument();
    expect(screen.getByText("本当に削除しますか？")).toBeInTheDocument();
  });

  it("backdrop をクリックすると onCancel が呼ばれる", async () => {
    const user = userEvent.setup();
    const onCancel = vi.fn();
    render(<ConfirmDialog {...baseProps} onCancel={onCancel} />);
    const backdrop = screen.getByRole("dialog").querySelector(".absolute.inset-0") as HTMLElement;
    await user.click(backdrop);
    expect(onCancel).toHaveBeenCalledTimes(1);
  });

  it("Escape キーで onCancel が呼ばれる", async () => {
    const user = userEvent.setup();
    const onCancel = vi.fn();
    render(<ConfirmDialog {...baseProps} onCancel={onCancel} />);
    await user.keyboard("{Escape}");
    expect(onCancel).toHaveBeenCalledTimes(1);
  });

  it("isOpen 中にキャンセルボタンが自動フォーカスされる", () => {
    render(<ConfirmDialog {...baseProps} />);
    expect(screen.getByRole("button", { name: "キャンセル" })).toHaveFocus();
  });

  it("isDanger=true で確認ボタンは赤", () => {
    render(<ConfirmDialog {...baseProps} />);
    expect(screen.getByRole("button", { name: "削除" }).className).toMatch(/bg-red-600/);
  });

  it("isDanger=false で確認ボタンは indigo", () => {
    render(<ConfirmDialog {...baseProps} isDanger={false} />);
    expect(screen.getByRole("button", { name: "削除" }).className).toMatch(/bg-indigo-600/);
  });

  it("confirmLabel を変えられる", () => {
    render(<ConfirmDialog {...baseProps} confirmLabel="更新する" />);
    expect(screen.getByRole("button", { name: "更新する" })).toBeInTheDocument();
  });

  it("isLoading=true で両ボタンが disabled になり、確認ボタンにスピナーが出る", () => {
    render(<ConfirmDialog {...baseProps} isLoading />);
    expect(screen.getByRole("button", { name: "キャンセル" })).toBeDisabled();
    const confirm = screen.getByRole("button", { name: "削除" });
    expect(confirm).toBeDisabled();
    expect(confirm.querySelector(".animate-spin")).not.toBeNull();
  });

  it("確認ボタンをクリックすると onConfirm が呼ばれる", async () => {
    const user = userEvent.setup();
    const onConfirm = vi.fn();
    render(<ConfirmDialog {...baseProps} onConfirm={onConfirm} />);
    await user.click(screen.getByRole("button", { name: "削除" }));
    expect(onConfirm).toHaveBeenCalledTimes(1);
  });

  it("role=dialog と aria 属性がセットされる", () => {
    render(<ConfirmDialog {...baseProps} />);
    const dialog = screen.getByRole("dialog");
    expect(dialog).toHaveAttribute("aria-modal", "true");
    expect(dialog).toHaveAttribute("aria-labelledby", "confirm-dialog-title");
    expect(dialog).toHaveAttribute("aria-describedby", "confirm-dialog-message");
  });
});
