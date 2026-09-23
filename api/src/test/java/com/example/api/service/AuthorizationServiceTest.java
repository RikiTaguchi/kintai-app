package com.example.api.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.api.exception.AuthorizationFailedException;
import com.example.api.exception.InvalidInputException;
import com.example.api.security.CustomUserDetails;
import com.example.api.service.dto.AccountDto;
import com.example.api.service.dto.TutorDto;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock private TutorService tutorService;

    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService(tutorService);
    }

    // ============ 基本原子的な3メソッド ============

    @Nested
    @DisplayName("assertManagerSelf / assertTutorSelf / assertPathId / assertResourceBelongsToTutor / assertManagerClassroom")
    class Assertions {

        @Test
        @DisplayName("assertManagerSelf: 一致 → 通過")
        void managerSelfOk() {
            UUID mid = UUID.randomUUID();
            CustomUserDetails ud = managerUser(mid, UUID.randomUUID());
            assertDoesNotThrow(() -> authorizationService.assertManagerSelf(ud, mid));
        }

        @Test
        @DisplayName("assertManagerSelf: 不一致 → 403")
        void managerSelfForbidden() {
            CustomUserDetails ud = managerUser(UUID.randomUUID(), UUID.randomUUID());
            AuthorizationFailedException ex = assertThrows(AuthorizationFailedException.class,
                () -> authorizationService.assertManagerSelf(ud, UUID.randomUUID()));
            assertEquals("アクセス権限がありません", ex.getMessage());
        }

        @Test
        @DisplayName("assertTutorSelf: 一致 → 通過 / 不一致 → 403")
        void tutorSelf() {
            UUID tid = UUID.randomUUID();
            CustomUserDetails ud = tutorUser(tid, UUID.randomUUID());
            assertDoesNotThrow(() -> authorizationService.assertTutorSelf(ud, tid));
            assertThrows(AuthorizationFailedException.class,
                () -> authorizationService.assertTutorSelf(ud, UUID.randomUUID()));
        }

        @Test
        @DisplayName("assertPathId: 一致 → 通過 / 不一致 → 400")
        void pathId() {
            UUID id = UUID.randomUUID();
            assertDoesNotThrow(() -> authorizationService.assertPathId(id, id));
            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> authorizationService.assertPathId(id, UUID.randomUUID()));
            assertEquals("不正なリクエストパラメータです", ex.getMessage());
        }

        @Test
        @DisplayName("assertPathId: null 同士 → 通過")
        void pathIdBothNull() {
            assertDoesNotThrow(() -> authorizationService.assertPathId(null, null));
        }

        @Test
        @DisplayName("assertResourceBelongsToTutor: 一致 → 通過 / 不一致 → 403")
        void resourceOwnership() {
            UUID tid = UUID.randomUUID();
            assertDoesNotThrow(() -> authorizationService.assertResourceBelongsToTutor(tid, tid));
            assertThrows(AuthorizationFailedException.class,
                () -> authorizationService.assertResourceBelongsToTutor(tid, UUID.randomUUID()));
        }

        @Test
        @DisplayName("assertManagerClassroom: 教室一致 → 通過 / 不一致 → 403")
        void managerClassroom() {
            UUID classroomId = UUID.randomUUID();
            CustomUserDetails ud = managerUser(UUID.randomUUID(), classroomId);
            assertDoesNotThrow(() -> authorizationService.assertManagerClassroom(ud, classroomId));
            assertThrows(AuthorizationFailedException.class,
                () -> authorizationService.assertManagerClassroom(ud, UUID.randomUUID()));
        }
    }

    // ============ 講師所有の検証 ============

    @Nested
    @DisplayName("assertManagerOwnsTutor")
    class ManagerOwnsTutor {

        @Test
        @DisplayName("自教室の講師 → 通過")
        void ownsClassroom() {
            UUID classroomId = UUID.randomUUID();
            CustomUserDetails ud = managerUser(UUID.randomUUID(), classroomId);
            TutorDto tutor = tutorDto(UUID.randomUUID(), classroomId);
            assertDoesNotThrow(() -> authorizationService.assertManagerOwnsTutor(ud, tutor));
        }

        @Test
        @DisplayName("他教室の講師 → 403")
        void notOwnsClassroom() {
            CustomUserDetails ud = managerUser(UUID.randomUUID(), UUID.randomUUID());
            TutorDto tutor = tutorDto(UUID.randomUUID(), UUID.randomUUID());
            assertThrows(AuthorizationFailedException.class,
                () -> authorizationService.assertManagerOwnsTutor(ud, tutor));
        }

        @Test
        @DisplayName("UUID指定版は tutorService.find を経由する")
        void byUuidDelegates() {
            UUID classroomId = UUID.randomUUID();
            UUID tutorId = UUID.randomUUID();
            CustomUserDetails ud = managerUser(UUID.randomUUID(), classroomId);

            when(tutorService.find(tutorId)).thenReturn(tutorDto(tutorId, classroomId));
            assertDoesNotThrow(() -> authorizationService.assertManagerOwnsTutor(ud, tutorId));
        }
    }

    // ============ 複合 ============

    @Nested
    @DisplayName("assertTutorAccess: manager/tutor/deny 3分岐")
    class TutorAccess {

        @Test
        @DisplayName("manager → 自教室講師 なら通過")
        void managerOwnsTutorOk() {
            UUID classroomId = UUID.randomUUID();
            CustomUserDetails ud = managerUser(UUID.randomUUID(), classroomId);
            TutorDto tutor = tutorDto(UUID.randomUUID(), classroomId);
            assertDoesNotThrow(() -> authorizationService.assertTutorAccess(ud, tutor));
        }

        @Test
        @DisplayName("manager → 他教室講師 なら 403")
        void managerForeignTutorForbidden() {
            CustomUserDetails ud = managerUser(UUID.randomUUID(), UUID.randomUUID());
            TutorDto tutor = tutorDto(UUID.randomUUID(), UUID.randomUUID());
            assertThrows(AuthorizationFailedException.class,
                () -> authorizationService.assertTutorAccess(ud, tutor));
        }

        @Test
        @DisplayName("tutor → 本人 なら通過")
        void tutorSelfOk() {
            UUID tutorId = UUID.randomUUID();
            UUID classroomId = UUID.randomUUID();
            CustomUserDetails ud = tutorUser(tutorId, classroomId);
            TutorDto tutor = tutorDto(tutorId, classroomId);
            assertDoesNotThrow(() -> authorizationService.assertTutorAccess(ud, tutor));
        }

        @Test
        @DisplayName("tutor → 他人 なら 403")
        void tutorOtherForbidden() {
            CustomUserDetails ud = tutorUser(UUID.randomUUID(), UUID.randomUUID());
            TutorDto tutor = tutorDto(UUID.randomUUID(), UUID.randomUUID());
            assertThrows(AuthorizationFailedException.class,
                () -> authorizationService.assertTutorAccess(ud, tutor));
        }

        @Test
        @DisplayName("UUID版は tutorService.find 経由")
        void uuidVersionUsesService() {
            UUID classroomId = UUID.randomUUID();
            UUID tutorId = UUID.randomUUID();
            CustomUserDetails ud = managerUser(UUID.randomUUID(), classroomId);
            when(tutorService.find(tutorId)).thenReturn(tutorDto(tutorId, classroomId));
            assertDoesNotThrow(() -> authorizationService.assertTutorAccess(ud, tutorId));
        }
    }

    // === helpers ===

    private CustomUserDetails managerUser(UUID managerId, UUID classroomId) {
        AccountDto d = new AccountDto(UUID.randomUUID(), "login", "pass", managerId, classroomId, null);
        return new CustomUserDetails(d);
    }

    private CustomUserDetails tutorUser(UUID tutorId, UUID classroomId) {
        AccountDto d = new AccountDto(UUID.randomUUID(), "login", "pass", null, classroomId, tutorId);
        return new CustomUserDetails(d);
    }

    private TutorDto tutorDto(UUID tutorId, UUID classroomId) {
        return new TutorDto(
            tutorId,
            "login", "pass",
            classroomId, "教室", 1,
            "first", "last",
            1, false, null
        );
    }
}
