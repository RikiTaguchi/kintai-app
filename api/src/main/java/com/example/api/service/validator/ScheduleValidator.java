package com.example.api.service.validator;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

import com.example.api.common.constant.LessonContents;
import com.example.api.exception.InvalidInputException;
import com.example.api.service.dto.schedule.ScheduleDto;
import com.example.api.service.dto.template.TemplateDto;
import com.example.api.service.dto.work.WorkDto;

public class ScheduleValidator {

    public static void checkInput(WorkDto work) {
        checkInput(ScheduleDto.fromWorkDto(work));
    }

    public static void checkInput(TemplateDto template) {
        checkInput(ScheduleDto.fromTemplateDto(template));
    }

    public static void checkInput(ScheduleDto dto) {

        var lesson = dto.getLessonScheduleDetailDto();
        var office = dto.getOfficeScheduleDetailDto();
        var other = dto.getOtherScheduleDetailDto();

        // null を安全側に正規化（Request→Dto 経路で空オブジェクトに変換されている場合でも
        // periodCodes が null のまま来る可能性に備える）
        List<String> periodCodes = lesson.getPeriodCodes() != null
            ? lesson.getPeriodCodes()
            : List.of();

        int lessonBreak = lesson.getBreakMinutes() != null ? lesson.getBreakMinutes() : 0;
        int otherBreak = other.getBreakMinutes() != null ? other.getBreakMinutes() : 0;

        if (!periodCodes.isEmpty() && lesson.getBreakMinutes() == null) {
            throw new InvalidInputException("授業業務の休憩時間を入力してください");
        }

        // その他業務は「完全に空」か「時刻・休憩・業務内容のすべてが入力済み」のどちらかのみ許容する。
        // いずれか1つでも入力されていれば他の全項目を要求する（all-or-nothing）。
        boolean otherStarted = other.getStartTime() != null
            || other.getEndTime() != null
            || other.getBreakMinutes() != null
            || (other.getDescription() != null && !other.getDescription().isBlank());

        if (otherStarted) {

            if (other.getStartTime() == null || other.getEndTime() == null) {
                throw new InvalidInputException("その他業務の開始・終了時刻は両方入力してください");
            }

            if (other.getBreakMinutes() == null) {
                throw new InvalidInputException("その他業務の休憩時間を入力してください");
            }

            if (other.getDescription() == null || other.getDescription().isBlank()) {
                throw new InvalidInputException("その他業務の業務内容を入力してください");
            }
        }

        // 事務業務の時刻は両方入力されているか、両方とも未入力かのどちらかのみ許容する
        boolean officeStarted = office.getStartTime() != null || office.getEndTime() != null;
        if (officeStarted && (office.getStartTime() == null || office.getEndTime() == null)) {
            throw new InvalidInputException("事務業務の開始・終了時刻は両方入力してください");
        }

        if (periodCodes.size() >= LessonContents.LESSON_OUT_SIDE_HOURS_BORDER
                && (lesson.getStartTime() == null || lesson.getEndTime() == null)) {
            throw new InvalidInputException("3コマ以上の場合、開始・終了時刻を入力してください");
        }

        // 授業時刻の整合性は、時刻が意味を持つケース（0コマで時刻のみの入力、または3コマ以上）のみ検証する。
        // 1〜2コマの場合は保存時に startTime/endTime が無効化される仕様のため、時刻の状態は一切問わない。
        boolean validateLessonTimes = lesson.getStartTime() != null && lesson.getEndTime() != null
            && (periodCodes.isEmpty() || periodCodes.size() >= LessonContents.LESSON_OUT_SIDE_HOURS_BORDER);

        if (validateLessonTimes) {

            if (!lesson.getStartTime().isBefore(lesson.getEndTime())) {
                throw new InvalidInputException("授業業務の終了時刻は開始時刻より後にしてください");
            }

            long totalDuration = Duration.between(lesson.getStartTime(), lesson.getEndTime()).toMinutes();
            long availableBreak = totalDuration - (long) periodCodes.size() * LessonContents.LESSON_MINUTES;
            if (availableBreak < lessonBreak) {
                throw new InvalidInputException("授業業務の休憩時間が勤務時間を超えています");
            }

            String customOrder = "MKSABCD";

            List<String> sortedPeriodCodes = periodCodes
                .stream()
                .sorted(Comparator.comparingInt(customOrder::indexOf))
                .toList();

            if (sortedPeriodCodes.isEmpty()) {
                throw new InvalidInputException("コマを1つ以上選択してください");
            }

            LocalTime firstPeriodStart = getPeriodStart(sortedPeriodCodes.getFirst());
            LocalTime lastPeriodEnd = getPeriodEnd(sortedPeriodCodes.getLast());

            if (firstPeriodStart.isBefore(lesson.getStartTime()) || lastPeriodEnd.isAfter(lesson.getEndTime())) {
                throw new InvalidInputException("選択したコマが授業時間の範囲外です");
            }
        }

        // 選択した各コマと事務・その他業務の重複チェック（コマ数問わず実施）
        for (String periodCode : periodCodes) {
            LocalTime periodStart = getPeriodStart(periodCode);
            LocalTime periodEnd = getPeriodEnd(periodCode);

            if (office.getStartTime() != null && office.getEndTime() != null) {
                if (isOverlapping(periodStart, periodEnd, office.getStartTime(), office.getEndTime())) {
                    throw new InvalidInputException("事務業務が授業コマの時間帯と重複しています");
                }
            }

            if (other.getStartTime() != null && other.getEndTime() != null) {
                if (isOverlapping(periodStart, periodEnd, other.getStartTime(), other.getEndTime())) {
                    throw new InvalidInputException("その他業務が授業コマの時間帯と重複しています");
                }
            }
        }

        if (office.getStartTime() != null && office.getEndTime() != null) {

            if (!office.getStartTime().isBefore(office.getEndTime())) {
                throw new InvalidInputException("事務業務の終了時刻は開始時刻より後にしてください");
            }

            if (other.getStartTime() != null && other.getEndTime() != null) {
                if (isOverlapping(office.getStartTime(), office.getEndTime(), other.getStartTime(), other.getEndTime())) {
                    int officeDuration = (int) Duration.between(office.getStartTime(), office.getEndTime()).toMinutes();
                    if (!isEnclosedWithinBreak(other.getStartTime(), other.getEndTime(), otherBreak, office.getStartTime(), office.getEndTime(), officeDuration)) {
                        throw new InvalidInputException("事務業務とその他業務の時間帯が重複しています");
                    }
                }
            }
        }

        if (other.getStartTime() != null && other.getEndTime() != null) {

            if (!other.getStartTime().isBefore(other.getEndTime())) {
                throw new InvalidInputException("その他業務の終了時刻は開始時刻より後にしてください");
            }

            if (Duration.between(other.getStartTime(), other.getEndTime()).toMinutes() < otherBreak) {
                throw new InvalidInputException("その他業務の休憩時間が勤務時間を超えています");
            }
        }

        // 逆転ケース防止: 授業時間帯内に配置された事務・その他の勤務時間合計が、
        // 授業の申告休憩時間を超えると、同じ時間が授業と別業務で二重に給与計上される。
        // (授業の申告休憩 = 授業時間帯内の空き時間のうち「別業務に振り替える時間 + 真の休憩」なので、
        //   実際の事務・その他の占有時間がそれを超えてはならない)
        if (validateLessonTimes) {

            LocalTime lessonStart = lesson.getStartTime();
            LocalTime lessonEnd = lesson.getEndTime();

            long occupiedMinutes = 0;

            if (office.getStartTime() != null && office.getEndTime() != null) {
                occupiedMinutes += clippedDuration(
                    office.getStartTime(), office.getEndTime(), lessonStart, lessonEnd);
            }

            if (other.getStartTime() != null && other.getEndTime() != null) {
                // その他は実働（その他の休憩を差し引いた時間）のみをカウントする
                long otherOccupied = clippedDuration(
                    other.getStartTime(), other.getEndTime(), lessonStart, lessonEnd) - otherBreak;
                occupiedMinutes += Math.max(otherOccupied, 0);
            }

            if (occupiedMinutes > lessonBreak) {
                throw new InvalidInputException("授業業務の休憩時間が不足しています");
            }
        }

    }

