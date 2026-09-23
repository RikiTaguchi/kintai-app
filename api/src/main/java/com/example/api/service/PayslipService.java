package com.example.api.service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.api.common.constant.LessonContents;
import com.example.api.common.constant.PremiumPayContents;
import com.example.api.mapper.SalaryMapper;
import com.example.api.mapper.entity.SalaryEntity;
import com.example.api.service.dto.SalaryDto;
import com.example.api.service.dto.payslip.PayslipDto;
import com.example.api.service.dto.payslip.PayslipItemDto;
import com.example.api.service.dto.work.WorkDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayslipService {

    private final SalaryMapper salaryMapper;

    public PayslipDto create(List<WorkDto> works) {

        if (works.isEmpty()) {
            return new PayslipDto();
        }

        List<SalaryDto> salaries = salaryMapper.selectAll(works.getFirst().getTutorId())
            .stream()
            .map(SalaryEntity::toDto)
            .toList();

        if (salaries.isEmpty()) {
            return new PayslipDto();
        }

        return calculatePayslip(works, salaries);

    }

    private PayslipDto calculatePayslip(List<WorkDto> works, List<SalaryDto> salaries) {

        PayslipDto dto = new PayslipDto();
        if (works.isEmpty()) return dto;

        List<SalaryDto> sortedSalaries = salaries.stream()
            .sorted(Comparator.comparing(SalaryDto::getEffectiveDate))
            .toList()
            .reversed();

        dto.setTutorId(works.getFirst().getTutorId());

        PayslipItemDto trainingAndStudyRoom = dto.getTrainingAndStudyRoomDto();
        PayslipItemDto outsideHoursWork = dto.getOutsideHoursWorkDto();
        PayslipItemDto overtimePremium = dto.getOvertimePremiumDto();
        PayslipItemDto nightShiftPremium = dto.getNightShiftPremiumDto();

        int lessonPay = 0;
        int periodCount = 0;
        int dailyAllowance = 0;
        int officeWorkPay = 0;
        int transportationFee = 0;

        for (WorkDto work : works) {
            SalaryDto salary = findSalaryForWork(sortedSalaries, work);
            if (salary == null) {
                log.warn("No applicable salary found for work {} on {}, skipping", work.getId(), work.getWorkingDate());
                continue;
            }

            int periods = work.getLessonWorkDetailDto().getPeriodCodes().size();
            lessonPay += periods * salary.getLessonWage();
            periodCount += periods;
            dailyAllowance += work.getDailyAllowance();

            officeWorkPay += calculateOfficeWorkPay(work, salary);

            accumulateOtherWorkPay(work, salary, trainingAndStudyRoom);

            accumulateOutsideHoursWork(work, salary, outsideHoursWork);

            accumulateOvertimePremium(work, salary, overtimePremium);

            accumulateNightShiftPremium(work, salary, nightShiftPremium);

            transportationFee += work.getTransportationFee();
        }

        dto.setLessonPay(lessonPay);
        dto.setPeriodCount(periodCount);
        dto.setDailyAllowance(dailyAllowance);
        dto.setOfficeWorkPay((int) Math.ceil((double) officeWorkPay / 60));
        dto.setTrainingAndStudyRoomDto(formatMinuteItem(trainingAndStudyRoom));
        dto.setOutsideHoursWorkDto(formatMinuteItem(outsideHoursWork));
        dto.setOvertimePremiumDto(formatMinuteItem(overtimePremium));
        dto.setNightShiftPremiumDto(formatMinuteItem(nightShiftPremium));
        dto.setOtherPay(0);
        dto.setTransportationFee(transportationFee);

        return dto;

    }

    private SalaryDto findSalaryForWork(List<SalaryDto> sortedSalaries, WorkDto work) {
        return sortedSalaries.stream()
            .filter(target -> !target.getEffectiveDate().isAfter(work.getWorkingDate()))
            .findFirst()
            .orElse(null);
    }

    private int calculateOfficeWorkPay(WorkDto work, SalaryDto salary) {
        var detail = work.getOfficeWorkDetailDto();
        if (detail.getStartTime() != null && detail.getEndTime() != null) {
            return (int) (Duration.between(detail.getStartTime(), detail.getEndTime()).toMinutes() * salary.getOfficeWage());
        }
        return 0;
    }

    private void accumulateOtherWorkPay(WorkDto work, SalaryDto salary, PayslipItemDto item) {
        var detail = work.getOtherWorkDetailDto();
        if (detail.getStartTime() != null && detail.getEndTime() != null) {
            int minutes = (int) (Duration.between(detail.getStartTime(), detail.getEndTime()).toMinutes() - detail.getBreakMinutes());
            item.setAmount(item.getAmount() + (minutes * salary.getOfficeWage()));
            item.setMinutes(item.getMinutes() + minutes);
        }
    }

    private void accumulateOutsideHoursWork(WorkDto work, SalaryDto salary, PayslipItemDto item) {
        var detail = work.getLessonWorkDetailDto();
        if (detail.getPeriodCodes().size() >= LessonContents.LESSON_OUT_SIDE_HOURS_BORDER) {
            int outsideHours = Math.max(
                (int) (Duration.between(detail.getStartTime(), detail.getEndTime()).toMinutes() -
                detail.getPeriodCodes().size() * LessonContents.LESSON_MINUTES -
                LessonContents.LESSON_PREPARING_MINUTES -
                detail.getBreakMinutes()),
                0
            );
            item.setAmount(item.getAmount() + (outsideHours * salary.getOfficeWage()));
            item.setMinutes(item.getMinutes() + outsideHours);
        }
    }

    private void accumulateOvertimePremium(WorkDto work, SalaryDto salary, PayslipItemDto item) {
        var detail = work.getLessonWorkDetailDto();
        if (detail.getStartTime() != null && detail.getEndTime() != null) {
            int overtime = Math.max(
                (int) (Duration.between(detail.getStartTime(), detail.getEndTime()).toMinutes() -
                LessonContents.LESSON_PREPARING_MINUTES -
                detail.getBreakMinutes()),
                0
            );

            if (overtime > PremiumPayContents.OVER_TIME_BORDER_LINE) {
                item.setAmount(item.getAmount() + (int) (overtime * salary.getOfficeWage() * PremiumPayContents.PREMIUM_PAY_RATE));
                item.setMinutes(item.getMinutes() + overtime);
            }
        }
    }

    private void accumulateNightShiftPremium(WorkDto work, SalaryDto salary, PayslipItemDto item) {
        int nightMinutes = 0;
        nightMinutes += calculateNightMinutes(work.getLessonWorkDetailDto().getStartTime(), work.getLessonWorkDetailDto().getEndTime());
        nightMinutes += calculateNightMinutes(work.getOfficeWorkDetailDto().getStartTime(), work.getOfficeWorkDetailDto().getEndTime());
        nightMinutes += calculateNightMinutes(work.getOtherWorkDetailDto().getStartTime(), work.getOtherWorkDetailDto().getEndTime());

        item.setAmount(item.getAmount() + (int) (nightMinutes * salary.getOfficeWage() * PremiumPayContents.PREMIUM_PAY_RATE));
        item.setMinutes(item.getMinutes() + nightMinutes);
    }

    private int calculateNightMinutes(LocalTime start, LocalTime end) {
        if (start == null || end == null) return 0;

        if (start.isAfter(PremiumPayContents.NIGHT_START_TIME)) {
            return (int) Duration.between(start, end).toMinutes();
        } else if (end.isAfter(PremiumPayContents.NIGHT_START_TIME)) {
            return (int) Duration.between(PremiumPayContents.NIGHT_START_TIME, end).toMinutes();
        }
        return 0;
    }

    private PayslipItemDto formatMinuteItem(PayslipItemDto item) {
        item.setAmount((int) Math.ceil((double) item.getAmount() / 60));
        return item;
    }

}
