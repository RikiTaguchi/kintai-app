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

    /**
     * 超過勤務割増のスケール分母。
     * 授業給(100分あたり)を60分(時給)へ換算して割増率を掛けるため、
     * 分 × lessonWage × 60 × 25 を累積し、最後に (100 × 60 × 100) で割って円に換算する。
     * （25/100 = 割増率 0.25、小数誤差を避けるため整数演算で保持する）
     */
    private static final long OVERTIME_SCALE_DENOMINATOR = 100L * 60 * 100;

    /**
     * 深夜割増のスケール分母。
     * 分 × officeWage × 25 を累積し、最後に (60 × 100) で割って円に換算する。
     */
    private static final long NIGHT_SCALE_DENOMINATOR = 60L * 100;

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
        long officeWorkPay = 0;
        int transportationFee = 0;
        // 割増系を含む全項目は小数点以下を保持できるスケール値（long）で累積し、最後に一度だけ円へ切り上げ換算する
        long overtimePremiumScaled = 0;
        long nightShiftPremiumScaled = 0;

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

            overtimePremiumScaled += calculateOvertimePremiumScaled(work, salary);
            overtimePremium.setMinutes(
                overtimePremium.getMinutes() + calculateOvertimeExcessMinutes(work));

            nightShiftPremiumScaled += calculateNightShiftPremiumScaled(work, salary);
            nightShiftPremium.setMinutes(
                nightShiftPremium.getMinutes() + calculateNightMinutesForWork(work));

            transportationFee += work.getTransportationFee();
        }

        dto.setLessonPay(lessonPay);
        dto.setPeriodCount(periodCount);
        dto.setDailyAllowance(dailyAllowance);
        dto.setOfficeWorkPay(toYenCeil(officeWorkPay));
        dto.setTrainingAndStudyRoomDto(formatMinuteItem(trainingAndStudyRoom));
        dto.setOutsideHoursWorkDto(formatMinuteItem(outsideHoursWork));
        overtimePremium.setAmount(toYenCeil(overtimePremiumScaled, OVERTIME_SCALE_DENOMINATOR));
        dto.setOvertimePremiumDto(overtimePremium);
        nightShiftPremium.setAmount(toYenCeil(nightShiftPremiumScaled, NIGHT_SCALE_DENOMINATOR));
        dto.setNightShiftPremiumDto(nightShiftPremium);
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

    /**
     * 事務給を「分 × 時給」のスケール（1/60円単位）で返す。
     * 日次で円への端数処理は行わず、月合計の最後に一度だけ切り上げる。
     */
    private long calculateOfficeWorkPay(WorkDto work, SalaryDto salary) {
        var detail = work.getOfficeWorkDetailDto();
        if (detail.getStartTime() != null && detail.getEndTime() != null) {
            return Duration.between(detail.getStartTime(), detail.getEndTime()).toMinutes() * salary.getOfficeWage();
        }
        return 0;
    }

    private void accumulateOtherWorkPay(WorkDto work, SalaryDto salary, PayslipItemDto item) {
        var detail = work.getOtherWorkDetailDto();
        if (detail.getStartTime() != null && detail.getEndTime() != null) {
            int minutes = (int) (Duration.between(detail.getStartTime(), detail.getEndTime()).toMinutes() - detail.getBreakMinutes());
            item.setScaledAmount(item.getScaledAmount() + (long) minutes * salary.getOfficeWage());
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
            item.setScaledAmount(item.getScaledAmount() + (long) outsideHours * salary.getOfficeWage());
            item.setMinutes(item.getMinutes() + outsideHours);
        }
    }

    /**
     * 超過勤務割増をスケール値（分 × lessonWage × 60 × 25）で返す。
     * 計算時給は事務給ではなく授業給を用い、100分あたりの登録値を60分あたりに換算してから割増率0.25を掛ける。
     * 日次の端数処理は行わず、月合計の最後に一度だけ円へ切り上げ換算する。
     */
    private long calculateOvertimePremiumScaled(WorkDto work, SalaryDto salary) {
        var detail = work.getLessonWorkDetailDto();
        if (detail.getStartTime() == null || detail.getEndTime() == null) {
            return 0;
        }

        int overtime = Math.max(
            (int) (Duration.between(detail.getStartTime(), detail.getEndTime()).toMinutes() -
            detail.getBreakMinutes()),
            0
        );

        if (overtime > PremiumPayContents.OVER_TIME_BORDER_LINE) {
            int excessMinutes = overtime - PremiumPayContents.OVER_TIME_BORDER_LINE;
            // 分 × 授業給(100分あたり) × 60/100(時給換算) × 25/100(割増率) を整数スケールで保持
            return (long) excessMinutes * salary.getLessonWage() * 60 * 25;
        }
        return 0;
    }

    /**
     * 超過勤務割増の対象分数のみを返す（表示用の minutes 集計に使用）。
     */
    private int calculateOvertimeExcessMinutes(WorkDto work) {
        var detail = work.getLessonWorkDetailDto();
        if (detail.getStartTime() == null || detail.getEndTime() == null) {
            return 0;
        }

        int overtime = Math.max(
            (int) (Duration.between(detail.getStartTime(), detail.getEndTime()).toMinutes() -
            detail.getBreakMinutes()),
            0
        );

        if (overtime > PremiumPayContents.OVER_TIME_BORDER_LINE) {
            return overtime - PremiumPayContents.OVER_TIME_BORDER_LINE;
        }
        return 0;
    }

    /**
     * 深夜割増をスケール値（分 × officeWage × 25）で返す。
     * 日次の端数処理は行わず、月合計の最後に一度だけ円へ切り上げ換算する。
     */
    private long calculateNightShiftPremiumScaled(WorkDto work, SalaryDto salary) {
        long nightMinutes = calculateNightMinutesForWork(work);
        return nightMinutes * salary.getOfficeWage() * 25;
    }

    /**
     * 勤務1件あたりの深夜対象分数（lesson / office / other の合計）を返す。
     */
    private int calculateNightMinutesForWork(WorkDto work) {
        int nightMinutes = 0;
        nightMinutes += calculateNightMinutes(work.getLessonWorkDetailDto().getStartTime(), work.getLessonWorkDetailDto().getEndTime());
        nightMinutes += calculateNightMinutes(work.getOfficeWorkDetailDto().getStartTime(), work.getOfficeWorkDetailDto().getEndTime());
        nightMinutes += calculateNightMinutes(work.getOtherWorkDetailDto().getStartTime(), work.getOtherWorkDetailDto().getEndTime());
        return nightMinutes;
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
        item.setAmount(toYenCeil(item.getScaledAmount()));
        return item;
    }

    /** 1/60円スケール値を円へ切り上げ換算する（分 × 時給 のスケール）。 */
    private int toYenCeil(long scaledAmount) {
        return toYenCeil(scaledAmount, 60L);
    }

    /** 任意スケール値を円へ切り上げ換算する。 */
    private int toYenCeil(long scaledAmount, long denominator) {
        return (int) Math.ceil((double) scaledAmount / denominator);
    }

}
