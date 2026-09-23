package com.example.api.mapper.entity;

import java.time.LocalDate;
import java.util.UUID;

import com.example.api.service.dto.TutorDto;

import lombok.Data;

@Data
public class TutorEntity {
    
    private UUID id;
    private UUID accountId;
    private String loginId;
    private String password;
    private UUID classroomId;
    private String classroomName;
    private Integer classroomNumber;
    private String firstName;
    private String lastName;
    private Integer tutorNumber;
    private Boolean terminated;
    private LocalDate terminationDate;

    public TutorDto toDto() {
        return new TutorDto(
            this.id,
            this.loginId,
            this.password,
            this.classroomId,
            this.classroomName,
            this.classroomNumber,
            this.firstName,
            this.lastName,
            this.tutorNumber,
            this.terminated,
            this.terminationDate
        );
    }

}
