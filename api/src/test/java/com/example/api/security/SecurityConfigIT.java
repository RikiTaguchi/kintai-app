package com.example.api.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.api.AbstractPostgresIT;
import com.example.api.service.LoginService;
import com.example.api.service.dto.ManagerDto;
import com.example.api.service.dto.TutorDto;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigIT extends AbstractPostgresIT {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private LoginService loginService;

    @Test
    @DisplayName("未認証: /api/works/{id} → 401")
    void unauthenticated_fails401() throws Exception {
        mockMvc.perform(get("/api/works/" + java.util.UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("permitAll: GET /api/classrooms → 200 (認証不要)")
    void public_classroomsOK() throws Exception {
        mockMvc.perform(get("/api/classrooms"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("permitAll: GET /api/csrf → 200 + XSRF-TOKEN 発行")
    void public_csrfTokenIssued() throws Exception {
        mockMvc.perform(get("/api/csrf"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST (CSRF なし): /api/tutors/login → 403")
    void postWithoutCsrfFails403() throws Exception {
        String json = """
            {"loginId":"tutor1","password":"password123"}
            """;
        mockMvc.perform(post("/api/tutors/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isForbidden()); // CSRF token が無い
    }

    @Test
    @DisplayName("POST (CSRF あり): /api/tutors/login → service は呼ばれるが未認証 → 401")
    void postWithCsrfGives401OnBadCreds() throws Exception {
        org.mockito.Mockito.when(loginService.loginTutor(
                org.mockito.ArgumentMatchers.any(TutorDto.class),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
            .thenThrow(new com.example.api.exception.LoginException("認証失敗"));

        String json = """
            {"loginId":"tutor1","password":"wrongpass"}
            """;
        mockMvc.perform(post("/api/tutors/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(csrf()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/managers/login (CSRF あり・存在しないアカウントへのログイン試行も認証層の 401 は起さない → service 例外で 401)")
    void managerLoginUnauthorized() throws Exception {
        org.mockito.Mockito.when(loginService.loginManager(
                org.mockito.ArgumentMatchers.any(ManagerDto.class),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
            .thenThrow(new com.example.api.exception.LoginException("ログインIDまたはパスワードが正しくありません"));

        String json = """
            {"loginId":"m1","password":"wrongpass"}
            """;
        mockMvc.perform(post("/api/managers/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(csrf()))
            .andExpect(status().isUnauthorized())
            .andExpect(status().reason(org.hamcrest.Matchers.nullValue()));
    }
}
