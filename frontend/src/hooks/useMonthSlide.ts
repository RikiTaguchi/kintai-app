import { useRef, useLayoutEffect, useCallback } from "react";
import { useMonthNavigation } from "@/hooks/useMonthNavigation";
import { useRegisterSwipe } from "@/context/SwipeContext";

const ALL_SLIDE_CLASSES = [
  "month-slide-next-a", "month-slide-next-b",
  "month-slide-prev-a", "month-slide-prev-b",
];

export function useMonthSlide(
  initialYear: number,
  initialMonth: number,
  onNavigate: (year: number, month: number) => void
) {
  const { year, month, prevMonth, nextMonth } = useMonthNavigation(initialYear, initialMonth, onNavigate);

  const contentRef = useRef<HTMLDivElement>(null);
  const dirRef = useRef<"next" | "prev">("next");
  const flipRef = useRef(false);
  const lastPeriod = useRef({ year: initialYear, month: initialMonth });

  const goPrev = useCallback(() => {
    dirRef.current = "prev";
    prevMonth();
  }, [prevMonth]);

  const goNext = useCallback(() => {
    dirRef.current = "next";
    nextMonth();
  }, [nextMonth]);

  useRegisterSwipe(goPrev, goNext);

  useLayoutEffect(() => {
    // 年月が変化していない場合はスキップ（初回マウント・StrictMode 二重実行を除外）
    if (lastPeriod.current.year === year && lastPeriod.current.month === month) return;
    lastPeriod.current = { year, month };

    const el = contentRef.current;
    if (!el) return;

    flipRef.current = !flipRef.current;
    const variant = flipRef.current ? "a" : "b";
    ALL_SLIDE_CLASSES.forEach((c) => el.classList.remove(c));
    el.classList.add(`month-slide-${dirRef.current}-${variant}`);
  }, [year, month]);

  return { year, month, prevMonth: goPrev, nextMonth: goNext, contentRef };
}
