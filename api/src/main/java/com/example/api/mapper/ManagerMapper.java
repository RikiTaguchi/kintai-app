package com.example.api.mapper;

import java.util.Optional;
import java.util.UUID;

import org.apache.ibatis.annotations.Mapper;

import com.example.api.mapper.entity.ManagerEntity;

@Mapper
public interface ManagerMapper {
    
    Optional<ManagerEntity> select(UUID managerId);

    void insert(ManagerEntity entity);

    void update(ManagerEntity entity);

}
