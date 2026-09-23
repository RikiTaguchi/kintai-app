package com.example.api.mapper.entity;

import java.time.LocalDate;
import java.util.UUID;

import com.example.api.service.dto.SalaryDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalaryEntity {
    
    private UUID id;
    private UUID tutorId;
    private LocalDate effectiveDate;
    private Integer lessonWage;
    private Integer officeWage;
    private Integer transportationFee;

    public SalaryDto toDto() {
        return new SalaryDto(
            this.id,
            this.tutorId,
            this.effectiveDate,
            this.lessonWage,
            this.officeWage,
            this.transportationFee
        );
    }

    public static SalaryEntity fromDto(SalaryDto dto) {
        return new SalaryEntity(
            dto.getId(),
            dto.getTutorId(),
            dto.getEffectiveDate(),
            dto.getLessonWage(),
            dto.getOfficeWage(),
            dto.getTransportationFee()
        );
    }

}
