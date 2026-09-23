package com.example.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.api.mapper.ClassroomMapper;
import com.example.api.mapper.entity.ClassroomEntity;
import com.example.api.service.dto.ClassroomDto;

@ExtendWith(MockitoExtension.class)
class ClassroomServiceTest {

    @Mock private ClassroomMapper classroomMapper;

    private ClassroomService classroomService;

    @BeforeEach
    void setUp() {
        classroomService = new ClassroomService(classroomMapper);
    }

    @Test
    @DisplayName("findAll: 全教室を返す")
    void findAll() {
        ClassroomEntity c1 = entity("戸塚", 22);
        ClassroomEntity c2 = entity("東戸塚", 23);
        when(classroomMapper.selectAll()).thenReturn(List.of(c1, c2));

        List<ClassroomDto> result = classroomService.findAll();
        assertEquals(2, result.size());
        assertEquals("戸塚", result.get(0).getName());
        assertEquals("東戸塚", result.get(1).getName());
    }

    @Test
    @DisplayName("findAll: 0件 → 空リスト")
    void empty() {
        when(classroomMapper.selectAll()).thenReturn(List.of());
        assertEquals(0, classroomService.findAll().size());
    }

    private ClassroomEntity entity(String name, int number) {
        ClassroomEntity e = new ClassroomEntity();
        e.setId(UUID.randomUUID());
        e.setName(name);
        e.setClassroomNumber(number);
        return e;
    }
}
