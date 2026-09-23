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
import com.example.api.mapper.entity.work.OfficeWorkDetailEntity;
import com.example.api.mapper.entity.work.WorkEntity;

@MybatisTest
@ActiveProfiles("test")
class OfficeWorkDetailMapperIT extends AbstractPostgresIT {

    @Autowired private AccountMapper accountMapper;
    @Autowired private TutorMapper tutorMapper;
    @Autowired private WorkMapper workMapper;
    @Autowired private OfficeWorkDetailMapper officeWorkDetailMapper;

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
    @DisplayName("insert → update で開始/終了時刻を更新できる")
    void insertAndUpdate() {
        OfficeWorkDetailEntity e = new OfficeWorkDetailEntity();
        e.setWorkId(workId);
        e.setStartTime(LocalTime.of(9, 0));
        e.setEndTime(LocalTime.of(11, 0));
        officeWorkDetailMapper.insert(e);

        OfficeWorkDetailEntity loaded = workMapper.select(workId).orElseThrow()
            .getOfficeWorkDetailEntity();
        loaded.setStartTime(LocalTime.of(8, 30));
        loaded.setEndTime(LocalTime.of(10, 0));
        officeWorkDetailMapper.update(loaded);

        OfficeWorkDetailEntity updated = workMapper.select(workId).orElseThrow()
            .getOfficeWorkDetailEntity();
        assertEquals(LocalTime.of(8, 30), updated.getStartTime());
        assertEquals(LocalTime.of(10, 0), updated.getEndTime());
    }
}
