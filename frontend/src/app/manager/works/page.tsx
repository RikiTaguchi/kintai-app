"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { getTutors } from "@/lib/api";
import { TutorResponse } from "@/types";
import { inputClass } from "@/components/ui/FormField";
import { getDefaultPeriod, getErrorMessage } from "@/lib/utils";
import { useToast } from "@/context/ToastContext";
import EmptyState from "@/components/ui/EmptyState";
import Button from "@/components/ui/Button";

export default function ManagerWorksPage() {
  const router = useRouter();
  const { addToast } = useToast();
  const defaultPeriod = getDefaultPeriod();

  const [tutors, setTutors] = useState<TutorResponse[]>([]);
  const [isTutorsLoading, setIsTutorsLoading] = useState(true);
  const [year, setYear] = useState(defaultPeriod.year);
  const [month, setMonth] = useState(defaultPeriod.month);
  const [isDownloading, setIsDownloading] = useState(false);

  const handleDownload = async () => {
    setIsDownloading(true);
    try {
      const res = await fetch(`/excel/payslips?year=${year}&month=${month}`);
      if (res.redirected) { window.location.href = res.url; return; }
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

  useEffect(() => {
    getTutors()
      .then(setTutors)
      .catch((e) => addToast(getErrorMessage(e), "error"))
      .finally(() => setIsTutorsLoading(false));
  }, [addToast]);

  const handleTutorChange = (tutorId: string) => {
    if (!tutorId) return;
    router.push(`/manager/works/${tutorId}?year=${year}&month=${month}`);
  };

  const currentYear = new Date().getFullYear();

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">勤務管理</h1>
        <div className="flex gap-2">
          <Button size="sm" onClick={handleDownload} disabled={isDownloading}>
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
          <Link href="/manager/works/_/register">
            <Button size="sm">+ 勤務を登録</Button>
          </Link>
        </div>
      </div>

      <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-4 mb-6 flex flex-wrap gap-3">
        <div className="flex flex-col gap-1 min-w-48">
          <label className="text-xs font-medium text-gray-600 dark:text-gray-400">講師</label>
          {isTutorsLoading ? (
            <div className="h-9 bg-gray-100 dark:bg-gray-700 rounded-lg animate-pulse w-48" />
          ) : (
            <select defaultValue="" onChange={(e) => handleTutorChange(e.target.value)} className={inputClass}>
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
          <select value={year} onChange={(e) => setYear(Number(e.target.value))} className={inputClass}>
            {Array.from({ length: 5 }, (_, i) => currentYear - 2 + i).map((y) => (
              <option key={y} value={y}>{y}年</option>
            ))}
          </select>
        </div>
        <div className="flex flex-col gap-1">
          <label className="text-xs font-medium text-gray-600 dark:text-gray-400">月</label>
          <select value={month} onChange={(e) => setMonth(Number(e.target.value))} className={inputClass}>
            {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
              <option key={m} value={m}>{m}月</option>
            ))}
          </select>
        </div>
      </div>

      <EmptyState message="講師を選択してください" />
    </div>
  );
}
