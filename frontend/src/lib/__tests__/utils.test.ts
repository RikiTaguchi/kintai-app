import { describe, expect, it, vi } from "vitest";

import {
  formatDate,
  formatTime,
  getApplicableFee,
  getDefaultPeriod,
  getErrorMessage,
  getPeriodFromDate,
  parsePeriodParams,
} from "@/lib/utils";
import type { SalaryResponse } from "@/types";

function salary(effectiveDate: string, transportationFee: number): SalaryResponse {
  return {
    id: "s-" + effectiveDate,
    tutorId: "t-1",
    effectiveDate,
    lessonWage: 0,
    officeWage: 0,
    transportationFee,
  };
}

describe("formatTime", () => {
  it('"HH:mm:ss" を "HH:mm" に丸める', () => {
    expect(formatTime("09:30:00")).toBe("09:30");
    expect(formatTime("23:59:59")).toBe("23:59");
  });

  it("null / 空文字は '-' を返す", () => {
    expect(formatTime(null)).toBe("-");
    expect(formatTime("")).toBe("-");
  });

  it('先頭5文字をそのまま返す（"9:00" のような短い値でもそのまま）', () => {
    expect(formatTime("9:00")).toBe("9:00");
  });
});

describe("formatDate", () => {
  it("yyyy-mm-dd を yyyy/mm/dd（曜）に整形する", () => {
    // 2025-09-22 は月曜日
    expect(formatDate("2025-09-22")).toBe("2025/09/22（月）");
    // 2025-01-01 は水曜日
    expect(formatDate("2025-01-01")).toBe("2025/01/01（水）");
  });

  it("日曜〜土曜の境界を正しく返す", () => {
    expect(formatDate("2025-09-21")).toContain("（日）");
    expect(formatDate("2025-09-27")).toContain("（土）");
  });

  it("null / undefined / 空文字は空文字を返す", () => {
    expect(formatDate(null)).toBe("");
    expect(formatDate(undefined)).toBe("");
    expect(formatDate("")).toBe("");
  });
});

describe("getDefaultPeriod", () => {
  it("26日より前はその月を返す", () => {
    vi.setSystemTime(new Date(2025, 8, 25)); // 2025-09-25
    expect(getDefaultPeriod()).toEqual({ year: 2025, month: 9 });
  });

  it("26日以降は翌月を返す", () => {
    vi.setSystemTime(new Date(2025, 8, 26)); // 2025-09-26
    expect(getDefaultPeriod()).toEqual({ year: 2025, month: 10 });
  });

  it("12月26日以降は翌年1月を返す", () => {
    vi.setSystemTime(new Date(2025, 11, 31)); // 2025-12-31
    expect(getDefaultPeriod()).toEqual({ year: 2026, month: 1 });
  });

  it("1日はその月を返す", () => {
    vi.setSystemTime(new Date(2025, 0, 1));
    expect(getDefaultPeriod()).toEqual({ year: 2025, month: 1 });
  });
});

describe("getPeriodFromDate", () => {
  it("25日までならその月", () => {
    expect(getPeriodFromDate("2025-09-01")).toEqual({ year: 2025, month: 9 });
    expect(getPeriodFromDate("2025-09-25")).toEqual({ year: 2025, month: 9 });
  });

  it("26日以降は翌月", () => {
    expect(getPeriodFromDate("2025-09-26")).toEqual({ year: 2025, month: 10 });
    expect(getPeriodFromDate("2025-10-10")).toEqual({ year: 2025, month: 10 });
  });

  it("12月26日以降は翌年1月", () => {
    expect(getPeriodFromDate("2025-12-26")).toEqual({ year: 2026, month: 1 });
    expect(getPeriodFromDate("2025-12-31")).toEqual({ year: 2026, month: 1 });
  });
});

