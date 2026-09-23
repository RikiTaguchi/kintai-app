package com.example.api;

import java.time.LocalTime;
import java.util.List;

import com.example.api.service.dto.schedule.LessonScheduleDetailDto;
import com.example.api.service.dto.schedule.OfficeScheduleDetailDto;
import com.example.api.service.dto.schedule.OtherScheduleDetailDto;
import com.example.api.service.dto.schedule.ScheduleDto;

/**
 * ScheduleValidator 系テストのためのビルダー。
 * 各メソッドで必要な部分だけ上書きして {@link ScheduleDto} を組み立てる。
 */
public final class ScheduleFixtures {

    private ScheduleFixtures() {}

    /** 何も入力されていない状態の ScheduleDto（3つの detail は非null） */
    public static ScheduleDto emptySchedule() {
        return new ScheduleDto(
            new LessonScheduleDetailDto(null, null, null, List.of()),
            new OfficeScheduleDetailDto(null, null),
            new OtherScheduleDetailDto(null, null, null, null)
        );
    }

    public static LessonScheduleDetailDto lesson(
            LocalTime start,
            LocalTime end,
            Integer breakMinutes,
            List<String> periodCodes) {
        return new LessonScheduleDetailDto(start, end, breakMinutes, periodCodes);
    }

    public static OfficeScheduleDetailDto office(LocalTime start, LocalTime end) {
        return new OfficeScheduleDetailDto(start, end);
    }

    public static OtherScheduleDetailDto other(
            LocalTime start,
            LocalTime end,
            Integer breakMinutes,
            String description) {
        return new OtherScheduleDetailDto(start, end, breakMinutes, description);
    }

    public static ScheduleDto schedule(
            LessonScheduleDetailDto lesson,
            OfficeScheduleDetailDto office,
            OtherScheduleDetailDto other) {
        return new ScheduleDto(lesson, office, other);
    }

    public static LocalTime time(int hour, int minute) {
        return LocalTime.of(hour, minute);
    }
}
