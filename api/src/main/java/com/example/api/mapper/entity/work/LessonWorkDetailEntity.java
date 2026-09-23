package com.example.api.mapper.entity.work;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.api.common.constant.LessonContents;
import com.example.api.service.dto.work.LessonWorkDetailDto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LessonWorkDetailEntity {
    
    private UUID id;
    private UUID workId;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer breakMinutes;
    private Boolean periodCodeM;
    private Boolean periodCodeK;
    private Boolean periodCodeS;
    private Boolean periodCodeA;
    private Boolean periodCodeB;
    private Boolean periodCodeC;
    private Boolean periodCodeD;

    public LessonWorkDetailDto toDto() {

        LessonWorkDetailDto dto = new LessonWorkDetailDto();

        dto.setId(this.id);
        dto.setWorkId(this.workId);
        dto.setStartTime(this.startTime);
        dto.setEndTime(this.endTime);
        dto.setBreakMinutes(this.breakMinutes);

        List<String> periodCodes = new ArrayList<>();
        if (this.periodCodeM) periodCodes.add("M");
        if (this.periodCodeK) periodCodes.add("K");
        if (this.periodCodeS) periodCodes.add("S");
        if (this.periodCodeA) periodCodes.add("A");
        if (this.periodCodeB) periodCodes.add("B");
        if (this.periodCodeC) periodCodes.add("C");
        if (this.periodCodeD) periodCodes.add("D");

        dto.setPeriodCodes(periodCodes);

        return dto;
    }

    public static LessonWorkDetailEntity fromDto(LessonWorkDetailDto dto) {

        LessonWorkDetailEntity entity = new LessonWorkDetailEntity();

        entity.setId(dto.getId());
        entity.setWorkId(dto.getWorkId());
        entity.applyDto(dto);

        return entity;

    }

    private void applyPeriodCodes(List<String> periodCodes) {

        if (periodCodes == null) {
            periodCodes = List.of();
        }

        this.periodCodeM = periodCodes.contains("M");
        this.periodCodeK = periodCodes.contains("K");
        this.periodCodeS = periodCodes.contains("S");
        this.periodCodeA = periodCodes.contains("A");
        this.periodCodeB = periodCodes.contains("B");
        this.periodCodeC = periodCodes.contains("C");
        this.periodCodeD = periodCodes.contains("D");

    }

    public void applyDto(LessonWorkDetailDto dto) {

        if (dto == null) return;

        // periodCodes が null の場合は空リスト扱い（NPE 回避）。
        // applyPeriodCodes と挙動を合わせるため、ここでも null → empty に正規化する。
        List<String> periodCodes = dto.getPeriodCodes() != null
            ? dto.getPeriodCodes()
            : List.of();

        if (!periodCodes.isEmpty() && periodCodes.size() < LessonContents.LESSON_OUT_SIDE_HOURS_BORDER) {
            this.startTime = null;
            this.endTime = null;
        } else {
            this.startTime = dto.getStartTime();
            this.endTime = dto.getEndTime();
        }
        this.breakMinutes = periodCodes.isEmpty() ? null : dto.getBreakMinutes();

        applyPeriodCodes(periodCodes);

    }

}
