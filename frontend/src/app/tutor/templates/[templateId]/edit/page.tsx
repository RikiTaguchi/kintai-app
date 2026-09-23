"use client";

import { useState, FormEvent, useEffect, use } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { getTemplate, editTemplate, getClassrooms } from "@/lib/api";
import { ClassroomResponse, PeriodCode } from "@/types";
import WorkDetailFields, {
  EMPTY_OFFICE,
  WorkDetailState,
  toApiTime,
  fromApiTime,
} from "@/components/work/WorkDetailFields";
import { inputClass } from "@/components/ui/FormField";
import Button from "@/components/ui/Button";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import { useToast } from "@/context/ToastContext";
import { getErrorMessage } from "@/lib/utils";

export default function TemplateEditPage({
  params,
}: {
  params: Promise<{ templateId: string }>;
}) {
  const { templateId } = use(params);
  const { user } = useAuth();
  const router = useRouter();
  const { addToast } = useToast();

  const [title, setTitle] = useState("");
  const [classroomId, setClassroomId] = useState("");
  const [transportationFee, setTransportationFee] = useState("0");
  const [classrooms, setClassrooms] = useState<ClassroomResponse[]>([]);
  const [workDetail, setWorkDetail] = useState<WorkDetailState>({
    lesson: { startTime: "", endTime: "", breakMinutes: "", periodCodes: [] },
    showOffice: false,
    office: EMPTY_OFFICE,
    showOther: false,
    other: { startTime: "", endTime: "", breakMinutes: "", description: "" },
  });
  const [isLoading, setIsLoading] = useState(true);
  const [loadFailed, setLoadFailed] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (!user) return;
    Promise.all([getTemplate(user.id, templateId), getClassrooms()])
      .then(([t, cls]) => {
        setClassrooms(cls);
        setTitle(t.title);
        setClassroomId(t.classroomId);
        setTransportationFee(String(t.transportationFee));
        setWorkDetail({
          lesson: {
            startTime: fromApiTime(t.lessonTemplateDetail.startTime),
            endTime: fromApiTime(t.lessonTemplateDetail.endTime),
            breakMinutes: t.lessonTemplateDetail.breakMinutes != null ? String(t.lessonTemplateDetail.breakMinutes) : "0",
            periodCodes: (t.lessonTemplateDetail.periodCodes ?? []) as PeriodCode[],
          },
          showOffice: !!t.officeTemplateDetail.startTime,
          office: t.officeTemplateDetail.startTime
            ? { startTime: fromApiTime(t.officeTemplateDetail.startTime), endTime: fromApiTime(t.officeTemplateDetail.endTime) }
            : EMPTY_OFFICE,
          showOther: !!t.otherTemplateDetail.startTime,
          other: t.otherTemplateDetail.startTime
            ? {
                startTime: fromApiTime(t.otherTemplateDetail.startTime),
                endTime: fromApiTime(t.otherTemplateDetail.endTime),
                breakMinutes: t.otherTemplateDetail.breakMinutes != null ? String(t.otherTemplateDetail.breakMinutes) : "0",
                description: t.otherTemplateDetail.description ?? "",
              }
            : { startTime: "", endTime: "", breakMinutes: "", description: "" },
        });
      })
      .catch((e) => { addToast(getErrorMessage(e), "error"); setLoadFailed(true); })
      .finally(() => setIsLoading(false));
  }, [user, templateId, addToast]);

  if (isLoading) return <div className="py-4"><LoadingSpinner /></div>;
  if (loadFailed) return (
    <div className="py-4">
      <button className="text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-400 cursor-pointer text-sm mb-4" onClick={() => router.back()}>← 戻る</button>
      <p className="text-sm text-gray-500 dark:text-gray-400">データを読み込めませんでした。</p>
    </div>
  );

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!user) return;
    setIsSaving(true);
    const { lesson, showOffice, office, showOther, other } = workDetail;
    try {
      await editTemplate(user.id, templateId, {
        id: templateId,
        tutorId: user.id,
        title,
        classroomId,
        transportationFee: Number(transportationFee),
        lessonTemplateDetail: {
          startTime: toApiTime(lesson.startTime),
          endTime: toApiTime(lesson.endTime),
          breakMinutes: lesson.breakMinutes ? Number(lesson.breakMinutes) : null,
          periodCodes: lesson.periodCodes,
        },
        officeTemplateDetail: {
          startTime: showOffice ? toApiTime(office.startTime) : null,
          endTime: showOffice ? toApiTime(office.endTime) : null,
        },
        otherTemplateDetail: {
          startTime: showOther ? toApiTime(other.startTime) : null,
          endTime: showOther ? toApiTime(other.endTime) : null,
          breakMinutes: showOther && other.breakMinutes ? Number(other.breakMinutes) : null,
          description: showOther && other.description ? other.description : null,
        },
      });
      addToast("テンプレートを更新しました", "success");
      router.replace("/tutor/templates");
    } catch (err) {
      addToast(getErrorMessage(err, "更新に失敗しました"), "error");
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="py-4">
      <h1 className="text-xl font-bold text-gray-900 dark:text-gray-100 mb-4">テンプレートを編集</h1>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-4 space-y-4">
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700 dark:text-gray-300">テンプレート名 <span className="text-red-500">*</span></label>
            <input type="text" value={title} onChange={(e) => setTitle(e.target.value)} required maxLength={100} placeholder="例: 通常授業" className={inputClass} />
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
            <input type="number" min="0" step="1" value={transportationFee} onChange={(e) => setTransportationFee(e.target.value)} required placeholder="0" className={inputClass} />
          </div>
        </div>

        <WorkDetailFields value={workDetail} onChange={setWorkDetail} colorScheme="teal" />

        <div className="flex gap-3">
          <Button type="submit" isLoading={isSaving} className="flex-1 justify-center! bg-teal-600! hover:bg-teal-700! focus:ring-teal-500!">保存</Button>
          <Button type="button" variant="secondary" onClick={() => router.back()}>戻る</Button>
        </div>
      </form>
    </div>
  );
}
