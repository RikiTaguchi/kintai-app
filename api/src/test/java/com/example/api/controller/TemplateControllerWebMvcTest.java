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
import com.example.api.service.TemplateService;
import com.example.api.service.dto.template.LessonTemplateDetailDto;
import com.example.api.service.dto.template.OfficeTemplateDetailDto;
import com.example.api.service.dto.template.OtherTemplateDetailDto;
import com.example.api.service.dto.template.TemplateDto;

@WebMvcTest(TemplateController.class)
@Import(SecurityConfig.class)
class TemplateControllerWebMvcTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private TemplateService templateService;
    @MockitoBean private AuthorizationService authorizationService;

    private UUID tutorId;
    private UUID classroomId;
    private UUID templateId;

    @BeforeEach
    void setUp() {
        tutorId = UUID.randomUUID();
        classroomId = UUID.randomUUID();
        templateId = UUID.randomUUID();
    }

    private TemplateDto templateDto() {
        TemplateDto dto = new TemplateDto();
        dto.setId(templateId);
        dto.setTutorId(tutorId);
        dto.setClassroomId(classroomId);
        dto.setTitle("基本テンプレート");
        dto.setTransportationFee(100);
        dto.setLessonTemplateDetailDto(new LessonTemplateDetailDto(null, templateId, null, null, null, List.of("M")));
        dto.setOfficeTemplateDetailDto(new OfficeTemplateDetailDto(null, templateId, null, null));
        dto.setOtherTemplateDetailDto(new OtherTemplateDetailDto(null, templateId, null, null, null, null));
        return dto;
    }

    private org.springframework.security.authentication.UsernamePasswordAuthenticationToken tutorAuth() {
        return SecurityUserTestHelper.auth(
            SecurityUserTestHelper.tutorUser(tutorId, classroomId), "TUTOR");
    }

    @Nested
    @DisplayName("GET /api/templates/{tutorId}")
    class FindAll {

        @Test
        @DisplayName("正常系: 200 (TUTOR 本人)")
        void success() throws Exception {
            when(templateService.findAll(tutorId)).thenReturn(List.of(templateDto()));

            mockMvc.perform(get("/api/templates/{tutorId}", tutorId)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

            verify(authorizationService).assertTutorSelf(any(), org.mockito.ArgumentMatchers.eq(tutorId));
        }

        @Test
        @DisplayName("他人のテンプレート → 403")
        void forbidden() throws Exception {
            org.mockito.Mockito.doThrow(
                new com.example.api.exception.AuthorizationFailedException("他人のリソースです"))
                .when(authorizationService).assertTutorSelf(any(), org.mockito.ArgumentMatchers.eq(tutorId));

            mockMvc.perform(get("/api/templates/{tutorId}", tutorId)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth())))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/templates/{tutorId}")
    class Register {

        @Test
        @DisplayName("正常系: 201")
        void success() throws Exception {
            TemplateDto dto = templateDto();
            when(templateService.register(any(TemplateDto.class))).thenReturn(dto);

            String json = """
                {
                  "tutorId": "%s",
                  "title": "基本テンプレート",
                  "classroomId": "%s",
                  "transportationFee": 100,
                  "lessonTemplateDetail": {"startTime":"10:00","endTime":"12:00","breakMinutes":0,"periodCodes":["M"]},
                  "officeTemplateDetail": {"startTime":"09:00","endTime":"10:00"},
                  "otherTemplateDetail": {"startTime":null,"endTime":null,"breakMinutes":0,"description":null}
                }
                """.formatted(tutorId, classroomId);

            mockMvc.perform(post("/api/templates/{tutorId}", tutorId)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth())
                    )
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(templateId.toString()));
        }

        @Test
        @DisplayName("バリデーション (title 空) → 400")
        void invalid() throws Exception {
            String json = """
                {
                  "tutorId": "%s",
                  "title": "",
                  "classroomId": "%s",
                  "transportationFee": 100,
                  "lessonTemplateDetail": {"startTime":"10:00","endTime":"12:00","breakMinutes":0,"periodCodes":["M"]},
                  "officeTemplateDetail": {"startTime":"09:00","endTime":"10:00"},
                  "otherTemplateDetail": {"startTime":null,"endTime":null,"breakMinutes":0,"description":null}
                }
                """.formatted(tutorId, classroomId);

            mockMvc.perform(post("/api/templates/{tutorId}", tutorId)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth()))
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());

            verify(templateService, never()).register(any());
        }
    }

    @Nested
    @DisplayName("PUT /api/templates/{tutorId}/{templateId}")
    class Edit {

        @Test
        @DisplayName("正常系: 200")
        void success() throws Exception {
            TemplateDto dto = templateDto();
            when(templateService.edit(any(TemplateDto.class))).thenReturn(dto);
            when(templateService.find(templateId)).thenReturn(dto);

            String json = """
                {
                  "id": "%s",
                  "tutorId": "%s",
                  "title": "更新テンプレート",
                  "classroomId": "%s",
                  "transportationFee": 100,
                  "lessonTemplateDetail": {"startTime":"10:00","endTime":"12:00","breakMinutes":0,"periodCodes":["M"]},
                  "officeTemplateDetail": {"startTime":"09:00","endTime":"10:00"},
                  "otherTemplateDetail": {"startTime":null,"endTime":null,"breakMinutes":0,"description":null}
                }
                """.formatted(templateId, tutorId, classroomId);

            mockMvc.perform(put("/api/templates/{tutorId}/{templateId}", tutorId, templateId)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth()))
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/templates/{tutorId}/{templateId}")
    class DeleteTest {

        @Test
        @DisplayName("正常系: 204")
        void success() throws Exception {
            when(templateService.find(templateId)).thenReturn(templateDto());

            mockMvc.perform(delete("/api/templates/{tutorId}/{templateId}", tutorId, templateId)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.authentication(tutorAuth()))
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

            verify(templateService).delete(templateId);
        }
    }
}
