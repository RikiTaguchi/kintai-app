"use client";

import { useState, useEffect, useCallback, useMemo, Suspense } from "react";
import Link from "next/link";
import { useSearchParams, useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { getWorks, deleteWork } from "@/lib/api";
import { WorkResponse } from "@/types";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import Button from "@/components/ui/Button";
import ConfirmDialog from "@/components/ui/ConfirmDialog";
import MonthNavigator from "@/components/ui/MonthNavigator";
import EmptyState from "@/components/ui/EmptyState";
import WorkDetailModal from "@/components/work/WorkDetailModal";
import CopyWorkModal from "@/components/work/CopyWorkModal";
import { useToast } from "@/context/ToastContext";
import { formatDate, parsePeriodParams, getErrorMessage } from "@/lib/utils";
import { useConfirm } from "@/hooks/useConfirm";
import { useMonthSlide } from "@/hooks/useMonthSlide";

const WEEKDAYS = ["日", "月", "火", "水", "木", "金", "土"];

function WorkCard({
  work,
  onDelete,
  onDetail,
}: {
  work: WorkResponse;
  onDelete: () => void;
  onDetail: () => void;
}) {
  const d = new Date(work.workingDate);
  const dow = WEEKDAYS[d.getDay()];
  const hasLesson = !!work.lessonWorkDetail.startTime || work.lessonWorkDetail.periodCodes?.length > 0;
  const hasOffice = !!work.officeWorkDetail.startTime;
  const hasOther = !!work.otherWorkDetail.startTime;

  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-4 cursor-pointer" onClick={onDetail}>
      <div className="flex items-start justify-between">
        <div>
          <div className="flex flex-col mb-1">
            <span className="text-base font-bold text-gray-900 dark:text-gray-100">
              {d.getMonth() + 1}/{d.getDate()}（{dow}）
            </span>
            <span className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">{work.classroomName}教室</span>
          </div>
          <div className="flex flex-wrap gap-1 mt-1">
            {hasLesson && (
              <span className="px-2 py-0.5 bg-teal-100 dark:bg-teal-900/50 text-teal-700 dark:text-teal-300 text-xs rounded-full font-medium">
                {work.lessonWorkDetail.periodCodes?.length > 0
                  ? work.lessonWorkDetail.periodCodes.join("")
                  : "授業"}
              </span>
            )}
            {hasOffice && (
              <span className="px-2 py-0.5 bg-teal-100 dark:bg-teal-900/50 text-teal-700 dark:text-teal-300 text-xs rounded-full font-medium">事務</span>
            )}
            {hasOther && (
              <span className="px-2 py-0.5 bg-teal-100 dark:bg-teal-900/50 text-teal-700 dark:text-teal-300 text-xs rounded-full font-medium">その他</span>
            )}
          </div>
        </div>
        <div className="flex flex-col items-end gap-1" onClick={(e) => e.stopPropagation()}>
          <div className="flex gap-1">
            <Button variant="ghost" size="sm" onClick={onDetail}>詳細</Button>
            <Link href={`/tutor/works/${work.id}/edit`}>
              <Button variant="ghost" size="sm">編集</Button>
            </Link>
          </div>
          <Button variant="ghost" size="sm" className="text-red-500!" onClick={onDelete}>削除</Button>
        </div>
      </div>
    </div>
  );
}

export default function TutorWorksPage() {
  return (
    <Suspense fallback={<LoadingSpinner />}>
      <TutorWorksContent />
    </Suspense>
  );
}

