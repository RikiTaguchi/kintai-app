import { describe, expect, it } from "vitest";

import {
  computeRow,
  findStandardFee,
  LESSON_BUFFER,
  LESSON_UNIT,
  minFrac,
  nightPortion,
  timeFrac,
  NIGHT_START,
  OVERTIME_THRESHOLD,
} from "@/lib/excelCalc";

const { floor } = Math;
const min = (n: number) => floor(n * 1440 + 1e-9); // fraction → 整数分

describe("timeFrac", () => {
  it("HH:mm → day fraction", () => {
    expect(timeFrac("12:00")).toBeCloseTo(0.5, 10);
    expect(timeFrac("00:00")).toBe(0);
  });

  it("HH:mm:ss も OK（秒は無視）", () => {
    expect(timeFrac("09:30:00")).toBeCloseTo((9 * 60 + 30) / 1440, 10);
    expect(timeFrac("12:34:56")).toBeCloseTo((12 * 60 + 34) / 1440, 10);
  });

  it("null / undefined / 空文字 → null", () => {
    expect(timeFrac(null)).toBeNull();
    expect(timeFrac(undefined)).toBeNull();
    expect(timeFrac("")).toBeNull();
  });

  describe("不正入力の防御", () => {
    it("範囲外の時刻（23 時超、60 分以上） → null", () => {
      expect(timeFrac("24:00")).toBeNull();
      expect(timeFrac("25:00")).toBeNull();
      expect(timeFrac("99:99")).toBeNull();
      expect(timeFrac("12:60")).toBeNull();
      expect(timeFrac("12:34:60")).toBeNull();
    });

    it("非数値・異形形式 → null", () => {
      expect(timeFrac("abc")).toBeNull();
      expect(timeFrac("12")).toBeNull();
      expect(timeFrac("12:")).toBeNull();
      expect(timeFrac(":30")).toBeNull();
      expect(timeFrac("12:3")).toBeNull(); // 1 桁の分は許容しない
      expect(timeFrac("12:30:5")).toBeNull(); // 1 桁の秒は許容しない
      expect(timeFrac("12-30")).toBeNull();
      expect(timeFrac("12.30")).toBeNull();
    });

    it("境界: 23:59 / 23:59:59 は OK", () => {
      expect(timeFrac("23:59")).toBeCloseTo((23 * 60 + 59) / 1440, 10);
      expect(timeFrac("23:59:59")).toBeCloseTo((23 * 60 + 59) / 1440, 10);
    });
  });
});

describe("minFrac", () => {
  it("分 → day fraction", () => {
    expect(minFrac(0)).toBe(0);
    expect(minFrac(60)).toBeCloseTo(60 / 1440, 10);
  });

  it("null / undefined → null", () => {
    expect(minFrac(null)).toBeNull();
    expect(minFrac(undefined)).toBeNull();
  });
});

describe("nightPortion", () => {
  it("深夜 0:00 終了（= fraction 1.0） → 2時間分", () => {
    // 22:00 から 24:00 までの 2 時間
    expect(min(nightPortion(1.0))).toBe(120);
  });

  it("22:00 ちょうど → 0", () => {
    expect(nightPortion(22 / 24)).toBe(0);
  });

  it("21:59 → 0（深夜帯手前）", () => {
    expect(nightPortion((21 * 60 + 59) / 1440)).toBe(0);
  });

  it("22:30 終了 → 30分", () => {
    expect(min(nightPortion((22 * 60 + 30) / 1440))).toBe(30);
  });

  it("null → 0", () => {
    expect(nightPortion(null)).toBe(0);
  });
});

describe("findStandardFee", () => {
  const salaries = [
    { effectiveDate: "2025-04-01", transportationFee: 400 },
    { effectiveDate: "2025-08-15", transportationFee: 500 },
    { effectiveDate: "2025-11-01", transportationFee: 600 },
  ];

  it("periodEnd 以前の最新のものを採用", () => {
    // 2025 年 9 月分 → periodEnd = 2025-09-25 以前の最新 → 2025-08-15
    expect(findStandardFee(salaries, 2025, 9)).toBe(500);
  });

  it("periodEnd が「その月の25日」である点を尊重する", () => {
    // 2025 年 4 月分 → periodEnd = 2025-04-25。4/1 のみ適用
    expect(findStandardFee(salaries, 2025, 4)).toBe(400);
  });

  it("どれも期間外なら 0", () => {
    expect(
      findStandardFee(
        [{ effectiveDate: "2099-01-01", transportationFee: 9999 }],
        2025,
        9
      )
    ).toBe(0);
  });

  it("空配列なら 0", () => {
    expect(findStandardFee([], 2025, 9)).toBe(0);
  });
});

