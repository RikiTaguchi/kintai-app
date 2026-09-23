package com.example.api.controller.request.manager;

import com.example.api.service.dto.ManagerDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ManagerLoginRequest {
    
    @NotBlank(message = "Login id is required")
    @Size(max = 50, message = "Login id must be 50 characters or less")
    private String loginId;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
    private String password;

    public ManagerDto toDto() {
        ManagerDto dto = new ManagerDto();
        dto.setLoginId(this.loginId);
        dto.setPassword(this.password);
        return dto;
    }

}
