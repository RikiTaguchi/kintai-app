import type { PayslipResponse } from "@/types";

/**
 * PayslipResponse から合計支給額を計算する純粋関数。
 * 講師画面 tutor/salaries/page.tsx の集計式を定校准化。
 */
export function totalPayslipAmount(payslip: PayslipResponse): number {
  return (
    payslip.lessonPay +
    payslip.dailyAllowance +
    payslip.officeWorkPay +
    (payslip.trainingAndStudyRoom?.amount ?? 0) +
    (payslip.outsideHoursWork?.amount ?? 0) +
    (payslip.overtimePremium?.amount ?? 0) +
    (payslip.nightShiftPremium?.amount ?? 0) +
    payslip.otherPay +
    payslip.transportationFee
  );
}
