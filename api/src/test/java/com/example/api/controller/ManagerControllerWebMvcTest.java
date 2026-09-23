package com.example.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.api.security.SecurityConfig;
import com.example.api.service.AuthorizationService;
import com.example.api.service.ManagerService;
import com.example.api.service.dto.ManagerDto;

@WebMvcTest(ManagerController.class)
@Import(SecurityConfig.class)
class ManagerControllerWebMvcTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private ManagerService managerService;
    @MockitoBean private AuthorizationService authorizationService;

    private UUID managerId;
    private UUID classroomId;

    @BeforeEach
    void setUp() {
        managerId = UUID.randomUUID();
        classroomId = UUID.randomUUID();
    }

    private ManagerDto managerDto() {
        return new ManagerDto(managerId, "manager1", null, classroomId, "戸塚", 22, "太郎", "山田");
    }

    @Nested
    @DisplayName("POST /api/managers (permitAll)")
    class Register {

        @Test
        @DisplayName("正常系: 201 + ManagerResponse")
        void success() throws Exception {
            ManagerDto created = managerDto();
            when(managerService.register(any(ManagerDto.class))).thenReturn(created);

            String json = """
                {
                  "loginId": "manager1",
                  "password": "password123",
                  "classroomId": "%s",
                  "firstName": "太郎",
                  "lastName": "山田"
                }
                """.formatted(classroomId);

            mockMvc.perform(post("/api/managers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(managerId.toString()))
                .andExpect(jsonPath("$.loginId").value("manager1"))
                .andExpect(jsonPath("$.firstName").value("太郎"));
        }

        @Test
        @DisplayName("異常系: バリデーション違反（loginId空） → 400")
        void invalidLoginId() throws Exception {
            String json = """
                {
                  "loginId": "",
                  "password": "password123",
                  "classroomId": "%s",
                  "firstName": "太郎",
                  "lastName": "山田"
                }
                """.formatted(classroomId);

            mockMvc.perform(post("/api/managers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isString());

            verify(managerService, never()).register(any());
        }
    }

    @Nested
    @DisplayName("PUT /api/managers/{managerId}")
    class Edit {

        @Test
        @DisplayName("正常系: 200 + ManagerResponse")
        @org.springframework.security.test.context.support.WithMockUser(
            username = "manager1", roles = {"MANAGER"})
        void success() throws Exception {
            ManagerDto updated = managerDto();
            when(managerService.edit(any(ManagerDto.class))).thenReturn(updated);

            String json = """
                {
                  "id": "%s",
                  "loginId": "manager1",
                  "classroomId": "%s",
                  "firstName": "太郎",
                  "lastName": "山田"
                }
                """.formatted(managerId, classroomId);

            mockMvc.perform(put("/api/managers/{managerId}", managerId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(managerId.toString()));

            verify(authorizationService).assertPathId(managerId, managerId);
            verify(authorizationService).assertManagerSelf(any(), org.mockito.ArgumentMatchers.eq(managerId));
            verify(managerService).edit(any(ManagerDto.class));
        }

        @Test
        @DisplayName("本人以外の操作 (assertManagerSelf がAuthorizationFailedException) → 403")
        @org.springframework.security.test.context.support.WithMockUser(
            username = "manager1", roles = {"MANAGER"})
        void forbidden() throws Exception {
            org.mockito.Mockito.doThrow(
                new com.example.api.exception.AuthorizationFailedException("本人以外の操作はできません"))
                .when(authorizationService).assertManagerSelf(any(), org.mockito.ArgumentMatchers.eq(managerId));

            String json = """
                {
                  "id": "%s",
                  "loginId": "manager1",
                  "classroomId": "%s",
                  "firstName": "太郎",
                  "lastName": "山田"
                }
                """.formatted(managerId, classroomId);

            mockMvc.perform(put("/api/managers/{managerId}", managerId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());

            verify(managerService, never()).edit(any());
        }
    }

    @Nested
    @DisplayName("PUT /api/managers/{managerId}/my-password")
    class ChangePassword {

        @Test
        @DisplayName("正常系: 204")
        @org.springframework.security.test.context.support.WithMockUser(
            username = "manager1", roles = {"MANAGER"})
        void success() throws Exception {
            String json = """
                {
                  "currentPassword": "oldpass123",
                  "newPassword": "newpass123"
                }
                """;

            mockMvc.perform(put("/api/managers/{managerId}/my-password", managerId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

            verify(managerService).changePassword(managerId, "oldpass123", "newpass123");
        }
    }

    @Nested
    @DisplayName("DELETE /api/managers/{managerId}")
    class Delete {

        @Test
        @DisplayName("正常系: 204")
        @org.springframework.security.test.context.support.WithMockUser(
            username = "manager1", roles = {"MANAGER"})
        void success() throws Exception {
            mockMvc.perform(delete("/api/managers/{managerId}", managerId)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

            verify(managerService).delete(managerId);
        }
    }
}
