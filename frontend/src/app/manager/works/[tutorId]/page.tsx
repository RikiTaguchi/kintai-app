"use client";

import { useState, useEffect, use, Suspense } from "react";
import Link from "next/link";
import { useSearchParams, useRouter } from "next/navigation";
import { getTutors, getSalaries } from "@/lib/api";
import { TutorResponse } from "@/types";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import Button from "@/components/ui/Button";
import { inputClass } from "@/components/ui/FormField";
import { useToast } from "@/context/ToastContext";
import { parsePeriodParams, getErrorMessage } from "@/lib/utils";
import WorkReportTable from "@/components/manager/WorkReportTable";

export default function ManagerTutorWorksPage({
  params,
}: {
  params: Promise<{ tutorId: string }>;
}) {
  const { tutorId } = use(params);
  return (
    <Suspense fallback={<LoadingSpinner />}>
      <TutorWorksContent tutorId={tutorId} />
    </Suspense>
  );
}

function TutorWorksContent({ tutorId }: { tutorId: string }) {
  const searchParams = useSearchParams();
  const router = useRouter();
  const { addToast } = useToast();
  const currentYear = new Date().getFullYear();
  const [isDownloading, setIsDownloading] = useState(false);

  const { year, month } = parsePeriodParams(searchParams.get("year"), searchParams.get("month"));

  const [tutors, setTutors] = useState<TutorResponse[]>([]);
  const [noSalary, setNoSalary] = useState(false);

  const tutor = tutors.find((t) => t.id === tutorId);

  const setParam = (key: string, value: string) => {
    const p = new URLSearchParams(searchParams.toString());
    p.set(key, value);
    router.replace(`/manager/works/${tutorId}?${p.toString()}`);
  };

  const handleTutorChange = (newTutorId: string) => {
    if (!newTutorId) {
      router.push("/manager/works");
      return;
    }
    router.push(`/manager/works/${newTutorId}?year=${year}&month=${month}`);
  };

  useEffect(() => {
    getTutors().then(setTutors).catch((e) => addToast(getErrorMessage(e), "error"));
    getSalaries(tutorId).then((s) => setNoSalary(s.length === 0)).catch(() => setNoSalary(true));
  }, [tutorId, addToast]);

  const handleDownload = async () => {
    setIsDownloading(true);
    try {
      const res = await fetch(`/excel/payslips?year=${year}&month=${month}`);
      if (res.redirected) {
        window.location.href = res.url;
        return;
      }
      if (!res.ok) throw new Error("ダウンロードに失敗しました");
      const blob = await res.blob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      const cd = res.headers.get("content-disposition") ?? "";
      const m = cd.match(/filename\*=UTF-8''(.+)/);
      a.download = m ? decodeURIComponent(m[1]) : `講師給フォーム.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
    } catch (e) {
      addToast(getErrorMessage(e, "ダウンロードに失敗しました"), "error");
    } finally {
      setIsDownloading(false);
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">勤務管理</h1>
        <div className="flex gap-2">
          <Button
            size="sm"
            onClick={handleDownload}
            disabled={isDownloading}
          >
            {isDownloading ? (
              "生成中..."
            ) : (
              <>
                <svg className="w-4 h-4 mr-1 inline-block" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                </svg>
                講師給フォームをDL
              </>
            )}
          </Button>
          <Link href={`/manager/works/${tutorId}/register`}>
            <Button size="sm">
              <svg className="w-4 h-4 mr-1 inline-block" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
              勤務を登録
            </Button>
          </Link>
        </div>
      </div>

      <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-4 mb-4 flex flex-wrap gap-3">
        <div className="flex flex-col gap-1 min-w-48">
          <label className="text-xs font-medium text-gray-600 dark:text-gray-400">講師</label>
          {tutors.length === 0 ? (
            <div className="h-9 bg-gray-100 dark:bg-gray-700 rounded-lg animate-pulse w-48" />
          ) : (
            <select value={tutorId} onChange={(e) => handleTutorChange(e.target.value)} className={inputClass}>
              <option value="">講師を選択</option>
              {tutors.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.lastName} {t.firstName}
                </option>
              ))}
            </select>
          )}
        </div>
        <div className="flex flex-col gap-1">
          <label className="text-xs font-medium text-gray-600 dark:text-gray-400">年</label>
          <select value={year} onChange={(e) => setParam("year", e.target.value)} className={inputClass}>
            {Array.from({ length: 5 }, (_, i) => currentYear - 2 + i).map((y) => (
              <option key={y} value={y}>{y}年</option>
            ))}
          </select>
        </div>
        <div className="flex flex-col gap-1">
          <label className="text-xs font-medium text-gray-600 dark:text-gray-400">月</label>
          <select value={month} onChange={(e) => setParam("month", e.target.value)} className={inputClass}>
            {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
              <option key={m} value={m}>{m}月</option>
            ))}
          </select>
        </div>
      </div>

      {noSalary && (
        <div className="bg-amber-50 dark:bg-amber-900/40 border border-amber-200 dark:border-amber-800 rounded-xl px-4 py-3 mb-4 flex items-center justify-between gap-4">
          <p className="text-sm text-amber-800 dark:text-amber-300">この講師の給与情報が登録されていません。</p>
          <Link href={`/manager/salaries/${tutorId}/register`} className="shrink-0">
            <Button size="sm" variant="secondary">給与情報を登録</Button>
          </Link>
        </div>
      )}

      {tutor ? (
        <WorkReportTable tutor={tutor} year={year} month={month} />
      ) : (
        <LoadingSpinner />
      )}
    </div>
  );
}
