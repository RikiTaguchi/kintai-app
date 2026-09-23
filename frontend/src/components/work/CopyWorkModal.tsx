"use client";

import { useState, useEffect, useCallback } from "react";
import { WorkResponse, WorkRegisterRequest } from "@/types";
import { useAuth } from "@/context/AuthContext";
import { getWorks, registerWork } from "@/lib/api";
import Button from "@/components/ui/Button";
import { useToast } from "@/context/ToastContext";
import { buildCalendarCells } from "@/lib/calendarCells";

const WEEKDAYS = ["日", "月", "火", "水", "木", "金", "土"];

interface Props {
  work: WorkResponse;
  onClose: () => void;
  onCopied: () => void;
  tutorId?: string;
  colorScheme?: "indigo" | "teal";
}

export default function CopyWorkModal({ work, onClose, onCopied, tutorId: tutorIdProp, colorScheme = "indigo" }: Props) {
  const { user } = useAuth();
  const { addToast } = useToast();
  const isTeal = colorScheme === "teal";
  const c = isTeal
    ? { spinner: "border-teal-600", selected: "bg-teal-600 text-white font-semibold", button: "bg-teal-600! hover:bg-teal-700! focus:ring-teal-500!" }
    : { spinner: "border-indigo-600", selected: "bg-indigo-600 text-white font-semibold", button: "" };

  const targetTutorId = tutorIdProp ?? user?.id ?? "";

  const [sourceYear, sourceMonth] = work.workingDate.split("-").map(Number);
  const [displayYear, setDisplayYear] = useState(sourceYear);
  const [displayMonth, setDisplayMonth] = useState(sourceMonth);
  const [bookedDates, setBookedDates] = useState<Set<string>>(new Set());
  const [isLoadingMonth, setIsLoadingMonth] = useState(false);
  const [selectedDates, setSelectedDates] = useState<Set<string>>(new Set());
  const [isSubmitting, setIsSubmitting] = useState(false);

  const loadBookedDates = useCallback(async () => {
    if (!targetTutorId) return;
    setIsLoadingMonth(true);
    try {
      const works = await getWorks(targetTutorId, displayYear, displayMonth);
      setBookedDates(new Set(works.map((w) => w.workingDate)));
    } catch {
      setBookedDates(new Set());
    } finally {
      setIsLoadingMonth(false);
    }
  }, [targetTutorId, displayYear, displayMonth]);

  useEffect(() => {
    loadBookedDates();
  }, [loadBookedDates]);

  const goToPrevMonth = () => {
    if (displayMonth === 1) {
      setDisplayYear((y) => y - 1);
      setDisplayMonth(12);
    } else {
      setDisplayMonth((m) => m - 1);
    }
  };

  const goToNextMonth = () => {
    if (displayMonth === 12) {
      setDisplayYear((y) => y + 1);
      setDisplayMonth(1);
    } else {
      setDisplayMonth((m) => m + 1);
    }
  };

  const toggleDate = (dateStr: string) => {
    setSelectedDates((prev) => {
      const next = new Set(prev);
      if (next.has(dateStr)) {
        next.delete(dateStr);
      } else {
        next.add(dateStr);
      }
      return next;
    });
  };

  const handleCopy = async () => {
    if (!targetTutorId || selectedDates.size === 0) return;
    setIsSubmitting(true);

    const base: WorkRegisterRequest = {
      tutorId: targetTutorId,
      classroomId: work.classroomId,
      workingDate: "",
      transportationFee: work.transportationFee,
      lessonWorkDetail: work.lessonWorkDetail,
      officeWorkDetail: work.officeWorkDetail,
      otherWorkDetail: work.otherWorkDetail,
    };

    const results = await Promise.allSettled(
      Array.from(selectedDates).map((date) =>
        registerWork(targetTutorId, { ...base, workingDate: date })
      )
    );
    setIsSubmitting(false);

    const successCount = results.filter((r) => r.status === "fulfilled").length;
    const failCount = results.filter((r) => r.status === "rejected").length;

    if (failCount === 0) {
      addToast(`${successCount}件の勤務情報をコピーしました`, "success");
    } else if (successCount === 0) {
      addToast(`コピーに失敗しました（${failCount}件）`, "error");
    } else {
      addToast(`${successCount}件コピー完了、${failCount}件失敗しました`, "error");
    }

    onCopied();
  };

  const calendarCells = buildCalendarCells(displayYear, displayMonth);

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />
      <div className="relative bg-white dark:bg-gray-800 rounded-2xl shadow-xl w-full max-w-sm">
        {/* ヘッダー */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-200 dark:border-gray-700">
          <h2 className="text-base font-semibold text-gray-900 dark:text-gray-100">
            コピーする日付を選択
          </h2>
          <button
            onClick={onClose}
            className="text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-300 cursor-pointer"
          >
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* 月ナビゲーター */}
        <div className="flex items-center justify-between px-5 py-3">
          <button
            onClick={goToPrevMonth}
            className="p-1.5 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-600 dark:text-gray-400 cursor-pointer"
          >
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <span className="font-semibold text-gray-900 dark:text-gray-100">
            {displayYear}年{displayMonth}月
          </span>
          <button
            onClick={goToNextMonth}
            className="p-1.5 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-600 dark:text-gray-400 cursor-pointer"
          >
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </button>
        </div>

        {/* カレンダー */}
        <div className="px-5 pb-4">
          <div className="grid grid-cols-7 mb-1">
            {WEEKDAYS.map((d) => (
              <div key={d} className="text-center text-xs font-medium text-gray-500 dark:text-gray-400 py-1">
                {d}
              </div>
            ))}
          </div>

          {isLoadingMonth ? (
            <div className="h-40 flex items-center justify-center">
              <div className={`w-6 h-6 border-2 ${c.spinner} border-t-transparent rounded-full animate-spin`} />
            </div>
          ) : (
            <div className="grid grid-cols-7 gap-y-1">
              {calendarCells.map((dateStr, i) => {
                if (!dateStr) return <div key={i} />;
                const [y, m, d] = dateStr.split("-").map(Number);
                const dow = new Date(y, m - 1, d).getDay();
                const isDisabled = bookedDates.has(dateStr);
                const isSelected = selectedDates.has(dateStr);

                return (
                  <button
                    key={dateStr}
                    disabled={isDisabled}
                    onClick={() => toggleDate(dateStr)}
                    className={[
                      "aspect-square flex items-center justify-center text-sm rounded-full transition-colors",
                      isDisabled
                        ? "text-gray-300 dark:text-gray-600 cursor-not-allowed"
                        : isSelected
                        ? c.selected
                        : dow === 0
                        ? "text-red-500 dark:text-red-400 hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer"
                        : dow === 6
                        ? "text-blue-500 dark:text-blue-400 hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer"
                        : "text-gray-900 dark:text-gray-100 hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer",
                    ].join(" ")}
                  >
                    {d}
                  </button>
                );
              })}
            </div>
          )}
        </div>

        {/* フッター */}
        <div className="px-5 pb-5 pt-3 border-t border-gray-100 dark:border-gray-700 flex items-center justify-between gap-3">
          <span className="text-sm text-gray-500 dark:text-gray-400">
            {selectedDates.size > 0 ? `${selectedDates.size}日選択中` : "日付を選択してください"}
          </span>
          <div className="flex gap-2 shrink-0">
            <Button variant="secondary" size="sm" onClick={onClose}>
              キャンセル
            </Button>
            <Button
              size="sm"
              className={c.button}
              disabled={selectedDates.size === 0 || isSubmitting}
              onClick={handleCopy}
            >
              {isSubmitting ? "コピー中..." : "コピーする"}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
