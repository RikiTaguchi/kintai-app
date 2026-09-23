package com.example.api.service.dto.schedule;

import com.example.api.service.dto.template.TemplateDto;
import com.example.api.service.dto.work.WorkDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDto {
    
    private LessonScheduleDetailDto lessonScheduleDetailDto;
    private OfficeScheduleDetailDto officeScheduleDetailDto;
    private OtherScheduleDetailDto otherScheduleDetailDto;

    public static ScheduleDto fromWorkDto(WorkDto work) {
        return new ScheduleDto(
            LessonScheduleDetailDto.fromLessonWorkDetailDto(work.getLessonWorkDetailDto()),
            OfficeScheduleDetailDto.fromOfficeWorkDetailDto(work.getOfficeWorkDetailDto()),
            OtherScheduleDetailDto.fromOtherWorkDetailDto(work.getOtherWorkDetailDto())
        );
    }

    public static ScheduleDto fromTemplateDto(TemplateDto template) {
        return new ScheduleDto(
            LessonScheduleDetailDto.fromLessonTemplateDetailDto(template.getLessonTemplateDetailDto()),
            OfficeScheduleDetailDto.fromOfficeTemplateDetailDto(template.getOfficeTemplateDetailDto()),
            OtherScheduleDetailDto.fromOtherTemplateDetailDto(template.getOtherTemplateDetailDto())
        );
    }

}
