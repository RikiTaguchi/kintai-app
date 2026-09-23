package com.example.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import com.example.api.AbstractPostgresIT;
import com.example.api.mapper.entity.AccountEntity;
import com.example.api.mapper.entity.ManagerEntity;

@MybatisTest
@ActiveProfiles("test")
class ManagerMapperIT extends AbstractPostgresIT {

    @Autowired private AccountMapper accountMapper;
    @Autowired private ManagerMapper managerMapper;

    private ManagerEntity seedManager() {
        String loginId = "manager_" + UUID.randomUUID();
        AccountEntity a = MapperTestFixtures.account(
            loginId, MapperTestFixtures.CLASSROOM_TOZUKA, UUID.randomUUID(), null);
        a.setManagerId(null); // account 自体に managerId は無い
        accountMapper.insert(a);

        ManagerEntity m = MapperTestFixtures.manager(a.getId(), MapperTestFixtures.CLASSROOM_TOZUKA, loginId);
        managerMapper.insert(m);
        return m;
    }

    @Test
    @DisplayName("insert → select で教室名が join で付与される")
    void insertAndSelect() {
        ManagerEntity m = seedManager();
        ManagerEntity got = managerMapper.select(m.getId()).orElseThrow();
        assertEquals(m.getAccountId(), got.getAccountId());
        assertEquals("戸塚", got.getClassroomName());
        assertEquals(22, got.getClassroomNumber());
        assertEquals("太郎", got.getFirstName());
    }

    @Test
    @DisplayName("select: 存在しない → Optional.empty")
    void selectMissing() {
        assertTrue(managerMapper.select(UUID.randomUUID()).isEmpty());
    }

    @Test
    @DisplayName("update で firstName/lastName/教室を更新できる")
    void update() {
        ManagerEntity m = seedManager();
        m.setFirstName("次郎");
        m.setLastName("鈴木");
        m.setClassroomId(MapperTestFixtures.CLASSROOM_TOZUKA_EAST);
        managerMapper.update(m);

        ManagerEntity got = managerMapper.select(m.getId()).orElseThrow();
        assertEquals("次郎", got.getFirstName());
        assertEquals("鈴木", got.getLastName());
        assertEquals("東戸塚", got.getClassroomName());
    }
}
