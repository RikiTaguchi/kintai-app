package com.example.api.service.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountDto {

    private UUID id;
    private String loginId;
    private String password;
    private UUID managerId;
    private UUID classroomId;
    private UUID tutorId;

}
