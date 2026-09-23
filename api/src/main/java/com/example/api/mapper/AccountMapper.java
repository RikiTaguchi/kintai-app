package com.example.api.mapper;

import java.util.Optional;
import java.util.UUID;

import org.apache.ibatis.annotations.Mapper;

import com.example.api.mapper.entity.AccountEntity;

@Mapper
public interface AccountMapper {

    Optional<AccountEntity> selectByLoginId(String loginId);

    Optional<AccountEntity> select(UUID accountId);

    void insert(AccountEntity entity);

    void update(AccountEntity entity);

    void delete(UUID accountId);

}
