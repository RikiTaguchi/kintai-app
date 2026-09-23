package com.example.api.mapper;

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
import com.example.api.mapper.entity.AccountEntity;
import com.example.api.mapper.entity.SalaryEntity;
import com.example.api.mapper.entity.TutorEntity;

@MybatisTest
@ActiveProfiles("test")
class SalaryMapperIT extends AbstractPostgresIT {

    @Autowired private AccountMapper accountMapper;
    @Autowired private TutorMapper tutorMapper;
    @Autowired private SalaryMapper salaryMapper;

    private UUID tutorId;

    @BeforeEach
    void seed() {
        String loginId = "tutor_" + UUID.randomUUID();
        AccountEntity a = MapperTestFixtures.account(
            loginId, MapperTestFixtures.CLASSROOM_TOZUKA, null, null);
        accountMapper.insert(a);

        TutorEntity t = MapperTestFixtures.tutor(a.getId(), MapperTestFixtures.CLASSROOM_TOZUKA, loginId);
        tutorMapper.insert(t);
        tutorId = t.getId();
    }

    private SalaryEntity salary(LocalDate effectiveDate, Integer lessonWage) {
        SalaryEntity e = new SalaryEntity();
        e.setTutorId(tutorId);
        e.setEffectiveDate(effectiveDate);
        e.setLessonWage(lessonWage);
        e.setOfficeWage(2000);
        e.setTransportationFee(100);
        return e;
    }

    @Test
    @DisplayName("selectAll: effective_date DESC でソートされた状態で返す")
    void selectAllOrderByDateDesc() {
        // Mapper XML に ORDER BY effective_date DESC を明示しているため、
        // 挿入順と関係なく常に日付降順で返却されることを検証する。
        salaryMapper.insert(salary(LocalDate.of(2025, 4, 1), 3000));
        salaryMapper.insert(salary(LocalDate.of(2024, 4, 1), 2900));
        salaryMapper.insert(salary(LocalDate.of(2025, 10, 1), 3100));

        List<SalaryEntity> list = salaryMapper.selectAll(tutorId);
        assertEquals(3, list.size());
        assertEquals(LocalDate.of(2025, 10, 1), list.get(0).getEffectiveDate());
        assertEquals(LocalDate.of(2025, 4, 1), list.get(1).getEffectiveDate());
        assertEquals(LocalDate.of(2024, 4, 1), list.get(2).getEffectiveDate());
    }

    @Test
    @DisplayName("selectEarliestEffectiveDate: 最古の適用開始日を返す")
    void selectEarliest() {
        salaryMapper.insert(salary(LocalDate.of(2025, 10, 1), 3100));
        salaryMapper.insert(salary(LocalDate.of(2024, 4, 1), 2900));

        LocalDate earliest = salaryMapper.selectEarliestEffectiveDate(tutorId).orElseThrow();
        assertEquals(LocalDate.of(2024, 4, 1), earliest);
    }

    @Test
    @DisplayName("existsByTutorIdAndEffectiveDate: 指定日が存在する講師で true")
    void existsByTutorAndDate() {
        SalaryEntity inserted = salary(LocalDate.of(2025, 4, 1), 3000);
        salaryMapper.insert(inserted);

        assertTrue(salaryMapper.existsByTutorIdAndEffectiveDate(
            tutorId, LocalDate.of(2025, 4, 1), null));
        assertFalse(salaryMapper.existsByTutorIdAndEffectiveDate(
            tutorId, LocalDate.of(2025, 4, 2), null));
        // excludeId を指定すると同じ行は除外され false になる
        assertFalse(salaryMapper.existsByTutorIdAndEffectiveDate(
            tutorId, LocalDate.of(2025, 4, 1), inserted.getId()));
    }

    @Test
    @DisplayName("select: 挿入した給与の金額が取れる")
    void select() {
        SalaryEntity e = salary(LocalDate.of(2025, 4, 1), 3000);
        salaryMapper.insert(e);

        SalaryEntity got = salaryMapper.select(e.getId()).orElseThrow();
        assertEquals(3000, got.getLessonWage());
        assertEquals(2000, got.getOfficeWage());
        assertEquals(100, got.getTransportationFee());
    }

    @Test
    @DisplayName("update: 給与額を更新できる")
    void update() {
        SalaryEntity e = salary(LocalDate.of(2025, 4, 1), 3000);
        salaryMapper.insert(e);

        e.setLessonWage(3200);
        e.setEffectiveDate(LocalDate.of(2025, 5, 1));
        salaryMapper.update(e);

        SalaryEntity got = salaryMapper.select(e.getId()).orElseThrow();
        assertEquals(3200, got.getLessonWage());
        assertEquals(LocalDate.of(2025, 5, 1), got.getEffectiveDate());
    }

    @Test
    @DisplayName("delete: 削除すると取得できなくなる")
    void delete() {
        SalaryEntity e = salary(LocalDate.of(2025, 4, 1), 3000);
        salaryMapper.insert(e);
        assertTrue(salaryMapper.select(e.getId()).isPresent());

        salaryMapper.delete(e.getId());
        assertTrue(salaryMapper.select(e.getId()).isEmpty());
    }
}
