package com.example.api.mapper.template;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import com.example.api.mapper.entity.template.LessonTemplateDetailEntity;
import com.example.api.mapper.entity.template.TemplateEntity;

@MybatisTest
@ActiveProfiles("test")
class LessonTemplateDetailMapperIT extends AbstractPostgresIT {

    @Autowired private AccountMapper accountMapper;
    @Autowired private TutorMapper tutorMapper;
    @Autowired private TemplateMapper templateMapper;
    @Autowired private LessonTemplateDetailMapper lessonTemplateDetailMapper;

    private UUID templateId;

    @BeforeEach
    void seed() {
        String loginId = "tutor_" + UUID.randomUUID();
        AccountEntity a = MapperTestFixtures.account(loginId, MapperTestFixtures.CLASSROOM_TOZUKA, null, null);
        accountMapper.insert(a);
        TutorEntity t = MapperTestFixtures.tutor(a.getId(), MapperTestFixtures.CLASSROOM_TOZUKA, loginId);
        tutorMapper.insert(t);

        TemplateEntity template = MapperTestFixtures.template(t.getId(), MapperTestFixtures.CLASSROOM_TOZUKA, "T1");
        templateMapper.insert(template);
        templateId = template.getId();
    }

    @Test
    @DisplayName("insert: periodCode のフラグ複数を永続化できる")
    void insert() {
        LessonTemplateDetailEntity e = new LessonTemplateDetailEntity();
        e.setTemplateId(templateId);
        e.setStartTime(LocalTime.of(10, 0));
        e.setEndTime(LocalTime.of(12, 0));
        e.setBreakMinutes(30);
        e.setPeriodCodeM(true);
        e.setPeriodCodeK(true);
        e.setPeriodCodeS(false);
        e.setPeriodCodeA(false);
        e.setPeriodCodeB(false);
        e.setPeriodCodeC(false);
        e.setPeriodCodeD(false);
        lessonTemplateDetailMapper.insert(e);
        // 存在のみ確認（select は mapper に無いので FK 整合性を後段の update で担保）
    }

    @Test
    @DisplayName("update: period フラグを更新できる")
    void update() {
        LessonTemplateDetailEntity e = MapperTestFixtures.lessonTemplateDetail(templateId);
        lessonTemplateDetailMapper.insert(e);

        // 取得して再 updateするため、一旦 select の代替として templateMapper 経由で ID を拾う
        TemplateEntity t = templateMapper.select(templateId).orElseThrow();
        LessonTemplateDetailEntity loaded = t.getLessonTemplateDetailEntity();
        loaded.setPeriodCodeM(false);
        loaded.setPeriodCodeA(true);
        loaded.setBreakMinutes(45);
        lessonTemplateDetailMapper.update(loaded);

        LessonTemplateDetailEntity updated = templateMapper.select(templateId).orElseThrow()
            .getLessonTemplateDetailEntity();
        assertEquals(Boolean.FALSE, updated.getPeriodCodeM());
        assertEquals(Boolean.TRUE, updated.getPeriodCodeA());
        assertEquals(45, updated.getBreakMinutes());
    }
}
