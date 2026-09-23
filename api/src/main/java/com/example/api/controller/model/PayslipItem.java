package com.example.api.controller.model;

import com.example.api.service.dto.payslip.PayslipItemDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayslipItem {
    
    private Integer amount;
    private Integer minutes;

    public static PayslipItem fromDto(PayslipItemDto dto) {
        return new PayslipItem(
            dto.getAmount(),
            dto.getMinutes()
        );
    }

}
