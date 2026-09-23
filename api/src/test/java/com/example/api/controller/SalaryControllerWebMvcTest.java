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
import com.example.api.service.SalaryService;
import com.example.api.service.dto.SalaryDto;

@WebMvcTest(SalaryController.class)
@Import(SecurityConfig.class)
class SalaryControllerWebMvcTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private SalaryService salaryService;
    @MockitoBean private AuthorizationService authorizationService;

    private UUID tutorId;
    private UUID salaryId;

    @BeforeEach
    void setUp() {
        tutorId = UUID.randomUUID();
        salaryId = UUID.randomUUID();
    }

    private SalaryDto salaryDto() {
        return new SalaryDto(salaryId, tutorId, LocalDate.of(2025, 4, 1), 3000, 2000, 100);
    }

    @Nested
    @DisplayName("GET /api/salaries/{tutorId}")
    class FindAll {

        @Test
        @DisplayName("正常系: 200 + list")
        @org.springframework.security.test.context.support.WithMockUser(roles = {"MANAGER"})
        void success() throws Exception {
            when(salaryService.findAll(tutorId)).thenReturn(List.of(salaryDto()));

            mockMvc.perform(get("/api/salaries/{tutorId}", tutorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

            verify(authorizationService).assertTutorAccess(any(), org.mockito.ArgumentMatchers.eq(tutorId));
        }
    }

    @Nested
    @DisplayName("GET /api/salaries/{tutorId}/{salaryId}")
    class Find {

        @Test
        @DisplayName("正常系: 200")
        @org.springframework.security.test.context.support.WithMockUser(roles = {"MANAGER"})
        void success() throws Exception {
            when(salaryService.find(salaryId)).thenReturn(salaryDto());

            mockMvc.perform(get("/api/salaries/{tutorId}/{salaryId}", tutorId, salaryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salaryId.toString()));

            verify(authorizationService).assertResourceBelongsToTutor(tutorId, tutorId);
        }

        @Test
        @DisplayName("他講師の給与 → 403")
        @org.springframework.security.test.context.support.WithMockUser(roles = {"MANAGER"})
        void forbidden() throws Exception {
            when(salaryService.find(salaryId)).thenReturn(salaryDto());
            org.mockito.Mockito.doThrow(
                new com.example.api.exception.AuthorizationFailedException("他講師のリソースです"))
                .when(authorizationService).assertResourceBelongsToTutor(tutorId, tutorId);

            mockMvc.perform(get("/api/salaries/{tutorId}/{salaryId}", tutorId, salaryId))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/salaries/{tutorId}")
    class Register {

        @Test
        @DisplayName("正常系: 201")
        @org.springframework.security.test.context.support.WithMockUser(roles = {"MANAGER"})
        void success() throws Exception {
            when(salaryService.register(any(SalaryDto.class))).thenReturn(salaryDto());

            String json = """
                {
                  "tutorId": "%s",
                  "effectiveDate": "2025-04-01",
                  "lessonWage": 3000,
                  "officeWage": 2000,
                  "transportationFee": 100
                }
                """.formatted(tutorId);

            mockMvc.perform(post("/api/salaries/{tutorId}", tutorId)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(salaryId.toString()));
        }

        @Test
        @DisplayName("バリデーション (lessonWage=0) → 400")
        @org.springframework.security.test.context.support.WithMockUser(roles = {"MANAGER"})
        void invalid() throws Exception {
            String json = """
                {
                  "tutorId": "%s",
                  "effectiveDate": "2025-04-01",
                  "lessonWage": 0,
                  "officeWage": 2000,
                  "transportationFee": 100
                }
                """.formatted(tutorId);

            mockMvc.perform(post("/api/salaries/{tutorId}", tutorId)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());

            verify(salaryService, never()).register(any());
        }
    }

    @Nested
    @DisplayName("PUT /api/salaries/{tutorId}/{salaryId}")
    class Edit {

        @Test
        @DisplayName("正常系: 200")
        @org.springframework.security.test.context.support.WithMockUser(roles = {"MANAGER"})
        void success() throws Exception {
            when(salaryService.edit(any(SalaryDto.class))).thenReturn(salaryDto());
            // edit 内で所有確認のため salaryService.find も呼ばれる
            when(salaryService.find(salaryId)).thenReturn(salaryDto());

            String json = """
                {
                  "id": "%s",
                  "tutorId": "%s",
                  "effectiveDate": "2025-04-01",
                  "lessonWage": 3000,
                  "officeWage": 2000,
                  "transportationFee": 100
                }
                """.formatted(salaryId, tutorId);

            mockMvc.perform(put("/api/salaries/{tutorId}/{salaryId}", tutorId, salaryId)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());

            verify(salaryService).edit(any(SalaryDto.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/salaries/{tutorId}/{salaryId}")
    class DeleteTest {

        @Test
        @DisplayName("正常系: 204")
        @org.springframework.security.test.context.support.WithMockUser(roles = {"MANAGER"})
        void success() throws Exception {
            when(salaryService.find(salaryId)).thenReturn(salaryDto());

            mockMvc.perform(delete("/api/salaries/{tutorId}/{salaryId}", tutorId, salaryId)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

            verify(salaryService).delete(salaryId);
        }
    }
}
