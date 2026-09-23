package com.example.api.mapper.entity.work;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.api.service.dto.work.LessonWorkDetailDto;
import com.example.api.service.dto.work.WorkDto;

class WorkEntityTest {

    @Nested
    @DisplayName("toDto")
    class ToDto {

        @Test
        @DisplayName("office/other が null でも空DTOに変換されて落ちない")
        void nullOfficeOtherTolerated() {
            WorkEntity e = fullEntity();
            e.setOfficeWorkDetailEntity(null);
            e.setOtherWorkDetailEntity(null);

            WorkDto dto = e.toDto();
            assertNotNull(dto.getOfficeWorkDetailDto());
            assertNotNull(dto.getOtherWorkDetailDto());
        }

        @Test
        @DisplayName("lessonWorkDetailEntity が null → 空 DTO で返却（Office/Other と挙動統一）")
        void nullLessonReturnsEmptyDto() {
            WorkEntity e = fullEntity();
            e.setLessonWorkDetailEntity(null);
            WorkDto dto = e.toDto();
            assertNotNull(dto.getLessonWorkDetailDto());
        }

        @Test
        @DisplayName("基本フィールドがコピーされる")
        void copiesBasicFields() {
            WorkEntity e = fullEntity();
            WorkDto dto = e.toDto();
            assertEquals(e.getId(), dto.getId());
            assertEquals(e.getTutorId(), dto.getTutorId());
            assertEquals(e.getClassroomId(), dto.getClassroomId());
            assertEquals(e.getClassroomName(), dto.getClassroomName());
            assertEquals(e.getClassroomNumber(), dto.getClassroomNumber());
            assertEquals(e.getWorkingDate(), dto.getWorkingDate());
            assertEquals(e.getTransportationFee(), dto.getTransportationFee());
        }
    }

    @Nested
    @DisplayName("fromDto")
    class FromDto {

        @Test
        @DisplayName("3つの detail を含む完全な WorkDto から変換")
        void convertsFull() {
            WorkDto dto = fullDto();
            WorkEntity entity = WorkEntity.fromDto(dto);
            assertEquals(dto.getId(), entity.getId());
            assertEquals(dto.getWorkingDate(), entity.getWorkingDate());
            assertNotNull(entity.getLessonWorkDetailEntity());
            assertNotNull(entity.getOfficeWorkDetailEntity());
            assertNotNull(entity.getOtherWorkDetailEntity());
        }
    }

    private static WorkEntity fullEntity() {
        UUID id = UUID.randomUUID();
        WorkEntity e = new WorkEntity();
        e.setId(id);
        e.setTutorId(UUID.randomUUID());
        e.setClassroomId(UUID.randomUUID());
        e.setClassroomName("戸塚");
        e.setClassroomNumber(22);
        e.setWorkingDate(LocalDate.of(2026, 1, 10));
        e.setTransportationFee(400);

        LessonWorkDetailEntity l = new LessonWorkDetailEntity();
        l.setPeriodCodeS(true);
        l.setPeriodCodeM(false);
        l.setPeriodCodeK(false);
        l.setPeriodCodeA(false);
        l.setPeriodCodeB(false);
        l.setPeriodCodeC(false);
        l.setPeriodCodeD(false);
        e.setLessonWorkDetailEntity(l);

        OfficeWorkDetailEntity o = new OfficeWorkDetailEntity();
        e.setOfficeWorkDetailEntity(o);

        OtherWorkDetailEntity ot = new OtherWorkDetailEntity();
        e.setOtherWorkDetailEntity(ot);
        return e;
    }

    private static WorkDto fullDto() {
        WorkDto dto = new WorkDto();
        dto.setId(UUID.randomUUID());
        dto.setTutorId(UUID.randomUUID());
        dto.setClassroomId(UUID.randomUUID());
        dto.setWorkingDate(LocalDate.of(2026, 1, 10));
        dto.setTransportationFee(400);
        dto.setDailyAllowance(200);
        dto.setLessonWorkDetailDto(new LessonWorkDetailDto(
            UUID.randomUUID(), UUID.randomUUID(), null, null, null, List.of("S")));
        dto.setOfficeWorkDetailDto(new com.example.api.service.dto.work.OfficeWorkDetailDto());
        dto.setOtherWorkDetailDto(new com.example.api.service.dto.work.OtherWorkDetailDto());
        return dto;
    }
}
