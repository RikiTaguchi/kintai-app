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
import com.example.api.mapper.entity.template.OfficeTemplateDetailEntity;
import com.example.api.mapper.entity.template.TemplateEntity;

@MybatisTest
@ActiveProfiles("test")
class OfficeTemplateDetailMapperIT extends AbstractPostgresIT {

    @Autowired private AccountMapper accountMapper;
    @Autowired private TutorMapper tutorMapper;
    @Autowired private TemplateMapper templateMapper;
    @Autowired private OfficeTemplateDetailMapper officeTemplateDetailMapper;

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
    @DisplayName("insert → update で開始/終了時刻を更新できる")
    void insertAndUpdate() {
        OfficeTemplateDetailEntity e = new OfficeTemplateDetailEntity();
        e.setTemplateId(templateId);
        e.setStartTime(LocalTime.of(9, 0));
        e.setEndTime(LocalTime.of(11, 0));
        officeTemplateDetailMapper.insert(e);

        OfficeTemplateDetailEntity loaded = templateMapper.select(templateId).orElseThrow()
            .getOfficeTemplateDetailEntity();
        loaded.setStartTime(LocalTime.of(8, 30));
        loaded.setEndTime(LocalTime.of(10, 30));
        officeTemplateDetailMapper.update(loaded);

        OfficeTemplateDetailEntity updated = templateMapper.select(templateId).orElseThrow()
            .getOfficeTemplateDetailEntity();
        assertEquals(LocalTime.of(8, 30), updated.getStartTime());
        assertEquals(LocalTime.of(10, 30), updated.getEndTime());
    }
}
