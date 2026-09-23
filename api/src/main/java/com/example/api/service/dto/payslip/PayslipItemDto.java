package com.example.api.service.dto.payslip;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayslipItemDto {
    
    private Integer amount;
    private Integer minutes;
    
}
