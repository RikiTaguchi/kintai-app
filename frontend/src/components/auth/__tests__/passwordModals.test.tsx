import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import PasswordChangeModal from "@/components/auth/PasswordChangeModal";
import PasswordResetModal from "@/components/auth/PasswordResetModal";

describe("PasswordChangeModal", () => {
  it("タイトル・3つのパスワードフィールドを描画する", () => {
    render(<PasswordChangeModal onClose={() => {}} onSubmit={async () => {}} />);
    expect(screen.getByText("パスワードを変更")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("現在のパスワードを入力")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("8文字以上")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("もう一度入力")).toBeInTheDocument();
  });

  it("新パスワードが確認フィールドと一致しない場合、onSubmit せずエラー表示", async () => {
    const user = userEvent.setup({ delay: null });
    const onSubmit = vi.fn().mockResolvedValue(void 0);
    render(<PasswordChangeModal onClose={() => {}} onSubmit={onSubmit} />);

    await user.type(screen.getByPlaceholderText("現在のパスワードを入力"), "current-pw");
    await user.type(screen.getByPlaceholderText("8文字以上"), "new-pass-01");
    await user.type(screen.getByPlaceholderText("もう一度入力"), "new-pass-02");
    await user.click(screen.getByRole("button", { name: "変更する" }));

    await waitFor(() => expect(screen.getByText("新しいパスワードが一致しません")).toBeInTheDocument());
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it("成立した場合、onSubmit(current, new) が呼ばれ onClose される", async () => {
    const user = userEvent.setup({ delay: null });
    const onSubmit = vi.fn().mockResolvedValue(void 0);
    const onClose = vi.fn();
    render(<PasswordChangeModal onClose={onClose} onSubmit={onSubmit} />);

    await user.type(screen.getByPlaceholderText("現在のパスワードを入力"), "current-pw");
    await user.type(screen.getByPlaceholderText("8文字以上"), "new-pass-01");
    await user.type(screen.getByPlaceholderText("もう一度入力"), "new-pass-01");
    await user.click(screen.getByRole("button", { name: "変更する" }));

    await waitFor(() => {
      expect(onSubmit).toHaveBeenCalledWith("current-pw", "new-pass-01");
      expect(onClose).toHaveBeenCalled();
    });
  });

  it("onSubmit がエラーを投げるとメッセージを表示（モーダルは閉じない）", async () => {
    const user = userEvent.setup({ delay: null });
    const onSubmit = vi.fn().mockRejectedValue(new Error("現在のパスワードが正しくありません"));
    const onClose = vi.fn();
    render(<PasswordChangeModal onClose={onClose} onSubmit={onSubmit} />);

    await user.type(screen.getByPlaceholderText("現在のパスワードを入力"), "wrong");
    await user.type(screen.getByPlaceholderText("8文字以上"), "new-pass-01");
    await user.type(screen.getByPlaceholderText("もう一度入力"), "new-pass-01");
    await user.click(screen.getByRole("button", { name: "変更する" }));

    await waitFor(() =>
      expect(screen.getByText("現在のパスワードが正しくありません")).toBeInTheDocument()
    );
    expect(onClose).not.toHaveBeenCalled();
  });

  it("キャンセルボタンで onClose が呼ばれる", async () => {
    const user = userEvent.setup({ delay: null });
    const onClose = vi.fn();
    render(<PasswordChangeModal onClose={onClose} onSubmit={async () => {}} />);
    await user.click(screen.getByRole("button", { name: "キャンセル" }));
    expect(onClose).toHaveBeenCalled();
  });
});

describe("PasswordResetModal", () => {
  it("講師名を表示する", () => {
    render(
      <PasswordResetModal tutorName="佐藤 花子" onClose={() => {}} onSubmit={async () => {}} />
    );
    expect(screen.getByText("佐藤 花子")).toBeInTheDocument();
    expect(screen.getByText(/新しいパスワードを設定します/)).toBeInTheDocument();
  });

  it("新パスワード不一致で onSubmit せずエラー", async () => {
    const user = userEvent.setup({ delay: null });
    const onSubmit = vi.fn().mockResolvedValue(void 0);
    render(
      <PasswordResetModal tutorName="佐藤" onClose={() => {}} onSubmit={onSubmit} />
    );

    await user.type(screen.getByPlaceholderText("8文字以上"), "new-pass-01");
    await user.type(screen.getByPlaceholderText("もう一度入力"), "new-pass-99");
    await user.click(screen.getByRole("button", { name: "リセットする" }));

    await waitFor(() => expect(screen.getByText("パスワードが一致しません")).toBeInTheDocument());
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it("成立時、onSubmit(new) が呼ばれ onClose される", async () => {
    const user = userEvent.setup({ delay: null });
    const onSubmit = vi.fn().mockResolvedValue(void 0);
    const onClose = vi.fn();
    render(
      <PasswordResetModal tutorName="佐藤" onClose={onClose} onSubmit={onSubmit} />
    );

    await user.type(screen.getByPlaceholderText("8文字以上"), "brand-new-pw");
    await user.type(screen.getByPlaceholderText("もう一度入力"), "brand-new-pw");
    await user.click(screen.getByRole("button", { name: "リセットする" }));

    await waitFor(() => {
      expect(onSubmit).toHaveBeenCalledWith("brand-new-pw");
      expect(onClose).toHaveBeenCalled();
    });
  });

  it("onSubmit の失敗をエラーメッセージで表示", async () => {
    const user = userEvent.setup({ delay: null });
    const onSubmit = vi.fn().mockRejectedValue(new Error("エラーです"));
    render(
      <PasswordResetModal tutorName="佐藤" onClose={() => {}} onSubmit={onSubmit} />
    );

    await user.type(screen.getByPlaceholderText("8文字以上"), "brand-new-pw");
    await user.type(screen.getByPlaceholderText("もう一度入力"), "brand-new-pw");
    await user.click(screen.getByRole("button", { name: "リセットする" }));

    await waitFor(() => expect(screen.getByText("エラーです")).toBeInTheDocument());
  });
});
