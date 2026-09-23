package com.example.api.mapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.ibatis.annotations.Mapper;

import com.example.api.mapper.entity.TutorEntity;

@Mapper
public interface TutorMapper {

    List<TutorEntity> selectAll(UUID classroomId);
    
    Optional<TutorEntity> select(UUID tutorId);

    void insert(TutorEntity entity);

    void update(TutorEntity entity);

}
