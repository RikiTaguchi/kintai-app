"use client";

import { WorkResponse, PERIOD_CODES } from "@/types";
import Button from "@/components/ui/Button";
import { formatDate, formatTime } from "@/lib/utils";

interface Props {
  work: WorkResponse;
  onClose: () => void;
  colorScheme?: "indigo" | "teal";
  showClassroom?: boolean;
  onEdit?: () => void;
  onDelete?: () => void;
  onCopy?: () => void;
}

export default function WorkDetailModal({
  work,
  onClose,
  colorScheme = "indigo",
  showClassroom = true,
  onEdit,
  onDelete,
  onCopy,
}: Props) {
  const isTeal = colorScheme === "teal";
  const c = isTeal
    ? { bg: "bg-teal-50", heading: "text-teal-800", badge: "bg-teal-600" }
    : { bg: "bg-indigo-50", heading: "text-indigo-800", badge: "bg-indigo-600" };
  const px = isTeal ? "px-5" : "px-6";
  const p = isTeal ? "p-5" : "p-6";
  const maxH = isTeal ? "max-h-[85vh]" : "max-h-[90vh]";
  const titleSize = isTeal ? "text-base" : "text-lg";

  const lessonPeriodLabels = (work.lessonWorkDetail?.periodCodes ?? []).map((code) => {
    const found = PERIOD_CODES.find((p) => p.code === code);
    return found ? `${code}（${found.label}）` : code;
  });

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />
      <div className={`relative bg-white dark:bg-gray-800 rounded-2xl shadow-xl w-full max-w-lg ${maxH} overflow-y-auto`}>
        <div className={`flex items-center justify-between ${px} py-4 border-b border-gray-200 dark:border-gray-700`}>
          <h2 className={`${titleSize} font-semibold text-gray-900 dark:text-gray-100`}>勤務詳細</h2>
          <button onClick={onClose} className="text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-400 cursor-pointer">
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div className={`${p} space-y-4`}>
          {showClassroom ? (
            <div className="grid grid-cols-3 gap-3 text-sm">
              <div>
                <p className="text-gray-500 dark:text-gray-400 text-xs mb-0.5">勤務日</p>
                <p className="font-medium">{ formatDate(work.workingDate)}</p>
              </div>
              <div>
                <p className="text-gray-500 dark:text-gray-400 text-xs mb-0.5">勤務教室</p>
                <p className="font-medium">{work.classroomName}</p>
              </div>
              <div>
                <p className="text-gray-500 dark:text-gray-400 text-xs mb-0.5">交通費</p>
                <p className="font-medium">¥{work.transportationFee.toLocaleString()}</p>
              </div>
            </div>
          ) : (
            <div className="grid grid-cols-3 gap-3 text-sm">
              <div className="col-span-2">
                <p className="text-gray-500 dark:text-gray-400 text-xs mb-0.5">勤務日</p>
                <p className="font-medium text-xs">{formatDate(work.workingDate)}</p>
              </div>
              <div>
                <p className="text-gray-500 dark:text-gray-400 text-xs mb-0.5">交通費</p>
                <p className="font-medium">¥{work.transportationFee.toLocaleString()}</p>
              </div>
            </div>
          )}

          {work.lessonWorkDetail && (
            <section className={`${isTeal ? "bg-teal-50 dark:bg-teal-900/40" : "bg-indigo-50 dark:bg-indigo-900/40"} rounded-xl p-4`}>
              <h3 className={`text-sm font-semibold ${isTeal ? "text-teal-800 dark:text-teal-300" : "text-indigo-800 dark:text-indigo-300"} mb-3`}>授業業務</h3>
              <div className="grid grid-cols-3 gap-2 text-sm mb-2">
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">開始</p>
                  <p>{formatTime(work.lessonWorkDetail.startTime)}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">終了</p>
                  <p>{formatTime(work.lessonWorkDetail.endTime)}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">休憩</p>
                  <p>{work.lessonWorkDetail.breakMinutes != null ? `${work.lessonWorkDetail.breakMinutes}分` : "—"}</p>
                </div>
              </div>
              {lessonPeriodLabels.length > 0 && (
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">担当コマ</p>
                  <div className="flex flex-wrap gap-1">
                    {lessonPeriodLabels.map((label) => (
                      <span key={label} className={`${c.badge} text-white text-xs px-2 py-0.5 rounded-md`}>{label}</span>
                    ))}
                  </div>
                </div>
              )}
            </section>
          )}

          {work.officeWorkDetail.startTime && (
            <section className={`${isTeal ? "bg-teal-50 dark:bg-teal-900/40" : "bg-indigo-50 dark:bg-indigo-900/40"} rounded-xl p-4`}>
              <h3 className={`text-sm font-semibold ${isTeal ? "text-teal-800 dark:text-teal-300" : "text-indigo-800 dark:text-indigo-300"} mb-3`}>事務業務</h3>
              <div className="grid grid-cols-3 gap-2 text-sm">
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">開始</p>
                  <p>{formatTime(work.officeWorkDetail.startTime)}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">終了</p>
                  <p>{formatTime(work.officeWorkDetail.endTime)}</p>
                </div>
                <div />
              </div>
            </section>
          )}

          {work.otherWorkDetail.startTime && (
            <section className={`${isTeal ? "bg-teal-50 dark:bg-teal-900/40" : "bg-indigo-50 dark:bg-indigo-900/40"} rounded-xl p-4`}>
              <h3 className={`text-sm font-semibold ${isTeal ? "text-teal-800 dark:text-teal-300" : "text-indigo-800 dark:text-indigo-300"} mb-3`}>その他業務</h3>
              <div className="grid grid-cols-3 gap-2 text-sm mb-2">
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">開始</p>
                  <p>{formatTime(work.otherWorkDetail.startTime)}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">終了</p>
                  <p>{formatTime(work.otherWorkDetail.endTime)}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">休憩</p>
                  <p>{work.otherWorkDetail.breakMinutes != null ? `${work.otherWorkDetail.breakMinutes}分` : "—"}</p>
                </div>
              </div>
              {work.otherWorkDetail.description && (
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">業務内容</p>
                  <p className="text-sm">{work.otherWorkDetail.description}</p>
                </div>
              )}
            </section>
          )}
        </div>

        {isTeal ? (
          <div className={`${px} pb-5 space-y-2`}>
            {(onEdit || onCopy) && (
              <div className={`grid gap-2 ${onEdit && onCopy ? "grid-cols-2" : "grid-cols-1"}`}>
                {onEdit && (
                  <Button variant="secondary" className="justify-center!" onClick={onEdit}>編集</Button>
                )}
                {onCopy && (
                  <Button variant="secondary" className="justify-center!" onClick={onCopy}>他の日付にコピー</Button>
                )}
              </div>
            )}
            <div className="flex items-center justify-between">
              {onDelete ? (
                <Button variant="ghost" className="text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/40" onClick={onDelete}>
                  削除
                </Button>
              ) : <div />}
              <Button variant="secondary" onClick={onClose}>閉じる</Button>
            </div>
          </div>
        ) : (
          <div className={`${px} pb-5 flex items-center gap-2`}>
            {onDelete && (
              <Button variant="ghost" className="text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/40 mr-auto" onClick={onDelete}>
                削除
              </Button>
            )}
            {onCopy && (
              <Button variant="secondary" size="sm" onClick={onCopy}>
                他の日付にコピー
              </Button>
            )}
            <div className="ml-auto flex gap-2">
              {onEdit && (
                <Button variant="secondary" onClick={onEdit}>編集</Button>
              )}
              <Button variant="secondary" onClick={onClose}>閉じる</Button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
