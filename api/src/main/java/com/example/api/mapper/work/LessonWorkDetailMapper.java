package com.example.api.mapper.work;

import org.apache.ibatis.annotations.Mapper;

import com.example.api.mapper.entity.work.LessonWorkDetailEntity;

@Mapper
public interface LessonWorkDetailMapper {
    
    void insert(LessonWorkDetailEntity entity);

    void update(LessonWorkDetailEntity entity);

}
