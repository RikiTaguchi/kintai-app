import { act, renderHook } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import { useConfirm } from "@/hooks/useConfirm";

describe("useConfirm", () => {
  it("初期状態は target=null, isLoading=false", () => {
    const { result } = renderHook(() => useConfirm<string>());
    expect(result.current.target).toBeNull();
    expect(result.current.isLoading).toBe(false);
  });

  it("confirm(item) で target がセットされる", () => {
    const { result } = renderHook(() => useConfirm<{ id: number }>());
    act(() => result.current.confirm({ id: 42 }));
    expect(result.current.target).toEqual({ id: 42 });
  });

  it("confirm → cancel で再び null になる", () => {
    const { result } = renderHook(() => useConfirm<number>());
    act(() => result.current.confirm(1));
    expect(result.current.target).toBe(1);
    act(() => result.current.cancel());
    expect(result.current.target).toBeNull();
  });

  it("confirm を重ねると最後の値で上書きされる", () => {
    const { result } = renderHook(() => useConfirm<number>());
    act(() => result.current.confirm(1));
    act(() => result.current.confirm(99));
    expect(result.current.target).toBe(99);
  });

  it("setIsLoading フラグを外部から切り替えられる（確定処理中の連打抑止用）", () => {
    const { result } = renderHook(() => useConfirm<number>());
    act(() => result.current.setIsLoading(true));
    expect(result.current.isLoading).toBe(true);
    act(() => result.current.setIsLoading(false));
    expect(result.current.isLoading).toBe(false);
  });
});
