"use client";

import { useState, FormEvent, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { registerTemplate, getClassrooms, getSalaries } from "@/lib/api";
import { ClassroomResponse } from "@/types";
import { getApplicableFee, getErrorMessage } from "@/lib/utils";
import WorkDetailFields, {
  EMPTY_WORK_DETAIL,
  WorkDetailState,
  toApiTime,
} from "@/components/work/WorkDetailFields";
import { inputClass } from "@/components/ui/FormField";
import Button from "@/components/ui/Button";
import { useToast } from "@/context/ToastContext";

export default function TemplateRegisterPage() {
  const { user } = useAuth();
  const router = useRouter();
  const { addToast } = useToast();

  const [title, setTitle] = useState("");
  const [classroomId, setClassroomId] = useState("");
  const [transportationFee, setTransportationFee] = useState("0");
  const [classrooms, setClassrooms] = useState<ClassroomResponse[]>([]);
  const [workDetail, setWorkDetail] = useState<WorkDetailState>(EMPTY_WORK_DETAIL);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!user) return;
    const today = new Date().toISOString().slice(0, 10);
    Promise.all([getClassrooms(), getSalaries(user.id)]).then(([list, salaries]) => {
      setClassrooms(list);
      if (user.classroomId) setClassroomId(user.classroomId);
      setTransportationFee(getApplicableFee(salaries, today));
    }).catch((e) => addToast(getErrorMessage(e), "error"));
  }, [user, addToast]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!user) return;
    setIsLoading(true);
    const { lesson, showOffice, office, showOther, other } = workDetail;
    try {
      await registerTemplate(user.id, {
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
      addToast("テンプレートを登録しました", "success");
      router.replace("/tutor/templates");
    } catch (err) {
      addToast(getErrorMessage(err, "登録に失敗しました"), "error");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="py-4">
      <h1 className="text-xl font-bold text-gray-900 dark:text-gray-100 mb-4">テンプレートを登録</h1>

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
          <Button type="submit" isLoading={isLoading} className="flex-1 justify-center! bg-teal-600! hover:bg-teal-700! focus:ring-teal-500!">登録</Button>
          <Button type="button" variant="secondary" onClick={() => router.back()}>戻る</Button>
        </div>
      </form>
    </div>
  );
}
