package com.example.api.mapper.work;

import org.apache.ibatis.annotations.Mapper;

import com.example.api.mapper.entity.work.OtherWorkDetailEntity;

@Mapper
public interface OtherWorkDetailMapper {

    void insert(OtherWorkDetailEntity entity);

    void update(OtherWorkDetailEntity entity);
    
}
