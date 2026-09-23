/**
 * 勤務テンプレートから WorkDetailState を導出する純粋ロジック。
 * page.tsx (tutor/works/register) の冗長なフォーム初期化を共通化。
 */

import { TemplateResponse, PeriodCode } from "@/types";
import type { WorkDetailState } from "@/components/work/WorkDetailFields";
import { fromApiTime } from "@/components/work/WorkDetailFields";

export interface ApplyTemplateResult {
  classroomId: string;
  transportationFee: string;
  workDetail: WorkDetailState;
}

export function applyTemplate(
  templates: TemplateResponse[],
  templateId: string
): ApplyTemplateResult | null {
  const tmpl = templates.find((t) => t.id === templateId);
  if (!tmpl) return null;

  return {
    classroomId: tmpl.classroomId,
    transportationFee: String(tmpl.transportationFee),
    workDetail: {
      lesson: {
        startTime: fromApiTime(tmpl.lessonTemplateDetail.startTime),
        endTime: fromApiTime(tmpl.lessonTemplateDetail.endTime),
        breakMinutes:
          tmpl.lessonTemplateDetail.breakMinutes != null
            ? String(tmpl.lessonTemplateDetail.breakMinutes)
            : "0",
        periodCodes: (tmpl.lessonTemplateDetail.periodCodes ?? []) as PeriodCode[],
      },
      showOffice: !!tmpl.officeTemplateDetail.startTime,
      office: tmpl.officeTemplateDetail.startTime
        ? {
            startTime: fromApiTime(tmpl.officeTemplateDetail.startTime),
            endTime: fromApiTime(tmpl.officeTemplateDetail.endTime),
          }
        : { startTime: "", endTime: "" },
      showOther: !!tmpl.otherTemplateDetail.startTime,
      other: tmpl.otherTemplateDetail.startTime
        ? {
            startTime: fromApiTime(tmpl.otherTemplateDetail.startTime),
            endTime: fromApiTime(tmpl.otherTemplateDetail.endTime),
            breakMinutes:
              tmpl.otherTemplateDetail.breakMinutes != null
                ? String(tmpl.otherTemplateDetail.breakMinutes)
                : "0",
            description: tmpl.otherTemplateDetail.description ?? "",
          }
        : { startTime: "", endTime: "", breakMinutes: "", description: "" },
    },
  };
}