describe("parsePeriodParams", () => {
  const FALLBACK = "fallback を確認";

  it("正常値はそのままパースする", () => {
    vi.setSystemTime(new Date(2025, 8, 15));
    expect(parsePeriodParams("2026", "3")).toEqual({ year: 2026, month: 3 });
  });

  it("null / 非数値 / 小数はデフォルト期間にフォールバックする", () => {
    vi.setSystemTime(new Date(2025, 8, 15)); // → {2025, 9}
    expect(parsePeriodParams(null, null), FALLBACK).toEqual({ year: 2025, month: 9 });
    expect(parsePeriodParams("abc", "def")).toEqual({ year: 2025, month: 9 });
    expect(parsePeriodParams("2025.5", "9.9")).toEqual({ year: 2025, month: 9 });
  });

  it("年の範囲外（<2000 or >2100）はフォールバック", () => {
    vi.setSystemTime(new Date(2025, 8, 15));
    expect(parsePeriodParams("1999", "9")).toEqual({ year: 2025, month: 9 });
    expect(parsePeriodParams("2101", "9")).toEqual({ year: 2025, month: 9 });
    expect(parsePeriodParams("2000", "9").year).toBe(2000); // 境界値は受理
    expect(parsePeriodParams("2100", "9").year).toBe(2100);
  });

  it("月の範囲外（<1 or >12）はフォールバック", () => {
    vi.setSystemTime(new Date(2025, 8, 15));
    expect(parsePeriodParams("2025", "0")).toEqual({ year: 2025, month: 9 });
    expect(parsePeriodParams("2025", "13")).toEqual({ year: 2025, month: 9 });
    expect(parsePeriodParams("2025", "1").month).toBe(1); // 境界値は受理
    expect(parsePeriodParams("2025", "12").month).toBe(12);
  });

  it("年と月は独立に検証される（片方だけ不正でも片方は使われる）", () => {
    vi.setSystemTime(new Date(2025, 8, 15));
    // 年が不正 → デフォルト年、月は有効値が使われる
    expect(parsePeriodParams("1999", "5")).toEqual({ year: 2025, month: 5 });
    // 月が不正 → デフォルト月、年は有効値が使われる
    expect(parsePeriodParams("2030", "13")).toEqual({ year: 2030, month: 9 });
  });
});

describe("getErrorMessage", () => {
  it("Error インスタンスから message を取り出す", () => {
    expect(getErrorMessage(new Error("DB接続失敗"))).toBe("DB接続失敗");
  });

  it("Error 以外は既定のフォールバックを返す", () => {
    expect(getErrorMessage("string エラー")).toBe("予期せぬエラーが発生しました");
    expect(getErrorMessage(404)).toBe("予期せぬエラーが発生しました");
    expect(getErrorMessage(null)).toBe("予期せぬエラーが発生しました");
    expect(getErrorMessage(undefined)).toBe("予期せぬエラーが発生しました");
  });

  it("カスタムフォールバックを指定できる", () => {
    expect(getErrorMessage(null, "保存に失敗しました")).toBe("保存に失敗しました");
  });

  it("Error のサブクラスも message を返す", () => {
    expect(getErrorMessage(new TypeError("型が違います"))).toBe("型が違います");
  });
});

describe("getApplicableFee", () => {
  const salaries = [
    salary("2025-04-01", 500), // 古い
    salary("2025-08-01", 700), // 新しい
  ];

  it("勤務日以前の最新の給与の交通費を返す", () => {
    // 2025-09-10 には 8月版(700) が適用
    expect(getApplicableFee(salaries, "2025-09-10")).toBe("700");
    // 2025-05-01 には 4月版(500) が適用
    expect(getApplicableFee(salaries, "2025-05-01")).toBe("500");
  });

  it("適用開始日当日はその給与が適用される", () => {
    expect(getApplicableFee(salaries, "2025-08-01")).toBe("700");
    expect(getApplicableFee(salaries, "2025-04-01")).toBe("500");
  });

  it("どの給与よりも前の日付は '0' を返す", () => {
    expect(getApplicableFee(salaries, "2025-03-31")).toBe("0");
    expect(getApplicableFee([], "2025-09-10")).toBe("0");
  });

  it("給与リストが未ソートでも最新を正しく選ぶ", () => {
    const shuffled = [salary("2025-08-01", 700), salary("2025-04-01", 500)];
    expect(getApplicableFee(shuffled, "2025-09-10")).toBe("700");
  });
});
