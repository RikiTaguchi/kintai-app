package com.example.api.controller.request.work;

import java.time.LocalDate;
import java.util.UUID;

import com.example.api.controller.model.work.LessonWorkDetail;
import com.example.api.controller.model.work.OfficeWorkDetail;
import com.example.api.controller.model.work.OtherWorkDetail;
import com.example.api.controller.validation.ValidWorkDetail;
import com.example.api.service.dto.work.WorkDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
@ValidWorkDetail
public class WorkRegisterRequest {

    @NotNull(message = "Tutor id is required")
    private UUID tutorId;

    @NotNull(message = "Classroom id is required")
    private UUID classroomId;

    @NotNull(message = "Working date is required")
    private LocalDate workingDate;

    @NotNull(message = "Transportation fee is required")
    @PositiveOrZero(message = "Transportation fee must be zero or a positive number")
    private Integer transportationFee;

    @Valid
    private LessonWorkDetail lessonWorkDetail;

    @Valid
    private OfficeWorkDetail officeWorkDetail;

    @Valid
    private OtherWorkDetail otherWorkDetail;

    public WorkDto toDto() {
        WorkDto dto = new WorkDto();
        dto.setTutorId(this.tutorId);
        dto.setClassroomId(this.classroomId);
        dto.setWorkingDate(this.workingDate);
        dto.setTransportationFee(this.transportationFee);
        dto.setLessonWorkDetailDto(toLessonDto(this.lessonWorkDetail));
        dto.setOfficeWorkDetailDto(toOfficeDto(this.officeWorkDetail));
        dto.setOtherWorkDetailDto(toOtherDto(this.otherWorkDetail));
        return dto;
    }

    private static com.example.api.service.dto.work.LessonWorkDetailDto toLessonDto(LessonWorkDetail detail) {
        if (detail == null) {
            return new com.example.api.service.dto.work.LessonWorkDetailDto();
        }
        return detail.toDto();
    }

    private static com.example.api.service.dto.work.OfficeWorkDetailDto toOfficeDto(OfficeWorkDetail detail) {
        if (detail == null) {
            return new com.example.api.service.dto.work.OfficeWorkDetailDto();
        }
        return detail.toDto();
    }

    private static com.example.api.service.dto.work.OtherWorkDetailDto toOtherDto(OtherWorkDetail detail) {
        if (detail == null) {
            return new com.example.api.service.dto.work.OtherWorkDetailDto();
        }
        return detail.toDto();
    }

}
