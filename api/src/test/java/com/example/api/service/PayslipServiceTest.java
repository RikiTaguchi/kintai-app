package com.example.api.service;

import static com.example.api.service.PayslipFixtures.lesson;
import static com.example.api.service.PayslipFixtures.office;
import static com.example.api.service.PayslipFixtures.other;
import static com.example.api.service.PayslipFixtures.salary;
import static com.example.api.service.PayslipFixtures.t;
import static com.example.api.service.PayslipFixtures.workBase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.api.mapper.SalaryMapper;
import com.example.api.service.dto.payslip.PayslipDto;
import com.example.api.service.dto.work.WorkDto;

@ExtendWith(MockitoExtension.class)
class PayslipServiceTest {

    @Mock
    private SalaryMapper salaryMapper;

    @InjectMocks
    private PayslipService payslipService;

    private UUID tutorId;
    private UUID salaryTutor;

    @BeforeEach
    void setUp() {
        tutorId = UUID.randomUUID();
        salaryTutor = tutorId; // 給与レコードは同一 tutor に属する
    }

    @Nested
    @DisplayName("空入力")
    class EmptyInputs {

        @Test
        @DisplayName("works が空 → 全部0の PayslipDto")
        void emptyWorks() {
            PayslipDto result = payslipService.create(List.of());

            assertEquals(0, result.getLessonPay());
            assertEquals(0, result.getPeriodCount());
            assertEquals(0, result.getDailyAllowance());
            assertEquals(0, result.getOfficeWorkPay());
            assertEquals(0, result.getOtherPay());
            assertEquals(0, result.getTransportationFee());
        }

        @Test
        @DisplayName("salaries が空 → 全部0の PayslipDto")
        void emptySalaries() {
            WorkDto work = workBase(tutorId, LocalDate.of(2026, 1, 10));
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of());

            PayslipDto result = payslipService.create(List.of(work));

