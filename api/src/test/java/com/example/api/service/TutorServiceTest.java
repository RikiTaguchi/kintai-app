package com.example.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.api.exception.InvalidInputException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.mapper.TutorMapper;
import com.example.api.mapper.entity.TutorEntity;
import com.example.api.service.dto.TutorDto;

@ExtendWith(MockitoExtension.class)
class TutorServiceTest {

    @Mock private TutorMapper tutorMapper;
    @Mock private AccountService accountService;

    private TutorService tutorService;

    @BeforeEach
    void setUp() {
        tutorService = new TutorService(tutorMapper, accountService);
    }

    @Nested
    @DisplayName("findAll / find")
    class Finders {

        @Test
        @DisplayName("find: 存在しない → 404")
        void findMissing() {
            UUID id = UUID.randomUUID();
            when(tutorMapper.select(id)).thenReturn(Optional.empty());
            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> tutorService.find(id));
            assertEquals("講師が見つかりません", ex.getMessage());
        }

        @Test
        @DisplayName("findAll: 教室IDで抽出")
        void findAll() {
            UUID classroomId = UUID.randomUUID();
            TutorEntity t1 = tutor(UUID.randomUUID(), classroomId, true, LocalDate.of(2025, 1, 1));
            TutorEntity t2 = tutor(UUID.randomUUID(), classroomId, false, null);
            when(tutorMapper.selectAll(classroomId)).thenReturn(List.of(t1, t2));

            List<TutorDto> result = tutorService.findAll(classroomId);
            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("edit の terminated ルール")
    class EditTerminated {

        @Test
        @DisplayName("terminated=true だが terminationDate=null → InvalidInputException")
        void requiresTerminationDate() {
            TutorDto dto = baseDto();
            dto.setTerminated(true);
            dto.setTerminationDate(null);

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> tutorService.edit(dto));
            assertEquals("退職日を入力してください", ex.getMessage());
        }

        @Test
        @DisplayName("terminated=false → terminationDate は null に戻る")
        void resetsDateWhenNotTerminated() {
            TutorDto dto = baseDto();
            dto.setTerminated(false);
            dto.setTerminationDate(LocalDate.of(2025, 1, 1)); // 過去の値が入っていても消すべき

            TutorEntity updatedEntity = tutor(dto.getId(), dto.getClassroomId(), false, null);
            when(tutorMapper.select(dto.getId())).thenReturn(Optional.of(updatedEntity));

            tutorService.edit(dto);

            org.mockito.ArgumentCaptor<TutorEntity> cap =
                org.mockito.ArgumentCaptor.forClass(TutorEntity.class);
            verify(tutorMapper).update(cap.capture());
            assertNull(cap.getValue().getTerminationDate());
        }

        @Test
        @DisplayName("terminated=true かつ日あり → 保存される")
        void terminationDateKept() {
            LocalDate date = LocalDate.of(2025, 6, 1);
            TutorDto dto = baseDto();
            dto.setTerminated(true);
            dto.setTerminationDate(date);

            TutorEntity updatedEntity = tutor(dto.getId(), dto.getClassroomId(), true, date);
            when(tutorMapper.select(dto.getId())).thenReturn(Optional.of(updatedEntity));

            tutorService.edit(dto);

            org.mockito.ArgumentCaptor<TutorEntity> cap =
                org.mockito.ArgumentCaptor.forClass(TutorEntity.class);
            verify(tutorMapper).update(cap.capture());
            assertEquals(date, cap.getValue().getTerminationDate());
        }
    }

    @Nested
    @DisplayName("resetPassword / changePassword / delete")
    class PasswordsAndDelete {

        @Test
        @DisplayName("各操作は講師の accountId を介して AccountService に委譲")
        void delegates() {
            UUID tutorId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            TutorEntity t = tutor(tutorId, UUID.randomUUID(), false, null);
            t.setAccountId(accountId);
            when(tutorMapper.select(tutorId)).thenReturn(Optional.of(t));

            tutorService.resetPassword(tutorId, "new");
            verify(accountService).resetPassword(accountId, "new");

            tutorService.changePassword(tutorId, "cur", "new");
            verify(accountService).changePassword(accountId, "cur", "new");

            tutorService.delete(tutorId);
            verify(accountService).delete(accountId);
        }

        @Test
        @DisplayName("講師が見つからない → 404 (3メソッド)")
        void missing() {
            UUID tutorId = UUID.randomUUID();
            when(tutorMapper.select(tutorId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                () -> tutorService.resetPassword(tutorId, "new"));
            assertThrows(ResourceNotFoundException.class,
                () -> tutorService.changePassword(tutorId, "cur", "new"));
            assertThrows(ResourceNotFoundException.class,
                () -> tutorService.delete(tutorId));
        }
    }

    // helpers

    private TutorDto baseDto() {
        TutorDto d = new TutorDto();
        d.setId(UUID.randomUUID());
        d.setClassroomId(UUID.randomUUID());
        d.setTutorNumber(1);
        d.setFirstName("first");
        d.setLastName("last");
        return d;
    }

    private TutorEntity tutor(UUID id, UUID classroomId, Boolean terminated, LocalDate terminationDate) {
        TutorEntity t = new TutorEntity();
        t.setId(id);
        t.setAccountId(UUID.randomUUID());
        t.setClassroomId(classroomId);
        t.setTutorNumber(1);
        t.setFirstName("first");
        t.setLastName("last");
        t.setTerminated(terminated);
        t.setTerminationDate(terminationDate);
        return t;
    }
}
