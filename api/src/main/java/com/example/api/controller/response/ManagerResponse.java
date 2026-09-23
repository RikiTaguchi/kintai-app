package com.example.api.controller.response;

import java.util.UUID;

import com.example.api.service.dto.ManagerDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ManagerResponse {

    private UUID id;
    private String loginId;
    private UUID classroomId;
    private String classroomName;
    private Integer classroomNumber;
    private String firstName;
    private String lastName;

    public static ManagerResponse fromDto(ManagerDto dto) {
        return new ManagerResponse(
            dto.getId(),
            dto.getLoginId(),
            dto.getClassroomId(),
            dto.getClassroomName(),
            dto.getClassroomNumber(),
            dto.getFirstName(),
            dto.getLastName()
        );
    }

}
