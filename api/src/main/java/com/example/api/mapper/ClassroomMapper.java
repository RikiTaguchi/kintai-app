package com.example.api.mapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.ibatis.annotations.Mapper;

import com.example.api.mapper.entity.ClassroomEntity;

@Mapper
public interface ClassroomMapper {
    
    List<ClassroomEntity> selectAll();

    Optional<ClassroomEntity> select(UUID classroomId);

}
