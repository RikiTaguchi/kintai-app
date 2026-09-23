"use client";

import { useState, FormEvent, useEffect, use } from "react";
import { useRouter } from "next/navigation";
import { getSalary, editSalary } from "@/lib/api";
import { inputClass } from "@/components/ui/FormField";
import Button from "@/components/ui/Button";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import { useToast } from "@/context/ToastContext";
import { getErrorMessage } from "@/lib/utils";

export default function SalaryEditPage({
  params,
}: {
  params: Promise<{ tutorId: string; salaryId: string }>;
}) {
  const { tutorId, salaryId } = use(params);
  const router = useRouter();
  const { addToast } = useToast();

  const [effectiveDate, setEffectiveDate] = useState("");
  const [lessonWage, setLessonWage] = useState("");
  const [officeWage, setOfficeWage] = useState("");
  const [transportationFee, setTransportationFee] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [loadFailed, setLoadFailed] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    getSalary(tutorId, salaryId)
      .then((s) => {
        setEffectiveDate(s.effectiveDate);
        setLessonWage(String(s.lessonWage));
        setOfficeWage(String(s.officeWage));
        setTransportationFee(String(s.transportationFee));
      })
      .catch((e) => { addToast(getErrorMessage(e), "error"); setLoadFailed(true); })
      .finally(() => setIsLoading(false));
  }, [tutorId, salaryId, addToast]);

  if (isLoading) return <LoadingSpinner />;
  if (loadFailed) return (
    <div>
      <button className="text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-400 cursor-pointer text-sm mb-4" onClick={() => router.back()}>← 戻る</button>
      <p className="text-sm text-gray-500 dark:text-gray-400">データを読み込めませんでした。</p>
    </div>
  );

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    try {
      await editSalary(tutorId, salaryId, {
        id: salaryId,
        tutorId,
        effectiveDate,
        lessonWage: Number(lessonWage),
        officeWage: Number(officeWage),
        transportationFee: Number(transportationFee),
      });
      addToast("給与情報を更新しました", "success");
      router.replace(`/manager/tutors/${tutorId}`);
    } catch (err) {
      addToast(getErrorMessage(err, "更新に失敗しました"), "error");
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100 mb-6">給与情報編集</h1>

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
            <Button type="submit" isLoading={isSaving}>保存</Button>
            <Button type="button" variant="secondary" onClick={() => router.back()}>キャンセル</Button>
          </div>
        </form>
      </div>
    </div>
  );
}