            assertEquals(0, result.getLessonPay());
            assertTrue(result.getTutorId() == null || result.getTutorId().equals(tutorId));
        }
    }

    @Nested
    @DisplayName("給与バージョン選択")
    class SalaryVersionSelection {

        @Test
        @DisplayName("勤務日に有効な給与が1件 → それが使われる")
        void singleSalaryUsed() {
            LocalDate workDate = LocalDate.of(2026, 2, 10);
            // lessonWage=2000, officeWage=1000
            var s = salary(UUID.randomUUID(), tutorId, LocalDate.of(2026, 1, 1), 2000, 1000, 0);

            WorkDto work = workBase(tutorId, workDate);
            work.setLessonWorkDetailDto(lesson(null, null, null, List.of("S", "A")));

            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(s));

            PayslipDto r = payslipService.create(List.of(work));
            assertEquals(2 * 2000, r.getLessonPay());
            assertEquals(2, r.getPeriodCount());
        }

        @Test
        @DisplayName("複数給与で effectiveDate<=workingDate のうち最新が使われる")
        void latestBeforeDateChosen() {
            LocalDate workDate = LocalDate.of(2026, 3, 10);
            var old = salary(UUID.randomUUID(), tutorId, LocalDate.of(2025, 1, 1), 1000, 800, 0);
            var mid = salary(UUID.randomUUID(), tutorId, LocalDate.of(2026, 1, 1), 2000, 900, 0);
            var future = salary(UUID.randomUUID(), tutorId, LocalDate.of(2026, 4, 1), 3000, 1000, 0);

            WorkDto work = workBase(tutorId, workDate);
            work.setLessonWorkDetailDto(lesson(null, null, null, List.of("S")));

            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(future, old, mid));

            PayslipDto r = payslipService.create(List.of(work));
            // mid (=2026-01-01) が最新適用 → 2000
            assertEquals(2000, r.getLessonPay());
        }

        @Test
        @DisplayName("有効な給与が未来のみ → スキップされ0")
        void noApplicableSalary() {
            LocalDate workDate = LocalDate.of(2020, 1, 10);
            var future = salary(UUID.randomUUID(), tutorId, LocalDate.of(2026, 1, 1), 2000, 1000, 0);

            WorkDto work = workBase(tutorId, workDate);
            work.setLessonWorkDetailDto(lesson(null, null, null, List.of("S")));

            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(future));

            PayslipDto r = payslipService.create(List.of(work));
            assertEquals(0, r.getLessonPay());
            assertEquals(0, r.getPeriodCount());
        }
    }

    @Nested
    @DisplayName("月跨ぎの給与適用")
    class SalaryChangeMidPeriod {

        @Test
        @DisplayName("給与改定日を跨ぐ勤務: 改定前と改定後で正しい lesson wage が適用される")
        void salaryCutoverAcrossMonths() {
            // 期間 2026/3/26 - 2026/4/25（4月分）に 4/1 の給与改定がある
            // 3/30 は旧給与 lesson 2000 を、4/10 は新給与 lesson 2500 を参照すべき
            LocalDate oldWorkDate = LocalDate.of(2026, 3, 30);
            LocalDate newWorkDate = LocalDate.of(2026, 4, 10);

            var old = salary(UUID.randomUUID(), tutorId, LocalDate.of(2026, 1, 1), 2000, 1000, 0);
            var newer = salary(UUID.randomUUID(), tutorId, LocalDate.of(2026, 4, 1), 2500, 1000, 0);

            WorkDto w1 = workBase(tutorId, oldWorkDate);
            w1.setLessonWorkDetailDto(lesson(null, null, null, List.of("S")));
            WorkDto w2 = workBase(tutorId, newWorkDate);
            w2.setLessonWorkDetailDto(lesson(null, null, null, List.of("S")));

            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(old, newer));

            PayslipDto r = payslipService.create(List.of(w1, w2));

            // それぞれ 1 コマ分: (2000 * 1) + (2500 * 1) = 4500
            assertEquals(2000 + 2500, r.getLessonPay());
            assertEquals(2, r.getPeriodCount());
        }

        @Test
        @DisplayName("給与改定日ちょうどの勤務は新しい給与が適用される")
        void effectiveDateBoundaryAppliesNewSalary() {
            // 4/1 に新しい給与が発効する → 4/1 の勤務は新しい wage が使われるはず
            LocalDate workDate = LocalDate.of(2026, 4, 1);

            var old = salary(UUID.randomUUID(), tutorId, LocalDate.of(2026, 1, 1), 2000, 1000, 0);
            var newer = salary(UUID.randomUUID(), tutorId, LocalDate.of(2026, 4, 1), 2500, 1000, 0);

            WorkDto w = workBase(tutorId, workDate);
            w.setLessonWorkDetailDto(lesson(null, null, null, List.of("S")));

            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(old, newer));

            PayslipDto r = payslipService.create(List.of(w));

            // 4/1 ちょうどなら新しい lesson wage 2500
            assertEquals(2500, r.getLessonPay());
        }

        @Test
        @DisplayName("改定日の前日は旧給与が適用される")
        void dayBeforeCutoverUsesOldSalary() {
            LocalDate workDate = LocalDate.of(2026, 3, 31);

            var old = salary(UUID.randomUUID(), tutorId, LocalDate.of(2026, 1, 1), 2000, 1000, 0);
            var newer = salary(UUID.randomUUID(), tutorId, LocalDate.of(2026, 4, 1), 2500, 1000, 0);

            WorkDto w = workBase(tutorId, workDate);
            w.setLessonWorkDetailDto(lesson(null, null, null, List.of("S")));

            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(old, newer));

            PayslipDto r = payslipService.create(List.of(w));

            // 3/31 なら旧 lesson wage 2000（4/1 はまだ effective start にならない）
            assertEquals(2000, r.getLessonPay());
        }
    }

    @Nested
    @DisplayName("事務給与（分→円、ceil(1/60)）")
    class OfficeWorkPay {

        @Test
        @DisplayName("60分勤務 → 時給そのまま")
        void exactHour() {
            WorkDto work = workBase(tutorId, LocalDate.of(2026, 1, 10));
            work.setOfficeWorkDetailDto(office(t(18, 0), t(19, 0)));
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(work));
            // officeWorkPay = 60 * 1000 = 60000 (分×時給); ceil(/60) → 1000円
            assertEquals(1000, r.getOfficeWorkPay());
        }

        @Test
        @DisplayName("30分勤務 → 0.5時間分 = 500円（端数なし）")
        void halfHour() {
            WorkDto work = workBase(tutorId, LocalDate.of(2026, 1, 10));
            work.setOfficeWorkDetailDto(office(t(18, 0), t(18, 30)));
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(work));
            assertEquals(500, r.getOfficeWorkPay());
        }

        @Test
        @DisplayName("1分勤務 → 切上げで17円")
        void oneMinute() {
            WorkDto work = workBase(tutorId, LocalDate.of(2026, 1, 10));
            work.setOfficeWorkDetailDto(office(t(18, 0), t(18, 1)));
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(work));
            // 1 * 1000 / 60 = 16.6... → 17
            assertEquals(17, r.getOfficeWorkPay());
        }

        @Test
        @DisplayName("時刻がnull → 0")
        void nullTimes() {
            WorkDto work = workBase(tutorId, LocalDate.of(2026, 1, 10));
            // office detail は null のまま
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(work));
            assertEquals(0, r.getOfficeWorkPay());
        }
    }

    @Nested
    @DisplayName("その他業務（training & study room）")
    class OtherWorkPay {

        @Test
        @DisplayName("勤務60分で休憩0 → amount のベースは 60*1000=60000, ceil/60 = 1000円, minutes=60")
        void basicOtherWork() {
            WorkDto work = workBase(tutorId, LocalDate.of(2026, 1, 10));
            work.setOtherWorkDetailDto(other(t(10, 0), t(11, 0), 0, "training"));
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(work));
            assertEquals(1000, r.getTrainingAndStudyRoomDto().getAmount());
            assertEquals(60, r.getTrainingAndStudyRoomDto().getMinutes());
        }

        @Test
        @DisplayName("休憩20分は差し引かれる")
        void breakDeducted() {
            WorkDto work = workBase(tutorId, LocalDate.of(2026, 1, 10));
            work.setOtherWorkDetailDto(other(t(10, 0), t(12, 0), 20, "training"));
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(work));
            // (120 - 20) * 1000 = 100000 → /60 = 1666.66 → ceil = 1667
            assertEquals(1667, r.getTrainingAndStudyRoomDto().getAmount());
            assertEquals(100, r.getTrainingAndStudyRoomDto().getMinutes());
        }
    }

    @Nested
    @DisplayName("時間外授業（3コマ以上のみ）")
    class OutsideHoursWork {

        @Test
        @DisplayName("2コマなら計上されない")
        void belowBorder() {
            WorkDto work = workBase(tutorId, LocalDate.of(2026, 1, 10));
            work.setLessonWorkDetailDto(lesson(t(13, 0), t(15, 30), 0, List.of("S", "A")));
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(work));
            assertEquals(0, r.getOutsideHoursWorkDto().getMinutes());
            assertEquals(0, r.getOutsideHoursWorkDto().getAmount());
        }

        @Test
        @DisplayName("3コマだが時間内なら 0 分以上は計上されない")
        void threePeriodsNoExcess() {
            // 3コマ(300分)+休憩(30)+準備(20) = 350 → 勤務350分でちょうど
            WorkDto work = workBase(tutorId, LocalDate.of(2026, 1, 10));
            work.setLessonWorkDetailDto(lesson(t(13, 0), t(18, 50), 30, List.of("S", "A", "B")));
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(work));
            assertEquals(0, r.getOutsideHoursWorkDto().getMinutes());
        }

        @Test
        @DisplayName("3コマ超過 → 超過分が計上される")
        void threePeriodsWithExcess() {
            // 勤務=13:00-19:00(360分), 3コマ(300)+準備(20)+休憩(10)=330 → 超過30分
            WorkDto work = workBase(tutorId, LocalDate.of(2026, 1, 10));
            work.setLessonWorkDetailDto(lesson(t(13, 0), t(19, 0), 10, List.of("S", "A", "B")));
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(work));
            assertEquals(30, r.getOutsideHoursWorkDto().getMinutes());
            // 30 * 1000 = 30000 → /60 = 500
            assertEquals(500, r.getOutsideHoursWorkDto().getAmount());
        }
    }

    @Nested
    @DisplayName("残業手当 (>600分, 0.25倍)")
    class OvertimePremium {

        @Test
        @DisplayName("総勤務 610分(break0) → 残業590分ではなく超過判定で600を超えた分だけが計上")
        void over600Minutes() {
            // 勤務 9:00-22:00 = 780分, 休憩20 → overtime = 780 - 20 = 760 (> 600) で発動
            // 超過分のみが計上される: excess = 760 - 600 = 160
            WorkDto work = workBase(tutorId, LocalDate.of(2026, 1, 10));
            work.setLessonWorkDetailDto(lesson(t(9, 0), t(22, 0), 20, List.of("S", "A", "B")));
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(work));
            // overtime = 780 - 20 = 760 (> 600) → excess = 160 → amount = 160*1000*0.25 = 40000 → ceil/60 = 667
            assertEquals(160, r.getOvertimePremiumDto().getMinutes());
            assertEquals((int) Math.ceil((double) (160 * 1000 * 0.25) / 60), r.getOvertimePremiumDto().getAmount());
        }

        @Test
        @DisplayName("総勤務 500分 → 残業判定外で0")
        void underBorder() {
            WorkDto work = workBase(tutorId, LocalDate.of(2026, 1, 10));
            work.setLessonWorkDetailDto(lesson(t(13, 0), t(21, 20), 0, List.of("S", "A", "B")));
            // duration=500, prep=20, break=0 → 480 < 600
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(work));
            assertEquals(0, r.getOvertimePremiumDto().getMinutes());
            assertEquals(0, r.getOvertimePremiumDto().getAmount());
        }

        @Test
        @DisplayName("勤務時刻がnull → 0")
        void nullTimes() {
            WorkDto work = workBase(tutorId, LocalDate.of(2026, 1, 10));
            work.setLessonWorkDetailDto(lesson(null, null, null, List.of()));
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(work));
            assertEquals(0, r.getOvertimePremiumDto().getMinutes());
        }
    }

    @Nested
    @DisplayName("深夜手当 (22:00〜)")
    class NightShiftPremium {

        @Test
        @DisplayName("完全に22時以降 → 全部が深夜")
        void fullyAfterNightStart() {
            WorkDto work = workBase(tutorId, LocalDate.of(2026, 1, 10));
            work.setLessonWorkDetailDto(lesson(t(22, 30), t(23, 30), 0, List.of("S")));
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(work));
            assertEquals(60, r.getNightShiftPremiumDto().getMinutes());
            // 60 * 1000 * 0.25 = 15000 → /60 = 250
            assertEquals(250, r.getNightShiftPremiumDto().getAmount());
        }

        @Test
        @DisplayName("22時を跨ぐ → 22時以降のみ計上")
        void crossingNightStart() {
            WorkDto work = workBase(tutorId, LocalDate.of(2026, 1, 10));
            work.setLessonWorkDetailDto(lesson(t(21, 0), t(23, 0), 0, List.of("S")));
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(work));
            // 22:00-23:00 = 60分のみ
            assertEquals(60, r.getNightShiftPremiumDto().getMinutes());
        }

        @Test
        @DisplayName("完全に22時前 → 0")
        void entirelyBeforeNightStart() {
            WorkDto work = workBase(tutorId, LocalDate.of(2026, 1, 10));
            work.setLessonWorkDetailDto(lesson(t(13, 0), t(18, 0), 0, List.of("S")));
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(work));
            assertEquals(0, r.getNightShiftPremiumDto().getMinutes());
        }

        @Test
        @DisplayName("lesson/office/other 3種を合算")
        void accumulatesAcrossThreeBlocks() {
            WorkDto work = workBase(tutorId, LocalDate.of(2026, 1, 10));
            work.setLessonWorkDetailDto(lesson(t(22, 0), t(22, 30), 0, List.of("S"))); // +30
            work.setOfficeWorkDetailDto(office(t(22, 0), t(23, 0)));                   // +60
            work.setOtherWorkDetailDto(other(t(22, 30), t(23, 30), 0, "x"));           // +60
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(work));
            assertEquals(150, r.getNightShiftPremiumDto().getMinutes());
        }

        @Test
        @DisplayName("全てnull → 0")
        void allNull() {
            WorkDto work = workBase(tutorId, LocalDate.of(2026, 1, 10));
            // 全detail null（workBase 経由だが上書き）
            work.setLessonWorkDetailDto(lesson(null, null, null, List.of()));
            work.setOfficeWorkDetailDto(office(null, null));
            work.setOtherWorkDetailDto(other(null, null, null, null));
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(work));
            assertEquals(0, r.getNightShiftPremiumDto().getMinutes());
        }
    }

    @Nested
    @DisplayName("その他集約")
    class Aggregation {

        @Test
        @DisplayName("dailyAllowance と transportationFee は合算のみ")
        void sumDailyAllowanceAndTransportation() {
            WorkDto w1 = workBase(tutorId, LocalDate.of(2026, 1, 10));
            w1.setDailyAllowance(200);
            w1.setTransportationFee(500);
            WorkDto w2 = workBase(tutorId, LocalDate.of(2026, 1, 11));
            w2.setDailyAllowance(200);
            w2.setTransportationFee(500);

            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(w1, w2));
            assertEquals(400, r.getDailyAllowance());
            assertEquals(1000, r.getTransportationFee());
        }

        @Test
        @DisplayName("otherPay は常に0")
        void otherPayAlwaysZero() {
            WorkDto work = workBase(tutorId, LocalDate.of(2026, 1, 10));
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(work));
            assertEquals(0, r.getOtherPay());
        }

        @Test
        @DisplayName("複数workの量を合算・tutorIdは先頭workのもの")
        void aggregatesAcrossWorks() {
            WorkDto w1 = workBase(tutorId, LocalDate.of(2026, 1, 10));
            w1.setLessonWorkDetailDto(lesson(null, null, null, List.of("S")));
            WorkDto w2 = workBase(tutorId, LocalDate.of(2026, 1, 11));
            w2.setLessonWorkDetailDto(lesson(null, null, null, List.of("S", "A")));

            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                salary(UUID.randomUUID(), tutorId, LocalDate.of(2020, 1, 1), 2000, 1000, 0)));

            PayslipDto r = payslipService.create(List.of(w1, w2));
            assertEquals(3, r.getPeriodCount());
            assertEquals(3 * 2000, r.getLessonPay());
            assertEquals(tutorId, r.getTutorId());
        }
    }
}
