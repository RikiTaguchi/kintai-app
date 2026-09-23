package com.example.api.mapper.entity.template;

import java.time.LocalTime;
import java.util.UUID;

import com.example.api.service.dto.template.OfficeTemplateDetailDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficeTemplateDetailEntity {
    
    private UUID id;
    private UUID templateId;
    private LocalTime startTime;
    private LocalTime endTime;

    public OfficeTemplateDetailDto toDto() {
        return new OfficeTemplateDetailDto(
            this.id,
            this.templateId,
            this.startTime,
            this.endTime
        );
    }

    public static OfficeTemplateDetailEntity fromDto(OfficeTemplateDetailDto dto) {
        return new OfficeTemplateDetailEntity(
            dto.getId(),
            dto.getTemplateId(),
            dto.getStartTime(),
            dto.getEndTime()
        );
    }

}
