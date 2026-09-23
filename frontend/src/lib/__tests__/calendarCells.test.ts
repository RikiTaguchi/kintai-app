import { describe, expect, it } from "vitest";

import { buildCalendarCells } from "@/lib/calendarCells";

describe("buildCalendarCells", () => {
  it("2025-09-01（月曜始まり）→ 前方 1 個が null、以降 30 日分", () => {
    const cells = buildCalendarCells(2025, 9);
    // 2025-09-01 は月曜（DoW=1）
    expect(cells[0]).toBeNull();
    expect(cells[1]).toBe("2025-09-01");
    expect(cells[30]).toBe("2025-09-30");
    expect(cells).toHaveLength(31);
  });

  it("2025-08-01（金曜始まり）→ 前方 5 個 null", () => {
    const cells = buildCalendarCells(2025, 8);
    // 2025-08-01 = 金曜（DoW=5）
    for (let i = 0; i < 5; i++) expect(cells[i]).toBeNull();
    expect(cells[5]).toBe("2025-08-01");
    expect(cells[35]).toBe("2025-08-31");
    expect(cells).toHaveLength(36);
  });

  it("2月（うるう年ではない） → 28 日分", () => {
    const cells = buildCalendarCells(2025, 2);
    // 2025-02-28 = 金曜、これが配列最後
    expect(cells[cells.length - 1]).toBe("2025-02-28");
  });

  it("うるう年の2月 → 29 日分", () => {
    const cells = buildCalendarCells(2024, 2);
    expect(cells[cells.length - 1]).toBe("2024-02-29");
  });

  it("yyyy-mm-dd のフォーマットが enforce", () => {
    const cells = buildCalendarCells(2025, 9);
    // 全要素（非 null）が YYYY-MM-DD 形式
    for (const c of cells) {
      if (c == null) continue;
      expect(c).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    }
    // 先頭月が 1 桁月でも "09" になっている
    expect(cells.find((c) => c != null && c.endsWith("01"))).toBe("2025-09-01");
  });
});