    private static long clippedDuration(LocalTime start, LocalTime end, LocalTime clipStart, LocalTime clipEnd) {
        LocalTime s = start.isAfter(clipStart) ? start : clipStart;
        LocalTime e = end.isBefore(clipEnd) ? end : clipEnd;
        if (!s.isBefore(e)) {
            return 0;
        }
        return Duration.between(s, e).toMinutes();
    }

    private static LocalTime getPeriodStart(String periodCode) {
        return switch (periodCode) {
            case "M" -> LessonContents.LESSON_M_START;
            case "K" -> LessonContents.LESSON_K_START;
            case "S" -> LessonContents.LESSON_S_START;
            case "A" -> LessonContents.LESSON_A_START;
            case "B" -> LessonContents.LESSON_B_START;
            case "C" -> LessonContents.LESSON_C_START;
            case "D" -> LessonContents.LESSON_D_START;
            default -> throw new InvalidInputException("不正なコマコードです");
        };
    }

    private static LocalTime getPeriodEnd(String periodCode) {
        return switch (periodCode) {
            case "M" -> LessonContents.LESSON_M_END;
            case "K" -> LessonContents.LESSON_K_END;
            case "S" -> LessonContents.LESSON_S_END;
            case "A" -> LessonContents.LESSON_A_END;
            case "B" -> LessonContents.LESSON_B_END;
            case "C" -> LessonContents.LESSON_C_END;
            case "D" -> LessonContents.LESSON_D_END;
            default -> throw new InvalidInputException("不正なコマコードです");
        };
    }

    private static boolean isOverlapping(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private static boolean isEnclosedWithinBreak(
        LocalTime outerStart, LocalTime outerEnd, int outerBreakMinutes,
        LocalTime innerStart, LocalTime innerEnd, int innerNetMinutes
    ) {
        return outerStart.isBefore(innerStart) &&
               outerEnd.isAfter(innerEnd) &&
               outerBreakMinutes >= innerNetMinutes;
    }

}
