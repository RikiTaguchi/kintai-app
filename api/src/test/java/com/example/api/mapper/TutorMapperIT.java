package com.example.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import com.example.api.AbstractPostgresIT;
import com.example.api.mapper.entity.AccountEntity;
import com.example.api.mapper.entity.TutorEntity;

@MybatisTest
@ActiveProfiles("test")
class TutorMapperIT extends AbstractPostgresIT {

    @Autowired private AccountMapper accountMapper;
    @Autowired private TutorMapper tutorMapper;

    private TutorEntity seedTutor() {
        String loginId = "tutor_" + UUID.randomUUID();
        AccountEntity a = MapperTestFixtures.account(
            loginId, MapperTestFixtures.CLASSROOM_TOZUKA, null, UUID.randomUUID());
        a.setTutorId(null);
        accountMapper.insert(a);

        TutorEntity t = MapperTestFixtures.tutor(a.getId(), MapperTestFixtures.CLASSROOM_TOZUKA, loginId);
        tutorMapper.insert(t);
        return t;
    }

    @Test
    @DisplayName("selectAll: 教室に紐づく講師を返す（他教室は含まない）")
    void selectAll() {
        TutorEntity t1 = seedTutor();
        List<TutorEntity> list = tutorMapper.selectAll(MapperTestFixtures.CLASSROOM_TOZUKA);
        assertTrue(list.stream().anyMatch(t -> t.getId().equals(t1.getId())));
        assertTrue(list.stream().allMatch(t -> t.getClassroomId().equals(MapperTestFixtures.CLASSROOM_TOZUKA)));
    }

    @Test
    @DisplayName("select: 教室名が join で付与される")
    void select() {
        TutorEntity t = seedTutor();
        TutorEntity got = tutorMapper.select(t.getId()).orElseThrow();
        assertEquals("戸塚", got.getClassroomName());
        assertEquals(22, got.getClassroomNumber());
    }

    @Test
    @DisplayName("update: terminated と terminationDate を永続化できる")
    void update() {
        TutorEntity t = seedTutor();
        t.setTerminated(true);
        t.setTerminationDate(LocalDate.of(2025, 3, 31));
        t.setFirstName("次");
        tutorMapper.update(t);

        TutorEntity got = tutorMapper.select(t.getId()).orElseThrow();
        assertEquals(true, got.getTerminated());
        assertEquals(LocalDate.of(2025, 3, 31), got.getTerminationDate());
        assertEquals("次", got.getFirstName());
    }

    @Test
    @DisplayName("select: 存在しない → Optional.empty")
    void selectMissing() {
        assertTrue(tutorMapper.select(UUID.randomUUID()).isEmpty());
    }
}
