package com.example.api.controller.response;

import java.util.UUID;

import com.example.api.service.dto.ClassroomDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClassroomResponse {
    
    private UUID id;
    private String name;
    private Integer classroomNumber;

    public static ClassroomResponse fromDto(ClassroomDto dto) {
        return new ClassroomResponse(
            dto.getId(),
            dto.getName(),
            dto.getClassroomNumber()
        );
    }

}
