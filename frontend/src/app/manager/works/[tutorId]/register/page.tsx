"use client";

import { useState, FormEvent, useEffect, use } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { registerWork, getClassrooms, getSalaries, getTutors } from "@/lib/api";
import { ClassroomResponse, TutorResponse } from "@/types";
import WorkDetailFields, {
  EMPTY_WORK_DETAIL,
  WorkDetailState,
  toApiTime,
} from "@/components/work/WorkDetailFields";
import { inputClass } from "@/components/ui/FormField";
import Button from "@/components/ui/Button";
import { useToast } from "@/context/ToastContext";
import { getApplicableFee, getPeriodFromDate, getErrorMessage } from "@/lib/utils";

export default function ManagerWorkRegisterPage({
  params,
}: {
  params: Promise<{ tutorId: string }>;
}) {
  const { tutorId } = use(params);
  const { user } = useAuth();
  const router = useRouter();
  const { addToast } = useToast();

  const today = new Date().toISOString().slice(0, 10);
  const [workingDate, setWorkingDate] = useState(today);
  const [selectedTutorId, setSelectedTutorId] = useState(tutorId === "_" ? "" : tutorId);
  const [classroomId, setClassroomId] = useState("");
  const [transportationFee, setTransportationFee] = useState("");
  const [classrooms, setClassrooms] = useState<ClassroomResponse[]>([]);
  const [tutors, setTutors] = useState<TutorResponse[]>([]);
  const [workDetail, setWorkDetail] = useState<WorkDetailState>(EMPTY_WORK_DETAIL);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const salaryPromise = tutorId === "_" ? Promise.resolve([]) : getSalaries(tutorId);
    Promise.all([getClassrooms(), getTutors(), salaryPromise]).then(([cls, tutorList, salaries]) => {
      setClassrooms(cls);
      setTutors(tutorList);
      if (user?.classroomId) setClassroomId(user.classroomId);
      setTransportationFee(getApplicableFee(salaries, today));
    }).catch((e) => addToast(getErrorMessage(e), "error"));
  }, [user, addToast]);

  const handleTutorChange = async (newTutorId: string) => {
    setSelectedTutorId(newTutorId);
    if (!newTutorId) return;
    try {
      const salaries = await getSalaries(newTutorId);
      setTransportationFee(getApplicableFee(salaries, today));
    } catch (e) {
      addToast(getErrorMessage(e), "error");
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!selectedTutorId) return;
    setIsLoading(true);
    const { lesson, showOffice, office, showOther, other } = workDetail;
    try {
      await registerWork(selectedTutorId, {
        tutorId: selectedTutorId,
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
      router.replace(`/manager/works/${selectedTutorId}?year=${year}&month=${month}`);
    } catch (err) {
      addToast(getErrorMessage(err, "登録に失敗しました"), "error");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100 mb-6">勤務情報登録</h1>

      <form onSubmit={handleSubmit} className="space-y-4 max-w-xl">
        <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-4 space-y-4">
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700 dark:text-gray-300">対象講師 <span className="text-red-500">*</span></label>
            {tutors.length === 0 ? (
              <div className="h-9 bg-gray-100 dark:bg-gray-700 rounded-lg animate-pulse" />
            ) : (
              <select value={selectedTutorId} onChange={(e) => handleTutorChange(e.target.value)} required className={inputClass}>
                <option value="">講師を選択</option>
                {tutors.map((t) => (
                  <option key={t.id} value={t.id}>{t.lastName} {t.firstName}</option>
                ))}
              </select>
            )}
          </div>
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

        <WorkDetailFields value={workDetail} onChange={setWorkDetail} />

        <div className="flex gap-3">
          <Button type="submit" isLoading={isLoading}>登録</Button>
          <Button type="button" variant="secondary" onClick={() => router.back()}>キャンセル</Button>
        </div>
      </form>
    </div>
  );
}
