package com.example.api.controller.model.work;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkDateRange {
    
    private LocalDate dateFrom;
    private LocalDate dateTo;

}
