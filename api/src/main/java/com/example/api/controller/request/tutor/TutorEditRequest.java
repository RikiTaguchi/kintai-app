package com.example.api.controller.request.tutor;

import java.time.LocalDate;
import java.util.UUID;

import com.example.api.service.dto.TutorDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TutorEditRequest {
    
    @NotNull(message = "Id is required")
    private UUID id;

    @PositiveOrZero(message = "Tutor number must be zero or a positive number")
    private Integer tutorNumber;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be 100 characters or less")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be 100 characters or less")
    private String lastName;

    @NotNull(message = "Terminated is required")
    private Boolean terminated;

    private LocalDate terminationDate;

    public TutorDto toDto() {
        TutorDto dto = new TutorDto();
        dto.setId(this.id);
        dto.setTutorNumber(this.tutorNumber);
        dto.setFirstName(this.firstName);
        dto.setLastName(this.lastName);
        dto.setTerminated(this.terminated);
        dto.setTerminationDate(this.terminationDate);
        return dto;
    }

}
