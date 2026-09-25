package com.example.api.service.dto.payslip;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PayslipItemDto {

    private Integer amount = 0;
    private Integer minutes = 0;
    /**
     * 金額の累積用スケール値（分 × 時給など 1/60 円相当）。
     * サービス内部でのみ使用し、レスポンスには含めない。
     */
    @JsonIgnore
    private Long scaledAmount = 0L;

    public PayslipItemDto(Integer amount, Integer minutes) {
        this.amount = amount;
        this.minutes = minutes;
        this.scaledAmount = 0L;
    }

}
