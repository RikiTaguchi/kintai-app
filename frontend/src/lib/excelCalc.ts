/**
 * 講師給フォーム（Excel出力）用の時刻・長さの純粋計算ロジック
 *
 * NOTE: このモジュール内の「時間」はすべて「1日の分数」(0〜1.0) として扱う。
 *   - 例: 22:00 = 22/24, 10:30 = (10*60+30)/1440
 * Excel に出力される sheet を ExcelJS で操作するため、フロント側では
 * JS の Date ではなく fractions of a day で集計とフォーマットの両方を行う。
 */

/** 1 コマの授業時間（100 分）を 1 日で表した値 */
export const LESSON_UNIT = 100 / 1440; // 1:40 per period
/** 教材等のバッファ時間（予定時間超過を「時間外」とみなさないゆとり） */
export const LESSON_BUFFER = 20 / 1440; // 0:20 buffer
/** 通常勤務の上限（8 時間） */
export const OVERTIME_THRESHOLD = 8 / 24; // 8:00
/** 深夜割増の開始時刻（22:00） */
export const NIGHT_START = 22 / 24; // 22:00

/** "HH:mm" または "HH:mm:ss"（0-23時 / 0-59分 / 0-59秒）の形式チェック */
const TIME_PATTERN = /^([01]\d|2[0-3]):([0-5]\d)(?::([0-5]\d))?$/;

/**
 * "HH:mm" または "HH:mm:ss" 形式の文字列を 1 日の分数に変換する。
 * パース不能・空文字/null/undefined・形式不正・範囲外の値の場合は null を返す。
 */
export function timeFrac(s: string | null | undefined): number | null {
  if (!s) return null;
  const match = TIME_PATTERN.exec(s);
  if (!match) return null;
  const hours = Number(match[1]);
  const minutes = Number(match[2]);
  return (hours * 60 + minutes) / 1440;
}

/**
 * 休憩時間（分）を 1 日の分数に変換する。
 * null/undefined の場合は null を返す。
 */
export function minFrac(mins: number | null | undefined): number | null {
  return mins == null ? null : mins / 1440;
}

/**
 * [start, end] のうち 22:00 以降に該当する分の長さを返す。
 * start / end が null の場合や、深夜帯に交差しない場合は 0。
 */
export function nightPortion(start: number | null, end: number | null): number {
  if (start == null || end == null) return 0;
  const s = Math.max(start, NIGHT_START);
  const e = Math.max(end, NIGHT_START);
  return Math.max(0, e - s);
}

/**
 * year / month の指定月（正確にはその月の 25 日締め）に適用される標準交通費を
 * salary リストから返す。該当がなければ 0。
 *
 * salary は `{ effectiveDate: "YYYY-MM-DD", transportationFee: number }` の
 * オブジェクトで、すでに降順/昇順いずれでもよい（内部で max を取るため）。
 */
export function findStandardFee(
  salaries: { effectiveDate: string; transportationFee: number }[],
  year: number,
  month: number
): number {
  const periodEnd = `${year}-${String(month).padStart(2, "0")}-25`;
  const valid = salaries.filter((s) => s.effectiveDate <= periodEnd);
  if (!valid.length) return 0;
  return valid.reduce((a, b) =>
    a.effectiveDate >= b.effectiveDate ? a : b
  ).transportationFee;
}

export interface RowValues {
  N: number | null;
  P: number | null;
  R: number | null;
  T: number | null;
  V: number | null;
  AH: number | null;
  AJ: number | null;
  AL: number | null;
  I: number;
  periodCodes: string;
  X: number | null; // office duration (V-T)
  AN: number | null; // training duration (AJ-AH-AL)
  AP: number | null; // overtime (positive only)
  AR: number | null; // excess overtime (AY > 8h)
  AY: number | null; // total work time
  AZ: number; // lesson night portion
  BA: number; // office night portion
  BB: number; // training night portion
  BC: number; // night total
  AT: number | null; // night shift (= BC, null if 0)
}

/** 講師給フォームの 1 行分の値（1 勤務あたり）を計算する */
export function computeRow(w: {
  lessonWorkDetail?: {
    startTime: string | null;
    endTime: string | null;
    breakMinutes: number | null;
    periodCodes: string[];
  } | null;
  officeWorkDetail?: { startTime: string | null; endTime: string | null } | null;
  otherWorkDetail?: {
    startTime: string | null;
    endTime: string | null;
    breakMinutes: number | null;
    description: string | null;
  } | null;
}): RowValues {
  const codes: string[] = w.lessonWorkDetail?.periodCodes ?? [];
  const I = codes.length;
  const N = timeFrac(w.lessonWorkDetail?.startTime);
  const P = timeFrac(w.lessonWorkDetail?.endTime);
  const R = minFrac(w.lessonWorkDetail?.breakMinutes);
  const T = timeFrac(w.officeWorkDetail?.startTime);
  const V = timeFrac(w.officeWorkDetail?.endTime);
  const AH = timeFrac(w.otherWorkDetail?.startTime);
  const AJ = timeFrac(w.otherWorkDetail?.endTime);
  const AL = minFrac(w.otherWorkDetail?.breakMinutes);

  const X = T != null && V != null ? V - T : null;
  const AN = AH != null && AJ != null ? AJ - AH - (AL ?? 0) : null;

  // Overtime = lesson time - (scheduled periods × 1:40) - 0:20 buffer
  let AP: number | null = null;
  if (P != null && N != null) {
    const raw = P - N - (R ?? 0) - LESSON_UNIT * I - LESSON_BUFFER;
    if (raw > 1e-9) AP = raw;
  }

  const lessonTime = P != null && N != null ? P - N - (R ?? 0) : 0;
  const officeTime = X ?? 0;
  const trainingTime = AN ?? 0;
  const totalWork = lessonTime + officeTime + trainingTime;
  const AY = totalWork > 1e-9 ? totalWork : null;

  const AZ = nightPortion(N, P);
  const BA = nightPortion(T, V);
  const BB = nightPortion(AH, AJ);
  const BC = AZ + BA + BB;

  let AR: number | null = null;
  if (AY != null && AY > OVERTIME_THRESHOLD + 1e-9) {
    AR = AY - OVERTIME_THRESHOLD;
  }

  const AT = BC > 1e-9 ? BC : null;
  // Note: PERIOD_ORDER は route.ts 側で定義（このモジュールからは受け取る形に
  // しない）だが、periodCodes の並びはここで正規化する
  const periodCodes = ["M", "K", "S", "A", "B", "C", "D"]
    .filter((p) => codes.includes(p))
    .join("");

  return {
    N, P, R, T, V, AH, AJ, AL, I, periodCodes,
    X, AN, AP, AR, AY, AZ, BA, BB, BC, AT,
  };
}
