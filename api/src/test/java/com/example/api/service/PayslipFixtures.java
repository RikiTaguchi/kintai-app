package com.example.api.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.example.api.mapper.entity.SalaryEntity;
import com.example.api.service.dto.SalaryDto;
import com.example.api.service.dto.work.LessonWorkDetailDto;
import com.example.api.service.dto.work.OfficeWorkDetailDto;
import com.example.api.service.dto.work.OtherWorkDetailDto;
import com.example.api.service.dto.work.WorkDto;

/**
 * PayslipService テスト用のビルダー。
 */
public final class PayslipFixtures {

    private PayslipFixtures() {}

    /** 毎時1000円、コマ2000円、通勤費なし、2020-01-01適用 */
    public static SalaryEntity basicSalary() {
        return salary(UUID.randomUUID(), UUID.randomUUID(), LocalDate.of(2020, 1, 1), 2000, 1000, 0);
    }

    public static SalaryEntity salary(
            UUID id, UUID tutorId, LocalDate effectiveDate,
            Integer lessonWage, Integer officeWage, Integer transportationFee) {
        SalaryEntity e = new SalaryEntity();
        e.setId(id);
        e.setTutorId(tutorId);
        e.setEffectiveDate(effectiveDate);
        e.setLessonWage(lessonWage);
        e.setOfficeWage(officeWage);
        e.setTransportationFee(transportationFee);
        return e;
    }

    public static SalaryDto salaryDto(
            LocalDate effectiveDate, Integer lessonWage, Integer officeWage, Integer transportationFee) {
        return new SalaryDto(UUID.randomUUID(), UUID.randomUUID(), effectiveDate, lessonWage, officeWage, transportationFee);
    }

    /** 空の WorkDto のテンプレート。子 detail はnullだが、講師系の計算が動くよう必須フィールドは埋める */
    public static WorkDto workBase(UUID tutorId, LocalDate workingDate) {
        WorkDto w = new WorkDto();
        w.setId(UUID.randomUUID());
        w.setTutorId(tutorId);
        w.setWorkingDate(workingDate);
        w.setDailyAllowance(0);
        w.setTransportationFee(0);
        // PayslipService は全 detail が非nullの想定
        w.setLessonWorkDetailDto(new LessonWorkDetailDto(null, null, null, null, null, List.of()));
        w.setOfficeWorkDetailDto(new OfficeWorkDetailDto(null, null, null, null));
        w.setOtherWorkDetailDto(new OtherWorkDetailDto(null, null, null, null, null, null));
        return w;
    }

    public static LessonWorkDetailDto lesson(
            LocalTime start, LocalTime end, Integer breakMin, List<String> periodCodes) {
        return new LessonWorkDetailDto(UUID.randomUUID(), UUID.randomUUID(), start, end, breakMin, periodCodes);
    }

    public static OfficeWorkDetailDto office(LocalTime start, LocalTime end) {
        return new OfficeWorkDetailDto(UUID.randomUUID(), UUID.randomUUID(), start, end);
    }

    public static OtherWorkDetailDto other(LocalTime start, LocalTime end, Integer breakMin, String desc) {
        return new OtherWorkDetailDto(UUID.randomUUID(), UUID.randomUUID(), start, end, breakMin, desc);
    }

    public static LocalTime t(int h, int m) {
        return LocalTime.of(h, m);
    }
}
