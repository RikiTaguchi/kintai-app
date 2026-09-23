package com.example.api.service.dto.template;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateDto {
    
    private UUID id;
    private UUID tutorId;
    private String title;
    private UUID classroomId;
    private String classroomName;
    private Integer classroomNumber;
    private Integer transportationFee;
    private LessonTemplateDetailDto lessonTemplateDetailDto;
    private OfficeTemplateDetailDto officeTemplateDetailDto;
    private OtherTemplateDetailDto otherTemplateDetailDto;

}
