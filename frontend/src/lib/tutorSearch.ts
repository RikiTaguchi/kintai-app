import type { TutorResponse } from "@/types";

/**
 * 講師一覧の検索フィルタ純粋関数。
 *
 * 氏名（lastName + firstName の連結）・ログイン ID・講師番号に対する部分一致。
 * 講師番号が null の場合は "" として扱う（= 番号検索がヒットしない）。
 */
export function filterTutors(tutors: TutorResponse[], search: string): TutorResponse[] {
  return tutors.filter(
    (t) =>
      `${t.lastName}${t.firstName}`.includes(search) ||
      t.loginId.includes(search) ||
      (t.tutorNumber !== null ? String(t.tutorNumber) : "").includes(search)
  );
}
