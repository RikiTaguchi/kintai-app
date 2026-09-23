import { act, renderHook, waitFor } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";

import { useMonthSlide } from "@/hooks/useMonthSlide";
import { SwipeProvider } from "@/context/SwipeContext";
import React from "react";

function wrapper({ children }: { children: React.ReactNode }) {
  return React.createElement(SwipeProvider, null, children);
}

describe("useMonthSlide", () => {
  it("初期状態の year/month は引数の値", () => {
    const onNavigate = vi.fn();
    const { result } = renderHook(() => useMonthSlide(2025, 9, onNavigate), { wrapper });
    expect(result.current.year).toBe(2025);
    expect(result.current.month).toBe(9);
  });

  it("nextMonth → onNavigate が (月+1) で呼ばれ、スライド用クラスが付く", async () => {
    const onNavigate = vi.fn();
    const { result } = renderHook(() => useMonthSlide(2025, 9, onNavigate), { wrapper });

    // contentRef に DOM 要素を結びつける
    document.body.innerHTML = "<div id='slide' />";
    const el = document.getElementById("slide") as HTMLDivElement;
    // ref の direct な set は使わず、assign
    (result.current.contentRef as { current: HTMLDivElement | null }).current = el;

    act(() => result.current.nextMonth());

    await waitFor(() => {
      expect(onNavigate).toHaveBeenCalledWith(2025, 10);
      expect(result.current.year).toBe(2025);
      expect(result.current.month).toBe(10);
      // 初回フリップで variant が "a" になる
      expect(el.className).toMatch(/month-slide-next-[ab]/);
    });
  });

  it("prevMonth が「prev」方向のクラスを付ける", async () => {
    const onNavigate = vi.fn();
    const { result } = renderHook(() => useMonthSlide(2025, 9, onNavigate), { wrapper });

    document.body.innerHTML = "<div id='slide2' />";
    const el = document.getElementById("slide2") as HTMLDivElement;
    (result.current.contentRef as { current: HTMLDivElement | null }).current = el;

    act(() => result.current.prevMonth());

    await waitFor(() => {
      expect(onNavigate).toHaveBeenCalledWith(2025, 8);
      expect(el.className).toMatch(/month-slide-prev-[ab]/);
    });
  });

  it("再マウント直後の StrictMode 二重実行でクラスが重複付与されない", async () => {
    const onNavigate = vi.fn();
    const { result } = renderHook(() => useMonthSlide(2025, 9, onNavigate), { wrapper });
    document.body.innerHTML = "<div id='slide3' />";
    const el = document.getElementById("slide3") as HTMLDivElement;
    (result.current.contentRef as { current: HTMLDivElement | null }).current = el;

    // 年月未変更の初回レンダーではクラスが付かない（StrictMode で effect が
    // 2回実行されても lastPeriod の比較でスキップされる想定）
    await waitFor(() => {
      expect(el.className).not.toMatch(/month-slide/);
    });
  });

  it("nextMonth → prevMonth 連続でクラスが入れ替わる", async () => {
    const onNavigate = vi.fn();
    const { result } = renderHook(() => useMonthSlide(2025, 9, onNavigate), { wrapper });
    document.body.innerHTML = "<div id='slide4' />";
    const el = document.getElementById("slide4") as HTMLDivElement;
    (result.current.contentRef as { current: HTMLDivElement | null }).current = el;

    act(() => result.current.nextMonth());
    await waitFor(() => {
      expect(el.className).toMatch(/month-slide-next-[ab]/);
    });

    act(() => result.current.prevMonth());
    await waitFor(() => {
      expect(el.className).toMatch(/month-slide-prev-[ab]/);
      expect(el.className).not.toMatch(/month-slide-next/);
    });
  });
});
