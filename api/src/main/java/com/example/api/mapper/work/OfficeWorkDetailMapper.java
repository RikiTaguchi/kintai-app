package com.example.api.mapper.work;

import org.apache.ibatis.annotations.Mapper;

import com.example.api.mapper.entity.work.OfficeWorkDetailEntity;

@Mapper
public interface OfficeWorkDetailMapper {

    void insert(OfficeWorkDetailEntity entity);

    void update(OfficeWorkDetailEntity entity);
    
}
