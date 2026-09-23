package com.example.api.service.dto.schedule;

import java.time.LocalTime;
import java.util.List;

import com.example.api.service.dto.template.LessonTemplateDetailDto;
import com.example.api.service.dto.work.LessonWorkDetailDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonScheduleDetailDto {
    
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer breakMinutes;
    private List<String> periodCodes;

    public static LessonScheduleDetailDto fromLessonWorkDetailDto(LessonWorkDetailDto work) {
        return new LessonScheduleDetailDto(
            work.getStartTime(),
            work.getEndTime(),
            work.getBreakMinutes(),
            work.getPeriodCodes()
        );
    }

    public static LessonScheduleDetailDto fromLessonTemplateDetailDto(LessonTemplateDetailDto template) {
        return new LessonScheduleDetailDto(
            template.getStartTime(),
            template.getEndTime(),
            template.getBreakMinutes(),
            template.getPeriodCodes()
        );
    }

}
