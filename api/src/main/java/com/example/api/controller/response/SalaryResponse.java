package com.example.api.controller.response;

import java.time.LocalDate;
import java.util.UUID;

import com.example.api.service.dto.SalaryDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalaryResponse {
    
    private UUID id;
    private UUID tutorId;
    private LocalDate effectiveDate;
    private Integer lessonWage;
    private Integer officeWage;
    private Integer transportationFee;

    public static SalaryResponse fromDto(SalaryDto dto) {
        return new SalaryResponse(
            dto.getId(),
            dto.getTutorId(),
            dto.getEffectiveDate(),
            dto.getLessonWage(),
            dto.getOfficeWage(),
            dto.getTransportationFee()
        );
    }

}
