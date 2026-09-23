package com.example.api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.example.api.exception.BusinessException;
import com.example.api.service.dto.AccountDto;

class CustomUserDetailsTest {

    @Nested
    @DisplayName("ロール振り分け")
    class RoleAssignment {

        @Test
        @DisplayName("managerId がある → ROLE_MANAGER, id は managerId")
        void managerRole() {
            UUID managerId = UUID.randomUUID();
            AccountDto dto = new AccountDto(
                UUID.randomUUID(), "manager1", "hashed", managerId, UUID.randomUUID(), null);

            CustomUserDetails ud = new CustomUserDetails(dto);
            assertEquals("ROLE_MANAGER", ud.getRole());
            assertEquals(managerId, ud.getId());
            assertTrue(ud.isManager());
            assertFalse(ud.isTutor());
            assertTrue(ud.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER")));
        }

        @Test
        @DisplayName("managerId がnullで tutorId がある → ROLE_TUTOR")
        void tutorRole() {
            UUID tutorId = UUID.randomUUID();
            AccountDto dto = new AccountDto(
                UUID.randomUUID(), "tutor1", "hashed", null, UUID.randomUUID(), tutorId);

            CustomUserDetails ud = new CustomUserDetails(dto);
            assertEquals("ROLE_TUTOR", ud.getRole());
            assertEquals(tutorId, ud.getId());
            assertTrue(ud.isTutor());
            assertFalse(ud.isManager());
        }

        @Test
        @DisplayName("両方null → BusinessException")
        void bothNull() {
            AccountDto dto = new AccountDto(
                UUID.randomUUID(), "x", "y", null, null, null);
            BusinessException ex = assertThrows(BusinessException.class,
                () -> new CustomUserDetails(dto));
            assertEquals("アカウントが見つかりません", ex.getMessage());
        }

        @Test
        @DisplayName("両方あり → managerId が優先される")
        void managerWinsWhenBothSet() {
            UUID managerId = UUID.randomUUID();
            UUID tutorId = UUID.randomUUID();
            AccountDto dto = new AccountDto(
                UUID.randomUUID(), "both", "y", managerId, UUID.randomUUID(), tutorId);
            CustomUserDetails ud = new CustomUserDetails(dto);
            assertEquals(managerId, ud.getId());
            assertEquals("ROLE_MANAGER", ud.getRole());
        }
    }

    @Nested
    @DisplayName("UserDetails インターフェース")
    class UserDetailsInterface {

        @Test
        @DisplayName("getUsername() は loginId を返す")
        void usernameEqualsLoginId() {
            AccountDto dto = new AccountDto(
                UUID.randomUUID(), "login1", "hashed", UUID.randomUUID(), null, null);
            CustomUserDetails ud = new CustomUserDetails(dto);
            assertEquals("login1", ud.getUsername());
            assertEquals("login1", ud.getLoginId());
        }

        @Test
        @DisplayName("authorities は単一のロールを含む")
        void authoritiesContainsRole() {
            AccountDto dto = new AccountDto(
                UUID.randomUUID(), "login1", "hashed", UUID.randomUUID(), null, null);
            CustomUserDetails ud = new CustomUserDetails(dto);
            assertEquals(1, ud.getAuthorities().size());
        }
    }
}
