package com.example.api.service;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import com.example.api.exception.LoginException;
import com.example.api.security.CustomUserDetails;
import com.example.api.service.dto.ManagerDto;
import com.example.api.service.dto.TutorDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final ManagerService managerService;
    private final TutorService tutorService;

    public ManagerDto loginManager(
        ManagerDto dto,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getLoginId(), dto.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            if (!userDetails.getRole().equals("ROLE_MANAGER")) {
                throw new LoginException("ログインIDまたはパスワードが正しくありません");
            }

            setupSession(authentication, httpRequest, httpResponse, "MANAGER");

            return managerService.find(userDetails.getId());

        } catch (AuthenticationException e) {
            log.warn("Manager authentication failed for loginId: {}", dto.getLoginId());
            throw new LoginException("ログインIDまたはパスワードが正しくありません");
        }
    }

    public TutorDto loginTutor(
        TutorDto dto,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getLoginId(), dto.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            if (!userDetails.getRole().equals("ROLE_TUTOR")) {
                throw new LoginException("ログインIDまたはパスワードが正しくありません");
            }

            TutorDto tutor = tutorService.find(userDetails.getId());
            if (tutor.getTerminated()) {
                throw new LoginException("ログインIDまたはパスワードが正しくありません");
            }

            setupSession(authentication, httpRequest, httpResponse, "TUTOR");

            return tutor;

        } catch (AuthenticationException e) {
            log.warn("Tutor authentication failed for loginId: {}", dto.getLoginId());
            throw new LoginException("ログインIDまたはパスワードが正しくありません");
        }
    }

    private void setupSession(
        Authentication authentication,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse,
        String role
    ) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        addRoleCookie(httpResponse, role);
    }

    private void addRoleCookie(HttpServletResponse response, String role) {

        ResponseCookie roleCookie = ResponseCookie.from("user_role", role)
            .path("/")
            .maxAge(Duration.ofDays(30))
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, roleCookie.toString());

    }

}
