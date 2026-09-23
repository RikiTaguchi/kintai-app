package com.example.api.controller.request.tutor;

import com.example.api.service.dto.TutorDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TutorLoginRequest {
    
    @NotBlank(message = "Login id is required")
    @Size(max = 50, message = "Login id must be 50 characters or less")
    private String loginId;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
    private String password;

    public TutorDto toDto() {
        TutorDto dto = new TutorDto();
        dto.setLoginId(this.loginId);
        dto.setPassword(this.password);
        return dto;
    }

}
