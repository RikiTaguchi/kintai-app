package com.example.api.service.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManagerDto {
    
    private UUID id;
    private String loginId;
    private String password;
    private UUID classroomId;
    private String classroomName;
    private Integer classroomNumber;
    private String firstName;
    private String lastName;

}
