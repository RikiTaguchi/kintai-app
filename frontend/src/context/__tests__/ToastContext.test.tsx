import { act, render, renderHook, screen, waitFor } from "@testing-library/react";
import React, { ReactNode } from "react";
import { describe, expect, it, vi } from "vitest";

import { ToastProvider, useToast } from "@/context/ToastContext";

function Trigger({ children }: { children: ReactNode }) {
  const { addToast } = useToast();
  const wrapper = ({ children }: { children: ReactNode }) =>
    React.createElement(ToastProvider, null, children);
  return wrapper({ children });
}

function ToastButton({
  message,
  type,
  label = "add",
}: { message: string; type?: "success" | "error"; label?: string }) {
  const { addToast } = useToast();
  return React.createElement(
    "button",
    { onClick: () => addToast(message, type) },
    label
  );
}

describe("ToastContext", () => {
  it("Provider なしで useToast を使うと例外", () => {
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
    expect(() => renderHook(() => useToast())).toThrow("useToast must be used within ToastProvider");
    consoleSpy.mockRestore();
  });

  it("addToast で success トーストが表示される", async () => {
    render(
      React.createElement(
        ToastProvider,
        null,
        React.createElement(ToastButton, { message: "保存しました" })
      )
    );

    act(() => screen.getByRole("button").click());

    await waitFor(() => {
      expect(screen.getByText("保存しました")).toBeInTheDocument();
    });
    // success → green 系のスタイル
    const toast = screen.getByText("保存しました").closest("div");
    expect(toast?.className).toMatch(/bg-green-50/);
  });

  it("type='error' で error トーストが表示される", async () => {
    render(
      React.createElement(
        ToastProvider,
        null,
        React.createElement(ToastButton, { message: "エラーです", type: "error" })
      )
    );

    act(() => screen.getByRole("button").click());

    await waitFor(() => {
      expect(screen.getByText("エラーです")).toBeInTheDocument();
    });
    const toast = screen.getByText("エラーです").closest("div");
    expect(toast?.className).toMatch(/bg-red-50/);
  });

  it("複数のトーストを積むことができる", async () => {
    render(
      React.createElement(
        ToastProvider,
        null,
        React.createElement(
          React.Fragment,
          null,
          React.createElement(ToastButton, { message: "Toast A", label: "a" }),
          React.createElement(ToastButton, { message: "Toast B", label: "b" })
        )
      )
    );

    act(() => screen.getByRole("button", { name: "a" }).click());
    act(() => screen.getByRole("button", { name: "b" }).click());

    await waitFor(() => {
      expect(screen.getByText("Toast A")).toBeInTheDocument();
      expect(screen.getByText("Toast B")).toBeInTheDocument();
    });
  });

  it("4000ms 後にトーストが自動で消える", async () => {
    vi.useFakeTimers();
    render(
      React.createElement(
        ToastProvider,
        null,
        React.createElement(ToastButton, { message: "短時間で消える" })
      )
    );

    act(() => screen.getByRole("button").click());
    expect(screen.getByText("短時間で消える")).toBeInTheDocument();

    // 4000ms 未満の間は残っている
    act(() => void vi.advanceTimersByTime(3999));
    expect(screen.getByText("短時間で消える")).toBeInTheDocument();

    // 4000ms を超えると削除される
    act(() => void vi.advanceTimersByTime(1));
    expect(screen.queryByText("短時間で消える")).not.toBeInTheDocument();
    vi.useRealTimers();
  });

  it("✕ ボタンで個別に閉じることができる", async () => {
    render(
      React.createElement(
        ToastProvider,
        null,
        React.createElement(ToastButton, { message: "手動で閉じる" })
      )
    );

    act(() => screen.getByRole("button").click());
    const toastMessage = await screen.findByText("手動で閉じる");
    expect(toastMessage).toBeInTheDocument();

    // クローズ ボタン（SVG のみのボタン）はクラス名で特定する必要があるが、
    // 最後の role="button" がクローズボタンになっているはず
    const buttons = screen.getAllByRole("button");
    const closeButton = buttons[buttons.length - 1];
    act(() => closeButton.click());

    await waitFor(() => {
      expect(screen.queryByText("手動で閉じる")).not.toBeInTheDocument();
    });
  });
});
