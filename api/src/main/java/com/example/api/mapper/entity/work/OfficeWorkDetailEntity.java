package com.example.api.mapper.entity.work;

import java.time.LocalTime;
import java.util.UUID;

import com.example.api.service.dto.work.OfficeWorkDetailDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfficeWorkDetailEntity {
    
    private UUID id;
    private UUID workId;
    private LocalTime startTime;
    private LocalTime endTime;

    public OfficeWorkDetailDto toDto() {
        return new OfficeWorkDetailDto(
            this.id,
            this.workId,
            this.startTime,
            this.endTime
        );
    }

    public static OfficeWorkDetailEntity fromDto(OfficeWorkDetailDto dto) {
        return new OfficeWorkDetailEntity(
            dto.getId(),
            dto.getWorkId(),
            dto.getStartTime(),
            dto.getEndTime()
        );
    }

}
