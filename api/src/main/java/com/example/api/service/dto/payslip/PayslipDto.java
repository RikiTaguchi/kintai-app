package com.example.api.service.dto.payslip;

import java.util.UUID;

import lombok.Data;

@Data
public class PayslipDto {

    private UUID tutorId;
    private Integer lessonPay = 0;
    private Integer periodCount = 0;
    private Integer dailyAllowance = 0;
    private Integer officeWorkPay = 0;
    private PayslipItemDto trainingAndStudyRoomDto = new PayslipItemDto(0, 0);
    private PayslipItemDto outsideHoursWorkDto = new PayslipItemDto(0, 0);
    private PayslipItemDto overtimePremiumDto = new PayslipItemDto(0, 0);
    private PayslipItemDto nightShiftPremiumDto = new PayslipItemDto(0, 0);
    private Integer otherPay = 0;
    private Integer transportationFee = 0;

}
