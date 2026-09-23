package com.example.api.controller.response;

import java.util.UUID;

import com.example.api.controller.model.PayslipItem;
import com.example.api.service.dto.payslip.PayslipDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayslipResponse {
    
    private UUID tutorId;
    private Integer lessonPay;
    private Integer periodCount;
    private Integer dailyAllowance;
    private Integer officeWorkPay;
    private PayslipItem trainingAndStudyRoom;
    private PayslipItem outsideHoursWork;
    private PayslipItem overtimePremium;
    private PayslipItem nightShiftPremium;
    private Integer otherPay;
    private Integer transportationFee;

    public static PayslipResponse fromDto(PayslipDto dto) {

        return new PayslipResponse(
            dto.getTutorId(),
            dto.getLessonPay(),
            dto.getPeriodCount(),
            dto.getDailyAllowance(),
            dto.getOfficeWorkPay(),
            PayslipItem.fromDto(dto.getTrainingAndStudyRoomDto()),
            PayslipItem.fromDto(dto.getOutsideHoursWorkDto()),
            PayslipItem.fromDto(dto.getOvertimePremiumDto()),
            PayslipItem.fromDto(dto.getNightShiftPremiumDto()),
            dto.getOtherPay(),
            dto.getTransportationFee()
        );
        
    }

}
