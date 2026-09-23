package com.example.api.service.dto.template;

import java.time.LocalTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfficeTemplateDetailDto {
    
    private UUID id;
    private UUID templateId;
    private LocalTime startTime;
    private LocalTime endTime;

}
