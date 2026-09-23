import { describe, expect, it } from "vitest";

import { calcIncomeTax } from "@/lib/incomeTax";

/**
 * calcIncomeTax のテスト。
 *
 * テーブル切替ルール:
 *   - 旧版: 2025年11月分まで（支払日 2025/12/10 まで）
 *   - 新版: 2025年12月分から（支払日 2026/1/10 以降）
 *
 * 旧版テーブル（2025年11月まで適用）:
 *   - 88,000円から課税開始
 *   - 356,000–359,000 の区間は 12080 で単調性が崩れている（旧テーブルの仕様通りとする）
 *
 * 新版テーブル（2025年12月から適用）:
 *   - 105,000円から課税開始
 *   - 356,000–359,000 の区間は 12220 で単調性が維持されている
 *
 * year/month 省略時は新版テーブルを使用する（既存呼び出しとの後方互換のため）。
 */
describe("calcIncomeTax（新版 = 2025年12月以降）", () => {
  describe("非課税範囲", () => {
    it("105000 未満は 0", () => {
      expect(calcIncomeTax(0, 2025, 12)).toBe(0);
      expect(calcIncomeTax(88000, 2025, 12)).toBe(0);
      expect(calcIncomeTax(104999, 2025, 12)).toBe(0);
    });
  });

  describe("テーブル境界（最初と最後）", () => {
    it("105000–106999 は 170", () => {
      expect(calcIncomeTax(105000, 2025, 12)).toBe(170);
      expect(calcIncomeTax(106999, 2025, 12)).toBe(170);
    });

    it("107000–108999 は 280", () => {
      expect(calcIncomeTax(107000, 2025, 12)).toBe(280);
    });

    it("737000–739999 は 71380（最終区間）", () => {
      expect(calcIncomeTax(737000, 2025, 12)).toBe(71380);
      expect(calcIncomeTax(739999, 2025, 12)).toBe(71380);
    });
  });

  describe("区切り付近の代表値（2万円台）", () => {
    it("221000–223999 は 5150", () => {
      expect(calcIncomeTax(221000, 2025, 12)).toBe(5150);
    });

    it("215000–216999 は 4910", () => {
      expect(calcIncomeTax(216999, 2025, 12)).toBe(4910);
    });

    it("308000–310999 は 8300", () => {
      expect(calcIncomeTax(308000, 2025, 12)).toBe(8300);
    });
  });

  describe("旧版で単調性が破壊されていた区間（新版は修正済）", () => {
    it("353000–355999 は 11980", () => {
      expect(calcIncomeTax(354000, 2025, 12)).toBe(11980);
    });

    it("356000–358999 は 12220（旧版 12080/13080 でなく 12220 で単調）", () => {
      expect(calcIncomeTax(356000, 2025, 12)).toBe(12220);
      expect(calcIncomeTax(356999, 2025, 12)).toBe(12220);
    });

    it("359000–361999 は 12470", () => {
      expect(calcIncomeTax(359000, 2025, 12)).toBe(12470);
    });
  });

  describe("中間区間の単調増加性", () => {
    it("支給額が増えると税額は単調非減少", () => {
      const values = [
        110_000, 150_000, 200_000, 250_000, 300_000, 350_000, 400_000,
        450_000, 500_000, 550_000, 600_000, 650_000, 700_000,
      ];
      let prev = -1;
      for (const v of values) {
        const cur = calcIncomeTax(v, 2025, 12);
        expect(cur).toBeGreaterThan(prev);
        prev = cur;
      }
    });
  });

  describe("テーブル上限超過", () => {
    it("740000 以上はどの区間にも該当せず 0", () => {
      expect(calcIncomeTax(740000, 2025, 12)).toBe(0);
      expect(calcIncomeTax(10_000_000, 2025, 12)).toBe(0);
      expect(calcIncomeTax(99_999_999, 2025, 12)).toBe(0);
    });
  });

  describe("負値・端数", () => {
    it("負の給与は 0", () => {
      expect(calcIncomeTax(-1, 2025, 12)).toBe(0);
    });

    it("端数は切り捨てられて区間所属で判定", () => {
      expect(calcIncomeTax(105_999.9, 2025, 12)).toBe(170);
      expect(calcIncomeTax(106_000.5, 2025, 12)).toBe(170);
    });
  });
});

