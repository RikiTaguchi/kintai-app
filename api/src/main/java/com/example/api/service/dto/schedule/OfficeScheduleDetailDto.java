package com.example.api.service.dto.schedule;

import java.time.LocalTime;

import com.example.api.service.dto.template.OfficeTemplateDetailDto;
import com.example.api.service.dto.work.OfficeWorkDetailDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficeScheduleDetailDto {

    private LocalTime startTime;
    private LocalTime endTime;

    public static OfficeScheduleDetailDto fromOfficeWorkDetailDto(OfficeWorkDetailDto work) {
        return new OfficeScheduleDetailDto(
            work.getStartTime(),
            work.getEndTime()
        );
    }

    public static OfficeScheduleDetailDto fromOfficeTemplateDetailDto(OfficeTemplateDetailDto template) {
        return new OfficeScheduleDetailDto(
            template.getStartTime(),
            template.getEndTime()
        );
    }

}
