package com.example.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.api.exception.BusinessException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.mapper.SalaryMapper;
import com.example.api.mapper.entity.work.LessonWorkDetailEntity;
import com.example.api.mapper.entity.work.WorkEntity;
import com.example.api.mapper.work.WorkMapper;
import com.example.api.service.dto.work.LessonWorkDetailDto;
import com.example.api.service.dto.work.WorkDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkService 単体テスト（主に calculateDateRange / calculateDailyAllowance / calculateSalaryEffectiveDate）")
class WorkServiceTest {

    @Mock private WorkMapper workMapper;
    @Mock private com.example.api.mapper.work.LessonWorkDetailMapper lessonWorkDetailMapper;
    @Mock private com.example.api.mapper.work.OfficeWorkDetailMapper officeWorkDetailMapper;
    @Mock private com.example.api.mapper.work.OtherWorkDetailMapper otherWorkDetailMapper;
    @Mock private SalaryMapper salaryMapper;

    @InjectMocks
    private WorkService workService;

    private UUID tutorId;

    @BeforeEach
    void setUp() {
        tutorId = UUID.randomUUID();
    }

    // ========= findAll (calculateDateRange) =========

    @Nested
    @DisplayName("findAll: 日付範囲は 26日 — 翌月25日")
    class FindAllDateRange {

        @Test
        @DisplayName("month=12 の場合: 11/26 - 12/25")
        void december() {
            when(workMapper.selectAll(tutorId, LocalDate.of(2025, 11, 26), LocalDate.of(2025, 12, 25)))
                .thenReturn(List.of());

            workService.findAll(tutorId, 2025, 12);
            org.mockito.Mockito.verify(workMapper)
                .selectAll(tutorId, LocalDate.of(2025, 11, 26), LocalDate.of(2025, 12, 25));
        }

        @Test
        @DisplayName("month=1 は year-1 の 12/26 — 当年 1/25")
        void januaryRollback() {
            when(workMapper.selectAll(tutorId, LocalDate.of(2025, 12, 26), LocalDate.of(2026, 1, 25)))
                .thenReturn(List.of());

            workService.findAll(tutorId, 2026, 1);
            org.mockito.Mockito.verify(workMapper)
                .selectAll(tutorId, LocalDate.of(2025, 12, 26), LocalDate.of(2026, 1, 25));
        }

        @Test
        @DisplayName("月の中間: month=7 は 6/26 - 7/25")
        void mid() {
            when(workMapper.selectAll(tutorId, LocalDate.of(2025, 6, 26), LocalDate.of(2025, 7, 25)))
                .thenReturn(List.of());

            workService.findAll(tutorId, 2025, 7);
            org.mockito.Mockito.verify(workMapper)
                .selectAll(tutorId, LocalDate.of(2025, 6, 26), LocalDate.of(2025, 7, 25));
        }

        @Test
        @DisplayName("works が空 → 給与取得は呼ばれず空リスト")
        void emptyShortCircuit() {
            when(workMapper.selectAll(tutorId, LocalDate.of(2025, 5, 26), LocalDate.of(2025, 6, 25)))
                .thenReturn(List.of());

            List<WorkDto> result = workService.findAll(tutorId, 2025, 6);
            assertEquals(0, result.size());
        }
    }

    // ========= calculateSalaryEffectiveDate (via find) =========

    @Nested
    @DisplayName("find: 給与情報欠如で ResourceNotFoundException")
    class FindSalaryMissing {

        @Test
        @DisplayName("work 存在するが salary が0件 → ResourceNotFoundException")
        void salaryMissing() {
            UUID workId = UUID.randomUUID();
            WorkEntity e = workEntity(tutorId, LocalDate.of(2026, 1, 10), List.of("S"));
            when(workMapper.select(workId)).thenReturn(Optional.of(e));
            when(salaryMapper.selectEarliestEffectiveDate(tutorId)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> workService.find(workId));
            assertEquals("給与情報が見つかりません", ex.getMessage());
        }

        @Test
        @DisplayName("work 自体がない → ResourceNotFoundException")
        void workMissing() {
            UUID workId = UUID.randomUUID();
            when(workMapper.select(workId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> workService.find(workId));
        }
    }

    // ========= calculateDailyAllowance 3×3 マトリクス =========

    @Nested
    @DisplayName("日当計算マトリクス（workingDate × salaryEffectiveDate）")
    class DailyAllowanceMatrix {

        // workingDate は常に WORK_DATE_REVISION_2025_10 (2025-10-01) 前後で切り替える
        // salaryEffectiveDate も 2022-04 / 2024-08 の3レンジ

