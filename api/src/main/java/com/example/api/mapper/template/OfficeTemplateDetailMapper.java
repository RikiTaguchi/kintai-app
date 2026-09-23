package com.example.api.mapper.template;

import org.apache.ibatis.annotations.Mapper;

import com.example.api.mapper.entity.template.OfficeTemplateDetailEntity;

@Mapper
public interface OfficeTemplateDetailMapper {
    
    void insert(OfficeTemplateDetailEntity entity);

    void update(OfficeTemplateDetailEntity entity);

}
