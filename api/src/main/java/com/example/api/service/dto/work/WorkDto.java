package com.example.api.service.dto.work;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkDto {
    
    private UUID id;
    private UUID tutorId;
    private UUID classroomId;
    private String classroomName;
    private Integer classroomNumber;
    private LocalDate workingDate;
    private Integer dailyAllowance;
    private Integer transportationFee;
    private LessonWorkDetailDto lessonWorkDetailDto;
    private OfficeWorkDetailDto officeWorkDetailDto;
    private OtherWorkDetailDto otherWorkDetailDto;

}
