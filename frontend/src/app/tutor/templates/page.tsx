"use client";

import { useState, useEffect, useCallback } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { getTemplates, deleteTemplate } from "@/lib/api";
import { TemplateResponse } from "@/types";
import LoadingSpinner from "@/components/ui/LoadingSpinner";
import Button from "@/components/ui/Button";
import ConfirmDialog from "@/components/ui/ConfirmDialog";
import TemplateDetailModal from "@/components/template/TemplateDetailModal";
import { useToast } from "@/context/ToastContext";
import { useConfirm } from "@/hooks/useConfirm";
import EmptyState from "@/components/ui/EmptyState";
import { getErrorMessage } from "@/lib/utils";

export default function TutorTemplatesPage() {
  const { user } = useAuth();
  const router = useRouter();
  const [templates, setTemplates] = useState<TemplateResponse[]>([]);
  const { addToast } = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [detailTemplate, setDetailTemplate] = useState<TemplateResponse | null>(null);
  const deleteConfirm = useConfirm<TemplateResponse>();

  const load = useCallback(() => {
    if (!user) return;
    setIsLoading(true);
    getTemplates(user.id)
      .then(setTemplates)
      .catch((e) => addToast(getErrorMessage(e), "error"))
      .finally(() => setIsLoading(false));
  }, [user, addToast]);

  useEffect(() => { load(); }, [load]);

  const handleDelete = async () => {
    if (!deleteConfirm.target || !user) return;
    deleteConfirm.setIsLoading(true);
    try {
      await deleteTemplate(user.id, deleteConfirm.target.id);
      deleteConfirm.cancel();
      addToast("テンプレートを削除しました", "success");
      load();
    } catch (e) {
      addToast(getErrorMessage(e, "削除に失敗しました"), "error");
    } finally {
      deleteConfirm.setIsLoading(false);
    }
  };

  return (
    <div className="py-4">
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-xl font-bold text-gray-900 dark:text-gray-100">テンプレート</h1>
        <Link href="/tutor/templates/register">
          <Button size="sm" className="bg-teal-600! hover:bg-teal-700!">
            <svg className="w-4 h-4 mr-1 inline-block" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            追加
          </Button>
        </Link>
      </div>

      {isLoading ? (
        <LoadingSpinner />
      ) : templates.length === 0 ? (
        <EmptyState message="テンプレートはまだありません" subMessage="よく使う勤務パターンを保存しておくと便利です" />
      ) : (
        <div className="space-y-3">
          {templates.map((t) => {
            const hasLesson = !!t.lessonTemplateDetail.startTime || (t.lessonTemplateDetail.periodCodes?.length ?? 0) > 0;
            const hasOffice = !!t.officeTemplateDetail.startTime;
            const hasOther = !!t.otherTemplateDetail.startTime;
            return (
              <div
                key={t.id}
                className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-4 cursor-pointer"
                onClick={() => setDetailTemplate(t)}
              >
                <div className="flex items-start justify-between">
                  <div>
                    <div className="flex flex-col mb-1">
                      <span className="text-base font-bold text-gray-900 dark:text-gray-100">{t.title}</span>
                      <span className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">{t.classroomName}教室</span>
                    </div>
                    <div className="flex flex-wrap gap-1 mt-1">
                      {hasLesson && (
                        <span className="px-2 py-0.5 bg-teal-100 dark:bg-teal-900/50 text-teal-700 dark:text-teal-300 text-xs rounded-full font-medium">
                          {(t.lessonTemplateDetail.periodCodes?.length ?? 0) > 0
                            ? t.lessonTemplateDetail.periodCodes!.join("")
                            : "授業"}
                        </span>
                      )}
                      {hasOffice && (
                        <span className="px-2 py-0.5 bg-teal-100 dark:bg-teal-900/50 text-teal-700 dark:text-teal-300 text-xs rounded-full font-medium">事務</span>
                      )}
                      {hasOther && (
                        <span className="px-2 py-0.5 bg-teal-100 dark:bg-teal-900/50 text-teal-700 dark:text-teal-300 text-xs rounded-full font-medium">その他</span>
                      )}
                    </div>
                  </div>
                  <div className="flex flex-col items-end gap-1" onClick={(e) => e.stopPropagation()}>
                    <div className="flex gap-1">
                      <Button variant="ghost" size="sm" onClick={() => setDetailTemplate(t)}>詳細</Button>
                      <Link href={`/tutor/templates/${t.id}/edit`}>
                        <Button variant="ghost" size="sm">編集</Button>
                      </Link>
                    </div>
                    <Button variant="ghost" size="sm" className="text-red-500!" onClick={() => deleteConfirm.confirm(t)}>削除</Button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {detailTemplate && (
        <TemplateDetailModal
          template={detailTemplate}
          onClose={() => setDetailTemplate(null)}
          onEdit={() => {
            router.push(`/tutor/templates/${detailTemplate.id}/edit`);
            setDetailTemplate(null);
          }}
          onDelete={() => {
            deleteConfirm.confirm(detailTemplate);
            setDetailTemplate(null);
          }}
        />
      )}

      <ConfirmDialog
        isOpen={!!deleteConfirm.target}
        title="テンプレートを削除しますか？"
        message={`「${deleteConfirm.target?.title}」を削除します。`}
        isLoading={deleteConfirm.isLoading}
        onConfirm={handleDelete}
        onCancel={deleteConfirm.cancel}
      />
    </div>
  );
}
