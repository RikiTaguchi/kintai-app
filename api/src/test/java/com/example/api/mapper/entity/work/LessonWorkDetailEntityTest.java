package com.example.api.mapper.entity.work;

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

import com.example.api.service.dto.work.LessonWorkDetailDto;

class LessonWorkDetailEntityTest {

    @Nested
    @DisplayName("toDto")
    class ToDto {

        @Test
        @DisplayName("全フラグ true のとき全コマが含まれる")
        void allTrue() {
            LessonWorkDetailEntity e = entity(true, true, true, true, true, true, true);
            LessonWorkDetailDto dto = e.toDto();
            assertEquals(List.of("M", "K", "S", "A", "B", "C", "D"), dto.getPeriodCodes());
        }

        @Test
        @DisplayName("全フラグ false のとき空リスト")
        void allFalse() {
            LessonWorkDetailEntity e = entity(false, false, false, false, false, false, false);
            assertEquals(List.of(), e.toDto().getPeriodCodes());
        }

        @Test
        @DisplayName("順序は MKSA BCD の固定順")
        void orderFixed() {
            LessonWorkDetailEntity e = entity(false, false, true, false, false, true, false);
            assertEquals(List.of("S", "C"), e.toDto().getPeriodCodes());
        }

        @Test
        @DisplayName("id/workId/startTime/endTime/breakMinutes がコピーされる")
        void basicFieldsCopied() {
            UUID id = UUID.randomUUID();
            UUID workId = UUID.randomUUID();
            LocalTime start = LocalTime.of(13, 0);
            LocalTime end = LocalTime.of(15, 0);
            LessonWorkDetailEntity e = entity(true, false, false, false, false, false, false);
            e.setId(id);
            e.setWorkId(workId);
            e.setStartTime(start);
            e.setEndTime(end);
            e.setBreakMinutes(20);

            LessonWorkDetailDto dto = e.toDto();
            assertEquals(id, dto.getId());
            assertEquals(workId, dto.getWorkId());
            assertEquals(start, dto.getStartTime());
            assertEquals(end, dto.getEndTime());
            assertEquals(20, dto.getBreakMinutes());
        }
    }

    @Nested
    @DisplayName("fromDto / applyDto")
    class FromDto {

        @Test
        @DisplayName("3コマ以上 → start/end が保存される")
        void threeOrMorePeriodsKeepTimes() {
            LessonWorkDetailDto dto = dto(
                LocalTime.of(13, 0), LocalTime.of(18, 30), 30, List.of("S", "A", "B"));
            LessonWorkDetailEntity entity = LessonWorkDetailEntity.fromDto(dto);
            assertEquals(LocalTime.of(13, 0), entity.getStartTime());
            assertEquals(LocalTime.of(18, 30), entity.getEndTime());
        }

        @Test
        @DisplayName("3コマ未満（1〜2コマ） → start/end が null 化される")
        void oneOrTwoPeriodsDropsTimes() {
            LessonWorkDetailDto dto = dto(
                LocalTime.of(13, 0), LocalTime.of(15, 0), 0, List.of("S"));
            LessonWorkDetailEntity entity = LessonWorkDetailEntity.fromDto(dto);
            assertNull(entity.getStartTime());
            assertNull(entity.getEndTime());
        }

        @Test
        @DisplayName("0コマ → start/end は除外ルール適用なし（dto値がそのまま）")
        void zeroPeriodsNoDrop() {
            LessonWorkDetailDto dto = dto(
                LocalTime.of(13, 0), LocalTime.of(15, 0), null, List.of());
            LessonWorkDetailEntity entity = LessonWorkDetailEntity.fromDto(dto);
            // 0コマは periodCodes.isEmpty() なので3コマ未満ルールに入らない → dtoの値が反映
            assertEquals(LocalTime.of(13, 0), entity.getStartTime());
            // ただし breakMinutes は null に強制される
            assertNull(entity.getBreakMinutes());
        }

        @Test
        @DisplayName("各コマコードでのからbooleanへの反映")
        void periodCodesToFlags() {
            LessonWorkDetailDto dto = dto(null, null, 10, List.of("M", "K", "D"));
            LessonWorkDetailEntity entity = LessonWorkDetailEntity.fromDto(dto);
            assertTrue(entity.getPeriodCodeM());
            assertTrue(entity.getPeriodCodeK());
            assertFalse(entity.getPeriodCodeS());
            assertFalse(entity.getPeriodCodeA());
            assertFalse(entity.getPeriodCodeB());
            assertFalse(entity.getPeriodCodeC());
            assertTrue(entity.getPeriodCodeD());
        }

        @Test
        @DisplayName("periodCodes が null → 全 periodCode フラグが false として扱われる")
        void nullPeriodCodes() {
            // null は空リスト扱いに正規化される (applyPeriodCodes と挙動を統一)。
            LessonWorkDetailDto dto = dto(null, null, null, null);
            LessonWorkDetailEntity entity = LessonWorkDetailEntity.fromDto(dto);
            assertFalse(entity.getPeriodCodeM());
            assertFalse(entity.getPeriodCodeK());
            assertFalse(entity.getPeriodCodeS());
            assertFalse(entity.getPeriodCodeA());
            assertFalse(entity.getPeriodCodeB());
            assertFalse(entity.getPeriodCodeC());
            assertFalse(entity.getPeriodCodeD());
        }

        @Test
        @DisplayName("applyDto(null) は何もしない")
        void applyNullIsNoOp() {
            LessonWorkDetailEntity entity = entity(true, false, false, false, false, false, false);
            entity.applyDto(null);
            assertTrue(entity.getPeriodCodeM());
        }
    }

    // === helper ===

    private static LessonWorkDetailEntity entity(
            boolean m, boolean k, boolean s, boolean a, boolean b, boolean c, boolean d) {
        LessonWorkDetailEntity e = new LessonWorkDetailEntity();
        e.setPeriodCodeM(m);
        e.setPeriodCodeK(k);
        e.setPeriodCodeS(s);
        e.setPeriodCodeA(a);
        e.setPeriodCodeB(b);
        e.setPeriodCodeC(c);
        e.setPeriodCodeD(d);
        return e;
    }

    private static LessonWorkDetailDto dto(
            LocalTime start, LocalTime end, Integer breakMin, List<String> codes) {
        LessonWorkDetailDto d = new LessonWorkDetailDto();
        d.setId(UUID.randomUUID());
        d.setWorkId(UUID.randomUUID());
        d.setStartTime(start);
        d.setEndTime(end);
        d.setBreakMinutes(breakMin);
        d.setPeriodCodes(codes);
        return d;
    }
}
