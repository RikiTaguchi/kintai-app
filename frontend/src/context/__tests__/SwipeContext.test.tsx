import { act, renderHook } from "@testing-library/react";
import React from "react";
import { describe, expect, it, vi } from "vitest";

import {
  SwipeProvider,
  useRegisterSwipe,
  useSwipeMainHandlers,
} from "@/context/SwipeContext";

const wrapper = ({ children }: { children: React.ReactNode }) =>
  React.createElement(SwipeProvider, null, children);

describe("SwipeContext", () => {
  it("useSwipeMainHandlers は onTouchStart/onTouchEnd を返す", () => {
    const { result } = renderHook(() => useSwipeMainHandlers(), { wrapper });
    expect(typeof result.current.onTouchStart).toBe("function");
    expect(typeof result.current.onTouchEnd).toBe("function");
  });

  it("登録前の mainHandlers は何も起こさない（no-op）", () => {
    const { result } = renderHook(() => useSwipeMainHandlers(), { wrapper });
    // 登録していない状態ではクラッシュしない
    act(() => {
      result.current.onTouchStart({ touches: [{ clientX: 100 }] } as never);
      result.current.onTouchEnd({ changedTouches: [{ clientX: 10 }] } as never);
    });
  });

  it("useRegisterSwipe で onPrev / onNext が mainHandlers 経由で呼ばれる", () => {
    const onPrev = vi.fn();
    const onNext = vi.fn();

    // Hook でハンドラ登録（useSwipe は renderHook 内で呼ばれる）
    const { result } = renderHook(
      (props: { p: () => void; n: () => void }) => {
        useRegisterSwipe(props.p, props.n);
        return useSwipeMainHandlers();
      },
      { wrapper, initialProps: { p: onPrev, n: onNext } }
    );

    act(() => {
      result.current.onTouchStart({ touches: [{ clientX: 200 }] } as never);
      result.current.onTouchEnd({ changedTouches: [{ clientX: 100 }] } as never);
    });
    expect(onNext).toHaveBeenCalledTimes(1);

    act(() => {
      result.current.onTouchStart({ touches: [{ clientX: 100 }] } as never);
      result.current.onTouchEnd({ changedTouches: [{ clientX: 200 }] } as never);
    });
    expect(onPrev).toHaveBeenCalledTimes(1);
  });

  it("unregister 後は mainHandlers が no-op になる（hook unmount）", () => {
    const onNext = vi.fn();

    const { result, unmount } = renderHook(
      (props: { n: () => void }) => {
        useRegisterSwipe(() => {}, props.n);
        return useSwipeMainHandlers();
      },
      { wrapper, initialProps: { n: onNext } }
    );

    act(() => {
      result.current.onTouchStart({ touches: [{ clientX: 200 }] } as never);
      result.current.onTouchEnd({ changedTouches: [{ clientX: 100 }] } as never);
    });
    expect(onNext).toHaveBeenCalledTimes(1);

    onNext.mockClear();
    unmount();

    act(() => {
      result.current.onTouchStart({ touches: [{ clientX: 200 }] } as never);
      result.current.onTouchEnd({ changedTouches: [{ clientX: 100 }] } as never);
    });
    expect(onNext).not.toHaveBeenCalled();
  });
});
