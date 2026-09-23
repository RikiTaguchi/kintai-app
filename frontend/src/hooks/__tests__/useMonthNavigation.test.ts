import { act, renderHook } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";

import { useMonthNavigation } from "@/hooks/useMonthNavigation";

describe("useMonthNavigation", () => {
  it("初期値は引数の year/month", () => {
    const { result } = renderHook(() => useMonthNavigation(2025, 9));
    expect(result.current.year).toBe(2025);
    expect(result.current.month).toBe(9);
  });

  it("nextMonth: 月内では +1", () => {
    const { result } = renderHook(() => useMonthNavigation(2025, 9));
    act(() => result.current.nextMonth());
    expect(result.current.year).toBe(2025);
    expect(result.current.month).toBe(10);
  });

  it("prevMonth: 月内では -1", () => {
    const { result } = renderHook(() => useMonthNavigation(2025, 9));
    act(() => result.current.prevMonth());
    expect(result.current.year).toBe(2025);
    expect(result.current.month).toBe(8);
  });

  it("12 月で nextMonth → 翌年 1 月（年ロールオーバー）", () => {
    const { result } = renderHook(() => useMonthNavigation(2025, 12));
    act(() => result.current.nextMonth());
    expect(result.current.year).toBe(2026);
    expect(result.current.month).toBe(1);
  });

  it("1 月で prevMonth → 前年 12 月（年ロールバック）", () => {
    const { result } = renderHook(() => useMonthNavigation(2025, 1));
    act(() => result.current.prevMonth());
    expect(result.current.year).toBe(2024);
    expect(result.current.month).toBe(12);
  });

  it("onChange は遷移先の (year, month) で呼ばれる", () => {
    const onChange = vi.fn();
    const { result } = renderHook(() => useMonthNavigation(2025, 12, onChange));
    act(() => result.current.nextMonth());
    expect(onChange).toHaveBeenCalledWith(2026, 1);
    act(() => result.current.prevMonth());
    expect(onChange).toHaveBeenCalledWith(2025, 12);
  });

  it("onChange 未指定でもエラーにならない", () => {
    const { result } = renderHook(() => useMonthNavigation(2025, 9));
    expect(() => act(() => result.current.nextMonth())).not.toThrow();
    expect(() => act(() => result.current.prevMonth())).not.toThrow();
  });

  it("複数回の prev/next は累積して進む", () => {
    const { result } = renderHook(() => useMonthNavigation(2025, 1));
    act(() => result.current.prevMonth());
    act(() => result.current.prevMonth());
    act(() => result.current.prevMonth());
    expect(result.current.year).toBe(2024);
    expect(result.current.month).toBe(10);
    act(() => result.current.nextMonth());
    expect(result.current.month).toBe(11);
  });
});
