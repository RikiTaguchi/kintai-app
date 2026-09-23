package com.example.api.mapper.entity.template;

import java.time.LocalTime;
import java.util.UUID;

import com.example.api.service.dto.template.OtherTemplateDetailDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtherTemplateDetailEntity {
    
    private UUID id;
    private UUID templateId;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer breakMinutes;
    private String description;

    public OtherTemplateDetailDto toDto() {
        return new OtherTemplateDetailDto(
            this.id,
            this.templateId,
            this.startTime,
            this.endTime,
            this.breakMinutes,
            this.description
        );
    }

    public static OtherTemplateDetailEntity fromDto(OtherTemplateDetailDto dto) {
        return new OtherTemplateDetailEntity(
            dto.getId(),
            dto.getTemplateId(),
            dto.getStartTime(),
            dto.getEndTime(),
            dto.getBreakMinutes(),
            dto.getDescription()
        );
    }

}
