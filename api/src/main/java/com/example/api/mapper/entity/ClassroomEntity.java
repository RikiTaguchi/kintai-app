package com.example.api.mapper.entity;

import java.util.UUID;

import com.example.api.service.dto.ClassroomDto;

import lombok.Data;

@Data
public class ClassroomEntity {

    private UUID id;
    private String name;
    private Integer classroomNumber;

    public ClassroomDto toDto() {
        return new ClassroomDto(
            this.id,
            this.name,
            this.classroomNumber
        );
    }

}
