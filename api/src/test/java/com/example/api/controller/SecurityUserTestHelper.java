package com.example.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.example.api.security.CustomUserDetails;
import com.example.api.service.dto.AccountDto;

/**
 * @WebMvcTest で @AuthenticationPrincipal CustomUserDetails を注入するためのヘルパー。
 */
final class SecurityUserTestHelper {

    private SecurityUserTestHelper() {}

    static CustomUserDetails managerUser(UUID managerId, UUID classroomId) {
        return new CustomUserDetails(
            new AccountDto(UUID.randomUUID(), "manager1", "pass", managerId, classroomId, null));
    }

    static CustomUserDetails tutorUser(UUID tutorId, UUID classroomId) {
        return new CustomUserDetails(
            new AccountDto(UUID.randomUUID(), "tutor1", "pass", null, classroomId, tutorId));
    }

    static UsernamePasswordAuthenticationToken auth(CustomUserDetails user, String role) {
        return new UsernamePasswordAuthenticationToken(
            user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }
}
