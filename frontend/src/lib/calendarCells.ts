/**
 * 「もう一方の日にコピー」モーダル等で使うカレンダー 6x7 セル配列の構築。
 *
 * buildCalendarCells の第 1 引数は週の先頭曜日（0=日曜日。Date#getDay() 準拠）。
 * 戻り値は空白セル（null） + パディングなし日付文字列（"YYYY-MM-DD"）の配列で、
 * toBeRendered の cellCount は firstDow + daysInMonth となる。
 */

export function buildCalendarCells(year: number, month: number): (string | null)[] {
  const firstDow = new Date(year, month - 1, 1).getDay();
  const daysInMonth = new Date(year, month, 0).getDate();
  const cells: (string | null)[] = Array(firstDow).fill(null);
  for (let d = 1; d <= daysInMonth; d++) {
    cells.push(
      `${year}-${String(month).padStart(2, "0")}-${String(d).padStart(2, "0")}`
    );
  }
  return cells;
}
