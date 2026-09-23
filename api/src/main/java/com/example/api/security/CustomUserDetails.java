package com.example.api.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.api.exception.BusinessException;
import com.example.api.service.dto.AccountDto;

import lombok.Getter;

@Getter
public class CustomUserDetails implements UserDetails {
    
    private final UUID id;
    private final String loginId;
    private final String password;
    private final String role;
    private final UUID classroomId;

    public CustomUserDetails(AccountDto dto) {
        this.loginId = dto.getLoginId();
        this.password = dto.getPassword();
        this.classroomId = dto.getClassroomId();

        if (dto.getManagerId() != null) {
            this.id = dto.getManagerId();
            this.role = "ROLE_MANAGER";
        } else if (dto.getTutorId() != null) {
            this.id = dto.getTutorId();
            this.role = "ROLE_TUTOR";
        } else {
            throw new BusinessException("アカウントが見つかりません");
        }
    }

    public String getLoginId() {
        return this.loginId;
    }

    @Override
    public String getUsername() {
        return getLoginId();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role));
    }

    public Boolean isManager() {
        return "ROLE_MANAGER".equals(this.role);
    }

    public Boolean isTutor() {
        return "ROLE_TUTOR".equals(this.role);
    }

}
