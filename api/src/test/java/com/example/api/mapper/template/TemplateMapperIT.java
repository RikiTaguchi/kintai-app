package com.example.api.mapper.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.example.api.mapper.entity.template.LessonTemplateDetailEntity;
import com.example.api.mapper.entity.template.OfficeTemplateDetailEntity;
import com.example.api.mapper.entity.template.OtherTemplateDetailEntity;
import com.example.api.mapper.entity.template.TemplateEntity;

@MybatisTest
@ActiveProfiles("test")
class TemplateMapperIT extends AbstractPostgresIT {

    @Autowired private AccountMapper accountMapper;
    @Autowired private TutorMapper tutorMapper;
    @Autowired private TemplateMapper templateMapper;
    @Autowired private LessonTemplateDetailMapper lessonTemplateDetailMapper;
    @Autowired private OfficeTemplateDetailMapper officeTemplateDetailMapper;
    @Autowired private OtherTemplateDetailMapper otherTemplateDetailMapper;

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

    private TemplateEntity template(String title) {
        TemplateEntity e = MapperTestFixtures.template(tutorId, classroomId, title);
        templateMapper.insert(e);
        lessonTemplateDetailMapper.insert(MapperTestFixtures.lessonTemplateDetail(e.getId()));
        officeTemplateDetailMapper.insert(MapperTestFixtures.officeTemplateDetail(e.getId()));
        otherTemplateDetailMapper.insert(MapperTestFixtures.otherTemplateDetail(e.getId()));
        return e;
    }

    @Test
    @DisplayName("selectAll: 講師IDで抽出、教室名が join で付与される")
    void selectAll() {
        TemplateEntity t1 = template("朝テンプレ");
        TemplateEntity t2 = template("夜テンプレ");

        List<TemplateEntity> list = templateMapper.selectAll(tutorId);
        assertTrue(list.stream().anyMatch(x -> x.getId().equals(t1.getId())));
        assertTrue(list.stream().anyMatch(x -> x.getId().equals(t2.getId())));
        assertTrue(list.stream().allMatch(x -> "戸塚".equals(x.getClassroomName())));
    }

    @Test
    @DisplayName("select: 子detail も join で取得される")
    void select() {
        TemplateEntity t = template("朝テンプレ");
        TemplateEntity got = templateMapper.select(t.getId()).orElseThrow();

        assertEquals("朝テンプレ", got.getTitle());
        assertEquals("戸塚", got.getClassroomName());
        assertEquals(22, got.getClassroomNumber());
        // lesson detail 同梱（XML で join されている前提）
        assertTrue(got.getLessonTemplateDetailEntity() != null);
        assertEquals(Boolean.TRUE, got.getLessonTemplateDetailEntity().getPeriodCodeM());
    }

    @Test
    @DisplayName("update: title と transportationFee を更新できる")
    void update() {
        TemplateEntity t = template("改題前");
        t.setTitle("改題後");
        t.setTransportationFee(200);
        templateMapper.update(t);

        TemplateEntity got = templateMapper.select(t.getId()).orElseThrow();
        assertEquals("改題後", got.getTitle());
        assertEquals(200, got.getTransportationFee());
    }

    @Test
    @DisplayName("delete: カスケードで detail ごと削除される（取得できなくなる）")
    void delete() {
        TemplateEntity t = template("消すテンプレ");
        assertTrue(templateMapper.select(t.getId()).isPresent());

        templateMapper.delete(t.getId());
        assertTrue(templateMapper.select(t.getId()).isEmpty());
    }

    @Test
    @DisplayName("select: 存在しない ID → Optional.empty")
    void selectMissing() {
        assertTrue(templateMapper.select(UUID.randomUUID()).isEmpty());
    }
}
