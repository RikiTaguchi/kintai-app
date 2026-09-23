"use client";

import { useState, FormEvent, useEffect, use } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { getWork, editWork, getClassrooms } from "@/lib/api";
import { ClassroomResponse, PeriodCode } from "@/types";
import WorkDetailFields, {
  WorkDetailState,
  EMPTY_OFFICE,
  toApiTime,
  fromApiTime,
} from "@/components/work/WorkDetailFields";
import { inputClass } from "@/components/ui/FormField";
import Button from "@/components/ui/Button";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import { useToast } from "@/context/ToastContext";
import { getPeriodFromDate, getErrorMessage } from "@/lib/utils";

export default function TutorWorkEditPage({
  params,
}: {
  params: Promise<{ workId: string }>;
}) {
  const { workId } = use(params);
  const { user } = useAuth();
  const router = useRouter();
  const { addToast } = useToast();

  const [workingDate, setWorkingDate] = useState("");
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
    Promise.all([getWork(user.id, workId), getClassrooms()])
      .then(([w, cls]) => {
        setClassrooms(cls);
        setWorkingDate(w.workingDate);
        setClassroomId(w.classroomId);
        setTransportationFee(String(w.transportationFee));
        setWorkDetail({
          lesson: {
            startTime: fromApiTime(w.lessonWorkDetail.startTime),
            endTime: fromApiTime(w.lessonWorkDetail.endTime),
            breakMinutes: w.lessonWorkDetail.breakMinutes != null ? String(w.lessonWorkDetail.breakMinutes) : "0",
            periodCodes: (w.lessonWorkDetail.periodCodes ?? []) as PeriodCode[],
          },
          showOffice: !!w.officeWorkDetail.startTime,
          office: w.officeWorkDetail.startTime
            ? { startTime: fromApiTime(w.officeWorkDetail.startTime), endTime: fromApiTime(w.officeWorkDetail.endTime) }
            : EMPTY_OFFICE,
          showOther: !!w.otherWorkDetail.startTime,
          other: w.otherWorkDetail.startTime
            ? {
                startTime: fromApiTime(w.otherWorkDetail.startTime),
                endTime: fromApiTime(w.otherWorkDetail.endTime),
                breakMinutes: w.otherWorkDetail.breakMinutes != null ? String(w.otherWorkDetail.breakMinutes) : "0",
                description: w.otherWorkDetail.description ?? "",
              }
            : { startTime: "", endTime: "", breakMinutes: "", description: "" },
        });
      })
      .catch((e) => { addToast(getErrorMessage(e), "error"); setLoadFailed(true); })
      .finally(() => setIsLoading(false));
  }, [user, workId, addToast]);

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
      await editWork(user.id, workId, {
        id: workId,
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
      addToast("勤務情報を更新しました", "success");
      const { year, month } = getPeriodFromDate(workingDate);
      router.replace(`/tutor/works?year=${year}&month=${month}`);
    } catch (err) {
      addToast(getErrorMessage(err, "更新に失敗しました"), "error");
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="py-4">
      <h1 className="text-xl font-bold text-gray-900 dark:text-gray-100 mb-4">勤務を編集</h1>

      <form onSubmit={handleSubmit} className="space-y-4">
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
          <Button type="submit" isLoading={isSaving} className="flex-1 justify-center! bg-teal-600! hover:bg-teal-700! focus:ring-teal-500!">保存</Button>
          <Button type="button" variant="secondary" onClick={() => router.back()}>戻る</Button>
        </div>
      </form>
    </div>
  );
}
