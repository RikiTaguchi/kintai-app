package com.example.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

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
import com.example.api.exception.LoginException;
import com.example.api.service.LoginService;
import com.example.api.service.dto.ManagerDto;
import com.example.api.service.dto.TutorDto;

@WebMvcTest(LoginController.class)
@Import(SecurityConfig.class)
class LoginControllerWebMvcTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private LoginService loginService;

    @Nested
    @DisplayName("POST /api/managers/login (permitAll)")
    class ManagerLogin {

        @Test
        @DisplayName("正常系: 200 + ManagerResponse")
        void success() throws Exception {
            UUID managerId = UUID.randomUUID();
            UUID classroomId = UUID.randomUUID();
            ManagerDto dto = new ManagerDto(managerId, "manager1", null, classroomId, "戸塚", 22, "太郎", "山田");
            when(loginService.loginManager(any(ManagerDto.class), any(), any())).thenReturn(dto);

            String json = """
                {"loginId":"manager1","password":"password123"}
                """;

            mockMvc.perform(post("/api/managers/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(managerId.toString()))
                .andExpect(jsonPath("$.loginId").value("manager1"));
        }

        @Test
        @DisplayName("認証失敗 (LoginException) → 401")
        void failed() throws Exception {
            when(loginService.loginManager(any(ManagerDto.class), any(), any()))
                .thenThrow(new LoginException("ログインIDまたはパスワードが正しくありません"));

            String json = """
                {"loginId":"manager1","password":"wrongpass"}
                """;

            mockMvc.perform(post("/api/managers/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("ログインIDまたはパスワードが正しくありません"));
        }

        @Test
        @DisplayName("バリデーション違反 (password 短い) → 400")
        void invalid() throws Exception {
            String json = """
                {"loginId":"m1","password":"short"}
                """;

            mockMvc.perform(post("/api/managers/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());

            verify(loginService, never()).loginManager(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("POST /api/tutors/login (permitAll)")
    class TutorLogin {

        @Test
        @DisplayName("正常系: 200 + TutorResponse")
        void success() throws Exception {
            UUID tutorId = UUID.randomUUID();
            UUID classroomId = UUID.randomUUID();
            TutorDto dto = new TutorDto(tutorId, "tutor1", null, classroomId, "戸塚", 22, "花子", "佐藤", 1, false, null);
            when(loginService.loginTutor(any(TutorDto.class), any(), any())).thenReturn(dto);

            String json = """
                {"loginId":"tutor1","password":"password123"}
                """;

            mockMvc.perform(post("/api/tutors/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tutorId.toString()));
        }
    }
}
