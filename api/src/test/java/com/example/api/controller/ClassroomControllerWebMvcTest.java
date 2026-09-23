package com.example.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.api.security.SecurityConfig;
import com.example.api.service.ClassroomService;
import com.example.api.service.dto.ClassroomDto;

@WebMvcTest(ClassroomController.class)
@Import(SecurityConfig.class)
class ClassroomControllerWebMvcTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ClassroomService classroomService;

    @Test
    @DisplayName("GET /api/classrooms: 200 + 教室リスト（permitAll）")
    void findAll() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(classroomService.findAll()).thenReturn(List.of(
            new ClassroomDto(id1, "戸塚", 22),
            new ClassroomDto(id2, "東戸塚", 23)
        ));

        mockMvc.perform(get("/api/classrooms"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(id1.toString()))
            .andExpect(jsonPath("$[0].name").value("戸塚"))
            .andExpect(jsonPath("$[1].name").value("東戸塚"));
    }
}
