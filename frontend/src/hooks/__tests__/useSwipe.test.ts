import { act, renderHook } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";

import { useSwipe } from "@/hooks/useSwipe";

/** TouchEvent を作れない jsdom 環境のため、最低限の形で擬似的に作る */
function touchEvent(x: number): React.TouchEvent {
  return {
    touches: [{ clientX: x }],
    changedTouches: [{ clientX: x }],
  } as unknown as React.TouchEvent;
}

function touchEndEvent(x: number): React.TouchEvent {
  return {
    changedTouches: [{ clientX: x }],
  } as unknown as React.TouchEvent;
}

describe("useSwipe", () => {
  it("右→左に 101px 以上で onNext が呼ばれる", () => {
    const onNext = vi.fn();
    const onPrev = vi.fn();
    const { result } = renderHook(() => useSwipe(onPrev, onNext));

    act(() => result.current.onTouchStart(touchEvent(200)));
    act(() => result.current.onTouchEnd(touchEndEvent(99)));

    expect(onNext).toHaveBeenCalledTimes(1);
    expect(onPrev).not.toHaveBeenCalled();
  });

  it("左→右に 101px 以上で onPrev が呼ばれる", () => {
    const onNext = vi.fn();
    const onPrev = vi.fn();
    const { result } = renderHook(() => useSwipe(onPrev, onNext));

    act(() => result.current.onTouchStart(touchEvent(100)));
    act(() => result.current.onTouchEnd(touchEndEvent(201)));

    expect(onPrev).toHaveBeenCalledTimes(1);
    expect(onNext).not.toHaveBeenCalled();
  });

  it("ちょうど 100px は発動しない（閾値は厳密超過）", () => {
    const onNext = vi.fn();
    const onPrev = vi.fn();
    const { result } = renderHook(() => useSwipe(onPrev, onNext));

    act(() => result.current.onTouchStart(touchEvent(200)));
    act(() => result.current.onTouchEnd(touchEndEvent(100)));

    expect(onNext).not.toHaveBeenCalled();
    expect(onPrev).not.toHaveBeenCalled();
  });

  it("閾値以下のスワイプ（+-99px）は何も起きない", () => {
    const onNext = vi.fn();
    const onPrev = vi.fn();
    const { result } = renderHook(() => useSwipe(onPrev, onNext));

    act(() => result.current.onTouchStart(touchEvent(100)));
    act(() => result.current.onTouchEnd(touchEndEvent(199)));
    expect(onNext).not.toHaveBeenCalled();
    expect(onPrev).not.toHaveBeenCalled();

    act(() => result.current.onTouchStart(touchEvent(100)));
    act(() => result.current.onTouchEnd(touchEndEvent(1)));
    expect(onNext).not.toHaveBeenCalled();
    expect(onPrev).not.toHaveBeenCalled();
  });

  it("onTouchStart なしで onTouchEnd を呼んでもクラッシュしない", () => {
    const onNext = vi.fn();
    const onPrev = vi.fn();
    const { result } = renderHook(() => useSwipe(onPrev, onNext));

    expect(() => act(() => result.current.onTouchEnd(touchEndEvent(0)))).not.toThrow();
    expect(onNext).not.toHaveBeenCalled();
    expect(onPrev).not.toHaveBeenCalled();
  });

  it("スワイプ後は startX がリセットされ、次回 onTouchEnd は反応しない", () => {
    const onNext = vi.fn();
    const { result } = renderHook(() => useSwipe(() => {}, onNext));

    act(() => result.current.onTouchStart(touchEvent(200)));
    act(() => result.current.onTouchEnd(touchEndEvent(0)));
    expect(onNext).toHaveBeenCalledTimes(1);

    // 同じ起点ないで再度 onTouchEnd → 反応しない
    act(() => result.current.onTouchEnd(touchEndEvent(0)));
    expect(onNext).toHaveBeenCalledTimes(1);
  });

  it("ハンドラが迷子にならない（リリース後に差し替わったコールバックが使われる）", () => {
    const onNextV1 = vi.fn();
    const onNextV2 = vi.fn();
    const { result, rerender } = renderHook(
      ({ next }: { next: () => void }) => useSwipe(() => {}, next),
      { initialProps: { next: onNextV1 } }
    );

    // 新しいコールバックに差し替わってからスワイプ
    rerender({ next: onNextV2 });

    act(() => result.current.onTouchStart(touchEvent(200)));
    act(() => result.current.onTouchEnd(touchEndEvent(99)));

    expect(onNextV1).not.toHaveBeenCalled();
    expect(onNextV2).toHaveBeenCalledTimes(1);
  });
});
