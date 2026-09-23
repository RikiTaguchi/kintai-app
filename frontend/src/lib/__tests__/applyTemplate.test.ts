import { describe, expect, it } from "vitest";

import { applyTemplate } from "@/lib/applyTemplate";
import type { TemplateResponse } from "@/types";

function makeTemplate(overrides: Partial<TemplateResponse> = {}): TemplateResponse {
  return {
    id: "tp-1",
    tutorId: "t-1",
    title: "土曜日テンプレ",
    classroomId: "c-tds",
    classroomName: "戸塚",
    classroomNumber: 22,
    transportationFee: 300,
    lessonTemplateDetail: {
      startTime: "10:00:00",
      endTime: "12:00:00",
      breakMinutes: 30,
      periodCodes: ["M", "K"],
    },
    officeTemplateDetail: { startTime: null, endTime: null },
    otherTemplateDetail: {
      startTime: null,
      endTime: null,
      breakMinutes: null,
      description: null,
    },
    ...overrides,
  };
}

describe("applyTemplate", () => {
  it("対象 template を見つけて classroomId / 交通費 / WorkDetailState を返す", () => {
    const out = applyTemplate([makeTemplate()], "tp-1");
    expect(out).not.toBeNull();
    expect(out!.classroomId).toBe("c-tds");
    expect(out!.transportationFee).toBe("300");
    expect(out!.workDetail.lesson.startTime).toBe("10:00");
    expect(out!.workDetail.lesson.endTime).toBe("12:00");
    expect(out!.workDetail.lesson.breakMinutes).toBe("30");
    expect(out!.workDetail.lesson.periodCodes).toEqual(["M", "K"]);
  });

  it("見つからない templateId は null を返す", () => {
    expect(applyTemplate([makeTemplate()], "does-not-exist")).toBeNull();
    expect(applyTemplate([], "t")).toBeNull();
  });

  it("office startTime あり → showOffice=true, office 値を反映", () => {
    const tpl = makeTemplate({
      officeTemplateDetail: { startTime: "08:00:00", endTime: "09:00:00" },
    });
    const out = applyTemplate([tpl], "tp-1");
    expect(out!.workDetail.showOffice).toBe(true);
    expect(out!.workDetail.office.startTime).toBe("08:00");
    expect(out!.workDetail.office.endTime).toBe("09:00");
  });

  it("office startTime が null → showOffice=false, office は初期値", () => {
    const out = applyTemplate([makeTemplate()], "tp-1");
    expect(out!.workDetail.showOffice).toBe(false);
    expect(out!.workDetail.office).toEqual({ startTime: "", endTime: "" });
  });

  it("other startTime あり → showOther=true, description も含める", () => {
    const tpl = makeTemplate({
      otherTemplateDetail: {
        startTime: "13:00:00",
        endTime: "14:00:00",
        breakMinutes: 0,
        description: "掃除",
      },
    });
    const out = applyTemplate([tpl], "tp-1");
    expect(out!.workDetail.showOther).toBe(true);
    expect(out!.workDetail.other.description).toBe("掃除");
  });

  it("lessonTemplateDetail.startTime の秒付き " + '"10:30:00"' + " は fromApiTime で HH:mm に", () => {
    const tpl = makeTemplate({
      lessonTemplateDetail: {
        startTime: "10:30:00",
        endTime: "12:00:00",
        breakMinutes: 0,
        periodCodes: [],
      },
    });
    const out = applyTemplate([tpl], "tp-1");
    expect(out!.workDetail.lesson.startTime).toBe("10:30");
  });

  it("lessonTemplateDetail.breakMinutes が null の場合は '0' に変換される", () => {
    const tpl = makeTemplate({
      lessonTemplateDetail: {
        ...makeTemplate().lessonTemplateDetail,
        breakMinutes: null,
      },
    });
    const out = applyTemplate([tpl], "tp-1");
    expect(out!.workDetail.lesson.breakMinutes).toBe("0");
  });
});
