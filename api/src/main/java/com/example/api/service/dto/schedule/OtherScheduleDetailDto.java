package com.example.api.service.dto.schedule;

import java.time.LocalTime;

import com.example.api.service.dto.template.OtherTemplateDetailDto;
import com.example.api.service.dto.work.OtherWorkDetailDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtherScheduleDetailDto {
    
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer breakMinutes;
    private String description;

    public static OtherScheduleDetailDto fromOtherWorkDetailDto(OtherWorkDetailDto work) {
        return new OtherScheduleDetailDto(
            work.getStartTime(),
            work.getEndTime(),
            work.getBreakMinutes(),
            work.getDescription()
        );
    }

    public static OtherScheduleDetailDto fromOtherTemplateDetailDto(OtherTemplateDetailDto template) {
        return new OtherScheduleDetailDto(
            template.getStartTime(),
            template.getEndTime(),
            template.getBreakMinutes(),
            template.getDescription()
        );
    }

}
