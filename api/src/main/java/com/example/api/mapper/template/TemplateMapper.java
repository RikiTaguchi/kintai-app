package com.example.api.mapper.template;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.ibatis.annotations.Mapper;

import com.example.api.mapper.entity.template.TemplateEntity;

@Mapper
public interface TemplateMapper {
    
    List<TemplateEntity> selectAll(UUID tutorId);

    Optional<TemplateEntity> select(UUID templateId);

    void insert(TemplateEntity entity);

    void update(TemplateEntity entity);

    void delete(UUID templateId);

}
