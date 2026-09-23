package com.example.api.mapper.entity.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.api.service.dto.template.LessonTemplateDetailDto;

class LessonTemplateDetailEntityTest {

    @Nested
    @DisplayName("toDto")
    class ToDto {

        @Test
        @DisplayName("フラグから periodCodes へ展開")
        void flagsToCodes() {
            LessonTemplateDetailEntity e = entity(true, true, false, false, true, false, false);
            assertEquals(List.of("M", "K", "B"), e.toDto().getPeriodCodes());
        }
    }

    @Nested
    @DisplayName("applyDto / fromDto")
    class FromDto {

        @Test
        @DisplayName("3コマ以上 → start/end が保持される")
        void keepsTimesWhenManyPeriods() {
            LessonTemplateDetailDto dto = new LessonTemplateDetailDto(
                UUID.randomUUID(), UUID.randomUUID(),
                LocalTime.of(13, 0), LocalTime.of(18, 0), 30, List.of("S", "A", "B"));
            LessonTemplateDetailEntity entity = LessonTemplateDetailEntity.fromDto(dto);
            assertEquals(LocalTime.of(13, 0), entity.getStartTime());
            assertEquals(LocalTime.of(18, 0), entity.getEndTime());
        }

        @Test
        @DisplayName("3コマ未満 → start/end が null 化")
        void dropsTimesWhenFewPeriods() {
            LessonTemplateDetailDto dto = new LessonTemplateDetailDto(
                UUID.randomUUID(), UUID.randomUUID(),
                LocalTime.of(13, 0), LocalTime.of(15, 0), 0, List.of("S"));
            LessonTemplateDetailEntity entity = LessonTemplateDetailEntity.fromDto(dto);
            assertNull(entity.getStartTime());
            assertNull(entity.getEndTime());
        }

        @Test
        @DisplayName("0コマ → breakMinutes が null 化")
        void zeroPeriodsBreakNull() {
            LessonTemplateDetailDto dto = new LessonTemplateDetailDto(
                UUID.randomUUID(), UUID.randomUUID(),
                LocalTime.of(13, 0), LocalTime.of(15, 0), 50, List.of());
            LessonTemplateDetailEntity entity = LessonTemplateDetailEntity.fromDto(dto);
            assertNull(entity.getBreakMinutes());
        }

        @Test
        @DisplayName("applyDto(null) は何もしない")
        void applyNullIsNoOp() {
            LessonTemplateDetailEntity entity = entity(true, false, false, false, false, false, false);
            entity.applyDto(null);
            assertTrue(entity.getPeriodCodeM());
            assertFalse(entity.getPeriodCodeK());
        }
    }

    private static LessonTemplateDetailEntity entity(
            boolean m, boolean k, boolean s, boolean a, boolean b, boolean c, boolean d) {
        LessonTemplateDetailEntity e = new LessonTemplateDetailEntity();
        e.setPeriodCodeM(m);
        e.setPeriodCodeK(k);
        e.setPeriodCodeS(s);
        e.setPeriodCodeA(a);
        e.setPeriodCodeB(b);
        e.setPeriodCodeC(c);
        e.setPeriodCodeD(d);
        return e;
    }
}
