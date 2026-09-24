"use client";

import { useState, useEffect, useCallback } from "react";
import Link from "next/link";
import { getTutors, deleteTutor } from "@/lib/api";
import { TutorResponse } from "@/types";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import Button from "@/components/ui/Button";
import ConfirmDialog from "@/components/ui/ConfirmDialog";
import { useToast } from "@/context/ToastContext";
import EmptyState from "@/components/ui/EmptyState";
import { getErrorMessage } from "@/lib/utils";
import { filterTutors } from "@/lib/tutorSearch";

export default function ManagerTutorsPage() {
  const { addToast } = useToast();
  const [tutors, setTutors] = useState<TutorResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [deleteTarget, setDeleteTarget] = useState<TutorResponse | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const load = useCallback(() => {
    setIsLoading(true);
    getTutors()
      .then((data) => setTutors([...data].sort((a, b) => {
        if (a.tutorNumber === null && b.tutorNumber === null) return 0;
        if (a.tutorNumber === null) return 1;
        if (b.tutorNumber === null) return -1;
        return a.tutorNumber - b.tutorNumber;
      })))
      .catch((e) => addToast(getErrorMessage(e), "error"))
      .finally(() => setIsLoading(false));
  }, [addToast]);

  useEffect(() => { load(); }, [load]);

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setIsDeleting(true);
    try {
      await deleteTutor(deleteTarget.id);
      setDeleteTarget(null);
      addToast("講師を削除しました", "success");
      load();
    } catch (e) {
      addToast(getErrorMessage(e, "削除に失敗しました"), "error");
    } finally {
      setIsDeleting(false);
    }
  };

  const filtered = filterTutors(tutors, search);

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">講師管理</h1>
        <Link href="/manager/tutors/register">
          <Button size="sm">+ 講師を登録</Button>
        </Link>
      </div>

      <div className="mb-4">
        <input
          type="search"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="名前・ログインID・講師番号で検索"
          className="w-full max-w-sm px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg text-base focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
      </div>

      {isLoading ? (
        <LoadingSpinner />
      ) : filtered.length === 0 ? (
        <EmptyState message="講師が登録されていません" />
      ) : (
        <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700">
              <tr>
                <th className="text-left px-4 py-3 font-medium text-gray-600 dark:text-gray-400">講師No.</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600 dark:text-gray-400">氏名</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600 dark:text-gray-400">ログインID</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600 dark:text-gray-400">状態</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
              {filtered.map((tutor) => (
                <tr key={tutor.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                  <td className="px-4 py-3 text-gray-700 dark:text-gray-300">{tutor.tutorNumber !== null ? tutor.tutorNumber : <span className="text-gray-400 dark:text-gray-500">未設定</span>}</td>
                  <td className="px-4 py-3">
                    <Link
                      href={`/manager/tutors/${tutor.id}`}
                      className="font-medium text-indigo-600 dark:text-indigo-400 hover:underline"
                    >
                      {tutor.lastName} {tutor.firstName}
                    </Link>
                  </td>
                  <td className="px-4 py-3 text-gray-600 dark:text-gray-400">{tutor.loginId}</td>
                  <td className="px-4 py-3">
                    <span
                      className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${
                        tutor.terminated
                          ? "bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400"
                          : "bg-green-100 dark:bg-green-900/40 text-green-700 dark:text-green-400"
                      }`}
                    >
                      {tutor.terminated ? "退職済" : "在籍中"}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2 justify-end">
                      <Link href={`/manager/tutors/${tutor.id}/edit`}>
                        <Button variant="ghost" size="sm">編集</Button>
                      </Link>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="text-red-600! hover:bg-red-50!"
                        onClick={() => setDeleteTarget(tutor)}
                      >
                        削除
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <ConfirmDialog
        isOpen={!!deleteTarget}
        title="講師を削除しますか？"
        message={`${deleteTarget?.lastName} ${deleteTarget?.firstName} を削除します。この操作は取り消せません。`}
        isLoading={isDeleting}
        onConfirm={handleDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
}
