"use client";

import { PERIOD_CODES, PeriodCode } from "@/types";
import { inputClass } from "@/components/ui/FormField";

export interface LessonDetailForm {
  startTime: string;
  endTime: string;
  breakMinutes: string;
  periodCodes: PeriodCode[];
}

export interface OfficeDetailForm {
  startTime: string;
  endTime: string;
}

export interface OtherDetailForm {
  startTime: string;
  endTime: string;
  breakMinutes: string;
  description: string;
}

export interface WorkDetailState {
  lesson: LessonDetailForm;
  showOffice: boolean;
  office: OfficeDetailForm;
  showOther: boolean;
  other: OtherDetailForm;
}

export const EMPTY_LESSON: LessonDetailForm = {
  startTime: "",
  endTime: "",
  breakMinutes: "0",
  periodCodes: [],
};

export const EMPTY_OFFICE: OfficeDetailForm = { startTime: "", endTime: "" };

export const EMPTY_OTHER: OtherDetailForm = {
  startTime: "",
  endTime: "",
  breakMinutes: "",
  description: "",
};

export const EMPTY_WORK_DETAIL: WorkDetailState = {
  lesson: EMPTY_LESSON,
  showOffice: false,
  office: EMPTY_OFFICE,
  showOther: false,
  other: EMPTY_OTHER,
};

/** "HH:mm" → "HH:mm:ss" (nullを送るためのヘルパー) */
export function toApiTime(v: string): string | null {
  return v ? `${v}:00` : null;
}

/** "HH:mm:ss" → "HH:mm" */
export function fromApiTime(v: string | null): string {
  if (!v) return "";
  return v.substring(0, 5);
}

interface Props {
  value: WorkDetailState;
  onChange: (v: WorkDetailState) => void;
  colorScheme?: "indigo" | "teal";
}

const COLOR_MAP = {
  teal: {
    sectionBg: "bg-teal-50 border-teal-100",
    title: "text-teal-800",
    deleteBtn: "text-teal-600 hover:text-teal-800",
    addBtn: "hover:border-teal-400 hover:text-teal-600",
    periodSelected: "bg-teal-600 text-white border-teal-600",
    periodHover: "hover:border-teal-400",
    periodSubSel: "text-teal-100",
  },
  indigo: {
    sectionBg: "bg-indigo-50 border-indigo-100",
    title: "text-indigo-800",
    deleteBtn: "text-indigo-600 hover:text-indigo-800",
    addBtn: "hover:border-indigo-400 hover:text-indigo-600",
    periodSelected: "bg-indigo-600 text-white border-indigo-600",
    periodHover: "hover:border-indigo-400",
    periodSubSel: "text-indigo-100",
  },
} as const;

function isTimeInvalid(start: string, end: string): boolean {
  return !!start && !!end && end <= start;
}

function handleBreakMinutesChange(
  val: string,
  setter: (v: string) => void
) {
  if (val !== "" && Number(val) < 0) return;
  setter(val);
}

