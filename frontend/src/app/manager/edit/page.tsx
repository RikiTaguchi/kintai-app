"use client";

import { useState, FormEvent, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { editManager, getClassrooms } from "@/lib/api";
import { ClassroomResponse } from "@/types";
import { inputClass } from "@/components/ui/FormField";
import Button from "@/components/ui/Button";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import { useToast } from "@/context/ToastContext";
import { getErrorMessage } from "@/lib/utils";

export default function ManagerEditPage() {
  const { user, isLoading, login } = useAuth();
  const router = useRouter();
  const { addToast } = useToast();

  const [loginId, setLoginId] = useState("");
  const [classroomId, setClassroomId] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [classrooms, setClassrooms] = useState<ClassroomResponse[]>([]);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (user) {
      setLoginId(user.loginId);
      setClassroomId(user.classroomId);
      setFirstName(user.firstName);
      setLastName(user.lastName);
    }
    getClassrooms().then(setClassrooms).catch((e) => addToast(getErrorMessage(e), "error"));
  }, [user, addToast]);

  if (isLoading) return <LoadingSpinner />;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!user) return;
    setIsSaving(true);
    try {
      const updated = await editManager(user.id, {
        id: user.id,
        loginId,
        classroomId,
        firstName,
        lastName,
      });
      login(updated, "ROLE_MANAGER");
      addToast("管理者情報を更新しました", "success");
      router.replace("/manager/detail");
    } catch (err) {
      addToast(getErrorMessage(err, "更新に失敗しました"), "error");
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100 mb-6">管理者情報編集</h1>

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
            <label className="text-sm font-medium text-gray-700 dark:text-gray-300">教室 <span className="text-red-500">*</span></label>
            <select value={classroomId} onChange={(e) => setClassroomId(e.target.value)} required className={inputClass}>
              <option value="">教室を選択</option>
              {classrooms.map((c) => (
                <option key={c.id} value={c.id}>{c.name} ({c.classroomNumber})</option>
              ))}
            </select>
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-gray-700 dark:text-gray-300">ログインID <span className="text-red-500">*</span></label>
            <input type="text" value={loginId} onChange={(e) => setLoginId(e.target.value)} required maxLength={50} placeholder="ログインIDを入力" className={inputClass} />
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
