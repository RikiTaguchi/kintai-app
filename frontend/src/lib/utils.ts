import { SalaryResponse } from "@/types";

const WEEKDAYS = ["日", "月", "火", "水", "木", "金", "土"];

/** "HH:mm:ss" または null → "HH:mm" または "-" */
export function formatTime(t: string | null): string {
  if (!t) return "-";
  return t.substring(0, 5);
}

/** "yyyy-mm-dd" → "yyyy/mm/dd（曜）" */
export function formatDate(date: string | null | undefined): string {
  if (!date) return "";
  const [year, month, day] = date.split("-");
  const dow = WEEKDAYS[new Date(Number(year), Number(month) - 1, Number(day)).getDay()];
  return `${year}/${month}/${day}（${dow}）`;
}

/** 今日の日付から表示すべき年月を返す（26日以降は翌月） */
export function getDefaultPeriod(): { year: number; month: number } {
  const today = new Date();
  const day = today.getDate();
  const month = today.getMonth() + 1;
  const year = today.getFullYear();
  if (day >= 26) {
    return month === 12 ? { year: year + 1, month: 1 } : { year, month: month + 1 };
  }
  return { year, month };
}

/** 勤務日の日付から所属する年月を返す（26日以降は翌月） */
export function getPeriodFromDate(dateStr: string): { year: number; month: number } {
  const [y, m, d] = dateStr.split("-").map(Number);
  if (d >= 26) {
    return m === 12 ? { year: y + 1, month: 1 } : { year: y, month: m + 1 };
  }
  return { year: y, month: m };
}

/** URLクエリパラメータを安全にパースして年月を返す（不正値はデフォルト期間にフォールバック） */
export function parsePeriodParams(
  yearParam: string | null,
  monthParam: string | null
): { year: number; month: number } {
  const defaultPeriod = getDefaultPeriod();
  const year = Number(yearParam);
  const month = Number(monthParam);
  return {
    year: Number.isInteger(year) && year >= 2000 && year <= 2100 ? year : defaultPeriod.year,
    month: Number.isInteger(month) && month >= 1 && month <= 12 ? month : defaultPeriod.month,
  };
}

/** エラーオブジェクトからメッセージ文字列を安全に取り出す */
export function getErrorMessage(e: unknown, fallback = "予期せぬエラーが発生しました"): string {
  return e instanceof Error ? e.message : fallback;
}

/** 勤務日に適用される給与情報から交通費を返す */
export function getApplicableFee(salaries: SalaryResponse[], dateStr: string): string {
  const filtered = salaries.filter((s) => s.effectiveDate <= dateStr);
  if (filtered.length === 0) return "0";
  const latest = filtered.reduce((a, b) => (a.effectiveDate >= b.effectiveDate ? a : b));
  return String(latest.transportationFee);
}