function TutorWorksContent() {
  const { user } = useAuth();
  const searchParams = useSearchParams();
  const router = useRouter();
  const { addToast } = useToast();

  const initialPeriod = parsePeriodParams(searchParams.get("year"), searchParams.get("month"));
  const { year, month, prevMonth, nextMonth, contentRef } = useMonthSlide(
    initialPeriod.year,
    initialPeriod.month,
    (y, m) => router.replace(`/tutor/works?year=${y}&month=${m}`, { scroll: false })
  );

  const [works, setWorks] = useState<WorkResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isError, setIsError] = useState(false);
  const [detailWork, setDetailWork] = useState<WorkResponse | null>(null);
  const [copyWork, setCopyWork] = useState<WorkResponse | null>(null);
  const deleteConfirm = useConfirm<WorkResponse>();

  const loadWorks = useCallback(() => {
    if (!user) return;
    setIsLoading(true);
    setIsError(false);
    getWorks(user.id, year, month)
      .then(setWorks)
      .catch((e) => { addToast(getErrorMessage(e), "error"); setIsError(true); })
      .finally(() => setIsLoading(false));
  }, [user, year, month, addToast]);

  useEffect(() => { loadWorks(); }, [loadWorks]);

  const sortedWorks = useMemo(
    () => [...works].sort((a, b) => a.workingDate.localeCompare(b.workingDate)),
    [works]
  );

  const handleDelete = async () => {
    if (!deleteConfirm.target || !user) return;
    deleteConfirm.setIsLoading(true);
    try {
      await deleteWork(user.id, deleteConfirm.target.id);
      deleteConfirm.cancel();
      addToast("勤務情報を削除しました", "success");
      loadWorks();
    } catch (e) {
      addToast(getErrorMessage(e, "削除に失敗しました"), "error");
    } finally {
      deleteConfirm.setIsLoading(false);
    }
  };

  const today = new Date().toISOString().slice(0, 10);
  const attendedCount = sortedWorks.filter(w => w.workingDate <= today).length;
  const scheduledCount = sortedWorks.filter(w => w.workingDate > today).length;

  return (
    <div ref={contentRef} className="py-4">
      <MonthNavigator year={year} month={month} onPrev={prevMonth} onNext={nextMonth} />

      {/* サマリー */}
      {sortedWorks.length > 0 && (
        <div className="bg-teal-50 dark:bg-teal-900/40 rounded-xl p-3 mb-4 flex justify-around text-center">
          <div>
            <p className="text-xs text-teal-600 dark:text-teal-400">出勤済み</p>
            <p className="text-lg font-bold text-teal-800 dark:text-teal-300">{attendedCount}日</p>
          </div>
          <div>
            <p className="text-xs text-teal-600 dark:text-teal-400">出勤予定</p>
            <p className="text-lg font-bold text-teal-800 dark:text-teal-300">{scheduledCount}日</p>
          </div>
        </div>
      )}

      {/* 登録ボタン */}
      <div className="mb-4">
        <Link href="/tutor/works/register">
          <Button className="w-full justify-center! bg-teal-600! hover:bg-teal-700! focus:ring-teal-500!">
            <svg className="w-4 h-4 mr-1 inline-block" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            勤務を登録
          </Button>
        </Link>
      </div>

      {/* 勤務リスト */}
      {isLoading ? (
        <LoadingSpinner />
      ) : isError ? (
        <div className="text-center py-12">
          <p className="text-sm text-red-500 dark:text-red-400 mb-3">データの取得に失敗しました</p>
          <Button variant="secondary" size="sm" onClick={loadWorks}>再取得</Button>
        </div>
      ) : sortedWorks.length === 0 ? (
        <EmptyState message="この月の勤務情報はありません" />
      ) : (
        <div className="space-y-3">
          {sortedWorks.map((w) => (
            <WorkCard
              key={w.id}
              work={w}
              onDelete={() => deleteConfirm.confirm(w)}
              onDetail={() => setDetailWork(w)}
            />
          ))}
        </div>
      )}

      {detailWork && (
        <WorkDetailModal
          work={detailWork}
          onClose={() => setDetailWork(null)}
          onEdit={() => {
            router.push(`/tutor/works/${detailWork.id}/edit`);
            setDetailWork(null);
          }}
          onDelete={() => {
            deleteConfirm.confirm(detailWork);
            setDetailWork(null);
          }}
          onCopy={() => {
            setCopyWork(detailWork);
            setDetailWork(null);
          }}
          colorScheme="teal"
          showClassroom={false}
        />
      )}

      {copyWork && (
        <CopyWorkModal
          work={copyWork}
          onClose={() => setCopyWork(null)}
          onCopied={() => {
            setCopyWork(null);
            loadWorks();
          }}
          colorScheme="teal"
        />
      )}

      <ConfirmDialog
        isOpen={!!deleteConfirm.target}
        title="勤務情報を削除しますか？"
        message={`${formatDate(deleteConfirm.target?.workingDate)} の勤務情報を削除します。`}
        isLoading={deleteConfirm.isLoading}
        onConfirm={handleDelete}
        onCancel={deleteConfirm.cancel}
      />
    </div>
  );
}
