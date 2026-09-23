package com.example.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.api.mapper.ClassroomMapper;
import com.example.api.mapper.entity.ClassroomEntity;
import com.example.api.service.dto.ClassroomDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClassroomService {
    
    private final ClassroomMapper classroomMapper;

    public List<ClassroomDto> findAll() {
        return classroomMapper.selectAll()
            .stream()
            .map(ClassroomEntity::toDto)
            .toList();
    }

}
