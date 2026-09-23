package com.example.api.controller.request.manager;

import java.util.UUID;

import com.example.api.service.dto.ManagerDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ManagerRegisterRequest {
    
    @NotBlank(message = "Login id is required")
    @Size(max = 50, message = "Login id must be 50 characters or less")
    private String loginId;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
    private String password;

    @NotNull(message = "Classroom id is required")
    private UUID classroomId;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be 100 characters or less")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be 100 characters or less")
    private String lastName;

    public ManagerDto toDto() {
        ManagerDto dto = new ManagerDto();
        dto.setLoginId(this.loginId);
        dto.setPassword(this.password);
        dto.setClassroomId(this.classroomId);
        dto.setFirstName(this.firstName);
        dto.setLastName(this.lastName);
        return dto;
    }

}
