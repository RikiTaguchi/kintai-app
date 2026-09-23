package com.example.api.controller.response;

import java.time.LocalDate;
import java.util.UUID;

import com.example.api.service.dto.TutorDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TutorResponse {
    
    private UUID id;
    private String loginId;
    private UUID classroomId;
    private String classroomName;
    private Integer classroomNumber;
    private String firstName;
    private String lastName;
    private Integer tutorNumber;
    private Boolean terminated;
    private LocalDate terminationDate;

    public static TutorResponse fromDto(TutorDto dto) {
        return new TutorResponse(
            dto.getId(),
            dto.getLoginId(),
            dto.getClassroomId(),
            dto.getClassroomName(),
            dto.getClassroomNumber(),
            dto.getFirstName(),
            dto.getLastName(),
            dto.getTutorNumber(),
            dto.getTerminated(),
            dto.getTerminationDate()
        );
    }

}
