package com.example.api.controller.response;

import java.time.LocalDate;
import java.util.UUID;

import com.example.api.controller.model.work.LessonWorkDetail;
import com.example.api.controller.model.work.OfficeWorkDetail;
import com.example.api.controller.model.work.OtherWorkDetail;
import com.example.api.service.dto.work.WorkDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkResponse {
    
    private UUID id;
    private UUID tutorId;
    private UUID classroomId;
    private String classroomName;
    private Integer classroomNumber;
    private LocalDate workingDate;
    private Integer transportationFee;
    private Integer dailyAllowance;
    private LessonWorkDetail lessonWorkDetail;
    private OfficeWorkDetail officeWorkDetail;
    private OtherWorkDetail otherWorkDetail;

    public static WorkResponse fromDto(WorkDto dto) {
        return new WorkResponse(
            dto.getId(),
            dto.getTutorId(),
            dto.getClassroomId(),
            dto.getClassroomName(),
            dto.getClassroomNumber(),
            dto.getWorkingDate(),
            dto.getTransportationFee(),
            dto.getDailyAllowance(),
            LessonWorkDetail.fromDto(dto.getLessonWorkDetailDto()),
            OfficeWorkDetail.fromDto(dto.getOfficeWorkDetailDto()),
            OtherWorkDetail.fromDto(dto.getOtherWorkDetailDto())
        );
    }

}