describe("calcIncomeTax（旧版 = 2025年11月以前）", () => {
  describe("非課税範囲", () => {
    it("88000 未満は 0", () => {
      expect(calcIncomeTax(0, 2025, 11)).toBe(0);
      expect(calcIncomeTax(87999, 2025, 11)).toBe(0);
    });
  });

  describe("テーブル境界（最初）", () => {
    it("88000–88999 は 130（旧版の最小区間）", () => {
      expect(calcIncomeTax(88000, 2025, 11)).toBe(130);
      expect(calcIncomeTax(88999, 2025, 11)).toBe(130);
    });

    it("89000–89999 は 180", () => {
      expect(calcIncomeTax(89000, 2025, 11)).toBe(180);
    });
  });

  describe("旧版と新版で税額が異なる区間", () => {
    it("105000–106999 は旧版 1030（新版は 170）", () => {
      expect(calcIncomeTax(105000, 2025, 11)).toBe(1030);
      expect(calcIncomeTax(106999, 2025, 11)).toBe(1030);
    });

    it("150000 は旧版 2980（新版は 2420）", () => {
      expect(calcIncomeTax(150000, 2025, 11)).toBe(2980);
    });

    it("200000 は旧版 4770（新版は 4340）", () => {
      expect(calcIncomeTax(200000, 2025, 11)).toBe(4770);
    });
  });

  describe("旧版で単調性が破壊されている区間（353000〜362000）", () => {
    it("353000–355999 は 12830", () => {
      expect(calcIncomeTax(353000, 2025, 11)).toBe(12830);
      expect(calcIncomeTax(355999, 2025, 11)).toBe(12830);
    });

    it("356000–358999 は 12080（前区間より小さい = 旧版の既知の非単調区間）", () => {
      expect(calcIncomeTax(356000, 2025, 11)).toBe(12080);
      expect(calcIncomeTax(358999, 2025, 11)).toBe(12080);
    });

    it("359000–361999 は 13320（単調に戻る）", () => {
      expect(calcIncomeTax(359000, 2025, 11)).toBe(13320);
    });
  });

  describe("高額区間（旧版は10,000,000円まで網羅）", () => {
    it("740000–779999 は 73390", () => {
      expect(calcIncomeTax(740000, 2025, 11)).toBe(73390);
      expect(calcIncomeTax(779999, 2025, 11)).toBe(73390);
    });

    it("1700000–2169999 は 374180", () => {
      expect(calcIncomeTax(1700000, 2025, 11)).toBe(374180);
      expect(calcIncomeTax(2000000, 2025, 11)).toBe(374180);
      expect(calcIncomeTax(2169999, 2025, 11)).toBe(374180);
    });

    it("3500000–9999999 は 1125620", () => {
      expect(calcIncomeTax(3500000, 2025, 11)).toBe(1125620);
      expect(calcIncomeTax(9999999, 2025, 11)).toBe(1125620);
    });

    it("10000000 以上は範囲外で 0", () => {
      expect(calcIncomeTax(10000000, 2025, 11)).toBe(0);
    });
  });
});

describe("calcIncomeTax（テーブル切替判定）", () => {
  it("2025年11月分は旧版、2025年12月分は新版", () => {
    // 105000円は旧版 = 1030、新版 = 170 で明確に区別できる
    expect(calcIncomeTax(105000, 2025, 11)).toBe(1030);
    expect(calcIncomeTax(105000, 2025, 12)).toBe(170);
  });

  it("2026年以降は新版", () => {
    expect(calcIncomeTax(105000, 2026, 1)).toBe(170);
    expect(calcIncomeTax(105000, 2026, 12)).toBe(170);
    expect(calcIncomeTax(105000, 2030, 6)).toBe(170);
  });

  it("2025年より前は旧版", () => {
    expect(calcIncomeTax(105000, 2024, 12)).toBe(1030);
    expect(calcIncomeTax(105000, 2020, 1)).toBe(1030);
  });

  it("year/month 省略時は新版", () => {
    expect(calcIncomeTax(105000)).toBe(170);
    expect(calcIncomeTax(105000, 2025)).toBe(170);
    expect(calcIncomeTax(105000, undefined, 11)).toBe(170);
  });
});