        @ParameterizedTest
        @CsvSource({
            // ===== Era A: salary < 2022-04 (rate per period) =====
            // work < 2025-10:  legacy threshold=4
            "0,  2020-01-01, 2020-01-10, 0",     // 0コマ → 0
            "1,  2020-01-01, 2020-01-10, 390",   // 1コマ < 4 → Legacy full=390
            "3,  2020-01-01, 2020-01-10, 390",   // 3コマ < 4 → 390
            "4,  2020-01-01, 2020-01-10, 400",   // 4コマ → 100*4=400
            "5,  2020-01-01, 2020-01-10, 500",   // 5コマ → 500
            // work >= 2025-10:  current threshold=5
            "1,  2020-01-01, 2025-10-15, 410",   // 1コマ < 5 → current full=410
            "4,  2020-01-01, 2025-10-15, 410",   // 4コマ < 5 → 410
            "5,  2020-01-01, 2025-10-15, 500",   // 5コマ → 100*5=500
            // ===== Era B: 2022-04 ≤ salary < 2024-08 (full only) =====
            "0,  2023-05-01, 2020-01-10, 0",
            "1,  2023-05-01, 2020-01-10, 390",   // legacy full
            "5,  2023-05-01, 2020-01-10, 390",
            "0,  2023-05-01, 2025-10-15, 0",
            "1,  2023-05-01, 2025-10-15, 410",   // current full
            "7,  2023-05-01, 2025-10-15, 410",
            // ===== Era C: salary ≥ 2024-08 (half/full) =====
            "0,  2024-09-01, 2020-01-10, 0",
            "1,  2024-09-01, 2020-01-10, 195",   // single → legacy half
            "2,  2024-09-01, 2020-01-10, 390",   // not single → legacy full
            "5,  2024-09-01, 2020-01-10, 390",
            "0,  2024-09-01, 2025-10-15, 0",
            "1,  2024-09-01, 2025-10-15, 205",   // current half
            "3,  2024-09-01, 2025-10-15, 410",   // current full
            // ===== 境界値ちょうど確認 =====
            // salaryEffectiveDate の境界: 2022-04-01
            "1,  2022-03-31, 2020-01-10, 390",   // Era A 境界直前（per-period rate 適用範囲）
            "1,  2022-04-01, 2020-01-10, 390",   // Era B 境界ちょうど（同一結果になることを検証）
            "5,  2022-03-31, 2020-01-10, 500",   // Era A 5コマ → 5*100=500
            "5,  2022-04-01, 2020-01-10, 390",   // Era B 5コマ → full 390（rate 適用外）
            // salaryEffectiveDate の境界: 2024-08-01
            "1,  2024-07-31, 2020-01-10, 390",   // Era B 境界直前 → legacy full
            "1,  2024-08-01, 2020-01-10, 195",   // Era C 境界ちょうど → legacy half
            // workingDate の境界: 2025-10-01
            "2,  2024-08-01, 2025-09-30, 390",   // 直前 → legacy full
            "2,  2024-08-01, 2025-10-01, 410",   // ちょうど → current full
            "1,  2024-08-01, 2025-09-30, 195",   // 直前 → legacy half
            "1,  2024-08-01, 2025-10-01, 205",   // ちょうど → current half
        })
        @DisplayName("給与バージョン × 勤務日 × コマ数 → 日当")
        void allowanceMatrix(
            int periodCount,
            String salaryEffectiveDate,
            String workingDate,
            int expectedAllowance
        ) {
            UUID workId = UUID.randomUUID();
            WorkEntity e = workEntity(tutorId, LocalDate.parse(workingDate),
                periodCount == 0 ? List.of() : generatePeriods(periodCount));

            when(workMapper.select(workId)).thenReturn(Optional.of(e));
            when(salaryMapper.selectEarliestEffectiveDate(tutorId))
                .thenReturn(Optional.of(LocalDate.parse(salaryEffectiveDate)));

            WorkDto dto = workService.find(workId);
            assertEquals(expectedAllowance, dto.getDailyAllowance());
        }

        private List<String> generatePeriods(int n) {
            List<String> all = List.of("M", "K", "S", "A", "B", "C", "D");
            return all.subList(0, n);
        }
    }

    // ========= register / edit 例外 =========

    @Nested
    @DisplayName("register / edit バリデーション")
    class Validation {

        @Test
        @DisplayName("register: 勤務日が給与適用開始日より前 → BusinessException")
        void registerDateBeforeSalary() {
            WorkDto dto = workDto(tutorId, LocalDate.of(2020, 1, 10));
            when(salaryMapper.selectEarliestEffectiveDate(tutorId))
                .thenReturn(Optional.of(LocalDate.of(2020, 6, 1)));

            BusinessException ex = assertThrows(BusinessException.class,
                () -> workService.register(dto));
            assertEquals("勤務日が給与情報の適用開始日より前です", ex.getMessage());
        }

