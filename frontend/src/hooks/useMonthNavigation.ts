"use client";

import { useState } from "react";

export function useMonthNavigation(
  initialYear: number,
  initialMonth: number,
  onChange?: (year: number, month: number) => void
) {
  const [year, setYear] = useState(initialYear);
  const [month, setMonth] = useState(initialMonth);

  const prevMonth = () => {
    const [ny, nm] = month === 1 ? [year - 1, 12] : [year, month - 1];
    setYear(ny);
    setMonth(nm);
    onChange?.(ny, nm);
  };

  const nextMonth = () => {
    const [ny, nm] = month === 12 ? [year + 1, 1] : [year, month + 1];
    setYear(ny);
    setMonth(nm);
    onChange?.(ny, nm);
  };

  return { year, month, prevMonth, nextMonth };
}
