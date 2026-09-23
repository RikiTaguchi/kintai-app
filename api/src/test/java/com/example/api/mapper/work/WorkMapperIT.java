package com.example.api.mapper.work;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import com.example.api.AbstractPostgresIT;
import com.example.api.mapper.AccountMapper;
import com.example.api.mapper.MapperTestFixtures;
import com.example.api.mapper.TutorMapper;
import com.example.api.mapper.entity.AccountEntity;
import com.example.api.mapper.entity.TutorEntity;
import com.example.api.mapper.entity.work.WorkEntity;

@MybatisTest
@ActiveProfiles("test")
class WorkMapperIT extends AbstractPostgresIT {

    @Autowired private AccountMapper accountMapper;
    @Autowired private TutorMapper tutorMapper;
    @Autowired private WorkMapper workMapper;
    @Autowired private LessonWorkDetailMapper lessonWorkDetailMapper;
    @Autowired private OfficeWorkDetailMapper officeWorkDetailMapper;
    @Autowired private OtherWorkDetailMapper otherWorkDetailMapper;

    private UUID tutorId;
    private UUID classroomId;

    @BeforeEach
    void seed() {
        classroomId = MapperTestFixtures.CLASSROOM_TOZUKA;
        String loginId = "tutor_" + UUID.randomUUID();
        AccountEntity a = MapperTestFixtures.account(loginId, classroomId, null, null);
        accountMapper.insert(a);
        TutorEntity t = MapperTestFixtures.tutor(a.getId(), classroomId, loginId);
        tutorMapper.insert(t);
        tutorId = t.getId();
    }

    private WorkEntity seedWork(LocalDate date) {
        WorkEntity e = MapperTestFixtures.work(tutorId, classroomId, date);
        workMapper.insert(e);
        lessonWorkDetailMapper.insert(MapperTestFixtures.lessonWorkDetail(e.getId()));
        officeWorkDetailMapper.insert(MapperTestFixtures.officeWorkDetail(e.getId()));
        otherWorkDetailMapper.insert(MapperTestFixtures.otherWorkDetail(e.getId()));
        return e;
    }

    @Test
    @DisplayName("selectAll: 指定期間内の勤務を勤務日昇順で返す（期間外は含まない）")
    void selectAllByDateRange() {
        // 月次期間: 9/26 - 10/25（9月の集計サイクル）
        seedWork(LocalDate.of(2025, 9, 26)); // from 当日（含む）
        seedWork(LocalDate.of(2025, 10, 15));
        seedWork(LocalDate.of(2025, 10, 25)); // to 当日（含む）
        seedWork(LocalDate.of(2025, 9, 25)); // 期間外（含まない）
        seedWork(LocalDate.of(2025, 10, 26)); // 期間外（含まない）

        List<WorkEntity> list = workMapper.selectAll(
            tutorId, LocalDate.of(2025, 9, 26), LocalDate.of(2025, 10, 25));
        assertEquals(3, list.size());
        assertEquals(LocalDate.of(2025, 9, 26), list.get(0).getWorkingDate());
        assertEquals(LocalDate.of(2025, 10, 15), list.get(1).getWorkingDate());
        assertEquals(LocalDate.of(2025, 10, 25), list.get(2).getWorkingDate());
    }

    @Test
    @DisplayName("select: 教室名と子detailが join される")
    void select() {
        WorkEntity e = seedWork(LocalDate.of(2025, 9, 10));
        WorkEntity got = workMapper.select(e.getId()).orElseThrow();

        assertEquals("戸塚", got.getClassroomName());
        assertEquals(22, got.getClassroomNumber());
        assertTrue(got.getLessonWorkDetailEntity() != null);
        assertTrue(got.getOfficeWorkDetailEntity() != null);
        assertTrue(got.getOtherWorkDetailEntity() != null);
    }

    @Test
    @DisplayName("existsByTutorIdAndWorkingDate: 同日登録は true、別日は false、excludeId は無視")
    void exists() {
        WorkEntity e = seedWork(LocalDate.of(2025, 9, 10));

        assertTrue(workMapper.existsByTutorIdAndWorkingDate(
            tutorId, LocalDate.of(2025, 9, 10), null));
        assertFalse(workMapper.existsByTutorIdAndWorkingDate(
            tutorId, LocalDate.of(2025, 9, 11), null));
        assertFalse(workMapper.existsByTutorIdAndWorkingDate(
            tutorId, LocalDate.of(2025, 9, 10), e.getId()));
    }

    @Test
    @DisplayName("update: 交通費/勤務日を更新できる")
    void update() {
        WorkEntity e = seedWork(LocalDate.of(2025, 9, 10));
        e.setTransportationFee(200);
        e.setWorkingDate(LocalDate.of(2025, 9, 11));
        e.setClassroomId(MapperTestFixtures.CLASSROOM_TOZUKA_EAST);
        workMapper.update(e);

        WorkEntity got = workMapper.select(e.getId()).orElseThrow();
        assertEquals(200, got.getTransportationFee());
        assertEquals(LocalDate.of(2025, 9, 11), got.getWorkingDate());
        assertEquals("東戸塚", got.getClassroomName());
    }

    @Test
    @DisplayName("delete: カスケードで detail ごと削除される")
    void delete() {
        WorkEntity e = seedWork(LocalDate.of(2025, 9, 10));
        assertTrue(workMapper.select(e.getId()).isPresent());

        workMapper.delete(e.getId());
        assertTrue(workMapper.select(e.getId()).isEmpty());
    }
}
