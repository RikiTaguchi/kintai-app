"use client";

import { useState, FormEvent, useEffect, use } from "react";
import { useRouter } from "next/navigation";
import { getTutor, editTutor } from "@/lib/api";
import { inputClass } from "@/components/ui/FormField";
import Button from "@/components/ui/Button";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import { useToast } from "@/context/ToastContext";
import { getErrorMessage } from "@/lib/utils";

export default function TutorEditPage({
  params,
}: {
  params: Promise<{ tutorId: string }>;
}) {
  const { tutorId } = use(params);
  const router = useRouter();
  const { addToast } = useToast();

  const [tutorNumber, setTutorNumber] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [terminated, setTerminated] = useState(false);
  const [terminationDate, setTerminationDate] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [loadFailed, setLoadFailed] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    getTutor(tutorId)
      .then((t) => {
        setTutorNumber(t.tutorNumber !== null ? String(t.tutorNumber) : "");
        setFirstName(t.firstName);
        setLastName(t.lastName);
        setTerminated(t.terminated);
        setTerminationDate(t.terminationDate ?? "");
      })
      .catch((e) => { addToast(getErrorMessage(e), "error"); setLoadFailed(true); })
      .finally(() => setIsLoading(false));
  }, [tutorId, addToast]);

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
      await editTutor(tutorId, {
        id: tutorId,
        tutorNumber: tutorNumber !== "" ? Number(tutorNumber) : null,
        firstName,
        lastName,
        terminated,
        terminationDate: terminationDate || null,
      });
      addToast("講師情報を更新しました", "success");
      router.replace(`/manager/tutors/${tutorId}`);
    } catch (err) {
      addToast(getErrorMessage(err, "更新に失敗しました"), "error");
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100 mb-6">講師情報編集</h1>

      <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-6 max-w-lg">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <div className="flex flex-col gap-1">
              <label className="text-sm font-medium text-gray-700 dark:text-gray-300">姓 <span className="text-red-500">*</span></label>
              <input type="text" value={lastName} onChange={(e) => setLastName(e.target.value)} required maxLength={100} placeholder="例: 山田" className={inputClass} />
            </div>
            <div className="flex flex-col gap-1">
              <label className="text-sm font-medium text-gray-700 dark:text-gray-300">名 <span className="text-red-500">*</span></label>
              <input type="text" value={firstName} onChange={(e) => setFirstName(e.target.value)} required maxLength={100} placeholder="例: 太郎" className={inputClass} />
            </div>
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700 dark:text-gray-300">講師No.</label>
            <input type="number" min="0" step="1" value={tutorNumber} onChange={(e) => setTutorNumber(e.target.value)} placeholder="例: 1" className={inputClass} />
            <p className="text-xs text-gray-400 dark:text-gray-500 mt-0.5">未入力でも保存できます。あとから設定可能です。</p>
          </div>

          <div className="flex flex-col gap-2">
            <label className="text-sm font-medium text-gray-700 dark:text-gray-300">在籍状態 <span className="text-red-500">*</span></label>
            <div className="flex items-center gap-6">
              <label className="flex items-center gap-2 cursor-pointer">
                <input type="radio" name="terminated" checked={!terminated} onChange={() => setTerminated(false)} className="accent-indigo-600" />
                <span className="text-sm text-gray-900 dark:text-gray-100">在籍中</span>
              </label>
              <label className="flex items-center gap-2 cursor-pointer">
                <input type="radio" name="terminated" checked={terminated} onChange={() => setTerminated(true)} className="accent-indigo-600" />
                <span className="text-sm text-gray-900 dark:text-gray-100">退職済</span>
              </label>
            </div>
          </div>

          {terminated && (
            <div className="flex flex-col gap-1">
              <label className="text-sm font-medium text-gray-700 dark:text-gray-300">退職日 <span className="text-red-500">*</span></label>
              <input type="date" value={terminationDate} onChange={(e) => setTerminationDate(e.target.value)} required className={inputClass} />
            </div>
          )}

          <div className="flex gap-3 pt-2">
            <Button type="submit" isLoading={isSaving}>保存</Button>
            <Button type="button" variant="secondary" onClick={() => router.back()}>キャンセル</Button>
          </div>
        </form>
      </div>
    </div>
  );
}
