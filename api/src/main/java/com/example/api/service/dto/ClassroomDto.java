package com.example.api.service.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClassroomDto {
    
    private UUID id;
    private String name;
    private Integer classroomNumber;

}
