package com.example.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.api.exception.ResourceNotFoundException;
import com.example.api.mapper.entity.template.TemplateEntity;
import com.example.api.mapper.template.TemplateMapper;
import com.example.api.service.dto.template.LessonTemplateDetailDto;
import com.example.api.service.dto.template.OfficeTemplateDetailDto;
import com.example.api.service.dto.template.OtherTemplateDetailDto;
import com.example.api.service.dto.template.TemplateDto;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock private TemplateMapper templateMapper;
    @Mock private com.example.api.mapper.template.LessonTemplateDetailMapper lessonTemplateDetailMapper;
    @Mock private com.example.api.mapper.template.OfficeTemplateDetailMapper officeTemplateDetailMapper;
    @Mock private com.example.api.mapper.template.OtherTemplateDetailMapper otherTemplateDetailMapper;

    private TemplateService templateService;

    @BeforeEach
    void setUp() {
        templateService = new TemplateService(
            templateMapper,
            lessonTemplateDetailMapper,
            officeTemplateDetailMapper,
            otherTemplateDetailMapper
        );
    }

    @Nested
    @DisplayName("find")
    class Find {

        @Test
        @DisplayName("存在しない → 404")
        void missing() {
            UUID id = UUID.randomUUID();
            when(templateMapper.select(id)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> templateService.find(id));
        }

        @Test
        @DisplayName("findAll: 委譲して DTO 化")
        void findAll() {
            UUID tutorId = UUID.randomUUID();
            TemplateEntity t1 = new TemplateEntity();
            t1.setId(UUID.randomUUID());
            t1.setTutorId(tutorId);
            // TemplateEntity.toDto() は lesson detail を null ガードなしで呼ぶため必須
            t1.setLessonTemplateDetailEntity(lessonDetail());
            t1.setOfficeTemplateDetailEntity(new com.example.api.mapper.entity.template.OfficeTemplateDetailEntity());
            t1.setOtherTemplateDetailEntity(new com.example.api.mapper.entity.template.OtherTemplateDetailEntity());
            when(templateMapper.selectAll(tutorId)).thenReturn(List.of(t1));

            List<TemplateDto> result = templateService.findAll(tutorId);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("validator を通して各 mapper に insert")
        void insertsDetails() {
            UUID templateId = UUID.randomUUID();
            TemplateDto dto = baseDto();

            // templateMapper.insert 時に ID を採番したふりをする
            org.mockito.Mockito.doAnswer(inv -> {
                TemplateEntity e = inv.getArgument(0);
                e.setId(templateId);
                return null;
            }).when(templateMapper).insert(org.mockito.ArgumentMatchers.any());

            TemplateEntity saved = new TemplateEntity();
            saved.setId(templateId);
            saved.setTutorId(dto.getTutorId());
            saved.setLessonTemplateDetailEntity(lessonDetail());
            saved.setOfficeTemplateDetailEntity(new com.example.api.mapper.entity.template.OfficeTemplateDetailEntity());
            saved.setOtherTemplateDetailEntity(new com.example.api.mapper.entity.template.OtherTemplateDetailEntity());
            when(templateMapper.select(templateId)).thenReturn(Optional.of(saved));

            templateService.register(dto);

            verify(lessonTemplateDetailMapper).insert(org.mockito.ArgumentMatchers.any());
            verify(officeTemplateDetailMapper).insert(org.mockito.ArgumentMatchers.any());
            verify(otherTemplateDetailMapper).insert(org.mockito.ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("edit")
    class Edit {

        @Test
        @DisplayName("3つの detail mapper を update する")
        void updatesAll() {
            UUID templateId = UUID.randomUUID();
            TemplateDto dto = baseDto();
            dto.setId(templateId);

            TemplateEntity saved = new TemplateEntity();
            saved.setId(templateId);
            saved.setLessonTemplateDetailEntity(lessonDetail());
            saved.setOfficeTemplateDetailEntity(new com.example.api.mapper.entity.template.OfficeTemplateDetailEntity());
            saved.setOtherTemplateDetailEntity(new com.example.api.mapper.entity.template.OtherTemplateDetailEntity());
            when(templateMapper.select(templateId)).thenReturn(Optional.of(saved));

            templateService.edit(dto);

            verify(templateMapper).update(org.mockito.ArgumentMatchers.any());
            verify(lessonTemplateDetailMapper).update(org.mockito.ArgumentMatchers.any());
            verify(officeTemplateDetailMapper).update(org.mockito.ArgumentMatchers.any());
            verify(otherTemplateDetailMapper).update(org.mockito.ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Deletion {

        @Test
        @DisplayName("templateMapper.delete に委譲")
        void delegates() {
            UUID id = UUID.randomUUID();
            templateService.delete(id);
            verify(templateMapper).delete(id);
        }
    }

    // helpers

    // LessonTemplateDetailEntity は periodCode フラグが未set時(null)のまま toDto() すると NPE になるため、
    // 空 DTO を applyDto してフラグ群を boolean 初期化した状態で生成する。
    private com.example.api.mapper.entity.template.LessonTemplateDetailEntity lessonDetail() {
        LessonTemplateDetailDto empty = new LessonTemplateDetailDto(
            null, null, null, null, null, java.util.List.of());
        return com.example.api.mapper.entity.template.LessonTemplateDetailEntity.fromDto(empty);
    }

    private TemplateDto baseDto() {
        TemplateDto dto = new TemplateDto();
        dto.setId(UUID.randomUUID());
        dto.setTutorId(UUID.randomUUID());
        dto.setClassroomId(UUID.randomUUID());
        dto.setTitle("title");
        dto.setTransportationFee(0);
        // バリデータを通過する最小限の入力（全 detail 空）
        dto.setLessonTemplateDetailDto(new LessonTemplateDetailDto(
            null, null, null, null, null, java.util.List.of()));
        dto.setOfficeTemplateDetailDto(new OfficeTemplateDetailDto(
            null, null, null, null));
        dto.setOtherTemplateDetailDto(new OtherTemplateDetailDto(
            null, null, null, null, null, null));
        return dto;
    }
}
