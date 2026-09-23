package com.example.api.controller.response;

import java.util.UUID;

import com.example.api.controller.model.template.LessonTemplateDetail;
import com.example.api.controller.model.template.OfficeTemplateDetail;
import com.example.api.controller.model.template.OtherTemplateDetail;
import com.example.api.service.dto.template.TemplateDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TemplateResponse {
    
    private UUID id;
    private UUID tutorId;
    private String title;
    private UUID classroomId;
    private String classroomName;
    private Integer classroomNumber;
    private Integer transportationFee;
    private LessonTemplateDetail lessonTemplateDetail;
    private OfficeTemplateDetail officeTemplateDetail;
    private OtherTemplateDetail otherTemplateDetail;

    public static TemplateResponse fromDto(TemplateDto dto) {
        return new TemplateResponse(
            dto.getId(),
            dto.getTutorId(),
            dto.getTitle(),
            dto.getClassroomId(),
            dto.getClassroomName(),
            dto.getClassroomNumber(),
            dto.getTransportationFee(),
            LessonTemplateDetail.fromDto(dto.getLessonTemplateDetailDto()),
            OfficeTemplateDetail.fromDto(dto.getOfficeTemplateDetailDto()),
            OtherTemplateDetail.fromDto(dto.getOtherTemplateDetailDto())
        );
    }

}
