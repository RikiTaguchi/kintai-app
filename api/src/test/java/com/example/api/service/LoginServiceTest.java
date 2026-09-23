package com.example.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.example.api.exception.LoginException;
import com.example.api.security.CustomUserDetails;
import com.example.api.service.dto.AccountDto;
import com.example.api.service.dto.ManagerDto;
import com.example.api.service.dto.TutorDto;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private ManagerService managerService;
    @Mock private TutorService tutorService;

    private LoginService loginService;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        loginService = new LoginService(authenticationManager, managerService, tutorService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Nested
    @DisplayName("loginManager")
    class LoginManager {

        @Test
        @DisplayName("認証成功かつ MANAGER → 成功")
        void success() {
            UUID managerId = UUID.randomUUID();
            UUID classroomId = UUID.randomUUID();
            CustomUserDetails ud = managerUser(managerId, classroomId);
            Authentication auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
            when(authenticationManager.authenticate(any())).thenReturn(auth);

            ManagerDto expected = new ManagerDto(
                managerId, "m1", null, classroomId, "name", 22, "fn", "ln");
            when(managerService.find(managerId)).thenReturn(expected);

            ManagerDto dto = new ManagerDto();
            dto.setLoginId("m1");
            dto.setPassword("secret");

            ManagerDto result = loginService.loginManager(dto, request, response);

            assertEquals(managerId, result.getId());
            // user_role Cookie を発行していること
            String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
            org.junit.jupiter.api.Assertions.assertNotNull(setCookie);
            org.junit.jupiter.api.Assertions.assertTrue(setCookie.contains("user_role=MANAGER"));
            org.junit.jupiter.api.Assertions.assertTrue(setCookie.contains("HttpOnly"));
            org.junit.jupiter.api.Assertions.assertTrue(setCookie.contains("Secure"));
            org.junit.jupiter.api.Assertions.assertTrue(setCookie.contains("SameSite=Lax"));
        }

        @Test
        @DisplayName("tutor アカウントで manager ログイン試行 → LoginException")
        void tutorCannotLoginAsManager() {
            UUID tutorId = UUID.randomUUID();
            UUID classroomId = UUID.randomUUID();
            CustomUserDetails ud = tutorUser(tutorId, classroomId);
            Authentication auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
            when(authenticationManager.authenticate(any())).thenReturn(auth);

            ManagerDto dto = new ManagerDto();
            dto.setLoginId("t1");
            dto.setPassword("secret");

            LoginException ex = assertThrows(LoginException.class,
                () -> loginService.loginManager(dto, request, response));
            assertEquals("ログインIDまたはパスワードが正しくありません", ex.getMessage());
        }

        @Test
        @DisplayName("認証失敗 → LoginException")
        void authenticationFailed() {
            when(authenticationManager.authenticate(any()))
                .thenThrow(new AuthenticationException("bad creds") {});

            ManagerDto dto = new ManagerDto();
            dto.setLoginId("m1");
            dto.setPassword("wrong");

            LoginException ex = assertThrows(LoginException.class,
                () -> loginService.loginManager(dto, request, response));
            assertEquals("ログインIDまたはパスワードが正しくありません", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("loginTutor")
    class LoginTutor {

        @Test
        @DisplayName("認証成功かつ TUTOR（有効在職） → 成功")
        void success() {
            UUID tutorId = UUID.randomUUID();
            UUID classroomId = UUID.randomUUID();
            CustomUserDetails ud = tutorUser(tutorId, classroomId);
            Authentication auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
            when(authenticationManager.authenticate(any())).thenReturn(auth);

            TutorDto expected = new TutorDto(
                tutorId, "t1", null, classroomId, "n", 1, "fn", "ln", 1, false, null);
            when(tutorService.find(tutorId)).thenReturn(expected);

            TutorDto dto = new TutorDto();
            dto.setLoginId("t1");
            dto.setPassword("secret");

            TutorDto result = loginService.loginTutor(dto, request, response);
            assertEquals(tutorId, result.getId());

            String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
            org.junit.jupiter.api.Assertions.assertTrue(setCookie.contains("user_role=TUTOR"));
        }

        @Test
        @DisplayName("退職済み講師はログイン不可 → LoginException")
        void terminatedForbidden() {
            UUID tutorId = UUID.randomUUID();
            UUID classroomId = UUID.randomUUID();
            CustomUserDetails ud = tutorUser(tutorId, classroomId);
            Authentication auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
            when(authenticationManager.authenticate(any())).thenReturn(auth);

            TutorDto terminatedTutor = new TutorDto(
                tutorId, "t1", null, classroomId, "n", 1, "fn", "ln", 1, true,
                LocalDate.of(2024, 12, 31));
            when(tutorService.find(tutorId)).thenReturn(terminatedTutor);

            TutorDto dto = new TutorDto();
            dto.setLoginId("t1");
            dto.setPassword("secret");

            LoginException ex = assertThrows(LoginException.class,
                () -> loginService.loginTutor(dto, request, response));
            assertEquals("ログインIDまたはパスワードが正しくありません", ex.getMessage());
        }

        @Test
        @DisplayName("manager アカウントで tutor ログイン試行 → LoginException")
        void managerCannotLoginAsTutor() {
            UUID managerId = UUID.randomUUID();
            UUID classroomId = UUID.randomUUID();
            CustomUserDetails ud = managerUser(managerId, classroomId);
            Authentication auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
            when(authenticationManager.authenticate(any())).thenReturn(auth);

            TutorDto dto = new TutorDto();
            dto.setLoginId("m1");
            dto.setPassword("secret");

            assertThrows(LoginException.class,
                () -> loginService.loginTutor(dto, request, response));
        }

        @Test
        @DisplayName("認証失敗 → LoginException")
        void authenticationFailed() {
            when(authenticationManager.authenticate(any()))
                .thenThrow(new AuthenticationException("bad") {});

            TutorDto dto = new TutorDto();
            dto.setLoginId("t1");
            dto.setPassword("wrong");

            assertThrows(LoginException.class,
                () -> loginService.loginTutor(dto, request, response));
        }
    }

    // === helpers ===

    private CustomUserDetails managerUser(UUID managerId, UUID classroomId) {
        AccountDto d = new AccountDto(UUID.randomUUID(), "m1", "pass", managerId, classroomId, null);
        return new CustomUserDetails(d);
    }

    private CustomUserDetails tutorUser(UUID tutorId, UUID classroomId) {
        AccountDto d = new AccountDto(UUID.randomUUID(), "t1", "pass", null, classroomId, tutorId);
        return new CustomUserDetails(d);
    }
}
