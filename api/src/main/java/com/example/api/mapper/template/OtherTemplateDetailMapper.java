package com.example.api.mapper.template;

import org.apache.ibatis.annotations.Mapper;

import com.example.api.mapper.entity.template.OtherTemplateDetailEntity;

@Mapper
public interface OtherTemplateDetailMapper {
    
    void insert(OtherTemplateDetailEntity entity);

    void update(OtherTemplateDetailEntity entity);

}
