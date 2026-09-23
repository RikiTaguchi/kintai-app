package com.example.api.mapper.entity.template;

import java.util.UUID;

import com.example.api.service.dto.template.OfficeTemplateDetailDto;
import com.example.api.service.dto.template.OtherTemplateDetailDto;
import com.example.api.service.dto.template.TemplateDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateEntity {

    private UUID id;
    private UUID tutorId;
    private String title;
    private UUID classroomId;
    private String classroomName;
    private Integer classroomNumber;
    private Integer transportationFee;
    private LessonTemplateDetailEntity lessonTemplateDetailEntity;
    private OfficeTemplateDetailEntity officeTemplateDetailEntity;
    private OtherTemplateDetailEntity otherTemplateDetailEntity;

    public TemplateDto toDto() {
        return new TemplateDto(
            this.id,
            this.tutorId,
            this.title,
            this.classroomId,
            this.classroomName,
            this.classroomNumber,
            this.transportationFee,
            this.lessonTemplateDetailEntity.toDto(),
            this.officeTemplateDetailEntity != null
                ? this.officeTemplateDetailEntity.toDto() : new OfficeTemplateDetailDto(),
            this.otherTemplateDetailEntity != null
                ? this.otherTemplateDetailEntity.toDto() : new OtherTemplateDetailDto()
        );
    }

    public static TemplateEntity fromDto(TemplateDto dto) {
        return new TemplateEntity(
            dto.getId(),
            dto.getTutorId(),
            dto.getTitle(),
            dto.getClassroomId(),
            dto.getClassroomName(),
            dto.getClassroomNumber(),
            dto.getTransportationFee(),
            LessonTemplateDetailEntity.fromDto(dto.getLessonTemplateDetailDto()),
            OfficeTemplateDetailEntity.fromDto(dto.getOfficeTemplateDetailDto()),
            OtherTemplateDetailEntity.fromDto(dto.getOtherTemplateDetailDto())
        );
    }

}