describe("computeRow", () => {
  const baseWork = {
    lessonWorkDetail: {
      startTime: "10:00:00",
      endTime: "12:00:00",
      breakMinutes: 0,
      periodCodes: ["M", "K"],
    },
    officeWorkDetail: { startTime: null, endTime: null },
    otherWorkDetail: {
      startTime: null,
      endTime: null,
      breakMinutes: null,
      description: null,
    },
  };

  it("コマ数 I は periodCodes.length", () => {
    const r = computeRow(baseWork);
    expect(r.I).toBe(2);
  });

  it("periodCodes は PERIOD_ORDER の順序で並び替え・連結", () => {
    const w = {
      ...baseWork,
      lessonWorkDetail: {
        ...baseWork.lessonWorkDetail,
        periodCodes: ["K", "M", "B"],
      },
    };
    expect(computeRow(w).periodCodes).toBe("MKB");
  });

  it("予定時間（コマ数×100分）+ buffer 20分 は時間外にならない", () => {
    // 2 コマ = 3:20。これに 0:20 buffer を加えた 3:40 までに収まれば AP は null。
    // 10:00 - 13:40 = 3:40 で、ちょうど buffer 上限まで
    const w = {
      ...baseWork,
      lessonWorkDetail: {
        ...baseWork.lessonWorkDetail,
        startTime: "10:00:00",
        endTime: "13:40:00",
        breakMinutes: 0,
      },
    };
    expect(computeRow(w).AP).toBeNull();
  });

  it("buffer を 1 分超過すると AP が発火", () => {
    const w = {
      ...baseWork,
      lessonWorkDetail: {
        ...baseWork.lessonWorkDetail,
        startTime: "10:00:00",
        endTime: "13:41:00",
        breakMinutes: 0,
      },
    };
    const r = computeRow(w);
    expect(r.AP).not.toBeNull();
    expect(min(r.AP!)).toBe(1);
  });

  it("休憩分数は lesson time から引く", () => {
    const w = {
      ...baseWork,
      lessonWorkDetail: {
        ...baseWork.lessonWorkDetail,
        startTime: "10:00:00",
        endTime: "13:41:00",
        breakMinutes: 1, // 1 分でも入ると、buffer の 1 分超過を相殺
      },
    };
    expect(computeRow(w).AP).toBeNull();
  });

  it("授業も事務もなく 研修だけ → AY = 研修時間", () => {
    const w = {
      lessonWorkDetail: {
        startTime: null,
        endTime: null,
        breakMinutes: null,
        periodCodes: [],
      },
      officeWorkDetail: { startTime: null, endTime: null },
      otherWorkDetail: {
        startTime: "13:00:00",
        endTime: "15:00:00",
        breakMinutes: 0,
        description: "研修",
      },
    };
    const r = computeRow(w);
    expect(min(r.AY!)).toBe(120);
  });

  it("休憩跨ぎで勤務時間は減る", () => {
    // 10:00 - 13:00 = 3:00 = 180分、+30分休憩 → AY = 150分
    const w = {
      ...baseWork,
      lessonWorkDetail: {
        startTime: "10:00:00",
        endTime: "13:00:00",
        breakMinutes: 30,
        periodCodes: [],
      },
    };
    const r = computeRow(w);
    expect(min(r.AY!)).toBe(150);
  });

  it("授業 time が null で事務もなく研修もない → AY は null", () => {
    const w = {
      lessonWorkDetail: {
        startTime: null,
        endTime: null,
        breakMinutes: null,
        periodCodes: [],
      },
      officeWorkDetail: { startTime: null, endTime: null },
      otherWorkDetail: {
        startTime: null,
        endTime: null,
        breakMinutes: null,
        description: null,
      },
    };
    expect(computeRow(w).AY).toBeNull();
  });

  it("AY が 8h を超えると AR が発火", () => {
    // 8:00 - 19:00 = 11:00 = 660分 → 8h を 180 分超過
    const w = {
      ...baseWork,
      lessonWorkDetail: {
        startTime: "08:00:00",
        endTime: "19:00:00",
        breakMinutes: 0,
        periodCodes: [],
      },
    };
    const r = computeRow(w);
    expect(min(r.AY!)).toBe(660);
    expect(min(r.AR!)).toBe(180);
  });

  it("AY がちょうど 8h の場合は AR は発火しない", () => {
    // 9:00 - 17:00 = 8:00 = 480分 → 8h ちょうど（超過なし）
    const w = {
      ...baseWork,
      lessonWorkDetail: {
        startTime: "09:00:00",
        endTime: "17:00:00",
        breakMinutes: 0,
        periodCodes: [],
      },
    };
    const r = computeRow(w);
    expect(min(r.AY!)).toBe(480);
    expect(r.AR).toBeNull();
  });

  it("AY が 8h を 1 分超過すると AR が発火", () => {
    // 9:00 - 17:01 = 8:01 = 481分 → 8h を 1 分超過
    const w = {
      ...baseWork,
      lessonWorkDetail: {
        startTime: "09:00:00",
        endTime: "17:01:00",
        breakMinutes: 0,
        periodCodes: [],
      },
    };
    const r = computeRow(w);
    expect(min(r.AR!)).toBe(1);
  });

  it("深夜: 授業 21:00 終了 → 0 / 23:30 終了 → 90分", () => {
    const mk = (end: string) =>
      computeRow({
        ...baseWork,
        lessonWorkDetail: {
          ...baseWork.lessonWorkDetail,
          endTime: `${end}:00`,
        },
      });
    expect(min(mk("21:00").AZ)).toBe(0);
    expect(min(mk("23:30").AZ)).toBe(90);
  });

  it("事務のみ深夜跨ぎ → BA のみ > 0", () => {
    const w = {
      lessonWorkDetail: {
        startTime: null,
        endTime: null,
        breakMinutes: null,
        periodCodes: [],
      },
      officeWorkDetail: { startTime: "20:00:00", endTime: "23:00:00" },
      otherWorkDetail: {
        startTime: null,
        endTime: null,
        breakMinutes: null,
        description: null,
      },
    };
    const r = computeRow(w);
    expect(min(r.BA)).toBe(60);
    expect(min(r.AZ)).toBe(0);
    expect(min(r.BB)).toBe(0);
    expect(min(r.BC)).toBe(60);
    expect(min(r.AT!)).toBe(60);
  });

  it("事務 X = V - T", () => {
    const w = {
      lessonWorkDetail: {
        startTime: null,
        endTime: null,
        breakMinutes: null,
        periodCodes: [],
      },
      officeWorkDetail: { startTime: "09:00:00", endTime: "10:30:00" },
      otherWorkDetail: {
        startTime: null,
        endTime: null,
        breakMinutes: null,
        description: null,
      },
    };
    expect(min(computeRow(w).X!)).toBe(90);
  });

  it("AN = AJ - AH - AL（研修 - 休憩）", () => {
    const w = {
      lessonWorkDetail: {
        startTime: null,
        endTime: null,
        breakMinutes: null,
        periodCodes: [],
      },
      officeWorkDetail: { startTime: null, endTime: null },
      otherWorkDetail: {
        startTime: "13:00:00",
        endTime: "15:30:00",
        breakMinutes: 15,
        description: null,
      },
    };
    expect(min(computeRow(w).AN!)).toBe(135);
  });

  it("AT は BC=0 のとき null", () => {
    const r = computeRow(baseWork);
    // baseWork では 21:00 より前に終了するので AZ=0,BA=0,BB=0 → BC=0 → AT=null
    expect(r.AZ).toBe(0);
    expect(r.BA).toBe(0);
    expect(r.BB).toBe(0);
    expect(r.BC).toBe(0);
    expect(r.AT).toBeNull();
  });

  it("LESSON_UNIT は 100 / 1440", () => {
    expect(LESSON_UNIT).toBeCloseTo(100 / 1440, 10);
  });

  it("LESSON_BUFFER は 20 / 1440", () => {
    expect(LESSON_BUFFER).toBeCloseTo(20 / 1440, 10);
  });

  it("NIGHT_START は 22 / 24", () => {
    expect(NIGHT_START).toBeCloseTo(22 / 24, 10);
  });

  it("OVERTIME_THRESHOLD は 8 / 24", () => {
    expect(OVERTIME_THRESHOLD).toBeCloseTo(8 / 24, 10);
  });
});

describe("computeRow: 不正入力時の null 伝播", () => {
  it("startTime が形式不正な場合、N / AP / AY が null になり NaN にならない", () => {
    const out = computeRow({
      lessonWorkDetail: {
        startTime: "abc",
        endTime: "12:00:00",
        breakMinutes: 0,
        periodCodes: ["M"],
      },
    });
    expect(out.N).toBeNull();
    expect(out.AP).toBeNull();
    // AY は office/other の合計のみで構成される（lessonTime は 0 になる）
    expect(out.AY).toBeNull();
    // NaN が混入していないこと
    Object.values(out).forEach((v) => {
      if (typeof v === "number") {
        expect(Number.isFinite(v)).toBe(true);
      }
    });
  });

  it("endTime が範囲外（24:00）の場合も null 伝播", () => {
    const out = computeRow({
      lessonWorkDetail: {
        startTime: "10:00:00",
        endTime: "24:00:00",
        breakMinutes: 0,
        periodCodes: ["M"],
      },
    });
    expect(out.P).toBeNull();
    expect(out.AP).toBeNull();
    Object.values(out).forEach((v) => {
      if (typeof v === "number") {
        expect(Number.isFinite(v)).toBe(true);
      }
    });
  });
});
