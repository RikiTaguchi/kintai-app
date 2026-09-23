package com.example.api.controller.validation;

import com.example.api.controller.model.work.LessonWorkDetail;
import com.example.api.controller.model.work.OfficeWorkDetail;
import com.example.api.controller.model.work.OtherWorkDetail;
import com.example.api.controller.request.work.WorkEditRequest;
import com.example.api.controller.request.work.WorkRegisterRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * {@link ValidWorkDetail} のバリデータ実装。
 * 3 つの detail のうち、少なくとも 1 つが「意味のある内容」を持つことを検証する。
 *
 * <p>具体的には、detail が {@code null} でなく、かつ以下のいずれかを満たす場合に「有効」とみなす:
 * <ul>
 *   <li>{@link LessonWorkDetail}: periodCodes が非空、または startTime/endTime の両方が非 null
 *   <li>{@link OfficeWorkDetail}: startTime/endTime のいずれかが非 null
 *   <li>{@link OtherWorkDetail}: startTime/endTime のいずれかが非 null、または description が非空文字
 * </ul>
 *
 * <p>複数のエンドポイント（登録 / 編集）で使用するため、2 つの {@link ConstraintValidator}
 * 実装の共通基盤として抽象クラスで定義する。
 */
public abstract class WorkDetailValidator<T> implements ConstraintValidator<ValidWorkDetail, T> {

    @Override
    public boolean isValid(T value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // @NotNull は別アノテーションで判定
        }
        return hasAnyDetail(
            extractLesson(value),
            extractOffice(value),
            extractOther(value)
        );
    }

    private boolean hasAnyDetail(
        LessonWorkDetail lesson,
        OfficeWorkDetail office,
        OtherWorkDetail other
    ) {
        return isLessonActive(lesson) || isOfficeActive(office) || isOtherActive(other);
    }

    private boolean isLessonActive(LessonWorkDetail lesson) {
        if (lesson == null) return false;
        boolean hasPeriodCodes = lesson.getPeriodCodes() != null && !lesson.getPeriodCodes().isEmpty();
        // 1〜2コマの場合はサービス層で時刻が無効化（永続化されない）される仕様のため、
        // 時刻が片方だけの入力は「内容あり」とみなさない。
        // 0コマで両方の時刻が揃っている入力は、サービス層で「コマを1つ以上選択してください」として弾かれる。
        boolean hasValidTimes = lesson.getStartTime() != null && lesson.getEndTime() != null;
        return hasPeriodCodes || hasValidTimes;
    }

    private boolean isOfficeActive(OfficeWorkDetail office) {
        if (office == null) return false;
        return office.getStartTime() != null || office.getEndTime() != null;
    }

    private boolean isOtherActive(OtherWorkDetail other) {
        if (other == null) return false;
        if (other.getStartTime() != null || other.getEndTime() != null) return true;
        String desc = other.getDescription();
        return desc != null && !desc.isBlank();
    }

    protected abstract LessonWorkDetail extractLesson(T request);
    protected abstract OfficeWorkDetail extractOffice(T request);
    protected abstract OtherWorkDetail extractOther(T request);

    /**
     * {@link WorkRegisterRequest} 用バリデータ。
     */
    public static class ForRegister extends WorkDetailValidator<WorkRegisterRequest> {
        @Override
        protected LessonWorkDetail extractLesson(WorkRegisterRequest r) {
            return r.getLessonWorkDetail();
        }

        @Override
        protected OfficeWorkDetail extractOffice(WorkRegisterRequest r) {
            return r.getOfficeWorkDetail();
        }

        @Override
        protected OtherWorkDetail extractOther(WorkRegisterRequest r) {
            return r.getOtherWorkDetail();
        }
    }

    /**
     * {@link WorkEditRequest} 用バリデータ。
     */
    public static class ForEdit extends WorkDetailValidator<WorkEditRequest> {
        @Override
        protected LessonWorkDetail extractLesson(WorkEditRequest r) {
            return r.getLessonWorkDetail();
        }

        @Override
        protected OfficeWorkDetail extractOffice(WorkEditRequest r) {
            return r.getOfficeWorkDetail();
        }

        @Override
        protected OtherWorkDetail extractOther(WorkEditRequest r) {
            return r.getOtherWorkDetail();
        }
    }
}
