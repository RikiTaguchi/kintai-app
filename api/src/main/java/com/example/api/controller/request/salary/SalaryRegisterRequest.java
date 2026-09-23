package com.example.api.controller.request.salary;

import java.time.LocalDate;
import java.util.UUID;

import com.example.api.service.dto.SalaryDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class SalaryRegisterRequest {
    
    @NotNull(message = "Tutor id is required")
    private UUID tutorId;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    @NotNull(message = "Lesson wage is required")
    @Positive(message = "Lesson wage must be a positive number")
    private Integer lessonWage;

    @NotNull(message = "Office wage is required")
    @Positive(message = "Office wage must be a positive number")
    private Integer officeWage;

    @NotNull(message = "Transportation fee is required")
    @PositiveOrZero(message = "Transportation fee must be zero or a positive number")
    private Integer transportationFee;

    public SalaryDto toDto() {
        SalaryDto dto = new SalaryDto();
        dto.setTutorId(this.tutorId);
        dto.setEffectiveDate(this.effectiveDate);
        dto.setLessonWage(this.lessonWage);
        dto.setOfficeWage(this.officeWage);
        dto.setTransportationFee(this.transportationFee);
        return dto;
    }

}
