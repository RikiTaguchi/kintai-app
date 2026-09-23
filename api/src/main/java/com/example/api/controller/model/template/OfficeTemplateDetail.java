package com.example.api.controller.model.template;

import java.time.LocalTime;

import com.example.api.service.dto.template.OfficeTemplateDetailDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OfficeTemplateDetail {
    
    private LocalTime startTime;
    private LocalTime endTime;

    public OfficeTemplateDetailDto toDto() {
        OfficeTemplateDetailDto dto = new OfficeTemplateDetailDto();
        dto.setStartTime(this.startTime);
        dto.setEndTime(this.endTime);
        return dto;
    }

    public static OfficeTemplateDetail fromDto(OfficeTemplateDetailDto dto) {
        return new OfficeTemplateDetail(
            dto.getStartTime(),
            dto.getEndTime()
        );
    }

}
