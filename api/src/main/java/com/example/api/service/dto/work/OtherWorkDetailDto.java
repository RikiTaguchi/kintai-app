package com.example.api.service.dto.work;

import java.time.LocalTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtherWorkDetailDto {
    
    private UUID id;
    private UUID workId;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer breakMinutes;
    private String description;

}