        @Test
        @DisplayName("edit: 勤務日が給与適用開始日より前 → BusinessException")
        void editDateBeforeSalary() {
            WorkDto dto = workDto(tutorId, LocalDate.of(2020, 1, 10));
            when(salaryMapper.selectEarliestEffectiveDate(tutorId))
                .thenReturn(Optional.of(LocalDate.of(2020, 6, 1)));

            assertThrows(BusinessException.class, () -> workService.edit(dto));
        }
    }

    // ========= register 正常系 =========

    @Nested
    @DisplayName("register 正常系: Mapper 呼び出しと返却 DTO 検証")
    class RegisterSuccess {

        @Test
        @DisplayName("正常な DTO で workMapper/3 detail mapper の insert が各1回呼ばれる")
        void registersAllEntities() {
            WorkDto input = workDto(tutorId, LocalDate.of(2025, 10, 10));
            UUID persistedId = UUID.randomUUID();

            when(salaryMapper.selectEarliestEffectiveDate(tutorId))
                .thenReturn(Optional.of(LocalDate.of(2020, 1, 1)));
            when(workMapper.existsByTutorIdAndWorkingDate(tutorId, input.getWorkingDate(), null))
                .thenReturn(false);
            org.mockito.Mockito.doAnswer(inv -> {
                WorkEntity e = inv.getArgument(0);
                e.setId(persistedId);
                return null;
            }).when(workMapper).insert(any());
            WorkEntity persisted = workEntity(tutorId, input.getWorkingDate(), List.of());
            persisted.setId(persistedId);
            when(workMapper.select(persistedId))
                .thenReturn(Optional.of(persisted));

            WorkDto result = workService.register(input);

            org.mockito.Mockito.verify(workMapper).insert(any(WorkEntity.class));
            org.mockito.Mockito.verify(lessonWorkDetailMapper)
                .insert(any(com.example.api.mapper.entity.work.LessonWorkDetailEntity.class));
            org.mockito.Mockito.verify(officeWorkDetailMapper)
                .insert(any(com.example.api.mapper.entity.work.OfficeWorkDetailEntity.class));
            org.mockito.Mockito.verify(otherWorkDetailMapper)
                .insert(any(com.example.api.mapper.entity.work.OtherWorkDetailEntity.class));
            // 成功時は dailyAllowance 計算のため再取得が行われる
            org.mockito.Mockito.verify(workMapper).select(persistedId);
            assertEquals(persistedId, result.getId());
        }

        @Test
        @DisplayName("登録結果の dailyAllowance は calculateDailyAllowance で計算される")
        void returnedDtoContainsDailyAllowance() {
            WorkDto input = workDto(tutorId, LocalDate.of(2024, 9, 10));
            UUID persistedId = UUID.randomUUID();

            when(salaryMapper.selectEarliestEffectiveDate(tutorId))
                .thenReturn(Optional.of(LocalDate.of(2024, 8, 1)));
            when(workMapper.existsByTutorIdAndWorkingDate(tutorId, input.getWorkingDate(), null))
                .thenReturn(false);
            org.mockito.Mockito.doAnswer(inv -> {
                WorkEntity e = inv.getArgument(0);
                e.setId(persistedId);
                return null;
            }).when(workMapper).insert(any());
            // 再取得時には 1 コマを持つデータを返す → single なので half
            WorkEntity persisted = workEntity(tutorId, input.getWorkingDate(), List.of("M"));
            persisted.setId(persistedId);
            when(workMapper.select(persistedId))
                .thenReturn(Optional.of(persisted));

            WorkDto result = workService.register(input);
            // 2025-10 revision 前 + 1コマ → legacy half = 195
            assertEquals(195, result.getDailyAllowance());
        }

        @Test
        @DisplayName("重複する勤務日 → AlreadyExistsException")
        void duplicateDate() {
            WorkDto dto = workDto(tutorId, LocalDate.of(2024, 6, 10));
            when(salaryMapper.selectEarliestEffectiveDate(tutorId))
                .thenReturn(Optional.of(LocalDate.of(2020, 1, 1)));
            when(workMapper.existsByTutorIdAndWorkingDate(tutorId, dto.getWorkingDate(), null))
                .thenReturn(true);

            com.example.api.exception.AlreadyExistsException ex = assertThrows(
                com.example.api.exception.AlreadyExistsException.class,
                () -> workService.register(dto));
            assertEquals("この日付の勤務情報は既に登録されています", ex.getMessage());
            org.mockito.Mockito.verify(workMapper, org.mockito.Mockito.never()).insert(any());
        }
    }

    // ========= edit 正常系 =========

    @Nested
    @DisplayName("edit 正常系: Mapper update 呼び出し検証")
    class EditSuccess {

