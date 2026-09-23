"use client";

import { useState, useEffect, use } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { getTutor, deleteTutor, getSalaries, deleteSalary, resetTutorPassword } from "@/lib/api";
import { TutorResponse, SalaryResponse } from "@/types";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import Button from "@/components/ui/Button";
import ConfirmDialog from "@/components/ui/ConfirmDialog";
import DataRow from "@/components/ui/DataRow";
import PasswordResetModal from "@/components/auth/PasswordResetModal";
import { useToast } from "@/context/ToastContext";
import { formatDate, getErrorMessage } from "@/lib/utils";
import { useConfirm } from "@/hooks/useConfirm";
import EmptyState from "@/components/ui/EmptyState";

export default function TutorDetailPage({
  params,
}: {
  params: Promise<{ tutorId: string }>;
}) {
  const { tutorId } = use(params);
  const router = useRouter();
  const [tutor, setTutor] = useState<TutorResponse | null>(null);
  const [salaries, setSalaries] = useState<SalaryResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const { addToast } = useToast();
  const tutorDeleteConfirm = useConfirm<true>();
  const salaryDeleteConfirm = useConfirm<SalaryResponse>();
  const [showPasswordResetModal, setShowPasswordResetModal] = useState(false);

  useEffect(() => {
    Promise.all([
      getTutor(tutorId),
      getSalaries(tutorId),
    ])
      .then(([t, s]) => {
        setTutor(t);
        setSalaries([...s].sort((a, b) => a.effectiveDate.localeCompare(b.effectiveDate)));
      })
      .catch((e) => addToast(getErrorMessage(e), "error"))
      .finally(() => setIsLoading(false));
  }, [tutorId, addToast]);

  const handleDeleteTutor = async () => {
    tutorDeleteConfirm.setIsLoading(true);
    try {
      await deleteTutor(tutorId);
      router.replace("/manager/tutors");
    } catch (e) {
      addToast(getErrorMessage(e, "削除に失敗しました"), "error");
    } finally {
      tutorDeleteConfirm.setIsLoading(false);
      tutorDeleteConfirm.cancel();
    }
  };

  const handleDeleteSalary = async () => {
    if (!salaryDeleteConfirm.target) return;
    salaryDeleteConfirm.setIsLoading(true);
    try {
      await deleteSalary(tutorId, salaryDeleteConfirm.target.id);
      setSalaries(salaries.filter((s) => s.id !== salaryDeleteConfirm.target!.id));
      salaryDeleteConfirm.cancel();
      addToast("給与情報を削除しました", "success");
    } catch (e) {
      addToast(getErrorMessage(e, "削除に失敗しました"), "error");
    } finally {
      salaryDeleteConfirm.setIsLoading(false);
    }
  };

  if (isLoading) return <LoadingSpinner />;
  if (!tutor) return <p className="text-red-600">講師が見つかりません</p>;

  return (
    <div>
      <div className="flex items-center gap-3 mb-2">
        <button onClick={() => router.back()} className="text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-400 cursor-pointer">
          ← 一覧へ戻る
        </button>
      </div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
          {tutor.lastName} {tutor.firstName}
        </h1>
        <span className={`px-3 py-1 rounded-full text-xs font-medium ${tutor.terminated ? "bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400" : "bg-green-100 dark:bg-green-900/40 text-green-700 dark:text-green-400"}`}>
          {tutor.terminated ? "退職済" : "在籍中"}
        </span>
      </div>

      <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 divide-y divide-gray-100 dark:divide-gray-700 mb-4">
        <DataRow label="講師No." value={tutor.tutorNumber !== null ? String(tutor.tutorNumber) : "未設定"} />
        <DataRow label="教室" value={tutor.classroomName} />
        <DataRow label="ログインID" value={tutor.loginId} />
        {tutor.terminated && tutor.terminationDate && (
          <DataRow label="退職日" value={formatDate(tutor.terminationDate)} />
        )}
      </div>

      <div className="flex gap-3 mb-8">
        <Link href={`/manager/tutors/${tutorId}/edit`}>
          <Button variant="secondary">講師情報を編集</Button>
        </Link>
        <Button variant="secondary" onClick={() => setShowPasswordResetModal(true)}>
          パスワードをリセット
        </Button>
        <Link href={`/manager/works/${tutorId}`}>
          <Button variant="secondary">勤務情報を確認</Button>
        </Link>
        <Button variant="danger" onClick={() => tutorDeleteConfirm.confirm(true)}>削除</Button>
      </div>

      {showPasswordResetModal && tutor && (
        <PasswordResetModal
          tutorName={`${tutor.lastName} ${tutor.firstName}`}
          onClose={() => setShowPasswordResetModal(false)}
          onSubmit={async (newPassword) => {
            await resetTutorPassword(tutorId, { newPassword });
            addToast("パスワードをリセットしました", "success");
          }}
        />
      )}

      <div className="flex items-center justify-between mb-3">
        <h2 className="text-lg font-semibold text-gray-900 dark:text-gray-100">給与情報</h2>
        <Link href={`/manager/salaries/${tutorId}/register`}>
          <Button size="sm">+ 追加</Button>
        </Link>
      </div>

      {salaries.length === 0 ? (
        <EmptyState message="給与情報が登録されていません" />
      ) : (
        <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700">
              <tr>
                <th className="text-left px-4 py-3 font-medium text-gray-600 dark:text-gray-400">適用開始日</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600 dark:text-gray-400">コマ給</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600 dark:text-gray-400">事務給</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600 dark:text-gray-400">交通費</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
              {salaries.map((s) => (
                <tr key={s.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                  <td className="px-4 py-3">{formatDate(s.effectiveDate)}</td>
                  <td className="px-4 py-3">¥{s.lessonWage.toLocaleString()}</td>
                  <td className="px-4 py-3">¥{s.officeWage.toLocaleString()}</td>
                  <td className="px-4 py-3">¥{s.transportationFee.toLocaleString()}</td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2 justify-end">
                      <Link href={`/manager/salaries/${tutorId}/${s.id}/edit`}>
                        <Button variant="ghost" size="sm">編集</Button>
                      </Link>
                      <Button variant="ghost" size="sm" className="text-red-600! hover:bg-red-50!" onClick={() => salaryDeleteConfirm.confirm(s)}>削除</Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <ConfirmDialog
        isOpen={!!tutorDeleteConfirm.target}
        title="講師を削除しますか？"
        message={`${tutor.lastName} ${tutor.firstName} を削除します。この操作は取り消せません。`}
        isLoading={tutorDeleteConfirm.isLoading}
        onConfirm={handleDeleteTutor}
        onCancel={tutorDeleteConfirm.cancel}
      />
      <ConfirmDialog
        isOpen={!!salaryDeleteConfirm.target}
        title="給与情報を削除しますか？"
        message={`適用開始日: ${formatDate(salaryDeleteConfirm.target?.effectiveDate)} の給与情報を削除します。`}
        isLoading={salaryDeleteConfirm.isLoading}
        onConfirm={handleDeleteSalary}
        onCancel={salaryDeleteConfirm.cancel}
      />
    </div>
  );
}
