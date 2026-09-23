package com.example.api.mapper.entity.work;

import java.time.LocalTime;
import java.util.UUID;

import com.example.api.service.dto.work.OtherWorkDetailDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtherWorkDetailEntity {
    
    private UUID id;
    private UUID workId;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer breakMinutes;
    private String description;

    public OtherWorkDetailDto toDto() {
        return new OtherWorkDetailDto(
            this.id,
            this.workId,
            this.startTime,
            this.endTime,
            this.breakMinutes,
            this.description
        );
    }

    public static OtherWorkDetailEntity fromDto(OtherWorkDetailDto dto) {
        return new OtherWorkDetailEntity(
            dto.getId(),
            dto.getWorkId(),
            dto.getStartTime(),
            dto.getEndTime(),
            dto.getBreakMinutes(),
            dto.getDescription()
        );
    }

}