        @Test
        @DisplayName("正常な DTO で workMapper/3 detail mapper の update が各1回呼ばれる")
        void updatesAllEntities() {
            WorkDto input = workDto(tutorId, LocalDate.of(2025, 10, 10));
            UUID existingWorkId = input.getId();

            when(salaryMapper.selectEarliestEffectiveDate(tutorId))
                .thenReturn(Optional.of(LocalDate.of(2020, 1, 1)));
            when(workMapper.existsByTutorIdAndWorkingDate(tutorId, input.getWorkingDate(), existingWorkId))
                .thenReturn(false);
            when(workMapper.select(existingWorkId))
                .thenReturn(Optional.of(workEntityWithId(existingWorkId, tutorId, input.getWorkingDate(), List.of())));

            WorkDto result = workService.edit(input);

            org.mockito.Mockito.verify(workMapper).update(any(WorkEntity.class));
            org.mockito.Mockito.verify(lessonWorkDetailMapper)
                .update(any(com.example.api.mapper.entity.work.LessonWorkDetailEntity.class));
            org.mockito.Mockito.verify(officeWorkDetailMapper)
                .update(any(com.example.api.mapper.entity.work.OfficeWorkDetailEntity.class));
            org.mockito.Mockito.verify(otherWorkDetailMapper)
                .update(any(com.example.api.mapper.entity.work.OtherWorkDetailEntity.class));
            org.mockito.Mockito.verify(workMapper).select(existingWorkId);
            assertEquals(existingWorkId, result.getId());
        }

        @Test
        @DisplayName("再取得で work が存在しない場合 → ResourceNotFoundException")
        void editSelectMissing() {
            WorkDto input = workDto(tutorId, LocalDate.of(2024, 6, 10));
            UUID existingWorkId = input.getId();

            when(salaryMapper.selectEarliestEffectiveDate(tutorId))
                .thenReturn(Optional.of(LocalDate.of(2020, 1, 1)));
            when(workMapper.existsByTutorIdAndWorkingDate(tutorId, input.getWorkingDate(), existingWorkId))
                .thenReturn(false);
            when(workMapper.select(existingWorkId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> workService.edit(input));
        }
    }

    // ========= delete =========

    @Nested
    @DisplayName("delete")
    class Deletion {

        @Test
        @DisplayName("workMapper.delete が呼ばれる")
        void delegatesToMapper() {
            UUID workId = UUID.randomUUID();
            workService.delete(workId);
            org.mockito.Mockito.verify(workMapper).delete(workId);
        }
    }

    // ========= helpers =========

    private WorkEntity workEntity(UUID tutorId, LocalDate workingDate, List<String> periodCodes) {
        WorkEntity e = new WorkEntity();
        e.setId(UUID.randomUUID());
        e.setTutorId(tutorId);
        e.setClassroomId(UUID.randomUUID());
        e.setClassroomName("test");
        e.setClassroomNumber(1);
        e.setWorkingDate(workingDate);
        e.setTransportationFee(0);

        LessonWorkDetailEntity l = new LessonWorkDetailEntity();
        l.setPeriodCodeM(periodCodes.contains("M"));
        l.setPeriodCodeK(periodCodes.contains("K"));
        l.setPeriodCodeS(periodCodes.contains("S"));
        l.setPeriodCodeA(periodCodes.contains("A"));
        l.setPeriodCodeB(periodCodes.contains("B"));
        l.setPeriodCodeC(periodCodes.contains("C"));
        l.setPeriodCodeD(periodCodes.contains("D"));
        e.setLessonWorkDetailEntity(l);

        e.setOfficeWorkDetailEntity(new com.example.api.mapper.entity.work.OfficeWorkDetailEntity());
        e.setOtherWorkDetailEntity(new com.example.api.mapper.entity.work.OtherWorkDetailEntity());
        return e;
    }

    private WorkEntity workEntityWithId(UUID id, UUID tutorId, LocalDate workingDate, List<String> periodCodes) {
        WorkEntity e = workEntity(tutorId, workingDate, periodCodes);
        e.setId(id);
        return e;
    }

    private WorkDto workDto(UUID tutorId, LocalDate workingDate) {
        WorkDto d = new WorkDto();
        d.setId(UUID.randomUUID());
        d.setTutorId(tutorId);
        d.setClassroomId(UUID.randomUUID());
        d.setWorkingDate(workingDate);
        d.setTransportationFee(0);
        d.setLessonWorkDetailDto(new LessonWorkDetailDto(
            UUID.randomUUID(), UUID.randomUUID(), null, null, 0, List.of()));
        d.setOfficeWorkDetailDto(new com.example.api.service.dto.work.OfficeWorkDetailDto());
        d.setOtherWorkDetailDto(new com.example.api.service.dto.work.OtherWorkDetailDto());
        return d;
    }
}
