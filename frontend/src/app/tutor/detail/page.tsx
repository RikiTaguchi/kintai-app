"use client";

import { useState, useEffect } from "react";
import { useAuth } from "@/context/AuthContext";
import { TutorUser } from "@/context/AuthContext";
import { getSalaries, changeTutorPassword } from "@/lib/api";
import { SalaryResponse } from "@/types";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import Button from "@/components/ui/Button";
import DataRow from "@/components/ui/DataRow";
import PasswordChangeModal from "@/components/auth/PasswordChangeModal";
import { formatDate, getErrorMessage } from "@/lib/utils";
import { useToast } from "@/context/ToastContext";

export default function TutorDetailPage() {
  const { user, isLoading, logout } = useAuth();
  const { addToast } = useToast();
  const [salaries, setSalaries] = useState<SalaryResponse[]>([]);
  const [showPasswordModal, setShowPasswordModal] = useState(false);

  useEffect(() => {
    if (!user) return;
    getSalaries(user.id)
      .then((list) => setSalaries([...list].sort((a, b) => b.effectiveDate.localeCompare(a.effectiveDate))))
      .catch((e) => addToast(getErrorMessage(e), "error"));
  }, [user, addToast]);

  if (isLoading) return <LoadingSpinner />;

  const tutor = user as TutorUser | null;

  return (
    <div className="py-4">
      <h1 className="text-xl font-bold text-gray-900 dark:text-gray-100 mb-6">マイページ</h1>

      <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 divide-y divide-gray-100 dark:divide-gray-700 mb-6">
        <DataRow label="氏名" value={tutor ? `${tutor.lastName} ${tutor.firstName}` : ""} />
        <DataRow label="所属教室" value={tutor?.classroomName ?? ""} />
        <DataRow label="講師番号" value={tutor?.tutorNumber != null ? String(tutor.tutorNumber) : ""} />
        <DataRow label="ログインID" value={tutor?.loginId ?? ""} />
        <DataRow
          label="ステータス"
          value={tutor?.terminated ? "退職済" : "在籍中"}
          valueClass={tutor?.terminated ? "text-gray-500 dark:text-gray-400" : "text-green-700 dark:text-green-400 font-semibold"}
        />
      </div>

      {/* 給与情報の推移 */}
      {salaries.length > 0 && (
        <div className="mb-6">
          <h2 className="text-base font-semibold text-gray-900 dark:text-gray-100 mb-3">給与情報の推移</h2>
          <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 dark:bg-gray-700 border-b border-gray-200 dark:border-gray-700">
                <tr className="divide-x divide-gray-200 dark:divide-gray-700">
                  <th className="text-center px-2 py-3 text-xs font-medium text-gray-600 dark:text-gray-400 w-[1%] whitespace-nowrap">適用開始日</th>
                  <th className="text-center px-2 py-3 text-xs font-medium text-gray-600 dark:text-gray-400">コマ給</th>
                  <th className="text-center px-2 py-3 text-xs font-medium text-gray-600 dark:text-gray-400">事務給</th>
                  <th className="text-center px-2 py-3 text-xs font-medium text-gray-600 dark:text-gray-400">交通費</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                {salaries.map((s, i) => (
                  <tr key={s.id} className={`divide-x divide-gray-100 dark:divide-gray-700 ${i === 0 ? "bg-teal-50 dark:bg-teal-900/40" : ""}`}>
                    <td className="px-2 py-3 text-xs whitespace-nowrap text-center">{formatDate(s.effectiveDate)}</td>
                    <td className="px-2 py-3 text-xs text-right">¥{s.lessonWage.toLocaleString()}</td>
                    <td className="px-2 py-3 text-xs text-right">¥{s.officeWage.toLocaleString()}</td>
                    <td className="px-2 py-3 text-xs text-right">¥{s.transportationFee.toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <div className="bg-amber-50 dark:bg-amber-900/40 border border-amber-200 dark:border-amber-800 rounded-xl p-4 mb-6">
        <p className="text-xs text-amber-800 dark:text-amber-300">・ログインID・氏名・交通費の変更は、所属教室の管理者にお問い合わせください。</p>
      </div>

      <div className="space-y-3">
        <Button
          variant="secondary"
          onClick={() => setShowPasswordModal(true)}
          className="w-full justify-center!"
        >
          パスワードを変更
        </Button>
        <a
          href="/manual/tutor.html"
          target="_blank"
          rel="noopener noreferrer"
          className="w-full inline-flex items-center justify-center gap-2 rounded-lg font-medium transition-colors bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700 px-4 py-2 text-sm"
        >
          使い方マニュアル
        </a>
        <Button
          variant="secondary"
          onClick={logout}
          className="w-full justify-center!"
        >
          ログアウト
        </Button>
      </div>

      {showPasswordModal && user && (
        <PasswordChangeModal
          onClose={() => setShowPasswordModal(false)}
          onSubmit={async (currentPassword, newPassword) => {
            await changeTutorPassword(user.id, { currentPassword, newPassword });
            addToast("パスワードを変更しました", "success");
          }}
        />
      )}
    </div>
  );
}
