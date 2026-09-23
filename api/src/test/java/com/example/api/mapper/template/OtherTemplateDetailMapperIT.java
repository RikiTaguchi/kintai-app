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
import com.example.api.mapper.entity.template.OtherTemplateDetailEntity;
import com.example.api.mapper.entity.template.TemplateEntity;

@MybatisTest
@ActiveProfiles("test")
class OtherTemplateDetailMapperIT extends AbstractPostgresIT {

    @Autowired private AccountMapper accountMapper;
    @Autowired private TutorMapper tutorMapper;
    @Autowired private TemplateMapper templateMapper;
    @Autowired private OtherTemplateDetailMapper otherTemplateDetailMapper;

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
    @DisplayName("insert → update で description / break を更新できる")
    void insertAndUpdate() {
        OtherTemplateDetailEntity e = new OtherTemplateDetailEntity();
        e.setTemplateId(templateId);
        e.setStartTime(LocalTime.of(13, 0));
        e.setEndTime(LocalTime.of(14, 0));
        e.setBreakMinutes(10);
        e.setDescription("補習準備");
        otherTemplateDetailMapper.insert(e);

        OtherTemplateDetailEntity loaded = templateMapper.select(templateId).orElseThrow()
            .getOtherTemplateDetailEntity();
        loaded.setBreakMinutes(20);
        loaded.setDescription("自習室");
        otherTemplateDetailMapper.update(loaded);

        OtherTemplateDetailEntity updated = templateMapper.select(templateId).orElseThrow()
            .getOtherTemplateDetailEntity();
        assertEquals(20, updated.getBreakMinutes());
        assertEquals("自習室", updated.getDescription());
    }
}
