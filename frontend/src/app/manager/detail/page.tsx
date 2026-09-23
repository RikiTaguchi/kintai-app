"use client";

import Link from "next/link";
import { useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { deleteManager, changeManagerPassword } from "@/lib/api";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import Button from "@/components/ui/Button";
import ConfirmDialog from "@/components/ui/ConfirmDialog";
import DataRow from "@/components/ui/DataRow";
import PasswordChangeModal from "@/components/auth/PasswordChangeModal";
import { useToast } from "@/context/ToastContext";
import { useConfirm } from "@/hooks/useConfirm";
import { getErrorMessage } from "@/lib/utils";

export default function ManagerDetailPage() {
  const { user, isLoading, logout } = useAuth();
  const { addToast } = useToast();
  const deleteConfirm = useConfirm<true>();
  const [showPasswordModal, setShowPasswordModal] = useState(false);

  if (isLoading) return <LoadingSpinner />;

  const handleDelete = async () => {
    if (!user) return;
    deleteConfirm.setIsLoading(true);
    try {
      await deleteManager(user.id);
      await logout();
    } catch (err) {
      addToast(getErrorMessage(err, "削除に失敗しました"), "error");
    } finally {
      deleteConfirm.setIsLoading(false);
      deleteConfirm.cancel();
    }
  };

  return (
    <div>
      <div className="flex items-center gap-3 mb-6">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">管理者情報</h1>
      </div>

      <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 divide-y divide-gray-100 dark:divide-gray-700">
        <DataRow label="氏名" value={user ? `${user.lastName} ${user.firstName}` : ""} />
        <DataRow label="教室" value={user ? `${user.classroomName} (${user.classroomNumber})` : ""} />
        <DataRow label="ログインID" value={user?.loginId ?? ""} />
      </div>

      <div className="flex gap-3 mt-6">
        <Link href="/manager/edit">
          <Button variant="secondary">編集</Button>
        </Link>
        <Button variant="secondary" onClick={() => setShowPasswordModal(true)}>
          パスワードを変更
        </Button>
        <Button variant="danger" onClick={() => deleteConfirm.confirm(true)}>
          アカウント削除
        </Button>
      </div>

      {showPasswordModal && user && (
        <PasswordChangeModal
          onClose={() => setShowPasswordModal(false)}
          onSubmit={async (currentPassword, newPassword) => {
            await changeManagerPassword(user.id, { currentPassword, newPassword });
            addToast("パスワードを変更しました", "success");
          }}
        />
      )}

      <ConfirmDialog
        isOpen={!!deleteConfirm.target}
        title="アカウントを削除しますか？"
        message="この操作は取り消せません。（削除後も、講師の勤務情報・給与情報は保持されます。）"
        confirmLabel="削除する"
        isLoading={deleteConfirm.isLoading}
        onConfirm={handleDelete}
        onCancel={deleteConfirm.cancel}
      />
    </div>
  );
}
