package com.example.api.mapper.template;

import org.apache.ibatis.annotations.Mapper;

import com.example.api.mapper.entity.template.LessonTemplateDetailEntity;

@Mapper
public interface LessonTemplateDetailMapper {
    
    void insert(LessonTemplateDetailEntity entity);

    void update(LessonTemplateDetailEntity entity);

}
