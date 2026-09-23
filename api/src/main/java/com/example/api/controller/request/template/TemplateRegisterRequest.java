package com.example.api.controller.request.template;

import java.util.UUID;

import com.example.api.controller.model.template.LessonTemplateDetail;
import com.example.api.controller.model.template.OfficeTemplateDetail;
import com.example.api.controller.model.template.OtherTemplateDetail;
import com.example.api.service.dto.template.TemplateDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TemplateRegisterRequest {
    
    @NotNull(message = "Tutor id is required")
    private UUID tutorId;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
    private String title;

    @NotNull(message = "Classroom id is required")
    private UUID classroomId;

    @NotNull(message = "Transportation fee is required")
    @PositiveOrZero(message = "Transportation fee must be zero or a positive number")
    private Integer transportationFee;

    @Valid
    @NotNull(message = "Template detail is required")
    private LessonTemplateDetail lessonTemplateDetail;

    @Valid
    @NotNull(message = "Template detail is required")
    private OfficeTemplateDetail officeTemplateDetail;

    @Valid
    @NotNull(message = "Template detail is required")
    private OtherTemplateDetail otherTemplateDetail;

    public TemplateDto toDto() {
        TemplateDto dto = new TemplateDto();
        dto.setTutorId(this.tutorId);
        dto.setTitle(this.title);
        dto.setClassroomId(this.classroomId);
        dto.setTransportationFee(this.transportationFee);
        dto.setLessonTemplateDetailDto(this.lessonTemplateDetail.toDto());
        dto.setOfficeTemplateDetailDto(this.officeTemplateDetail.toDto());
        dto.setOtherTemplateDetailDto(this.otherTemplateDetail.toDto());
        return dto;
    }

}
