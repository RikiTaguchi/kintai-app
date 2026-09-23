package com.example.api.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.api.exception.AlreadyExistsException;
import com.example.api.exception.BusinessException;
import com.example.api.exception.InvalidInputException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.mapper.SalaryMapper;
import com.example.api.mapper.TutorMapper;
import com.example.api.mapper.entity.SalaryEntity;
import com.example.api.mapper.entity.TutorEntity;
import com.example.api.mapper.entity.work.WorkEntity;
import com.example.api.mapper.work.WorkMapper;
import com.example.api.service.dto.SalaryDto;

@ExtendWith(MockitoExtension.class)
class SalaryServiceTest {

    @Mock private SalaryMapper salaryMapper;
    @Mock private TutorMapper tutorMapper;
    @Mock private WorkMapper workMapper;

    @InjectMocks
    private SalaryService salaryService;

    private UUID tutorId;
    private UUID salaryId;

    @BeforeEach
    void setUp() {
        tutorId = UUID.randomUUID();
        salaryId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("findAll / find")
    class Finders {

        @Test
        @DisplayName("findAll: 結果を全件返す")
        void findAll() {
            when(salaryMapper.selectAll(tutorId)).thenReturn(List.of(
                entity(tutorId, LocalDate.of(2024, 1, 1)),
                entity(tutorId, LocalDate.of(2025, 1, 1))
            ));
            List<SalaryDto> r = salaryService.findAll(tutorId);
            assertEquals(2, r.size());
        }

        @Test
        @DisplayName("find: 存在すれば DTO を返す")
        void findPresent() {
            when(salaryMapper.select(salaryId)).thenReturn(Optional.of(entity(tutorId, LocalDate.of(2024, 1, 1))));
            SalaryDto r = salaryService.find(salaryId);
            assertEquals(tutorId, r.getTutorId());
        }

        @Test
        @DisplayName("find: 存在しない → ResourceNotFoundException")
        void findMissing() {
            when(salaryMapper.select(salaryId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> salaryService.find(salaryId));
        }
    }

    @Nested
    @DisplayName("validateEffectiveDate")
    class ValidateEffectiveDate {

        @Test
        @DisplayName("1900-01-01 より前 → InvalidInputException")
        void tooEarly() {
            SalaryDto dto = new SalaryDto(null, tutorId, LocalDate.of(1899, 12, 31), 1000, 1000, 0);
            assertThrows(InvalidInputException.class, () -> salaryService.register(dto));
        }

        @Test
        @DisplayName("1900-01-01 は OK")
        void minBoundaryOk() {
            SalaryDto dto = new SalaryDto(null, tutorId, LocalDate.of(1900, 1, 1), 1000, 1000, 0);
            when(salaryMapper.existsByTutorIdAndEffectiveDate(tutorId, LocalDate.of(1900, 1, 1), null))
                .thenReturn(false);
            UUID newId = UUID.randomUUID();
            // insert 時に ID が採番される想定を模倣
            org.mockito.Mockito.doAnswer(inv -> {
                SalaryEntity e = inv.getArgument(0);
                e.setId(newId);
                return null;
            }).when(salaryMapper).insert(any());
            when(salaryMapper.select(newId))
                .thenReturn(Optional.of(entity(tutorId, LocalDate.of(1900, 1, 1))));
            salaryService.register(dto);
        }

        @Test
        @DisplayName("2100-12-31 は OK")
        void maxBoundaryOk() {
            SalaryDto dto = new SalaryDto(null, tutorId, LocalDate.of(2100, 12, 31), 1000, 1000, 0);
            when(salaryMapper.existsByTutorIdAndEffectiveDate(tutorId, LocalDate.of(2100, 12, 31), null))
                .thenReturn(false);
            UUID newId = UUID.randomUUID();
            org.mockito.Mockito.doAnswer(inv -> {
                SalaryEntity e = inv.getArgument(0);
                e.setId(newId);
                return null;
            }).when(salaryMapper).insert(any());
            when(salaryMapper.select(newId))
                .thenReturn(Optional.of(entity(tutorId, LocalDate.of(2100, 12, 31))));
            salaryService.register(dto);
        }

        @Test
        @DisplayName("2100-12-31 より後 → InvalidInputException")
        void tooLate() {
            SalaryDto dto = new SalaryDto(null, tutorId, LocalDate.of(2101, 1, 1), 1000, 1000, 0);
            assertThrows(InvalidInputException.class, () -> salaryService.register(dto));
        }
    }

    @Nested
    @DisplayName("validateNoDuplicateEffectiveDate")
    class NoDuplicate {

        @Test
        @DisplayName("register: 同日の給与が既に存在 → AlreadyExistsException")
        void registerDuplicate() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            SalaryDto dto = new SalaryDto(null, tutorId, date, 1000, 1000, 0);
            when(salaryMapper.existsByTutorIdAndEffectiveDate(tutorId, date, null)).thenReturn(true);

            assertThrows(AlreadyExistsException.class, () -> salaryService.register(dto));
            verify(salaryMapper, never()).insert(any());
        }

        @Test
        @DisplayName("edit: 自分以外に同日が存在 → AlreadyExistsException")
        void editDuplicate() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            SalaryDto dto = new SalaryDto(salaryId, tutorId, date, 1000, 1000, 0);
            when(salaryMapper.existsByTutorIdAndEffectiveDate(tutorId, date, salaryId)).thenReturn(true);

            assertThrows(AlreadyExistsException.class, () -> salaryService.edit(dto));
        }

        @Test
        @DisplayName("edit: 自分だけは通過")
        void editSelfOk() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            SalaryDto dto = new SalaryDto(salaryId, tutorId, date, 1000, 1000, 0);
            when(salaryMapper.existsByTutorIdAndEffectiveDate(tutorId, date, salaryId)).thenReturn(false);
            when(salaryMapper.select(salaryId)).thenReturn(Optional.of(entity(tutorId, date)));

            assertDoesNotThrow(() -> salaryService.edit(dto));
        }
    }

    @Nested
    @DisplayName("delete: 存在勤務による拒否")
    class DeleteGuard {

        @Test
        @DisplayName("給与が1件のみで勤務が存在 → BusinessException")
        void refusesWhenWorkExists() {
            SalaryEntity target = entityWithId(salaryId, tutorId, LocalDate.of(2024, 1, 1));
            when(salaryMapper.select(salaryId)).thenReturn(Optional.of(target));

            UUID tutorInternalId = UUID.randomUUID();
            TutorEntity tutor = new TutorEntity();
            tutor.setId(tutorInternalId);
            tutor.setAccountId(UUID.randomUUID());
            when(tutorMapper.select(tutorId)).thenReturn(Optional.of(tutor));

            // 削除対象以外の給与はなし → dateTo は MAX (=2100-12-31)
            when(salaryMapper.selectAll(tutorInternalId)).thenReturn(List.of(target));

            when(workMapper.selectAll(tutorInternalId,
                LocalDate.of(1900, 1, 1), LocalDate.of(2100, 12, 31)))
                .thenReturn(List.of(new WorkEntity()));

            BusinessException ex = assertThrows(BusinessException.class,
                () -> salaryService.delete(salaryId));
            assertEquals("この給与情報は勤務情報で参照されているため削除できません", ex.getMessage());
            verify(salaryMapper, never()).delete(any());
        }

        @Test
        @DisplayName("削除対象以外の最古給与まで遡って work が存在 → BusinessException")
        void refusesWhenWorkWithinOtherSalaryRange() {
            SalaryEntity target = entityWithId(salaryId, tutorId, LocalDate.of(2024, 6, 1));
            SalaryEntity oldest = entityWithId(UUID.randomUUID(), tutorId, LocalDate.of(2024, 1, 1));

            when(salaryMapper.select(salaryId)).thenReturn(Optional.of(target));
            UUID tutorInternalId = UUID.randomUUID();
            TutorEntity tutor = new TutorEntity();
            tutor.setId(tutorInternalId);
            when(tutorMapper.select(tutorId)).thenReturn(Optional.of(tutor));

            when(salaryMapper.selectAll(tutorInternalId)).thenReturn(List.of(target, oldest));

            // dateTo = oldest.effectiveDate - 1 = 2024-01-01 - 1 = 2023-12-31
            when(workMapper.selectAll(tutorInternalId,
                LocalDate.of(1900, 1, 1), LocalDate.of(2023, 12, 31)))
                .thenReturn(List.of(new WorkEntity()));

            assertThrows(BusinessException.class, () -> salaryService.delete(salaryId));
        }

        @Test
        @DisplayName("勤務が存在しない → 削除OK")
        void allowsWhenNoWork() {
            SalaryEntity target = entityWithId(salaryId, tutorId, LocalDate.of(2024, 1, 1));
            when(salaryMapper.select(salaryId)).thenReturn(Optional.of(target));

            UUID tutorInternalId = UUID.randomUUID();
            TutorEntity tutor = new TutorEntity();
            tutor.setId(tutorInternalId);
            when(tutorMapper.select(tutorId)).thenReturn(Optional.of(tutor));

            when(salaryMapper.selectAll(tutorInternalId)).thenReturn(List.of(target));
            when(workMapper.selectAll(tutorInternalId,
                LocalDate.of(1900, 1, 1), LocalDate.of(2100, 12, 31)))
                .thenReturn(List.of());

            assertDoesNotThrow(() -> salaryService.delete(salaryId));
            verify(salaryMapper).delete(salaryId);
        }

        @Test
        @DisplayName("給与が見つからない → ResourceNotFoundException")
        void salaryMissing() {
            when(salaryMapper.select(salaryId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> salaryService.delete(salaryId));
        }

        @Test
        @DisplayName("講師が見つからない → ResourceNotFoundException")
        void tutorMissing() {
            SalaryEntity target = entityWithId(salaryId, tutorId, LocalDate.of(2024, 1, 1));
            when(salaryMapper.select(salaryId)).thenReturn(Optional.of(target));
            when(tutorMapper.select(tutorId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> salaryService.delete(salaryId));
        }
    }

    // === helpers ===

    private SalaryEntity entity(UUID tutorId, LocalDate effectiveDate) {
        SalaryEntity e = new SalaryEntity();
        e.setId(UUID.randomUUID());
        e.setTutorId(tutorId);
        e.setEffectiveDate(effectiveDate);
        e.setLessonWage(2000);
        e.setOfficeWage(1000);
        e.setTransportationFee(0);
        return e;
    }

    private SalaryEntity entityWithId(UUID id, UUID tutorId, LocalDate effectiveDate) {
        SalaryEntity e = entity(tutorId, effectiveDate);
        e.setId(id);
        return e;
    }
}
