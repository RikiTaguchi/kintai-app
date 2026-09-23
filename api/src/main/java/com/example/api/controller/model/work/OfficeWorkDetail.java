package com.example.api.controller.model.work;

import java.time.LocalTime;

import com.example.api.service.dto.work.OfficeWorkDetailDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OfficeWorkDetail {
    
    private LocalTime startTime;
    private LocalTime endTime;

    public OfficeWorkDetailDto toDto() {
        OfficeWorkDetailDto dto = new OfficeWorkDetailDto();
        dto.setStartTime(this.startTime);
        dto.setEndTime(this.endTime);
        return dto;
    }

    public static OfficeWorkDetail fromDto(OfficeWorkDetailDto dto) {
        return new OfficeWorkDetail(
            dto.getStartTime(),
            dto.getEndTime()
        );
    }

}
