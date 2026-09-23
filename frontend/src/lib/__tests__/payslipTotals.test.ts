import { describe, expect, it } from "vitest";

import { totalPayslipAmount } from "@/lib/payslipTotals";
import type { PayslipResponse } from "@/types";

function payslip(p: Partial<PayslipResponse>): PayslipResponse {
  return {
    tutorId: "t-1",
    lessonPay: 0,
    periodCount: 0,
    dailyAllowance: 0,
    officeWorkPay: 0,
    trainingAndStudyRoom: { amount: 0, minutes: 0 },
    outsideHoursWork: { amount: 0, minutes: 0 },
    overtimePremium: { amount: 0, minutes: 0 },
    nightShiftPremium: { amount: 0, minutes: 0 },
    otherPay: 0,
    transportationFee: 0,
    ...p,
  };
}

describe("totalPayslipAmount", () => {
  it("すべて 0 のとき 0", () => {
    expect(totalPayslipAmount(payslip({}))).toBe(0);
  });

  it("固定値 4 項目のみでも集計できる", () => {
    const total = totalPayslipAmount(
      payslip({
        lessonPay: 10_000,
        dailyAllowance: 1_000,
        officeWorkPay: 500,
        otherPay: 100,
        transportationFee: 300,
      })
    );
    expect(total).toBe(11_900);
  });

  it("PayslipItem の amount を正しく加算する", () => {
    const total = totalPayslipAmount(
      payslip({
        trainingAndStudyRoom: { amount: 100, minutes: 60 },
        outsideHoursWork: { amount: 200, minutes: 30 },
        overtimePremium: { amount: 300, minutes: 90 },
        nightShiftPremium: { amount: 400, minutes: 45 },
      })
    );
    expect(total).toBe(1000);
  });

  it("PayslipItem の minutes は金額に含めない（別列）", () => {
    const total = totalPayslipAmount(
      payslip({ trainingAndStudyRoom: { amount: 100, minutes: 9999 } })
    );
    expect(total).toBe(100);
  });

  it("すべての項目同時に入れた場合の合計", () => {
    const total = totalPayslipAmount(
      payslip({
        lessonPay: 1000,
        dailyAllowance: 100,
        officeWorkPay: 200,
        trainingAndStudyRoom: { amount: 300, minutes: 60 },
        outsideHoursWork: { amount: 400, minutes: 30 },
        overtimePremium: { amount: 500, minutes: 90 },
        nightShiftPremium: { amount: 600, minutes: 45 },
        otherPay: 700,
        transportationFee: 800,
      })
    );
    expect(total).toBe(4600);
  });
});
