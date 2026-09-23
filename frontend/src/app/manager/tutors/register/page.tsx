"use client";

import { useState, FormEvent } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { registerTutor, registerSalary } from "@/lib/api";
import { inputClass } from "@/components/ui/FormField";
import Button from "@/components/ui/Button";
import { useToast } from "@/context/ToastContext";
import { getErrorMessage } from "@/lib/utils";

export default function TutorRegisterPage() {
  const { user } = useAuth();
  const router = useRouter();

  // 講師情報
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [tutorNumber, setTutorNumber] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");

  // 給与情報
  const today = new Date().toISOString().slice(0, 10);
  const [effectiveDate, setEffectiveDate] = useState(today);
  const [lessonWage, setLessonWage] = useState("");
  const [officeWage, setOfficeWage] = useState("");
  const [transportationFee, setTransportationFee] = useState("");

  const { addToast } = useToast();
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!user) return;
    setIsLoading(true);

    let tutorId: string;
    try {
      const tutor = await registerTutor({
        loginId,
        password,
        classroomId: user.classroomId,
        tutorNumber: tutorNumber !== "" ? Number(tutorNumber) : null,
        firstName,
        lastName,
      });
      tutorId = tutor.id;
    } catch (err) {
      addToast(getErrorMessage(err, "講師登録に失敗しました"), "error");
      setIsLoading(false);
      return;
    }

    try {
      await registerSalary(tutorId, {
        tutorId,
        effectiveDate,
        lessonWage: Number(lessonWage),
        officeWage: Number(officeWage),
        transportationFee: Number(transportationFee),
      });
    } catch {
      addToast("給与情報の登録に失敗しました。講師詳細から再度登録してください。", "error");
    }
    addToast("講師を登録しました", "success");
    router.replace(`/manager/tutors/${tutorId}`);
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100 mb-6">講師登録</h1>

      <form onSubmit={handleSubmit} className="space-y-4 max-w-lg">
        <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-6 space-y-4">
          <h2 className="text-sm font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wide">講師情報</h2>

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
            <input
              type="number"
              min="0"
              step="1"
              value={tutorNumber}
              onChange={(e) => setTutorNumber(e.target.value)}
              className={inputClass}
              placeholder="例: 1"
            />
            <p className="text-xs text-gray-400 dark:text-gray-500 mt-0.5">未入力でも登録できます。あとから設定可能です。</p>
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700 dark:text-gray-300">ログインID <span className="text-red-500">*</span></label>
            <input type="text" value={loginId} onChange={(e) => setLoginId(e.target.value)} required maxLength={50} placeholder="ログインIDを入力" className={inputClass} />
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700 dark:text-gray-300">パスワード <span className="text-red-500">*</span></label>
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required minLength={8} maxLength={50} placeholder="8文字以上" className={inputClass} />
          </div>

        </div>

        <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-6 space-y-4">
          <h2 className="text-sm font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wide">給与情報</h2>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700 dark:text-gray-300">適用開始日 <span className="text-red-500">*</span></label>
            <input type="date" value={effectiveDate} onChange={(e) => setEffectiveDate(e.target.value)} required className={inputClass} />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="flex flex-col gap-1">
              <label className="text-sm font-medium text-gray-700 dark:text-gray-300">コマ給（円） <span className="text-red-500">*</span></label>
              <input
                type="number"
                min="1"
                step="1"
                value={lessonWage}
                onChange={(e) => setLessonWage(e.target.value)}
                required
                placeholder="例: 1500"
                className={inputClass}
              />
            </div>
            <div className="flex flex-col gap-1">
              <label className="text-sm font-medium text-gray-700 dark:text-gray-300">事務給（円） <span className="text-red-500">*</span></label>
              <input
                type="number"
                min="1"
                step="1"
                value={officeWage}
                onChange={(e) => setOfficeWage(e.target.value)}
                required
                placeholder="例: 1200"
                className={inputClass}
              />
            </div>
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700 dark:text-gray-300">交通費（円/日）<span className="text-red-500">*</span></label>
            <input
              type="number"
              min="0"
              step="1"
              value={transportationFee}
              onChange={(e) => setTransportationFee(e.target.value)}
              required
              placeholder="例: 500"
              className={inputClass}
            />
          </div>


        </div>

        <div className="flex gap-3">
          <Button type="submit" isLoading={isLoading}>登録</Button>
          <Button type="button" variant="secondary" onClick={() => router.back()}>キャンセル</Button>
        </div>
      </form>
    </div>
  );
}
