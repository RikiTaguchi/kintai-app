package com.example.api.service.dto.template;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonTemplateDetailDto {
    
    private UUID id;
    private UUID templateId;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer breakMinutes;
    private List<String> periodCodes;

}
