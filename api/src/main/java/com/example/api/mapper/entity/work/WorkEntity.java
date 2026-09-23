package com.example.api.mapper.entity.work;

import java.time.LocalDate;
import java.util.UUID;

import com.example.api.service.dto.work.LessonWorkDetailDto;
import com.example.api.service.dto.work.OfficeWorkDetailDto;
import com.example.api.service.dto.work.OtherWorkDetailDto;
import com.example.api.service.dto.work.WorkDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkEntity {
    
    private UUID id;
    private UUID tutorId;
    private UUID classroomId;
    private String classroomName;
    private Integer classroomNumber;
    private LocalDate workingDate;
    private Integer transportationFee;
    private LessonWorkDetailEntity lessonWorkDetailEntity;
    private OfficeWorkDetailEntity officeWorkDetailEntity;
    private OtherWorkDetailEntity otherWorkDetailEntity;

    public WorkDto toDto() {

        WorkDto dto = new WorkDto();

        dto.setId(this.id);
        dto.setTutorId(this.tutorId);
        dto.setClassroomId(this.classroomId);
        dto.setClassroomName(this.classroomName);
        dto.setClassroomNumber(this.classroomNumber);
        dto.setWorkingDate(this.workingDate);
        dto.setTransportationFee(this.transportationFee);
        // lessonWorkDetailEntity は Office/Other と同じく null 安全に扱い、
        // null の場合は空の LessonWorkDetailDto を返す（データ不整合時の NPE 回避）。
        dto.setLessonWorkDetailDto(this.lessonWorkDetailEntity != null
            ? this.lessonWorkDetailEntity.toDto() : new LessonWorkDetailDto());
        dto.setOfficeWorkDetailDto(this.officeWorkDetailEntity != null
            ? this.officeWorkDetailEntity.toDto() : new OfficeWorkDetailDto());
        dto.setOtherWorkDetailDto(this.otherWorkDetailEntity != null
            ? this.otherWorkDetailEntity.toDto() : new OtherWorkDetailDto());

        return dto;

    }

    public static WorkEntity fromDto(WorkDto dto) {
        return new WorkEntity(
            dto.getId(),
            dto.getTutorId(),
            dto.getClassroomId(),
            dto.getClassroomName(),
            dto.getClassroomNumber(),
            dto.getWorkingDate(),
            dto.getTransportationFee(),
            LessonWorkDetailEntity.fromDto(dto.getLessonWorkDetailDto()),
            OfficeWorkDetailEntity.fromDto(dto.getOfficeWorkDetailDto()),
            OtherWorkDetailEntity.fromDto(dto.getOtherWorkDetailDto())
        );
    }

}