export default function WorkDetailFields({
  value,
  onChange,
  colorScheme = "indigo",
}: Props) {
  const { lesson, showOffice, office, showOther, other } = value;

  const setLesson = (v: LessonDetailForm) => onChange({ ...value, lesson: v });
  const setShowOffice = (v: boolean) => onChange({ ...value, showOffice: v });
  const setOffice = (v: OfficeDetailForm) => onChange({ ...value, office: v });
  const setShowOther = (v: boolean) => onChange({ ...value, showOther: v });
  const setOther = (v: OtherDetailForm) => onChange({ ...value, other: v });

  const c = COLOR_MAP[colorScheme];

  const togglePeriod = (code: PeriodCode) => {
    const next = lesson.periodCodes.includes(code)
      ? lesson.periodCodes.filter((c) => c !== code)
      : [...lesson.periodCodes, code];
    if (next.length < 3) {
      setLesson({ ...lesson, periodCodes: next, startTime: "", endTime: "", breakMinutes: "0" });
    } else {
      setLesson({ ...lesson, periodCodes: next });
    }
  };

  return (
    <div className="space-y-4">
      {/* ── 授業業務 ── */}
      <section className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 overflow-hidden">
        <div className={`px-4 py-3 border-b ${c.sectionBg} ${colorScheme === "teal" ? "dark:bg-teal-900/40 dark:border-teal-800" : "dark:bg-indigo-900/40 dark:border-indigo-800"}`}>
          <h3 className={`text-sm font-semibold ${c.title} ${colorScheme === "teal" ? "dark:text-teal-300" : "dark:text-indigo-300"}`}>授業業務</h3>
        </div>
        <div className="p-4 space-y-4">
          <div className="flex flex-col gap-2">
            <label className="text-xs font-medium text-gray-600 dark:text-gray-400">担当コマ</label>
            <div className="grid grid-cols-3 gap-2">
              {PERIOD_CODES.map(({ code, label }) => {
                const selected = lesson.periodCodes.includes(code);
                return (
                  <button
                    key={code}
                    type="button"
                    onClick={() => togglePeriod(code)}
                    className={`flex flex-col items-center px-3 py-1.5 rounded-lg border text-xs font-medium transition-colors cursor-pointer ${
                      selected
                        ? c.periodSelected
                        : `bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 border-gray-300 dark:border-gray-600 ${c.periodHover}`
                    }`}
                  >
                    <span className="text-sm font-bold">{code}</span>
                    <span className={selected ? c.periodSubSel : "text-gray-400 dark:text-gray-500"}>
                      {label}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {lesson.periodCodes.length >= 3 && (
            <>
              <div className="grid grid-cols-1 gap-2 sm:grid-cols-2 sm:gap-3">
                <div className="flex flex-col gap-1">
                  <label className="text-xs font-medium text-gray-600 dark:text-gray-400">開始時刻<span className="text-red-500"> *</span></label>
                  <input
                    type="time"
                    value={lesson.startTime}
                    onChange={(e) => setLesson({ ...lesson, startTime: e.target.value })}
                    required
                    className={inputClass}
                  />
                </div>
                <div className="flex flex-col gap-1">
                  <label className="text-xs font-medium text-gray-600 dark:text-gray-400">終了時刻<span className="text-red-500"> *</span></label>
                  <input
                    type="time"
                    value={lesson.endTime}
                    onChange={(e) => setLesson({ ...lesson, endTime: e.target.value })}
                    required
                    className={inputClass}
                  />
                </div>
              </div>
              {isTimeInvalid(lesson.startTime, lesson.endTime) && (
                <p className="text-xs text-red-500 -mt-2">終了時刻は開始時刻より後に設定してください</p>
              )}
              <div className="flex flex-col gap-1">
                <label className="text-xs font-medium text-gray-600 dark:text-gray-400">休憩時間（分）<span className="text-red-500"> *</span></label>
                <input
                  type="number"
                  min="0"
                  step="1"
                  value={lesson.breakMinutes}
                  onChange={(e) => handleBreakMinutesChange(e.target.value, (v) => setLesson({ ...lesson, breakMinutes: v }))}
                  required
                  placeholder="例: 10"
                  className={inputClass}
                />
              </div>
            </>
          )}
        </div>
      </section>

      {/* ── 事務業務 ── */}
      {showOffice ? (
        <section className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 overflow-hidden">
          <div className={`px-4 py-3 border-b ${c.sectionBg} ${colorScheme === "teal" ? "dark:bg-teal-900/40 dark:border-teal-800" : "dark:bg-indigo-900/40 dark:border-indigo-800"} flex items-center justify-between`}>
            <h3 className={`text-sm font-semibold ${c.title} ${colorScheme === "teal" ? "dark:text-teal-300" : "dark:text-indigo-300"}`}>事務業務</h3>
            <button
              type="button"
              onClick={() => onChange({ ...value, showOffice: false, office: EMPTY_OFFICE })}
              className={`text-xs cursor-pointer ${c.deleteBtn} ${colorScheme === "teal" ? "dark:text-teal-400 dark:hover:text-teal-300" : "dark:text-indigo-400 dark:hover:text-indigo-300"}`}
            >
              削除
            </button>
          </div>
          <div className="p-4">
            <div className="grid grid-cols-1 gap-2 sm:grid-cols-2 sm:gap-3">
              <div className="flex flex-col gap-1">
                <label className="text-xs font-medium text-gray-600 dark:text-gray-400">開始時刻<span className="text-red-500"> *</span></label>
                <input
                  type="time"
                  value={office.startTime}
                  onChange={(e) => setOffice({ ...office, startTime: e.target.value })}
                  required
                  className={inputClass}
                />
              </div>
              <div className="flex flex-col gap-1">
                <label className="text-xs font-medium text-gray-600 dark:text-gray-400">終了時刻<span className="text-red-500"> *</span></label>
                <input
                  type="time"
                  value={office.endTime}
                  onChange={(e) => setOffice({ ...office, endTime: e.target.value })}
                  required
                  className={inputClass}
                />
              </div>
            </div>
            {isTimeInvalid(office.startTime, office.endTime) && (
              <p className="text-xs text-red-500 mt-2">終了時刻は開始時刻より後に設定してください</p>
            )}
          </div>
        </section>
      ) : (
        <button
          type="button"
          onClick={() => setShowOffice(true)}
          className={`w-full py-3 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-xl text-sm text-gray-500 dark:text-gray-400 transition-colors cursor-pointer flex items-center justify-center gap-2 ${c.addBtn} ${colorScheme === "teal" ? "dark:hover:border-teal-400 dark:hover:text-teal-400" : "dark:hover:border-indigo-400 dark:hover:text-indigo-400"}`}
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          事務業務を追加
        </button>
      )}

      {/* ── その他業務 ── */}
      {showOther ? (
        <section className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 overflow-hidden">
          <div className={`px-4 py-3 border-b ${c.sectionBg} ${colorScheme === "teal" ? "dark:bg-teal-900/40 dark:border-teal-800" : "dark:bg-indigo-900/40 dark:border-indigo-800"} flex items-center justify-between`}>
            <h3 className={`text-sm font-semibold ${c.title} ${colorScheme === "teal" ? "dark:text-teal-300" : "dark:text-indigo-300"}`}>その他業務</h3>
            <button
              type="button"
              onClick={() => onChange({ ...value, showOther: false, other: EMPTY_OTHER })}
              className={`text-xs cursor-pointer ${c.deleteBtn} ${colorScheme === "teal" ? "dark:text-teal-400 dark:hover:text-teal-300" : "dark:text-indigo-400 dark:hover:text-indigo-300"}`}
            >
              削除
            </button>
          </div>
          <div className="p-4 space-y-4">
            <div className="grid grid-cols-1 gap-2 sm:grid-cols-2 sm:gap-3">
              <div className="flex flex-col gap-1">
                <label className="text-xs font-medium text-gray-600 dark:text-gray-400">開始時刻<span className="text-red-500"> *</span></label>
                <input
                  type="time"
                  value={other.startTime}
                  onChange={(e) => setOther({ ...other, startTime: e.target.value })}
                  required
                  className={inputClass}
                />
              </div>
              <div className="flex flex-col gap-1">
                <label className="text-xs font-medium text-gray-600 dark:text-gray-400">終了時刻<span className="text-red-500"> *</span></label>
                <input
                  type="time"
                  value={other.endTime}
                  onChange={(e) => setOther({ ...other, endTime: e.target.value })}
                  required
                  className={inputClass}
                />
              </div>
            </div>
            {isTimeInvalid(other.startTime, other.endTime) && (
              <p className="text-xs text-red-500 -mt-2">終了時刻は開始時刻より後に設定してください</p>
            )}
            <div className="flex flex-col gap-1">
              <label className="text-xs font-medium text-gray-600 dark:text-gray-400">休憩時間（分）<span className="text-red-500"> *</span></label>
              <input
                type="number"
                min="0"
                step="1"
                value={other.breakMinutes}
                onChange={(e) => handleBreakMinutesChange(e.target.value, (v) => setOther({ ...other, breakMinutes: v }))}
                required
                placeholder="例: 10"
                className={inputClass}
              />
            </div>
            <div className="flex flex-col gap-1">
              <label className="text-xs font-medium text-gray-600 dark:text-gray-400">業務内容<span className="text-red-500"> *</span></label>
              <textarea
                value={other.description}
                onChange={(e) => setOther({ ...other, description: e.target.value })}
                placeholder="業務内容を入力"
                rows={3}
                required
                maxLength={500}
                className={`${inputClass} resize-none`}
              />
            </div>
          </div>
        </section>
      ) : (
        <button
          type="button"
          onClick={() => onChange({ ...value, showOther: true, other: { ...EMPTY_OTHER, breakMinutes: "0" } })}
          className={`w-full py-3 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-xl text-sm text-gray-500 dark:text-gray-400 transition-colors cursor-pointer flex items-center justify-center gap-2 ${c.addBtn} ${colorScheme === "teal" ? "dark:hover:border-teal-400 dark:hover:text-teal-400" : "dark:hover:border-indigo-400 dark:hover:text-indigo-400"}`}
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          その他業務を追加
        </button>
      )}
    </div>
  );
}
