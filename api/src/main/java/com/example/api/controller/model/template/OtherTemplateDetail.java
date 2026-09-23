package com.example.api.controller.model.template;

import java.time.LocalTime;

import com.example.api.service.dto.template.OtherTemplateDetailDto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtherTemplateDetail {

    private LocalTime startTime;
    private LocalTime endTime;
    @Min(value = 0, message = "Break minutes must be zero or a positive number")
    private Integer breakMinutes;
    @Size(max = 500, message = "Description must be 500 characters or less")
    private String description;

    public OtherTemplateDetailDto toDto() {
        OtherTemplateDetailDto dto = new OtherTemplateDetailDto();
        dto.setStartTime(this.startTime);
        dto.setEndTime(this.endTime);
        dto.setBreakMinutes(this.breakMinutes);
        dto.setDescription(this.description);
        return dto;
    }

    public static OtherTemplateDetail fromDto(OtherTemplateDetailDto dto) {
        return new OtherTemplateDetail(
            dto.getStartTime(),
            dto.getEndTime(),
            dto.getBreakMinutes(),
            dto.getDescription()
        );
    }

}
