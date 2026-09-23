package com.example.api.mapper.entity;

import java.util.UUID;

import com.example.api.service.dto.ManagerDto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ManagerEntity {
    
    private UUID id;
    private UUID accountId;
    private String loginId;
    private String password;
    private UUID classroomId;
    private String classroomName;
    private Integer classroomNumber;
    private String firstName;
    private String lastName;

    public ManagerDto toDto() {
        return new ManagerDto(
            this.id,
            this.loginId,
            this.password,
            this.classroomId,
            this.classroomName,
            this.classroomNumber,
            this.firstName,
            this.lastName
        );
    }

}
