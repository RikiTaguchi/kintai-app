package com.example.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import com.example.api.AbstractPostgresIT;
import com.example.api.mapper.entity.ClassroomEntity;

@MybatisTest
@ActiveProfiles("test")
class ClassroomMapperIT extends AbstractPostgresIT {

    @Autowired private ClassroomMapper classroomMapper;

    @Test
    @DisplayName("selectAll: シードの2教室（戸塚/東戸塚）を返す")
    void selectAllSeeded() {
        List<ClassroomEntity> list = classroomMapper.selectAll();
        assertEquals(2, list.size());
        assertEquals("戸塚", list.get(0).getName());
        assertEquals("東戸塚", list.get(1).getName());
    }

    @Test
    @DisplayName("select: 戸塚の情報が取れる")
    void select() {
        ClassroomEntity e = classroomMapper.select(MapperTestFixtures.CLASSROOM_TOZUKA)
            .orElseThrow();
        assertEquals("戸塚", e.getName());
        assertEquals(22, e.getClassroomNumber());
    }
}
