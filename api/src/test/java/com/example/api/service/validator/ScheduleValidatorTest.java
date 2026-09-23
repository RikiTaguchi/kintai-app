package com.example.api.service.validator;

import static com.example.api.ScheduleFixtures.emptySchedule;
import static com.example.api.ScheduleFixtures.lesson;
import static com.example.api.ScheduleFixtures.office;
import static com.example.api.ScheduleFixtures.other;
import static com.example.api.ScheduleFixtures.schedule;
import static com.example.api.ScheduleFixtures.time;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.api.exception.InvalidInputException;
import com.example.api.service.dto.schedule.ScheduleDto;

/**
 * {@link ScheduleValidator#checkInput(ScheduleDto)} の分岐網羅テスト。
 */
class ScheduleValidatorTest {

    @Nested
    @DisplayName("休憩時間の必須チェック")
    class BreakRequired {

        @Test
        @DisplayName("授業コマ選択済みで休憩時間がnull → 例外")
        void lessonPeriodsSelectedButBreakNull() {
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(null, null, null, List.of("M")));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("授業業務の休憩時間を入力してください", ex.getMessage());
        }

        @Test
        @DisplayName("その他業務ありで休憩時間がnull → 例外")
        void otherWorkStartedButBreakNull() {
            ScheduleDto dto = emptySchedule();
            dto.setOtherScheduleDetailDto(other(time(10, 0), time(12, 0), null, "test"));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("その他業務の休憩時間を入力してください", ex.getMessage());
        }

        @Test
        @DisplayName("その他業務：時刻・休憩が揃っていて業務内容がnull → 例外")
        void otherTimesAndBreakButNoDescription() {
            ScheduleDto dto = emptySchedule();
            dto.setOtherScheduleDetailDto(other(time(10, 0), time(12, 0), 0, null));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("その他業務の業務内容を入力してください", ex.getMessage());
        }

        @Test
        @DisplayName("その他業務：業務内容が空白のみ → 例外")
        void otherDescriptionBlank() {
            ScheduleDto dto = emptySchedule();
            dto.setOtherScheduleDetailDto(other(time(10, 0), time(12, 0), 0, "  "));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("その他業務の業務内容を入力してください", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("時刻の対存在（all-or-nothing）")
    class TimePairRequired {

        @Test
        @DisplayName("事務：開始時刻のみ → 例外")
        void officeStartOnly() {
            ScheduleDto dto = emptySchedule();
            dto.setOfficeScheduleDetailDto(office(time(10, 0), null));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("事務業務の開始・終了時刻は両方入力してください", ex.getMessage());
        }

        @Test
        @DisplayName("事務：終了時刻のみ → 例外")
        void officeEndOnly() {
            ScheduleDto dto = emptySchedule();
            dto.setOfficeScheduleDetailDto(office(null, time(12, 0)));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("事務業務の開始・終了時刻は両方入力してください", ex.getMessage());
        }

        @Test
        @DisplayName("その他：開始時刻のみ（休憩あり） → 対存在エラーが休憩エラーより先")
        void otherStartOnly() {
            ScheduleDto dto = emptySchedule();
            dto.setOtherScheduleDetailDto(other(time(10, 0), null, 0, "作業"));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("その他業務の開始・終了時刻は両方入力してください", ex.getMessage());
        }

        @Test
        @DisplayName("その他：終了時刻のみ（休憩あり） → 例外")
        void otherEndOnly() {
            ScheduleDto dto = emptySchedule();
            dto.setOtherScheduleDetailDto(other(null, time(12, 0), 0, "作業"));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("その他業務の開始・終了時刻は両方入力してください", ex.getMessage());
        }

        @Test
        @DisplayName("その他：終了時刻のみ・休憩null → 休憩エラーではなく対存在エラー")
        void otherEndOnlyWithoutBreak() {
            ScheduleDto dto = emptySchedule();
            dto.setOtherScheduleDetailDto(other(null, time(12, 0), null, "作業"));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("その他業務の開始・終了時刻は両方入力してください", ex.getMessage());
        }

        @Test
        @DisplayName("その他：業務内容のみ → 例外")
        void otherDescriptionOnly() {
            ScheduleDto dto = emptySchedule();
            dto.setOtherScheduleDetailDto(other(null, null, null, "作業"));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("その他業務の開始・終了時刻は両方入力してください", ex.getMessage());
        }

        @Test
        @DisplayName("その他：休憩時間のみ → 例外")
        void otherBreakOnly() {
            ScheduleDto dto = emptySchedule();
            dto.setOtherScheduleDetailDto(other(null, null, 0, null));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("その他業務の開始・終了時刻は両方入力してください", ex.getMessage());
        }

        @Test
        @DisplayName("その他：休憩時間のみ（0以外） → 例外")
        void otherBreakOnlyNonZero() {
            ScheduleDto dto = emptySchedule();
            dto.setOtherScheduleDetailDto(other(null, null, 30, null));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("その他業務の開始・終了時刻は両方入力してください", ex.getMessage());
        }

        @Test
        @DisplayName("授業：3コマ以上で開始時刻のみ → 例外（既存 L47-50 の回帰）")
        void lessonThreePeriodsStartOnly() {
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(13, 0), null, 0, List.of("S", "A", "B")));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("3コマ以上の場合、開始・終了時刻を入力してください", ex.getMessage());
        }

        @Test
        @DisplayName("授業：1〜2コマで時刻不整合（終了<開始） → 通過（時刻は無効化される仕様）")
        void lessonTwoPeriodsInconsistentTimesPasses() {
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(15, 0), time(13, 0), 0, List.of("S")));

            assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
        }

        @Test
        @DisplayName("授業：1〜2コマで片方のみ時刻 → 通過（時刻は無効化される仕様）")
        void lessonTwoPeriodsOneTimeOnlyPasses() {
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(13, 0), null, 0, List.of("S", "A")));

            assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
        }
    }

    @Nested
    @DisplayName("3コマ以上の授業ルール")
    class ThreePeriodRule {

        @Test
        @DisplayName("3コマ選択で開始時刻がnull → 例外")
        void threePeriodsNoStartTime() {
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(null, time(15, 0), 0, List.of("S", "A", "B")));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("3コマ以上の場合、開始・終了時刻を入力してください", ex.getMessage());
        }

        @Test
        @DisplayName("3コマ選択で終了時刻がnull → 例外")
        void threePeriodsNoEndTime() {
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(13, 0), null, 0, List.of("S", "A", "B")));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("3コマ以上の場合、開始・終了時刻を入力してください", ex.getMessage());
        }

        @Test
        @DisplayName("3コマかつ開始終了時刻あり → 通過")
        void threePeriodsWithTimesValid() {
            // 勤務 13:00-18:30（330分）, コマ3個（300分） → 余裕30分, 休憩30分
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(13, 0), time(18, 30), 30, List.of("S", "A", "B")));

            assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
        }
    }

    @Nested
    @DisplayName("授業業務の時刻整合性")
    class LessonTimeConsistency {

        @Test
        @DisplayName("終了 <= 開始 → 例外")
        void endBeforeStart() {
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(14, 0), time(13, 0), 0, List.of()));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("授業業務の終了時刻は開始時刻より後にしてください", ex.getMessage());
        }

        @Test
        @DisplayName("終了 == 開始 → 例外")
        void endEqualsStart() {
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(14, 0), time(14, 0), 0, List.of()));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("授業業務の終了時刻は開始時刻より後にしてください", ex.getMessage());
        }

        @Test
        @DisplayName("勤務時間を超える休憩 → 例外")
        void breakExceedsAvailable() {
            // 14:00-15:00 = 60min, 0コマで LESSON_MINUTES*0=0 → 60 usable, break=61
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(14, 0), time(15, 0), 61, List.of()));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("授業業務の休憩時間が勤務時間を超えています", ex.getMessage());
        }

        @Test
        @DisplayName("授業時間外のコマ選択 → 例外（早すぎ）")
        void periodBeforeWorkStart() {
            // 勤務 10:00-22:00 だが M=9:10 コマを選択（3コマ以上のため時刻が有効）
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(10, 0), time(22, 0), 120, List.of("M", "S", "A")));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("選択したコマが授業時間の範囲外です", ex.getMessage());
        }

        @Test
        @DisplayName("授業時間外のコマ選択 → 例外（遅すぎ）")
        void periodAfterWorkEnd() {
            // 勤務 13:00-19:00 だが D=19:50-21:20 コマを選択（3コマ以上のため時刻が有効）
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(13, 0), time(19, 0), 0, List.of("S", "A", "D")));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("選択したコマが授業時間の範囲外です", ex.getMessage());
        }

        @Test
        @DisplayName("1〜2コマでは授業時刻の整合性は検証されない（時刻は保存時に無効化される仕様）")
        void periodsOneOrTwoSkipTimeConsistency() {
            // 2コマ + コマ範囲外の時刻 → 時刻チェック自体がスキップされ通過
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(6, 0), time(7, 0), 0, List.of("S", "A")));

            assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
        }

        @Test
        @DisplayName("範囲内なら複数コマでも通過")
        void allPeriodsValidWhenWithinRange() {
            // 勤務 13:00-21:30（510分）, コマ5個（500分） → 余裕10分, 休憩10分
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(13, 0), time(21, 30), 10, List.of("S", "A", "B", "C", "D")));

            assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
        }
    }

    @Nested
    @DisplayName("コマ不正")
    class InvalidPeriodCode {

        @Test
        @DisplayName("不明なコマコード → 例外")
        void unknownCode() {
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(14, 0), time(16, 0), 0, List.of("X")));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("不正なコマコードです", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("事務・その他業務の時刻整合性")
    class OfficeAndOtherConsistency {

        @Test
        @DisplayName("事務：終了 <= 開始 → 例外")
        void officeEndBeforeStart() {
            ScheduleDto dto = emptySchedule();
            dto.setOfficeScheduleDetailDto(office(time(15, 0), time(14, 0)));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("事務業務の終了時刻は開始時刻より後にしてください", ex.getMessage());
        }

        @Test
        @DisplayName("その他：終了 <= 開始 → 例外")
        void otherEndBeforeStart() {
            ScheduleDto dto = emptySchedule();
            dto.setOtherScheduleDetailDto(other(time(15, 0), time(14, 0), 0, "x"));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("その他業務の終了時刻は開始時刻より後にしてください", ex.getMessage());
        }

        @Test
        @DisplayName("その他：休憩が勤務時間超過 → 例外")
        void otherBreakExceedsDuration() {
            ScheduleDto dto = emptySchedule();
            dto.setOtherScheduleDetailDto(other(time(14, 0), time(15, 0), 61, "x"));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("その他業務の休憩時間が勤務時間を超えています", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("授業休憩 vs 他業務占有の資源整合（逆転ケース防止）")
    class ResourceConsistency {

        // ── 空きコマ時間帯での業務（本対応の発端となった実運用パターン） ──
        // コマ A(14:50-16:20)・C(18:10-19:40)・D(19:50-21:20) を勤務 14:00-21:30 で選択。
        // 選択コマに挟まれた空き時間（16:20-18:10 = 110分）に事務・その他を配置する。

        @Test
        @DisplayName("A・C・Dコマ間の空き時間に事務100分＋控除100分 → 通過（境界ちょうど）")
        void freeSlotOccupationEqualToBreak() {
            // 空き 16:20-18:10 のうち事務 16:40-18:10（90分）、控除 90 → ちょうどで通過
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(14, 0), time(21, 30), 90, List.of("A", "C", "D")));
            dto.setOfficeScheduleDetailDto(office(time(16, 40), time(18, 10)));

            assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
        }

        @Test
        @DisplayName("A・C・Dコマ間の空き時間に事務100分だが控除50分 → 例外（本対応の発端ケース）")
        void freeSlotOccupationExceedsBreak() {
            // 事務 16:40-18:10（90分）が空き時間に収まるが、控除 50 では賄えない → 例外
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(14, 0), time(21, 30), 50, List.of("A", "C", "D")));
            dto.setOfficeScheduleDetailDto(office(time(16, 40), time(18, 10)));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("授業業務の休憩時間が不足しています", ex.getMessage());
        }

        @Test
        @DisplayName("A・C・Dコマ間の空き時間にその他100分＋控除100分（バッファなし） → 通過")
        void freeSlotOtherOccupiesWithinBreak() {
            // その他 16:30-18:10（100分占有、休憩 0）＋控除 100 → ちょうどで通過
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(14, 0), time(21, 30), 100, List.of("A", "C", "D")));
            dto.setOtherScheduleDetailDto(other(time(16, 30), time(18, 10), 0, "打ち合わせ"));

            assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
        }

        @Test
        @DisplayName("A・C・Dコマ間の空き時間にその他実働110分だが控除100分 → 例外")
        void freeSlotOtherActualOccupationExceedsBreak() {
            // その他 16:40-18:30（110分占有、休憩 0） → 実働 110 > 控除 100 → 例外
            // ※空き帯 (16:20-18:10) から C コマ側へ少しはみ出しているが、C コマの開始は 18:10 のため
            //    isOverlapping は発火しないが (16:40 < 18:10 は勝つが 18:30 > 18:10 により空き想定を外せる形)
            //    → 実際には C コマ (18:10-19:40) との重複チェックで弾かれないよう、空きに収めるべき。
            //    より安全に：空き内に留めるよう 16:20-18:10（占有 110分に収まるよう調整）
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(14, 0), time(21, 30), 100, List.of("A", "C", "D")));
            dto.setOtherScheduleDetailDto(other(time(16, 20), time(18, 10), 0, "打ち合わせ"));

            // 占有 110分（その他実働 110−0=110）> 控除 100 → 例外
            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("授業業務の休憩時間が不足しています", ex.getMessage());
        }

        @Test
        @DisplayName("A・C・Dコマ間で事務＋その他の合計占有が控除を超過 → 例外")
        void freeSlotNetTotalAcrossDetails() {
            // 空き帯 16:20-18:10（110分）で 事務 16:20-17:00（40分）＋その他 17:10-18:10（60分、他休 0）
            // 合計 100分 > 控除 90 → 例外
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(14, 0), time(21, 30), 90, List.of("A", "C", "D")));
            dto.setOfficeScheduleDetailDto(office(time(16, 20), time(17, 0)));
            dto.setOtherScheduleDetailDto(other(time(17, 10), time(18, 10), 0, "打ち合わせ"));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("授業業務の休憩時間が不足しています", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("スケジュール重複チェック")
    class Overlap {

        @Test
        @DisplayName("事務が授業コマに重複 → 例外")
        void officeOverlapsPeriod() {
            ScheduleDto dto = emptySchedule();
            // 授業 S=13:10-14:40
            dto.setLessonScheduleDetailDto(lesson(time(13, 0), time(15, 0), 0, List.of("S")));
            // 事務 14:00-15:00 → Sの時間帯内
            dto.setOfficeScheduleDetailDto(office(time(14, 0), time(15, 0)));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("事務業務が授業コマの時間帯と重複しています", ex.getMessage());
        }

        @Test
        @DisplayName("その他が授業コマに重複 → 例外")
        void otherOverlapsPeriod() {
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(13, 0), time(15, 0), 0, List.of("S")));
            dto.setOtherScheduleDetailDto(other(time(13, 30), time(14, 30), 0, "x"));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("その他業務が授業コマの時間帯と重複しています", ex.getMessage());
        }

        @Test
        @DisplayName("事務とその他が重複（許容内包なし） → 例外")
        void officeAndOtherOverlap() {
            ScheduleDto dto = emptySchedule();
            dto.setOfficeScheduleDetailDto(office(time(10, 0), time(12, 0)));
            dto.setOtherScheduleDetailDto(other(time(11, 0), time(13, 0), 0, "x"));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("事務業務とその他業務の時間帯が重複しています", ex.getMessage());
        }

        @Test
        @DisplayName("その他が事務を内包し、その他休憩が事務実働以上 → 通過")
        void otherEnclosesOfficeWithEnoughBreak() {
            // その他 9:00-18:00（休憩 240分）で事務 10:00-12:00（120分）を内包
            // otherBreak(240) >= officeDuration(120) のため許容
            ScheduleDto dto = emptySchedule();
            dto.setOfficeScheduleDetailDto(office(time(10, 0), time(12, 0)));
            dto.setOtherScheduleDetailDto(other(time(9, 0), time(18, 0), 240, "x"));

            assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
        }

        @Test
        @DisplayName("その他が事務を内包するが休憩が不足 → 例外")
        void enclosesButInsufficientBreak() {
            // その他 9:00-18:00（休憩 100分）で事務 10:00-12:00（120分）を内包
            // otherBreak(100) < officeDuration(120) のため拒否
            ScheduleDto dto = emptySchedule();
            dto.setOfficeScheduleDetailDto(office(time(10, 0), time(12, 0)));
            dto.setOtherScheduleDetailDto(other(time(9, 0), time(18, 0), 100, "x"));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("事務業務とその他業務の時間帯が重複しています", ex.getMessage());
        }

        @Test
        @DisplayName("数分だけずれていればOK（重複なし）")
        void touchingButNotOverlapping() {
            // 事務と連続するがその他の開始 = 事務の終了
            ScheduleDto dto = emptySchedule();
            dto.setOfficeScheduleDetailDto(office(time(10, 0), time(11, 0)));
            dto.setOtherScheduleDetailDto(other(time(11, 0), time(12, 0), 0, "x"));

            assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
        }

        @Test
        @DisplayName("空きコマの時間帯に事務業務 → 通過（選択コマとの重複のみ検査）")
        void officeInFreePeriodSlot() {
            // 授業 A(14:50-16:20), C(18:10-19:40), D(19:50-21:20) を選択
            // 勤務 14:00-21:30（450分）、コマ3個（300分）→ 余裕150分、休憩150分
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(14, 0), time(21, 30), 150, List.of("A", "C", "D")));
            // 空きの B コマ時間帯 (16:30-18:00) に事務 → どの選択コマとも重ならない
            dto.setOfficeScheduleDetailDto(office(time(16, 30), time(18, 0)));

            assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
        }

        @Test
        @DisplayName("事務がその他を包含 → 例外（包含の向きが逆は救済されない）")
        void officeEnclosesOther() {
            // 事務 9:00-13:00（240分）がその他 10:00-12:00（120分）を完全に含む形。
            // outer は常にその他固定のため、事務側が大きくても救済条件（outerBreak >= 事務実働）を満たせない。
            // その他休憩120分はその他勤務120分とちょうど同値で「休憩超過」は発火せず、重複例外に到達する
            ScheduleDto dto = emptySchedule();
            dto.setOfficeScheduleDetailDto(office(time(9, 0), time(13, 0)));
            dto.setOtherScheduleDetailDto(other(time(10, 0), time(12, 0), 120, "x"));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("事務業務とその他業務の時間帯が重複しています", ex.getMessage());
        }

        @Test
        @DisplayName("その他が事務を包含し、休憩が事務実働とちょうど一致 → 通過（境界 >=）")
        void otherEnclosesOfficeBreakExactlyEnough() {
            // その他 9:00-18:00（休憩ちょうど120分）で事務 10:00-12:00（実働120分）を厳密包含
            ScheduleDto dto = emptySchedule();
            dto.setOfficeScheduleDetailDto(office(time(10, 0), time(12, 0)));
            dto.setOtherScheduleDetailDto(other(time(9, 0), time(18, 0), 120, "x"));

            assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
        }

        @Test
        @DisplayName("開始時刻が一致する包含 → 例外（厳密包含のみ許容）")
        void enclosingWithSameStartTime() {
            // その他も事務も 10:00 開始。outerStart < innerStart を満たさないため厳密包含不成立。
            // その他勤務 10:00-18:00 = 480分・休憩 480分 で「休憩超過」は発火せず重複例外に到達する
            ScheduleDto dto = emptySchedule();
            dto.setOfficeScheduleDetailDto(office(time(10, 0), time(12, 0)));
            dto.setOtherScheduleDetailDto(other(time(10, 0), time(18, 0), 480, "x"));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("事務業務とその他業務の時間帯が重複しています", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("複合正常ケース")
    class ValidComplex {

        @Test
        @DisplayName("何もない空スケジュール → 通過")
        void emptyPasses() {
            assertDoesNotThrow(() -> ScheduleValidator.checkInput(emptySchedule()));
        }

        @Test
        @DisplayName("授業3コマ＋事務（コマ外）＋その他（別時間帯） → 通過")
        void mixedValidDay() {
            // 授業 S(13:10)/A(14:50)/B(16:30) で13:00-18:30（330分、余裕30分、休憩30分）
            // 事務 20:00-21:00（コマ外）
            // その他 21:30-22:00
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(13, 0), time(18, 30), 30, List.of("S", "A", "B")));
            dto.setOfficeScheduleDetailDto(office(time(20, 0), time(21, 0)));
            dto.setOtherScheduleDetailDto(other(time(21, 30), time(22, 0), 0, "x"));

            assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
        }
    }

    @Nested
    @DisplayName("網羅度向上: 抜け分岐の追加検証")
    class ExtraCoverage {

        @Nested
        @DisplayName("授業 startTime/endTime あり・コマなし")
        class StartEndWithNoPeriods {

            @Test
            @DisplayName("授業に時刻だけ入力・コマなし → 例外（コマを1つ以上）")
            void lessonStartEndWithNoPeriods() {
                ScheduleDto dto = emptySchedule();
                dto.setLessonScheduleDetailDto(lesson(time(13, 0), time(18, 0), 0, List.of()));

                InvalidInputException ex = assertThrows(InvalidInputException.class,
                    () -> ScheduleValidator.checkInput(dto));
                assertEquals("コマを1つ以上選択してください", ex.getMessage());
            }
        }

        @Nested
        @DisplayName("コマコードの混在バリデーション")
        class InvalidCodeMix {

            @Test
            @DisplayName("有効・無効混在（[M, X]） → 例外")
            void invalidCodeMixedWithValid() {
                // 時間 (13:00-15:40 = 160min) - コマ時間 (2*100 = 200min) < 0 なので
                // break=0 でないと「休憩時間超過」が先に発火しないよう start/end を広げる
                ScheduleDto dto = emptySchedule();
                dto.setLessonScheduleDetailDto(lesson(time(10, 0), time(14, 0), 0, List.of("M", "X")));

                InvalidInputException ex = assertThrows(InvalidInputException.class,
                    () -> ScheduleValidator.checkInput(dto));
                assertEquals("不正なコマコードです", ex.getMessage());
            }
        }

        @Nested
        @DisplayName("休憩時間の境界ちょうど")
        class BreakBoundary {

            @Test
            @DisplayName("授業: 休憩時間が「勤務時間 - コマ時間」とちょうど等しい → 通過")
            void breakEqualsAvailable() {
                // 13:00-14:40 (100min) で 1コマ(100min) → availableBreak=0, break=0 → 通過
                ScheduleDto dto = emptySchedule();
                dto.setLessonScheduleDetailDto(lesson(time(13, 0), time(14, 40), 0, List.of("S")));

                assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
            }

            @Test
            @DisplayName("その他: 休憩時間が勤務時間とちょうど等しい → 通過（< のため）")
            void otherBreakEqualsDuration() {
                ScheduleDto dto = emptySchedule();
                dto.setOtherScheduleDetailDto(other(time(10, 0), time(12, 0), 120, "x"));

                assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
            }

            @Test
            @DisplayName("その他: 休憩時間が勤務時間未満 → 通過")
            void otherBreakLessThanDuration() {
                ScheduleDto dto = emptySchedule();
                dto.setOtherScheduleDetailDto(other(time(10, 0), time(12, 0), 119, "x"));

                assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
            }
        }

        @Nested
        @DisplayName("単一業務のみの構成")
        class SingleCategory {

            @Test
            @DisplayName("事務のみ → 通過")
            void officeOnlyWork() {
                ScheduleDto dto = emptySchedule();
                dto.setOfficeScheduleDetailDto(office(time(10, 0), time(12, 0)));

                assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
            }

            @Test
            @DisplayName("その他のみ → 通過")
            void otherOnlyWork() {
                ScheduleDto dto = emptySchedule();
                dto.setOtherScheduleDetailDto(other(time(10, 0), time(12, 0), 30, "作業"));

                assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
            }
        }
    }

    @Nested
    @DisplayName("授業 3 コマ以上: 不足している不足分岐と境界ちょうど")
    class ThreePeriodBoundary {

        @Test
        @DisplayName("3コマ以上で start があり end がない → 例外（既存: end のみ欠落/両方欠落の組合せの補完）")
        void threePeriodsStartOnlyNullEnd() {
            // 既存の threePeriodsNoEndTime は「null + end なし」(=start も null)。
            // ここでは start のみがあるケースを固定する
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(13, 0), null, 0, List.of("S", "A", "B")));

            InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> ScheduleValidator.checkInput(dto));
            assertEquals("3コマ以上の場合、開始・終了時刻を入力してください", ex.getMessage());
        }

        @Test
        @DisplayName("3コマ + 勤務時間ちょうど = コマ時間合計 (利用可能休憩 == 0) + 休憩 0 → 通過")
        void breakZeroWhenAllTimeIsConsumedByPeriods() {
            // 13:00-18:00 (300分) で S・A・B = ちょうど 300分 → availableBreak = 0
            // lessonBreak = 0 → 0 == 0 で OK。3 コマ以上での「equals boundary」の固定
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(13, 0), time(18, 0), 0, List.of("S", "A", "B")));

            assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
        }

        @Test
        @DisplayName("3コマ + 勤務時間 = コマ時間合計 + 休憩ちょうど → 通過（availableBreak == lessonBreak）")
        void breakExactlyEqualsGap() {
            // 13:00-18:30 (330分)、3 コマ = 300分、余裕 30分、休憩 30分 → ちょうど
            // 0 コマ・1 コマの equals ケース（BreakBoundary.breakEqualsAvailable）とは別に、
            // 「3コマ以上で validateLessonTimes=true の経路で equals が通る」ことを固定する
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(13, 0), time(18, 30), 30, List.of("S", "A", "B")));

            assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
        }

        @Test
        @DisplayName("最初のコマ開始が授業開始時刻とちょうど一致 → 通過（範囲外にならない）")
        void firstPeriodStartsExactlyAtWorkStart() {
            // M = 9:10-10:40, K = 10:50-12:20, S = 13:10-14:40
            // lessonStart = 9:10（ちょうど M.start と同じ）, lessonEnd = 16:00
            // 勤務時間 = 410min, コマ合計 = 300min, availableBreak = 110min, lessonBreak = 10 → OK
            // L107 `firstPeriodStart.isBefore(lessonStart)` が false となる境界（ちょうど）は通過すべき
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(9, 10), time(16, 0), 10, List.of("M", "K", "S")));

            assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
        }

        @Test
        @DisplayName("最後のコマ終了が授業終了時刻とちょうど一致 → 通過（isAfter が false）")
        void lastPeriodEndsExactlyAtWorkEnd() {
            // S = 13:10-14:40, A = 14:50-16:20, B = 16:30-18:00
            // lessonStart = 13:00, lessonEnd = 18:00（B.end とちょうど同じ）
            // 勤務時間 = 300min, コマ合計 = 300min, availableBreak = 0, lessonBreak = 0 → OK
            // L107 `lastPeriodEnd.isAfter(lessonEnd)` が false となる境界（ちょうど）は通過すべき
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(time(13, 0), time(18, 0), 0, List.of("S", "A", "B")));

            assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
        }

        @Test
        @DisplayName("0コマ + 時刻なし + 休憩のみ → 通過（validateLessonTimes=false の経路に載る）")
        void zeroPeriodsNoTimesBreakOnlyPassesThrough() {
            // L78-79: startTime==null のため validateLessonTimes が false となり、
            // 「0コマで時刻のみ」した「コマを1つ以上選択してください」の例外経路 (L100-101) にも
            // 到達しないことを固定する
            ScheduleDto dto = emptySchedule();
            dto.setLessonScheduleDetailDto(lesson(null, null, 30, List.of()));

            assertDoesNotThrow(() -> ScheduleValidator.checkInput(dto));
        }
    }
}
