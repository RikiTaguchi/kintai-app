package com.example.api.controller.request.tutor;

import java.util.UUID;

import com.example.api.service.dto.TutorDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TutorRegisterRequest {
    
    @NotBlank(message = "Login id is required")
    @Size(max = 50, message = "Login id must be 50 characters or less")
    private String loginId;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
    private String password;

    @NotNull(message = "Classroom id is required")
    private UUID classroomId;

    @PositiveOrZero(message = "Tutor number must be zero or a positive number")
    private Integer tutorNumber;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be 100 characters or less")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be 100 characters or less")
    private String lastName;

    public TutorDto toDto() {
        TutorDto dto = new TutorDto();
        dto.setLoginId(this.loginId);
        dto.setPassword(this.password);
        dto.setClassroomId(this.classroomId);
        dto.setTutorNumber(this.tutorNumber);
        dto.setFirstName(this.firstName);
        dto.setLastName(this.lastName);
        return dto;
    }

}
