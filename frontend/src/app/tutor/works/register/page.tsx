"use client";

import { useState, FormEvent, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { registerWork, getClassrooms, getTemplates, getSalaries } from "@/lib/api";
import { ClassroomResponse, TemplateResponse } from "@/types";
import WorkDetailFields, {
  EMPTY_WORK_DETAIL,
  WorkDetailState,
  toApiTime,
} from "@/components/work/WorkDetailFields";
import { inputClass } from "@/components/ui/FormField";
import Button from "@/components/ui/Button";
import { useToast } from "@/context/ToastContext";
import { getApplicableFee, getPeriodFromDate, getErrorMessage } from "@/lib/utils";
import { applyTemplate } from "@/lib/applyTemplate";

export default function TutorWorkRegisterPage() {
  const { user } = useAuth();
  const router = useRouter();
  const { addToast } = useToast();

  const today = new Date().toISOString().slice(0, 10);
  const [workingDate, setWorkingDate] = useState(today);
  const [classroomId, setClassroomId] = useState("");
  const [transportationFee, setTransportationFee] = useState("");
  const [classrooms, setClassrooms] = useState<ClassroomResponse[]>([]);
  const [templates, setTemplates] = useState<TemplateResponse[]>([]);
  const [workDetail, setWorkDetail] = useState<WorkDetailState>(EMPTY_WORK_DETAIL);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!user) return;
    Promise.all([getClassrooms(), getTemplates(user.id), getSalaries(user.id)]).then(([cls, tmpl, salaries]) => {
      setClassrooms(cls);
      setTemplates(tmpl);
      if (user.classroomId) setClassroomId(user.classroomId);
      setTransportationFee(getApplicableFee(salaries, today));
    }).catch((e) => addToast(getErrorMessage(e), "error"));
  }, [user, addToast]);

  const applyTemplateTo = (templateId: string) => {
    const result = applyTemplate(templates, templateId);
    if (!result) return;
    setClassroomId(result.classroomId);
    setTransportationFee(result.transportationFee);
    setWorkDetail(result.workDetail);
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!user) return;
    setIsLoading(true);
    const { lesson, showOffice, office, showOther, other } = workDetail;
    try {
      await registerWork(user.id, {
        tutorId: user.id,
        classroomId,
        workingDate,
        transportationFee: Number(transportationFee),
        lessonWorkDetail: {
          startTime: toApiTime(lesson.startTime),
          endTime: toApiTime(lesson.endTime),
          breakMinutes: lesson.breakMinutes ? Number(lesson.breakMinutes) : null,
          periodCodes: lesson.periodCodes,
        },
        officeWorkDetail: {
          startTime: showOffice ? toApiTime(office.startTime) : null,
          endTime: showOffice ? toApiTime(office.endTime) : null,
        },
        otherWorkDetail: {
          startTime: showOther ? toApiTime(other.startTime) : null,
          endTime: showOther ? toApiTime(other.endTime) : null,
          breakMinutes: showOther && other.breakMinutes ? Number(other.breakMinutes) : null,
          description: showOther && other.description ? other.description : null,
        },
      });
      addToast("勤務情報を登録しました", "success");
      const { year, month } = getPeriodFromDate(workingDate);
      router.replace(`/tutor/works?year=${year}&month=${month}`);
    } catch (err) {
      addToast(getErrorMessage(err, "登録に失敗しました"), "error");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="py-4">
      <h1 className="text-xl font-bold text-gray-900 dark:text-gray-100 mb-4">勤務を登録</h1>

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* テンプレート適用 */}
        {templates.length > 0 && (
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-600 dark:text-gray-400">テンプレートから入力</label>
            <select
              defaultValue=""
              onChange={(e) => applyTemplateTo(e.target.value)}
              className={inputClass}
            >
              <option value="">テンプレートを選択（任意）</option>
              {templates.map((t) => (
                <option key={t.id} value={t.id}>{t.title}</option>
              ))}
            </select>
          </div>
        )}

        <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-4 space-y-4">
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700 dark:text-gray-300">勤務日 <span className="text-red-500">*</span></label>
            <input type="date" value={workingDate} onChange={(e) => setWorkingDate(e.target.value)} required className={inputClass} />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700 dark:text-gray-300">勤務教室 <span className="text-red-500">*</span></label>
            <select value={classroomId} onChange={(e) => setClassroomId(e.target.value)} required className={inputClass}>
              <option value="">勤務教室を選択</option>
              {classrooms.map((c) => (
                <option key={c.id} value={c.id}>{c.name}（{c.classroomNumber}）</option>
              ))}
            </select>
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700 dark:text-gray-300">交通費（円）<span className="text-red-500">*</span></label>
            <input type="number" min="0" step="1" value={transportationFee} onChange={(e) => setTransportationFee(e.target.value)} required placeholder="例: 500" className={inputClass} />
          </div>
        </div>

        <WorkDetailFields value={workDetail} onChange={setWorkDetail} colorScheme="teal" />

        <div className="flex gap-3">
          <Button type="submit" isLoading={isLoading} className="flex-1 justify-center! bg-teal-600! hover:bg-teal-700! focus:ring-teal-500!">登録</Button>
          <Button type="button" variant="secondary" onClick={() => router.back()}>戻る</Button>
        </div>
      </form>
    </div>
  );
}
