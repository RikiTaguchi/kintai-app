"use client";

import { useState, useEffect, useCallback, Suspense } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { getPayslip } from "@/lib/api";
import { PayslipResponse } from "@/types";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import Button from "@/components/ui/Button";
import EmptyState from "@/components/ui/EmptyState";
import MonthNavigator from "@/components/ui/MonthNavigator";
import { useToast } from "@/context/ToastContext";
import { parsePeriodParams, getErrorMessage } from "@/lib/utils";
import { calcIncomeTax } from "@/lib/incomeTax";
import { totalPayslipAmount } from "@/lib/payslipTotals";
import { useMonthSlide } from "@/hooks/useMonthSlide";

function minutesToHM(minutes: number): string {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return m > 0 ? `${h}時間${m}分` : `${h}時間`;
}

function PayslipRow({ label, amount, minutes }: { label: string; amount: number; minutes?: number }) {
  return (
    <div className="flex items-center justify-between py-3 border-b border-gray-100 dark:border-gray-700 last:border-0">
      <div>
        <p className="text-sm font-medium text-gray-900 dark:text-gray-100">{label}</p>
        {minutes != null && minutes > 0 && (
          <p className="text-xs text-gray-500 dark:text-gray-400">{minutesToHM(minutes)}</p>
        )}
      </div>
      <span className="text-sm font-semibold text-gray-900 dark:text-gray-100">¥{amount.toLocaleString()}</span>
    </div>
  );
}

function PayslipCountRow({ label, count }: { label: string; count: number }) {
  return (
    <div className="flex items-center justify-between py-3 border-b border-gray-100 dark:border-gray-700 last:border-0">
      <p className="text-sm font-medium text-gray-900 dark:text-gray-100">{label}</p>
      <span className="text-sm font-semibold text-gray-900 dark:text-gray-100">{count}コマ</span>
    </div>
  );
}

export default function TutorSalariesPage() {
  return (
    <Suspense fallback={<LoadingSpinner />}>
      <TutorSalariesContent />
    </Suspense>
  );
}

function TutorSalariesContent() {
  const { user } = useAuth();
  const searchParams = useSearchParams();
  const router = useRouter();
  const { addToast } = useToast();

  const initialPeriod = parsePeriodParams(searchParams.get("year"), searchParams.get("month"));
  const { year, month, prevMonth, nextMonth, contentRef } = useMonthSlide(
    initialPeriod.year,
    initialPeriod.month,
    (y, m) => router.replace(`/tutor/salaries?year=${y}&month=${m}`, { scroll: false })
  );

  const [payslip, setPayslip] = useState<PayslipResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isError, setIsError] = useState(false);
  const [withTax, setWithTax] = useState(false);

  const loadPayslip = useCallback(() => {
    if (!user) return;
    setIsLoading(true);
    setIsError(false);
    getPayslip(user.id, year, month)
      .then(setPayslip)
      .catch((e) => { addToast(getErrorMessage(e), "error"); setPayslip(null); setIsError(true); })
      .finally(() => setIsLoading(false));
  }, [user, year, month, addToast]);

  useEffect(() => { loadPayslip(); }, [loadPayslip]);

  const total = payslip ? totalPayslipAmount(payslip) : 0;
  const incomeTax = withTax ? calcIncomeTax(total, year, month) : 0;
  const netPay = total - incomeTax;

  return (
    <div ref={contentRef} className="py-4">
      <MonthNavigator year={year} month={month} onPrev={prevMonth} onNext={nextMonth} suffix=" 給与明細" />

      {isLoading ? (
        <LoadingSpinner />
      ) : isError ? (
        <div className="text-center py-12">
          <p className="text-sm text-red-500 dark:text-red-400 mb-3">データの取得に失敗しました</p>
          <Button variant="secondary" size="sm" onClick={loadPayslip}>再取得</Button>
        </div>
      ) : !payslip ? (
        <EmptyState message="この月の給与明細はありません" />
      ) : (
        <>
          {/* 合計 */}
          <div className="bg-teal-600 dark:bg-teal-700 rounded-xl p-5 mb-3 text-white text-center">
            <p className="text-sm opacity-80">{withTax ? "差引支給額" : "支給額"}</p>
            <p className="text-3xl font-bold mt-1">¥{(withTax ? netPay : total).toLocaleString()}</p>
            <p className={`text-sm mt-1 ${withTax ? "opacity-80" : "invisible"}`}>
              所得税 −¥{incomeTax.toLocaleString()}
            </p>
            <p className="text-xs opacity-70 mt-1">※勤務前の給与も含みます</p>
          </div>

          {/* 所得税トグル */}
          <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 px-4 py-3 mb-4 flex items-center justify-between">
            <p className="text-sm font-medium text-gray-700 dark:text-gray-300">所得税控除</p>
            <div className="flex gap-4">
              <label className="flex items-center gap-1.5 cursor-pointer">
                <input
                  type="radio"
                  name="taxToggle"
                  checked={!withTax}
                  onChange={() => setWithTax(false)}
                  className="accent-teal-600"
                />
                <span className="text-sm text-gray-700 dark:text-gray-300">なし</span>
              </label>
              <label className="flex items-center gap-1.5 cursor-pointer">
                <input
                  type="radio"
                  name="taxToggle"
                  checked={withTax}
                  onChange={() => setWithTax(true)}
                  className="accent-teal-600"
                />
                <span className="text-sm text-gray-700 dark:text-gray-300">あり</span>
              </label>
            </div>
          </div>

          {/* 内訳 */}
          <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 px-4 mb-4">
            <h2 className="text-sm font-semibold text-gray-700 dark:text-gray-300 py-3 border-b border-gray-100 dark:border-gray-700">支給内訳</h2>
            <PayslipRow
              label="コマ給"
              amount={payslip.periodCount > 0 ? Math.round(payslip.lessonPay / payslip.periodCount) : 0}
            />
            <PayslipCountRow label="コマ数" count={payslip.periodCount} />
            <PayslipRow label="コマ給×コマ数" amount={payslip.lessonPay} />
            <PayslipRow label="日当手当" amount={payslip.dailyAllowance} />
            <PayslipRow label="事務" amount={payslip.officeWorkPay} />
            <PayslipRow
              label="研修・自習室"
              amount={payslip.trainingAndStudyRoom?.amount ?? 0}
              minutes={payslip.trainingAndStudyRoom?.minutes}
            />
            <PayslipRow
              label="時間外勤務"
              amount={payslip.outsideHoursWork?.amount ?? 0}
              minutes={payslip.outsideHoursWork?.minutes}
            />
            <PayslipRow
              label="超過勤務割増"
              amount={payslip.overtimePremium?.amount ?? 0}
              minutes={payslip.overtimePremium?.minutes}
            />
            <PayslipRow
              label="深夜勤務割増"
              amount={payslip.nightShiftPremium?.amount ?? 0}
              minutes={payslip.nightShiftPremium?.minutes}
            />
            <PayslipRow label="その他" amount={payslip.otherPay} />
            <PayslipRow label="交通費" amount={payslip.transportationFee} />
          </div>
        </>
      )}
    </div>
  );
}
