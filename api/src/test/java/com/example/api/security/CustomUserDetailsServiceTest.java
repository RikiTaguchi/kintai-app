package com.example.api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.api.mapper.AccountMapper;
import com.example.api.mapper.entity.AccountEntity;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private AccountMapper accountMapper;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new CustomUserDetailsService(accountMapper);
    }

    @Test
    void loadUserByUsername_returnsUserDetails() {
        UUID accountId = UUID.randomUUID();
        UUID managerId = UUID.randomUUID();
        UUID classroomId = UUID.randomUUID();
        AccountEntity e = entity(accountId, managerId, null, classroomId);
        when(accountMapper.selectByLoginId("login1")).thenReturn(Optional.of(e));

        UserDetails ud = service.loadUserByUsername("login1");
        assertEquals("login1", ud.getUsername());
        assertTrue(ud.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")));
    }

    @Test
    void loadUserByUsername_throwsWhenMissing() {
        when(accountMapper.selectByLoginId("ghost")).thenReturn(Optional.empty());
        UsernameNotFoundException ex = assertThrows(
            UsernameNotFoundException.class,
            () -> service.loadUserByUsername("ghost"));
        assertEquals("account not found", ex.getMessage());
    }

    private AccountEntity entity(UUID id, UUID managerId, UUID tutorId, UUID classroomId) {
        AccountEntity e = new AccountEntity();
        e.setId(id);
        e.setLoginId("login1");
        e.setPassword("hashed");
        e.setManagerId(managerId);
        e.setTutorId(tutorId);
        e.setClassroomId(classroomId);
        return e;
    }
}
