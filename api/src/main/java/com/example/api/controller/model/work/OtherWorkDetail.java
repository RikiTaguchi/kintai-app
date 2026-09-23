package com.example.api.controller.model.work;

import java.time.LocalTime;

import com.example.api.service.dto.work.OtherWorkDetailDto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtherWorkDetail {

    private LocalTime startTime;
    private LocalTime endTime;
    @Min(value = 0, message = "Break minutes must be zero or a positive number")
    private Integer breakMinutes;
    @Size(max = 500, message = "Description must be 500 characters or less")
    private String description;

    public OtherWorkDetailDto toDto() {
        OtherWorkDetailDto dto = new OtherWorkDetailDto();
        dto.setStartTime(this.startTime);
        dto.setEndTime(this.endTime);
        dto.setBreakMinutes(this.breakMinutes);
        dto.setDescription(this.description);
        return dto;
    }

    public static OtherWorkDetail fromDto(OtherWorkDetailDto dto) {
        return new OtherWorkDetail(
            dto.getStartTime(),
            dto.getEndTime(),
            dto.getBreakMinutes(),
            dto.getDescription()
        );
    }

}
