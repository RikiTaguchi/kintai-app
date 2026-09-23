package com.example.api.controller.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * {@code WorkRegisterRequest} / {@code WorkEditRequest} に対する複合バリデーション。
 * 3 つの work detail（lesson / office / other）のうち、少なくとも 1 つが
 * 「意味のある内容」を持つことを検証する。
 *
 * <p>「意味のある内容」の定義:
 * <ul>
 *   <li>LessonWorkDetail: {@code periodCodes} 非空、または {@code startTime}/{@code endTime} の両方が非 null
 *       （1〜2コマの場合はサービス層で時刻が無効化される仕様のため、時刻片方のみは「内容なし」扱い）</li>
 *   <li>OfficeWorkDetail: {@code startTime}/{@code endTime} のいずれかが非 null
 *   <li>OtherWorkDetail: {@code startTime}/{@code endTime} のいずれかが非 null、または {@code description} 非空
 * </ul>
 *
 * <p>将来的に detail 間の相互制約（例: lesson が有効なら breakMinutes 必須）を
 * 追加する場合は {@link WorkDetailValidator} の実装を拡張する。
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {WorkDetailValidator.ForRegister.class, WorkDetailValidator.ForEdit.class})
public @interface ValidWorkDetail {

    String message() default "いずれかの業務を1つ以上入力してください";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
