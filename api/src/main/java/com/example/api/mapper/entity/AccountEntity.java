package com.example.api.mapper.entity;

import java.util.UUID;

import com.example.api.service.dto.AccountDto;

import lombok.Data;

@Data
public class AccountEntity {
    
    private UUID id;
    private String loginId;
    private String password;
    private UUID managerId;
    private UUID classroomId;
    private UUID tutorId;

    public AccountDto toDto() {
        return new AccountDto(
            this.id,
            this.loginId,
            this.password,
            this.managerId,
            this.classroomId,
            this.tutorId
        );
    }

}
