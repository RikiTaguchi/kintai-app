package com.example.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.api.security.SecurityConfig;
import com.example.api.service.AuthorizationService;
import com.example.api.service.ManagerService;
import com.example.api.service.TutorService;
import com.example.api.service.dto.ManagerDto;
import com.example.api.service.dto.TutorDto;

@WebMvcTest(TutorController.class)
@Import(SecurityConfig.class)
class TutorControllerWebMvcTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private TutorService tutorService;
    @MockitoBean private ManagerService managerService;
    @MockitoBean private AuthorizationService authorizationService;

    private UUID tutorId;
    private UUID classroomId;

    @BeforeEach
    void setUp() {
        tutorId = UUID.randomUUID();
        classroomId = UUID.randomUUID();
    }

    private TutorDto tutorDto() {
        return new TutorDto(tutorId, "tutor1", null, classroomId, "戸塚", 22, "花子", "佐藤", 1, false, null);
    }

    @Nested
    @DisplayName("GET /api/tutors")
    class FindAll {

        @Test
        @DisplayName("正常系: 200 + tutor 一覧 (MANAGER)")
        void success() throws Exception {
            UUID managerId = UUID.randomUUID();
            UUID managerClassroomId = classroomId;
            ManagerDto manager = new ManagerDto(managerId, "m1", null, managerClassroomId, "戸塚", 22, "太", "山");
            when(managerService.find(org.mockito.ArgumentMatchers.any())).thenReturn(manager);
            when(tutorService.findAll(managerClassroomId)).thenReturn(List.of(tutorDto()));

            mockMvc.perform(get("/api/tutors")
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.authentication(
                            SecurityUserTestHelper.auth(
                                SecurityUserTestHelper.managerUser(managerId, managerClassroomId),
                                "MANAGER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/tutors/{tutorId}")
    class Find {

        @Test
        @DisplayName("正常系: 200 + tutor")
        @org.springframework.security.test.context.support.WithMockUser(roles = {"MANAGER"})
        void success() throws Exception {
            TutorDto dto = tutorDto();
            when(tutorService.find(tutorId)).thenReturn(dto);

            mockMvc.perform(get("/api/tutors/{tutorId}", tutorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tutorId.toString()));

            verify(authorizationService).assertManagerOwnsTutor(any(), any(TutorDto.class));
        }

        @Test
        @DisplayName("他教室の講師 (assertManagerOwnsTutor が例外) → 403")
        @org.springframework.security.test.context.support.WithMockUser(roles = {"MANAGER"})
        void forbidden() throws Exception {
            TutorDto dto = tutorDto();
            when(tutorService.find(tutorId)).thenReturn(dto);
            org.mockito.Mockito.doThrow(
                new com.example.api.exception.AuthorizationFailedException("権限がありません"))
                .when(authorizationService).assertManagerOwnsTutor(any(), any(TutorDto.class));

            mockMvc.perform(get("/api/tutors/{tutorId}", tutorId))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/tutors")
    class Register {

        @Test
        @DisplayName("正常系: 201")
        @org.springframework.security.test.context.support.WithMockUser(roles = {"MANAGER"})
        void success() throws Exception {
            TutorDto dto = tutorDto();
            when(tutorService.register(any(TutorDto.class))).thenReturn(dto);

            String json = """
                {
                  "loginId": "tutor1",
                  "password": "password123",
                  "classroomId": "%s",
                  "tutorNumber": 1,
                  "firstName": "花子",
                  "lastName": "佐藤"
                }
                """.formatted(classroomId);

            mockMvc.perform(post("/api/tutors")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(tutorId.toString()));

            verify(authorizationService).assertManagerClassroom(any(), org.mockito.ArgumentMatchers.eq(classroomId));
        }

        @Test
        @DisplayName("他教室への登録 → 403")
        @org.springframework.security.test.context.support.WithMockUser(roles = {"MANAGER"})
        void forbidden() throws Exception {
            org.mockito.Mockito.doThrow(
                new com.example.api.exception.AuthorizationFailedException("教室が異なります"))
                .when(authorizationService).assertManagerClassroom(any(), org.mockito.ArgumentMatchers.eq(classroomId));

            String json = """
                {
                  "loginId": "tutor1",
                  "password": "password123",
                  "classroomId": "%s",
                  "tutorNumber": 1,
                  "firstName": "花子",
                  "lastName": "佐藤"
                }
                """.formatted(classroomId);

            mockMvc.perform(post("/api/tutors")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());

            verify(tutorService, never()).register(any());
        }
    }

    @Nested
    @DisplayName("PUT /api/tutors/{tutorId}")
    class Edit {

        @Test
        @DisplayName("正常系: 200")
        @org.springframework.security.test.context.support.WithMockUser(roles = {"MANAGER"})
        void success() throws Exception {
            TutorDto dto = tutorDto();
            when(tutorService.edit(any(TutorDto.class))).thenReturn(dto);

            String json = """
                {
                  "id": "%s",
                  "tutorNumber": 2,
                  "firstName": "花子",
                  "lastName": "佐藤",
                  "terminated": false
                }
                """.formatted(tutorId);

            mockMvc.perform(put("/api/tutors/{tutorId}", tutorId)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());

            verify(authorizationService).assertPathId(tutorId, tutorId);
            verify(authorizationService).assertManagerOwnsTutor(any(), org.mockito.ArgumentMatchers.eq(tutorId));
        }
    }

    @Nested
    @DisplayName("PUT /api/tutors/{tutorId}/password (管理者リセット)")
    class ResetPassword {

        @Test
        @DisplayName("正常系: 204")
        @org.springframework.security.test.context.support.WithMockUser(roles = {"MANAGER"})
        void success() throws Exception {
            String json = """
                {"newPassword": "newpassword123"}
                """;

            mockMvc.perform(put("/api/tutors/{tutorId}/password", tutorId)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

            verify(tutorService).resetPassword(tutorId, "newpassword123");
        }
    }

    @Nested
    @DisplayName("PUT /api/tutors/{tutorId}/my-password (本人変更)")
    class ChangePassword {

        @Test
        @DisplayName("正常系: 204 (TUTOR)")
        @org.springframework.security.test.context.support.WithMockUser(roles = {"TUTOR"})
        void success() throws Exception {
            String json = """
                {"currentPassword": "oldpass123", "newPassword": "newpass123"}
                """;

            mockMvc.perform(put("/api/tutors/{tutorId}/my-password", tutorId)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

            verify(tutorService).changePassword(tutorId, "oldpass123", "newpass123");
        }
    }

    @Nested
    @DisplayName("DELETE /api/tutors/{tutorId}")
    class DeleteTest {

        @Test
        @DisplayName("正常系: 204")
        @org.springframework.security.test.context.support.WithMockUser(roles = {"MANAGER"})
        void success() throws Exception {
            mockMvc.perform(delete("/api/tutors/{tutorId}", tutorId)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

            verify(tutorService).delete(tutorId);
        }
    }

    // === helper: CustomUserDetails 相当の AuthenticationPrincipal ===
    record CustomAuthentication(UUID id) {}
}
