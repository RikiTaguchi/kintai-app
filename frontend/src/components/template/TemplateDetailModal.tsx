"use client";

import { TemplateResponse, PERIOD_CODES } from "@/types";
import Button from "@/components/ui/Button";
import { formatTime } from "@/lib/utils";

interface Props {
  template: TemplateResponse;
  onClose: () => void;
  onEdit?: () => void;
  onDelete?: () => void;
}

export default function TemplateDetailModal({ template, onClose, onEdit, onDelete }: Props) {
  const lessonPeriodLabels = template.lessonTemplateDetail.periodCodes.map((code) => {
    const found = PERIOD_CODES.find((p) => p.code === code);
    return found ? `${code}（${found.label}）` : code;
  });

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />
      <div className="relative bg-white dark:bg-gray-800 rounded-2xl shadow-xl w-full max-w-lg max-h-[85vh] overflow-y-auto">
        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-200 dark:border-gray-700">
          <h2 className="text-base font-semibold text-gray-900 dark:text-gray-100">テンプレート詳細</h2>
          <button onClick={onClose} className="text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-400 cursor-pointer">
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div className="p-5 space-y-4">
          <div className="grid grid-cols-3 gap-3 text-sm">
            <div className="col-span-2">
              <p className="text-gray-500 dark:text-gray-400 text-xs mb-0.5">テンプレート名</p>
              <p className="font-medium">{template.title}</p>
            </div>
            <div>
              <p className="text-gray-500 dark:text-gray-400 text-xs mb-0.5">交通費</p>
              <p className="font-medium">¥{template.transportationFee.toLocaleString()}</p>
            </div>
          </div>

          <section className="bg-teal-50 dark:bg-teal-900/40 rounded-xl p-4">
            <h3 className="text-sm font-semibold text-teal-800 dark:text-teal-300 mb-3">授業業務</h3>
            <div className="grid grid-cols-3 gap-2 text-sm mb-2">
              <div>
                <p className="text-xs text-gray-500 dark:text-gray-400">開始</p>
                <p>{formatTime(template.lessonTemplateDetail.startTime)}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 dark:text-gray-400">終了</p>
                <p>{formatTime(template.lessonTemplateDetail.endTime)}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 dark:text-gray-400">休憩</p>
                <p>{template.lessonTemplateDetail.breakMinutes != null ? `${template.lessonTemplateDetail.breakMinutes}分` : "—"}</p>
              </div>
            </div>
            {lessonPeriodLabels.length > 0 && (
              <div>
                <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">担当コマ</p>
                <div className="flex flex-wrap gap-1">
                  {lessonPeriodLabels.map((label) => (
                    <span key={label} className="bg-teal-600 text-white text-xs px-2 py-0.5 rounded-md">{label}</span>
                  ))}
                </div>
              </div>
            )}
          </section>

          {template.officeTemplateDetail.startTime && (
            <section className="bg-teal-50 dark:bg-teal-900/40 rounded-xl p-4">
              <h3 className="text-sm font-semibold text-teal-800 dark:text-teal-300 mb-3">事務業務</h3>
              <div className="grid grid-cols-3 gap-2 text-sm">
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">開始</p>
                  <p>{formatTime(template.officeTemplateDetail.startTime)}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">終了</p>
                  <p>{formatTime(template.officeTemplateDetail.endTime)}</p>
                </div>
                <div />
              </div>
            </section>
          )}

          {template.otherTemplateDetail.startTime && (
            <section className="bg-teal-50 dark:bg-teal-900/40 rounded-xl p-4">
              <h3 className="text-sm font-semibold text-teal-800 dark:text-teal-300 mb-3">その他業務</h3>
              <div className="grid grid-cols-3 gap-2 text-sm mb-2">
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">開始</p>
                  <p>{formatTime(template.otherTemplateDetail.startTime)}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">終了</p>
                  <p>{formatTime(template.otherTemplateDetail.endTime)}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">休憩</p>
                  <p>{template.otherTemplateDetail.breakMinutes != null ? `${template.otherTemplateDetail.breakMinutes}分` : "—"}</p>
                </div>
              </div>
              {template.otherTemplateDetail.description && (
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">業務内容</p>
                  <p className="text-sm">{template.otherTemplateDetail.description}</p>
                </div>
              )}
            </section>
          )}
        </div>

        <div className="px-5 pb-5 space-y-2">
          {onEdit && (
            <Button variant="secondary" className="w-full justify-center!" onClick={onEdit}>編集</Button>
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
      </div>
    </div>
  );
}
