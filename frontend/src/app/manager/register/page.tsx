"use client";

import { useState, FormEvent, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { registerManager, getClassrooms } from "@/lib/api";
import { ClassroomResponse } from "@/types";
import { inputClass } from "@/components/ui/FormField";
import Button from "@/components/ui/Button";
import { useToast } from "@/context/ToastContext";
import { getErrorMessage } from "@/lib/utils";

export default function ManagerRegisterPage() {
  const { addToast } = useToast();
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [classroomId, setClassroomId] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [classrooms, setClassrooms] = useState<ClassroomResponse[]>([]);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();

  useEffect(() => {
    getClassrooms().then(setClassrooms).catch((e) => addToast(getErrorMessage(e), "error"));
  }, [addToast]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    setIsLoading(true);
    try {
      await registerManager({ loginId, password, classroomId, firstName, lastName });
      router.replace("/manager/login");
    } catch (err) {
      setError(getErrorMessage(err, "登録に失敗しました"));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 p-6">
      <div className="w-full max-w-sm">
        <div className="text-center mb-8">
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">管理者登録</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">新規アカウントを作成</p>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-gray-200 dark:border-gray-700 p-6">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <div className="flex flex-col gap-1">
                <label className="text-sm font-medium text-gray-700 dark:text-gray-300">姓 <span className="text-red-500">*</span></label>
                <input
                  type="text"
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                  required
                  maxLength={100}
                  className={inputClass}
                  placeholder="田中"
                />
              </div>
              <div className="flex flex-col gap-1">
                <label className="text-sm font-medium text-gray-700 dark:text-gray-300">名 <span className="text-red-500">*</span></label>
                <input
                  type="text"
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                  required
                  maxLength={100}
                  className={inputClass}
                  placeholder="太郎"
                />
              </div>
            </div>

            <div className="flex flex-col gap-1">
              <label className="text-sm font-medium text-gray-700 dark:text-gray-300">教室 <span className="text-red-500">*</span></label>
              <select
                value={classroomId}
                onChange={(e) => setClassroomId(e.target.value)}
                required
                className={inputClass}
              >
                <option value="">教室を選択</option>
                {classrooms.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name} ({c.classroomNumber})
                  </option>
                ))}
              </select>
            </div>

            <div className="flex flex-col gap-1">
              <label className="text-sm font-medium text-gray-700 dark:text-gray-300">ログインID <span className="text-red-500">*</span></label>
              <input
                type="text"
                value={loginId}
                onChange={(e) => setLoginId(e.target.value)}
                required
                maxLength={50}
                className={inputClass}
                placeholder="ログインIDを入力"
              />
            </div>

            <div className="flex flex-col gap-1">
              <label className="text-sm font-medium text-gray-700 dark:text-gray-300">パスワード <span className="text-red-500">*</span></label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                minLength={8}
                maxLength={50}
                className={inputClass}
                placeholder="8文字以上"
              />
            </div>

            {error && (
              <p className="text-sm text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-900/40 rounded-lg px-3 py-2">{error}</p>
            )}

            <Button type="submit" isLoading={isLoading} className="w-full">
              登録
            </Button>
          </form>
        </div>

        <p className="text-center text-sm text-gray-500 dark:text-gray-400 mt-4">
          すでにアカウントをお持ちの方は{" "}
          <Link href="/manager/login" className="text-indigo-600 dark:text-indigo-400 hover:underline">
            ログイン
          </Link>
        </p>
      </div>
    </div>
  );
}
