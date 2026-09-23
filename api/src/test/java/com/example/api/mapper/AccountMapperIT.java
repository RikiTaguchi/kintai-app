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

@MybatisTest
@ActiveProfiles("test")
class AccountMapperIT extends AbstractPostgresIT {

    @Autowired private AccountMapper accountMapper;

    private AccountEntity entity(String loginId) {
        AccountEntity e = new AccountEntity();
        e.setLoginId(loginId);
        e.setPassword("hashed");
        return e;
    }

    @Test
    @DisplayName("insert → select で同じエンティティが返る")
    void insertAndSelect() {
        AccountEntity e = entity("login_" + UUID.randomUUID());
        accountMapper.insert(e);

        AccountEntity got = accountMapper.select(e.getId()).orElseThrow();
        assertEquals(e.getLoginId(), got.getLoginId());
    }

    @Test
    @DisplayName("selectByLoginId: 登録した loginId で検索できる")
    void selectByLoginId() {
        AccountEntity e = entity("login_" + UUID.randomUUID());
        accountMapper.insert(e);

        AccountEntity got = accountMapper.selectByLoginId(e.getLoginId()).orElseThrow();
        assertEquals(e.getId(), got.getId());
    }

    @Test
    @DisplayName("selectByLoginId: 存在しない → Optional.empty")
    void selectByLoginIdEmpty() {
        assertTrue(accountMapper.selectByLoginId("not_exists_" + UUID.randomUUID()).isEmpty());
    }

    @Test
    @DisplayName("update で login_id を変更できる")
    void update() {
        AccountEntity e = entity("login_" + UUID.randomUUID());
        accountMapper.insert(e);

        e.setLoginId("updated_" + UUID.randomUUID());
        e.setPassword("newhashed");
        accountMapper.update(e);

        AccountEntity got = accountMapper.select(e.getId()).orElseThrow();
        assertEquals(e.getLoginId(), got.getLoginId());
        assertEquals("newhashed", got.getPassword());
    }

    @Test
    @DisplayName("delete で参照できなくなる")
    void delete() {
        AccountEntity e = entity("login_" + UUID.randomUUID());
        accountMapper.insert(e);
        assertTrue(accountMapper.select(e.getId()).isPresent());

        accountMapper.delete(e.getId());
        assertTrue(accountMapper.select(e.getId()).isEmpty());
    }
}
