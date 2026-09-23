import { useRef, useCallback, useEffect } from "react";

const SWIPE_THRESHOLD = 50;

export type SwipeHandlers = {
  onTouchStart: (e: React.TouchEvent) => void;
  onTouchEnd: (e: React.TouchEvent) => void;
};

export function useSwipe(onPrev: () => void, onNext: () => void): SwipeHandlers {
  const startX = useRef<number | null>(null);
  const onPrevRef = useRef(onPrev);
  const onNextRef = useRef(onNext);

  useEffect(() => { onPrevRef.current = onPrev; }, [onPrev]);
  useEffect(() => { onNextRef.current = onNext; }, [onNext]);

  const onTouchStart = useCallback((e: React.TouchEvent) => {
    startX.current = e.touches[0].clientX;
  }, []);

  const onTouchEnd = useCallback((e: React.TouchEvent) => {
    if (startX.current === null) return;
    const delta = startX.current - e.changedTouches[0].clientX;
    if (delta > SWIPE_THRESHOLD) onNextRef.current();
    else if (delta < -SWIPE_THRESHOLD) onPrevRef.current();
    startX.current = null;
  }, []);

  return { onTouchStart, onTouchEnd };
}
