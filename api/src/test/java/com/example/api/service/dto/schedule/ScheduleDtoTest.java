package com.example.api.service.dto.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.api.service.dto.template.LessonTemplateDetailDto;
import com.example.api.service.dto.template.OfficeTemplateDetailDto;
import com.example.api.service.dto.template.OtherTemplateDetailDto;
import com.example.api.service.dto.template.TemplateDto;
import com.example.api.service.dto.work.LessonWorkDetailDto;
import com.example.api.service.dto.work.OfficeWorkDetailDto;
import com.example.api.service.dto.work.OtherWorkDetailDto;
import com.example.api.service.dto.work.WorkDto;

class ScheduleDtoTest {

    @Test
    @DisplayName("fromWorkDto: 3つの detail が ScheduleDto にマッピングされる")
    void fromWorkDto() {
        WorkDto work = new WorkDto();
        work.setLessonWorkDetailDto(new LessonWorkDetailDto(
            UUID.randomUUID(), UUID.randomUUID(),
            LocalTime.of(13, 0), LocalTime.of(15, 0), 20, List.of("S")));
        work.setOfficeWorkDetailDto(new OfficeWorkDetailDto(
            UUID.randomUUID(), UUID.randomUUID(),
            LocalTime.of(18, 0), LocalTime.of(19, 0)));
        work.setOtherWorkDetailDto(new OtherWorkDetailDto(
            UUID.randomUUID(), UUID.randomUUID(),
            LocalTime.of(20, 0), LocalTime.of(21, 0), 10, "training"));

        ScheduleDto dto = ScheduleDto.fromWorkDto(work);

        assertNotNull(dto.getLessonScheduleDetailDto());
        assertNotNull(dto.getOfficeScheduleDetailDto());
        assertNotNull(dto.getOtherScheduleDetailDto());

        assertEquals(LocalTime.of(13, 0), dto.getLessonScheduleDetailDto().getStartTime());
        assertEquals(List.of("S"), dto.getLessonScheduleDetailDto().getPeriodCodes());
        assertEquals(LocalTime.of(18, 0), dto.getOfficeScheduleDetailDto().getStartTime());
        assertEquals(10, dto.getOtherScheduleDetailDto().getBreakMinutes());
        assertEquals("training", dto.getOtherScheduleDetailDto().getDescription());
    }

    @Test
    @DisplayName("fromTemplateDto: 3つの detail が ScheduleDto にマッピングされる")
    void fromTemplateDto() {
        TemplateDto template = new TemplateDto();
        template.setLessonTemplateDetailDto(new LessonTemplateDetailDto(
            UUID.randomUUID(), UUID.randomUUID(),
            LocalTime.of(13, 0), LocalTime.of(15, 0), 20, List.of("S")));
        template.setOfficeTemplateDetailDto(new OfficeTemplateDetailDto(
            UUID.randomUUID(), UUID.randomUUID(),
            LocalTime.of(18, 0), LocalTime.of(19, 0)));
        template.setOtherTemplateDetailDto(new OtherTemplateDetailDto(
            UUID.randomUUID(), UUID.randomUUID(),
            LocalTime.of(20, 0), LocalTime.of(21, 0), 10, "training"));

        ScheduleDto dto = ScheduleDto.fromTemplateDto(template);

        assertEquals(LocalTime.of(13, 0), dto.getLessonScheduleDetailDto().getStartTime());
        assertEquals(LocalTime.of(18, 0), dto.getOfficeScheduleDetailDto().getStartTime());
        assertEquals(10, dto.getOtherScheduleDetailDto().getBreakMinutes());
    }
}
