package com.example.api.service.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalaryDto {
    
    private UUID id;
    private UUID tutorId;
    private LocalDate effectiveDate;
    private Integer lessonWage;
    private Integer officeWage;
    private Integer transportationFee;

}
