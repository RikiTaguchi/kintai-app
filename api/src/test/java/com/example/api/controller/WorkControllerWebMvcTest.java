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
import com.example.api.service.WorkService;
import com.example.api.service.dto.work.LessonWorkDetailDto;
import com.example.api.service.dto.work.OfficeWorkDetailDto;
import com.example.api.service.dto.work.OtherWorkDetailDto;
import com.example.api.service.dto.work.WorkDto;

@WebMvcTest(WorkController.class)
@Import(SecurityConfig.class)
class WorkControllerWebMvcTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private WorkService workService;
    @MockitoBean private AuthorizationService authorizationService;

    private UUID tutorId;
    private UUID classroomId;
    private UUID workId;

    @BeforeEach
    void setUp() {
        tutorId = UUID.randomUUID();
        classroomId = UUID.randomUUID();
        workId = UUID.randomUUID();
    }

    private WorkDto workDto() {
        WorkDto dto = new WorkDto();
        dto.setId(workId);
        dto.setTutorId(tutorId);
        dto.setClassroomId(classroomId);
        dto.setWorkingDate(LocalDate.of(2025, 9, 10));
        dto.setTransportationFee(100);
        dto.setDailyAllowance(400);
        dto.setLessonWorkDetailDto(lessonWorkDetailDto());
        dto.setOfficeWorkDetailDto(new OfficeWorkDetailDto(null, workId, java.time.LocalTime.of(9, 0), java.time.LocalTime.of(10, 0)));
        dto.setOtherWorkDetailDto(new OtherWorkDetailDto(null, workId, null, null, 0, null));
        return dto;
    }

    private LessonWorkDetailDto lessonWorkDetailDto() {
        return new LessonWorkDetailDto(null, workId,
            java.time.LocalTime.of(10, 0), java.time.LocalTime.of(12, 0), 0, List.of("M"));
    }

    private org.springframework.security.authentication.UsernamePasswordAuthenticationToken tutorAuth() {
        return SecurityUserTestHelper.auth(
            SecurityUserTestHelper.tutorUser(tutorId, classroomId), "TUTOR");
    }

    private org.springframework.security.authentication.UsernamePasswordAuthenticationToken managerAuth() {
        return SecurityUserTestHelper.auth(
            SecurityUserTestHelper.managerUser(UUID.randomUUID(), classroomId), "MANAGER");
    }

    @Nested
    @DisplayName("GET /api/works/{tutorId}")
    class FindAll {

        @Test
        @DisplayName("正常系: 200 (TUTOR)")
        void success() throws Exception {
            when(workService.findAll(tutorId, 2025, 9)).thenReturn(List.of(workDto()));

            mockMvc.perform(get("/api/works/{tutorId}", tutorId)
                    .param("year", "2025").param("month", "9")
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

            verify(authorizationService).assertTutorAccess(any(), org.mockito.ArgumentMatchers.eq(tutorId));
        }

        @Test
        @DisplayName("他人の勤務 (assertTutorAccess 拒否) → 403")
        void forbidden() throws Exception {
            org.mockito.Mockito.doThrow(
                new com.example.api.exception.AuthorizationFailedException("アクセス権がありません"))
                .when(authorizationService).assertTutorAccess(any(), org.mockito.ArgumentMatchers.eq(tutorId));

            mockMvc.perform(get("/api/works/{tutorId}", tutorId)
                    .param("year", "2025").param("month", "9")
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth())))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/works/{tutorId}/{workId}")
    class Find {

        @Test
        @DisplayName("正常系: 200")
        void success() throws Exception {
            when(workService.find(workId)).thenReturn(workDto());

            mockMvc.perform(get("/api/works/{tutorId}/{workId}", tutorId, workId)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workId.toString()));

            verify(authorizationService).assertResourceBelongsToTutor(tutorId, tutorId);
        }
    }

    @Nested
    @DisplayName("POST /api/works/{tutorId}")
    class Register {

        @Test
        @DisplayName("正常系: 201")
        void success() throws Exception {
            WorkDto dto = workDto();
            when(workService.register(any(WorkDto.class))).thenReturn(dto);

            String json = """
                {
                  "tutorId": "%s",
                  "classroomId": "%s",
                  "workingDate": "2025-09-10",
                  "transportationFee": 100,
                  "lessonWorkDetail": {"startTime":"10:00","endTime":"12:00","breakMinutes":0,"periodCodes":["M"]},
                  "officeWorkDetail": {"startTime":"09:00","endTime":"10:00"},
                  "otherWorkDetail": {"startTime":null,"endTime":null,"breakMinutes":0,"description":null}
                }
                """.formatted(tutorId, classroomId);

            mockMvc.perform(post("/api/works/{tutorId}", tutorId)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth()))
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(workId.toString()));
        }

        @Test
        @DisplayName("バリデーション (transportationFee < 0) → 400")
        void invalid() throws Exception {
            String json = """
                {
                  "tutorId": "%s",
                  "classroomId": "%s",
                  "workingDate": "2025-09-10",
                  "transportationFee": -1,
                  "lessonWorkDetail": {"startTime":"10:00","endTime":"12:00","breakMinutes":0,"periodCodes":["M"]},
                  "officeWorkDetail": {"startTime":"09:00","endTime":"10:00"},
                  "otherWorkDetail": {"startTime":null,"endTime":null,"breakMinutes":0,"description":null}
                }
                """.formatted(tutorId, classroomId);

            mockMvc.perform(post("/api/works/{tutorId}", tutorId)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth()))
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());

            verify(workService, never()).register(any());
        }

        @Nested
        @DisplayName("複合バリデーション (@ValidWorkDetail)")
        class WorkDetailComposite {

            @Test
            @DisplayName("全 detail が null → 400")
            void allDetailsNull() throws Exception {
                String json = """
                    {
                      "tutorId": "%s",
                      "classroomId": "%s",
                      "workingDate": "2025-09-10",
                      "transportationFee": 0
                    }
                    """.formatted(tutorId, classroomId);

                mockMvc.perform(post("/api/works/{tutorId}", tutorId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth()))
                        .with(org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("いずれかの業務を1つ以上入力してください"));

                verify(workService, never()).register(any());
            }

            @Test
            @DisplayName("全 detail 空オブジェクト（中身全 null） → 400")
            void allDetailsEmpty() throws Exception {
                String json = """
                    {
                      "tutorId": "%s",
                      "classroomId": "%s",
                      "workingDate": "2025-09-10",
                      "transportationFee": 0,
                      "lessonWorkDetail": {"startTime":null,"endTime":null,"breakMinutes":null,"periodCodes":[]},
                      "officeWorkDetail": {"startTime":null,"endTime":null},
                      "otherWorkDetail": {"startTime":null,"endTime":null,"breakMinutes":null,"description":null}
                    }
                    """.formatted(tutorId, classroomId);

                mockMvc.perform(post("/api/works/{tutorId}", tutorId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth()))
                        .with(org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("lessonWorkDetail のみ有効 → 201")
            void lessonOnly() throws Exception {
                WorkDto dto = workDto();
                when(workService.register(any())).thenReturn(dto);

                String json = """
                    {
                      "tutorId": "%s",
                      "classroomId": "%s",
                      "workingDate": "2025-09-10",
                      "transportationFee": 0,
                      "lessonWorkDetail": {"startTime":"10:00","endTime":"12:00","breakMinutes":0,"periodCodes":["M"]}
                    }
                    """.formatted(tutorId, classroomId);

                mockMvc.perform(post("/api/works/{tutorId}", tutorId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth()))
                        .with(org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isCreated());

                verify(workService).register(any());
            }

            @Test
            @DisplayName("授業時刻が片方のみ（コマなし） → 400（サービス層で無効化される入力を BV で事前に弾く）")
            void lessonSingleTimeOnly() throws Exception {
                String json = """
                    {
                      "tutorId": "%s",
                      "classroomId": "%s",
                      "workingDate": "2025-09-10",
                      "transportationFee": 0,
                      "lessonWorkDetail": {"startTime":"10:00","endTime":null,"breakMinutes":null,"periodCodes":[]}
                    }
                    """.formatted(tutorId, classroomId);

                mockMvc.perform(post("/api/works/{tutorId}", tutorId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth()))
                        .with(org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("いずれかの業務を1つ以上入力してください"));

                verify(workService, never()).register(any());
            }

            @Test
            @DisplayName("授業時刻が両方あり（コマなし） → BV 層は通し、サービス層の判定に委ねる")
            void lessonBothTimesNoPeriodsPassBeanValidation() throws Exception {
                WorkDto dto = workDto();
                when(workService.register(any())).thenReturn(dto);

                String json = """
                    {
                      "tutorId": "%s",
                      "classroomId": "%s",
                      "workingDate": "2025-09-10",
                      "transportationFee": 0,
                      "lessonWorkDetail": {"startTime":"10:00","endTime":"12:00","breakMinutes":0,"periodCodes":[]}
                    }
                    """.formatted(tutorId, classroomId);

                // Bean Validation 層では弾かれず service まで到達することを検証
                // （0コマ+時刻両方ありはサービス層の ScheduleValidator が
                //  「コマを1つ以上選択してください」で弾く設計）
                mockMvc.perform(post("/api/works/{tutorId}", tutorId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth()))
                        .with(org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isCreated());

                verify(workService).register(any());
            }

            @Test
            @DisplayName("officeWorkDetail のみ有効 → 201")
            void officeOnly() throws Exception {
                WorkDto dto = workDto();
                when(workService.register(any())).thenReturn(dto);

                String json = """
                    {
                      "tutorId": "%s",
                      "classroomId": "%s",
                      "workingDate": "2025-09-10",
                      "transportationFee": 0,
                      "officeWorkDetail": {"startTime":"09:00","endTime":"17:00"}
                    }
                    """.formatted(tutorId, classroomId);

                mockMvc.perform(post("/api/works/{tutorId}", tutorId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth()))
                        .with(org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isCreated());
            }

            @Test
            @DisplayName("otherWorkDetail のみ有効 → 201")
            void otherOnly() throws Exception {
                WorkDto dto = workDto();
                when(workService.register(any())).thenReturn(dto);

                String json = """
                    {
                      "tutorId": "%s",
                      "classroomId": "%s",
                      "workingDate": "2025-09-10",
                      "transportationFee": 0,
                      "otherWorkDetail": {"startTime":"10:00","endTime":"12:00","breakMinutes":0,"description":"会議"}
                    }
                    """.formatted(tutorId, classroomId);

                mockMvc.perform(post("/api/works/{tutorId}", tutorId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth()))
                        .with(org.springframework.security.test.web.servlet.request
                            .SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isCreated());
            }
        }
    }

    @Nested
    @DisplayName("PUT /api/works/{tutorId}/{workId}")
    class Edit {

        @Test
        @DisplayName("正常系: 200")
        void success() throws Exception {
            WorkDto dto = workDto();
            when(workService.edit(any(WorkDto.class))).thenReturn(dto);
            when(workService.find(workId)).thenReturn(dto);

            String json = """
                {
                  "id": "%s",
                  "tutorId": "%s",
                  "classroomId": "%s",
                  "workingDate": "2025-09-11",
                  "transportationFee": 100,
                  "lessonWorkDetail": {"startTime":"10:00","endTime":"12:00","breakMinutes":0,"periodCodes":["M"]},
                  "officeWorkDetail": {"startTime":"09:00","endTime":"10:00"},
                  "otherWorkDetail": {"startTime":null,"endTime":null,"breakMinutes":0,"description":null}
                }
                """.formatted(workId, tutorId, classroomId);

            mockMvc.perform(put("/api/works/{tutorId}/{workId}", tutorId, workId)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth()))
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());

            verify(workService).edit(any(WorkDto.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/works/{tutorId}/{workId}")
    class DeleteTest {

        @Test
        @DisplayName("正常系: 204")
        void success() throws Exception {
            when(workService.find(workId)).thenReturn(workDto());

            mockMvc.perform(delete("/api/works/{tutorId}/{workId}", tutorId, workId)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth()))
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

            verify(workService).delete(workId);
        }
    }

    @Nested
    @DisplayName("管理者が講師の勤務を触れること")
    class ManagerFlows {

        @Test
        @DisplayName("GET works (MANAGER) → 200")
        void managerCanRead() throws Exception {
            when(workService.findAll(tutorId, 2025, 9)).thenReturn(List.of(workDto()));

            mockMvc.perform(get("/api/works/{tutorId}", tutorId)
                    .param("year", "2025").param("month", "9")
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.authentication(managerAuth())))
                .andExpect(status().isOk());
        }
    }
}
