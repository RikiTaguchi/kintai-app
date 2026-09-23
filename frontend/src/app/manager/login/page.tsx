"use client";

import { useState, FormEvent } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/context/AuthContext";
import { loginManager } from "@/lib/api";
import { inputClass } from "@/components/ui/FormField";
import Button from "@/components/ui/Button";
import { getErrorMessage } from "@/lib/utils";

export default function ManagerLoginPage() {
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuth();
  const router = useRouter();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    setIsLoading(true);
    try {
      const res = await loginManager({ loginId, password });
      login(res, "ROLE_MANAGER");
      router.replace("/manager");
    } catch (err) {
      setError(getErrorMessage(err, "ログインに失敗しました"));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 p-6">
      <div className="w-full max-w-sm">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 bg-indigo-100 dark:bg-indigo-900/50 rounded-2xl mb-4">
            <svg className="w-7 h-7 text-indigo-600 dark:text-indigo-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5" />
            </svg>
          </div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">管理者ログイン</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">勤怠管理システム</p>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-gray-200 dark:border-gray-700 p-6">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="flex flex-col gap-1">
              <label className="text-sm font-medium text-gray-700 dark:text-gray-300">ログインID</label>
              <input
                type="text"
                value={loginId}
                onChange={(e) => setLoginId(e.target.value)}
                required
                maxLength={50}
                autoComplete="username"
                className={inputClass}
                placeholder="ログインIDを入力"
              />
            </div>
            <div className="flex flex-col gap-1">
              <label className="text-sm font-medium text-gray-700 dark:text-gray-300">パスワード</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                minLength={8}
                maxLength={50}
                autoComplete="current-password"
                className={inputClass}
                placeholder="パスワードを入力"
              />
            </div>

            {error && (
              <p className="text-sm text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-900/40 rounded-lg px-3 py-2">{error}</p>
            )}

            <Button type="submit" isLoading={isLoading} className="w-full">
              ログイン
            </Button>
          </form>
        </div>

        <p className="text-center text-sm text-gray-500 dark:text-gray-400 mt-4">
          アカウントをお持ちでない方は{" "}
          <Link href="/manager/register" className="text-indigo-600 dark:text-indigo-400 hover:underline">
            こちら
          </Link>
        </p>
        <p className="text-center text-sm text-gray-400 dark:text-gray-500 mt-2">
          <Link href="/" className="hover:underline">
            トップページへ戻る
          </Link>
        </p>
        <p className="text-center text-sm text-gray-400 dark:text-gray-500 mt-2">
          <a href="/manual/manager.html" rel="noopener noreferrer" className="hover:underline">
            使い方を確認する
          </a>
        </p>
      </div>
    </div>
  );
}
