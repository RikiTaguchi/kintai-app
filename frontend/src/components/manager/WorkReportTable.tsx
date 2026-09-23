"use client";

import { useState, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import { getWorks, deleteWork } from "@/lib/api";
import { computeRow, type RowValues } from "@/lib/excelCalc";
import { TutorResponse, WorkResponse } from "@/types";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import EmptyState from "@/components/ui/EmptyState";
import ConfirmDialog from "@/components/ui/ConfirmDialog";
import WorkDetailModal from "@/components/work/WorkDetailModal";
import CopyWorkModal from "@/components/work/CopyWorkModal";
import { useConfirm } from "@/hooks/useConfirm";
import { useToast } from "@/context/ToastContext";
import { getErrorMessage } from "@/lib/utils";

const WEEKDAYS = ["日", "月", "火", "水", "木", "金", "土"];

/** 1日の分数（0〜1.0）を "h:mm" 形式の文字列に変換する */
function formatFrac(frac: number | null): string {
  if (frac == null) return "-";
  const total = Math.round(frac * 1440);
  return `${Math.floor(total / 60)}:${String(total % 60).padStart(2, "0")}`;
}

function weekdayOf(dateStr: string): string {
  const [y, m, d] = dateStr.split("-").map(Number);
  return WEEKDAYS[new Date(y, m - 1, d).getDay()];
}

/** "YYYY-MM-DD" → "YYYY/M/D"（曜日なし） */
function formatDateOnly(dateStr: string): string {
  const [y, m, d] = dateStr.split("-").map(Number);
  return `${y}/${m}/${d}`;
}

const CELL_BASE =
  "px-3 py-0 whitespace-nowrap align-middle text-center";
/** 数値系列に適用する固定幅（コマ数・開始・終了・休憩・時間・日次手当・交通費・時間外・超過・深夜で統一。h:mm や ¥4桁金額が収まる幅） */
const FIXED_WIDTH = "w-20 min-w-20";

function Cell({ value, title, fixed = false }: { value: string | null; title?: string; fixed?: boolean }) {
  return (
    <td className={`${CELL_BASE} ${fixed ? FIXED_WIDTH : ""}`} title={title}>
      {value ? value : <span className="text-gray-400 dark:text-gray-500">-</span>}
    </td>
  );
}

function TimeCell({ frac }: { frac: number | null }) {
  return <Cell value={frac == null ? null : formatFrac(frac)} fixed />;
}

function AllowanceCell({ amount }: { amount: number }) {
  return <Cell value={amount > 0 ? `¥${amount.toLocaleString()}` : null} fixed />;
}

const HEAD_BASE =
  "px-3 py-2 font-medium text-gray-600 dark:text-gray-400 whitespace-nowrap text-center align-middle border border-gray-200 dark:border-gray-700";

/** 縦2行結合の見出しセル */
function HeadRowSpan({ children, colSpan = 1, fixed = false }: { children: React.ReactNode; colSpan?: number; fixed?: boolean }) {
  return (
    <th rowSpan={2} colSpan={colSpan} className={`${HEAD_BASE} ${fixed ? FIXED_WIDTH : ""}`}>
      {children}
    </th>
  );
}

/** 改行を含む見出しテキスト（行を2つに分けて中央揃えで縦積み） */
function HeadTwoLine({ line1, line2 }: { line1: string; line2: string }) {
  return (
    <span className="inline-flex flex-col leading-tight">
      <span>{line1}</span>
      <span>{line2}</span>
    </span>
  );
}

/** グループ見出し（1行目のみ、横結合） */
function HeadGroup({ colSpan, children }: { colSpan: number; children: React.ReactNode }) {
  return (
    <th colSpan={colSpan} className={`${HEAD_BASE} border-l border-gray-200 dark:border-gray-700`}>
      {children}
    </th>
  );
}

/** サブ見出し（2行目） */
function HeadSub({ children, borderL = false, fixed = false }: { children: React.ReactNode; borderL?: boolean; fixed?: boolean }) {
  return (
    <th className={`${HEAD_BASE} ${borderL ? "border-l border-gray-200 dark:border-gray-700" : ""} ${fixed ? FIXED_WIDTH : ""}`}>
      {children}
    </th>
  );
}

interface Props {
  tutor: TutorResponse;
  year: number;
  month: number;
}

/** Excel の講師シートと同じ内容の勤務情報テーブル（モーダルを開かなくても確認できる一覧） */
export default function WorkReportTable({ tutor, year, month }: Props) {
  const router = useRouter();
  const { addToast } = useToast();
  const [works, setWorks] = useState<WorkResponse[] | null>(null);
  const [detailWork, setDetailWork] = useState<WorkResponse | null>(null);
  const [copyWork, setCopyWork] = useState<WorkResponse | null>(null);
  const deleteConfirm = useConfirm<WorkResponse>();

  const loadWorks = useCallback(() => {
    getWorks(tutor.id, year, month)
      .then((list) =>
        setWorks([...list].sort((a, b) => a.workingDate.localeCompare(b.workingDate)))
      )
      .catch((e) => addToast(getErrorMessage(e), "error"));
  }, [tutor.id, year, month, addToast]);

  useEffect(() => {
    loadWorks();
  }, [loadWorks]);

  const handleDelete = async () => {
    if (!deleteConfirm.target) return;
    deleteConfirm.setIsLoading(true);
    try {
      await deleteWork(tutor.id, deleteConfirm.target.id);
      deleteConfirm.cancel();
      addToast("勤務情報を削除しました", "success");
      loadWorks();
    } catch (e) {
      addToast(getErrorMessage(e, "削除に失敗しました"), "error");
    } finally {
      deleteConfirm.setIsLoading(false);
    }
  };

  if (works === null) return <LoadingSpinner />;
  if (works.length === 0) return <EmptyState message="この月の勤務情報はありません" />;

  const rows: { work: WorkResponse; rv: RowValues }[] = works.map((w) => ({
    work: w,
    rv: computeRow(w),
  }));

  return (
    <div className="max-w-full bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 overflow-x-auto p-1.5">
      <table className="min-w-full w-max text-sm border-collapse table-fixed bg-white dark:bg-gray-800 rounded-lg overflow-hidden">
        <thead className="bg-gray-100 dark:bg-gray-950 border-b-2 border-gray-300 dark:border-gray-600">
          <tr>
            <HeadRowSpan colSpan={2}>勤務日</HeadRowSpan>
            <HeadRowSpan>勤務コマ</HeadRowSpan>
            <HeadRowSpan fixed>コマ数</HeadRowSpan>
            <HeadRowSpan><HeadTwoLine line1="ヘルプ" line2="勤務教室" /></HeadRowSpan>
            <HeadGroup colSpan={3}>授業業務</HeadGroup>
            <HeadGroup colSpan={3}>事務業務</HeadGroup>
            <HeadRowSpan fixed><HeadTwoLine line1="日次" line2="手当" /></HeadRowSpan>
            <HeadRowSpan fixed>交通費</HeadRowSpan>
            <HeadGroup colSpan={5}>研修(実施会場) / 自習教室</HeadGroup>
            <HeadRowSpan fixed>時間外</HeadRowSpan>
            <HeadRowSpan fixed>超過</HeadRowSpan>
            <HeadRowSpan fixed>深夜</HeadRowSpan>
          </tr>
          <tr className="border-t border-gray-200 dark:border-gray-700">
            <HeadSub fixed>開始</HeadSub>
            <HeadSub fixed>終了</HeadSub>
            <HeadSub fixed>休憩</HeadSub>
            <HeadSub borderL fixed>開始</HeadSub>
            <HeadSub fixed>終了</HeadSub>
            <HeadSub fixed>時間</HeadSub>
            <HeadSub borderL>内容・会場</HeadSub>
            <HeadSub fixed>開始</HeadSub>
            <HeadSub fixed>終了</HeadSub>
            <HeadSub fixed>休憩</HeadSub>
            <HeadSub fixed>時間</HeadSub>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200 dark:divide-gray-700 bg-white dark:bg-gray-800">
          {rows.map(({ work: w, rv }, index) => {
            const isHelp = w.classroomId !== tutor.classroomId;
            return (
              <tr
                key={w.id}
                className={`h-11 cursor-pointer transition-colors ${
                  index % 2 === 0
                    ? "bg-white dark:bg-gray-800"
                    : "bg-gray-50 dark:bg-gray-900/60"
                } hover:bg-indigo-50 dark:hover:bg-gray-700`}
                onClick={() => setDetailWork(w)}
              >
                <td className={`${CELL_BASE} font-medium`}>{formatDateOnly(w.workingDate)}</td>
                <td className={CELL_BASE}>{weekdayOf(w.workingDate)}</td>
                <Cell value={rv.periodCodes || null} />
                <Cell value={rv.I > 0 ? String(rv.I) : null} fixed />
                <Cell value={isHelp && w.classroomName ? w.classroomName : null} />
                <TimeCell frac={rv.N} />
                <TimeCell frac={rv.P} />
                <TimeCell frac={rv.R} />
                <TimeCell frac={rv.T} />
                <TimeCell frac={rv.V} />
                <TimeCell frac={rv.X} />
                <AllowanceCell amount={w.dailyAllowance} />
                <AllowanceCell amount={w.transportationFee} />
                <td
                  className="px-3 py-0 align-middle text-center max-w-40 truncate"
                  title={w.otherWorkDetail.description ?? undefined}
                >
                  {w.otherWorkDetail.description ? (
                    w.otherWorkDetail.description
                  ) : (
                    <span className="text-gray-400 dark:text-gray-500">-</span>
                  )}
                </td>
                <TimeCell frac={rv.AH} />
                <TimeCell frac={rv.AJ} />
                <TimeCell frac={rv.AL} />
                <TimeCell frac={rv.AN} />
                <TimeCell frac={rv.AP} />
                <TimeCell frac={rv.AR} />
                <TimeCell frac={rv.AT} />
              </tr>
            );
          })}
        </tbody>
      </table>

      {detailWork && (
        <WorkDetailModal
          work={detailWork}
          onClose={() => setDetailWork(null)}
          onEdit={() => router.push(`/manager/works/${tutor.id}/${detailWork.id}/edit`)}
          onDelete={() => {
            deleteConfirm.confirm(detailWork);
            setDetailWork(null);
          }}
          onCopy={() => {
            setCopyWork(detailWork);
            setDetailWork(null);
          }}
        />
      )}

      {copyWork && (
        <CopyWorkModal
          work={copyWork}
          tutorId={tutor.id}
          onClose={() => setCopyWork(null)}
          onCopied={() => {
            setCopyWork(null);
            loadWorks();
          }}
        />
      )}

      <ConfirmDialog
        isOpen={!!deleteConfirm.target}
        title="勤務情報を削除しますか？"
        message={`${formatDateOnly(deleteConfirm.target?.workingDate ?? "")} の勤務情報を削除します。`}
        isLoading={deleteConfirm.isLoading}
        onConfirm={handleDelete}
        onCancel={deleteConfirm.cancel}
      />
    </div>
  );
}
