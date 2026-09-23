package com.example.api.controller.model.template;

import java.time.LocalTime;
import java.util.List;

import com.example.api.service.dto.template.LessonTemplateDetailDto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LessonTemplateDetail {

    private LocalTime startTime;
    private LocalTime endTime;
    @Min(value = 0, message = "Break minutes must be zero or a positive number")
    private Integer breakMinutes;
    @NotNull(message = "Period codes must not be null")
    private List<String> periodCodes;

    public LessonTemplateDetailDto toDto() {
        LessonTemplateDetailDto dto = new LessonTemplateDetailDto();
        dto.setStartTime(this.startTime);
        dto.setEndTime(this.endTime);
        dto.setBreakMinutes(this.breakMinutes);
        dto.setPeriodCodes(this.periodCodes);
        return dto;
    }

    public static LessonTemplateDetail fromDto(LessonTemplateDetailDto dto) {
        return new LessonTemplateDetail(
            dto.getStartTime(),
            dto.getEndTime(),
            dto.getBreakMinutes(),
            dto.getPeriodCodes()
        );
    }

}
