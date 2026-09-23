"use client";

import { useState, FormEvent, use } from "react";
import { useRouter } from "next/navigation";
import { registerSalary } from "@/lib/api";
import { inputClass } from "@/components/ui/FormField";
import Button from "@/components/ui/Button";
import { useToast } from "@/context/ToastContext";
import { getErrorMessage } from "@/lib/utils";

export default function SalaryRegisterPage({
  params,
}: {
  params: Promise<{ tutorId: string }>;
}) {
  const { tutorId } = use(params);
  const router = useRouter();

  const today = new Date().toISOString().slice(0, 10);
  const [effectiveDate, setEffectiveDate] = useState(today);
  const [lessonWage, setLessonWage] = useState("");
  const [officeWage, setOfficeWage] = useState("");
  const [transportationFee, setTransportationFee] = useState("");
  const { addToast } = useToast();
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      await registerSalary(tutorId, {
        tutorId,
        effectiveDate,
        lessonWage: Number(lessonWage),
        officeWage: Number(officeWage),
        transportationFee: Number(transportationFee),
      });
      addToast("給与情報を登録しました", "success");
      router.replace(`/manager/tutors/${tutorId}`);
    } catch (err) {
      addToast(getErrorMessage(err, "登録に失敗しました"), "error");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100 mb-6">給与情報登録</h1>

      <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-6 max-w-md">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700 dark:text-gray-300">適用開始日 <span className="text-red-500">*</span></label>
            <input type="date" value={effectiveDate} onChange={(e) => setEffectiveDate(e.target.value)} required className={inputClass} />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700 dark:text-gray-300">授業時給（円）<span className="text-red-500">*</span></label>
            <input type="number" min="1" step="1" value={lessonWage} onChange={(e) => setLessonWage(e.target.value)} required placeholder="例: 1200" className={inputClass} />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700 dark:text-gray-300">事務給（円）<span className="text-red-500">*</span></label>
            <input type="number" min="1" step="1" value={officeWage} onChange={(e) => setOfficeWage(e.target.value)} required placeholder="例: 1050" className={inputClass} />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700 dark:text-gray-300">交通費（円/日）<span className="text-red-500">*</span></label>
            <input type="number" min="0" step="1" value={transportationFee} onChange={(e) => setTransportationFee(e.target.value)} required placeholder="例: 500" className={inputClass} />
          </div>

          <div className="flex gap-3 pt-2">
            <Button type="submit" isLoading={isLoading}>登録</Button>
            <Button type="button" variant="secondary" onClick={() => router.back()}>キャンセル</Button>
          </div>
        </form>
      </div>
    </div>
  );
}
