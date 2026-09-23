package com.example.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.controller.response.ClassroomResponse;
import com.example.api.service.ClassroomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassroomController {
    
    private final ClassroomService classroomService;

    @GetMapping
    public ResponseEntity<List<ClassroomResponse>> findAll() {
        return ResponseEntity.ok(
            classroomService.findAll().stream()
                .map((classroom) -> ClassroomResponse.fromDto(classroom))
                .toList()
        );
    }

}
