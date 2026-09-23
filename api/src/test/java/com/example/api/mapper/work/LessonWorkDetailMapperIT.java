package com.example.api.mapper.work;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;
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
import com.example.api.mapper.entity.work.LessonWorkDetailEntity;
import com.example.api.mapper.entity.work.WorkEntity;

@MybatisTest
@ActiveProfiles("test")
class LessonWorkDetailMapperIT extends AbstractPostgresIT {

    @Autowired private AccountMapper accountMapper;
    @Autowired private TutorMapper tutorMapper;
    @Autowired private WorkMapper workMapper;
    @Autowired private LessonWorkDetailMapper lessonWorkDetailMapper;

    private UUID workId;

    @BeforeEach
    void seed() {
        String loginId = "tutor_" + UUID.randomUUID();
        AccountEntity a = MapperTestFixtures.account(loginId, MapperTestFixtures.CLASSROOM_TOZUKA, null, null);
        accountMapper.insert(a);
        TutorEntity t = MapperTestFixtures.tutor(a.getId(), MapperTestFixtures.CLASSROOM_TOZUKA, loginId);
        tutorMapper.insert(t);

        WorkEntity w = MapperTestFixtures.work(t.getId(), MapperTestFixtures.CLASSROOM_TOZUKA, LocalDate.of(2025, 9, 10));
        workMapper.insert(w);
        workId = w.getId();
    }

    @Test
    @DisplayName("insert → update で period フラグ/休憩を更新できる")
    void insertAndUpdate() {
        LessonWorkDetailEntity e = new LessonWorkDetailEntity();
        e.setWorkId(workId);
        e.setStartTime(LocalTime.of(10, 0));
        e.setEndTime(LocalTime.of(12, 0));
        e.setBreakMinutes(30);
        e.setPeriodCodeM(true);
        e.setPeriodCodeK(true);
        e.setPeriodCodeS(true);
        e.setPeriodCodeA(false);
        e.setPeriodCodeB(false);
        e.setPeriodCodeC(false);
        e.setPeriodCodeD(false);
        lessonWorkDetailMapper.insert(e);

        LessonWorkDetailEntity loaded = workMapper.select(workId).orElseThrow()
            .getLessonWorkDetailEntity();
        loaded.setPeriodCodeM(false);
        loaded.setPeriodCodeD(true);
        loaded.setBreakMinutes(45);
        lessonWorkDetailMapper.update(loaded);

        LessonWorkDetailEntity updated = workMapper.select(workId).orElseThrow()
            .getLessonWorkDetailEntity();
        assertEquals(Boolean.FALSE, updated.getPeriodCodeM());
        assertEquals(Boolean.TRUE, updated.getPeriodCodeD());
        assertEquals(45, updated.getBreakMinutes());
        assertEquals(LocalTime.of(10, 0), updated.getStartTime());
    }
}
