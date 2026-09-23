package com.example.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.exception.ResourceNotFoundException;
import com.example.api.mapper.entity.template.LessonTemplateDetailEntity;
import com.example.api.mapper.entity.template.OfficeTemplateDetailEntity;
import com.example.api.mapper.entity.template.OtherTemplateDetailEntity;
import com.example.api.mapper.entity.template.TemplateEntity;
import com.example.api.mapper.template.LessonTemplateDetailMapper;
import com.example.api.mapper.template.OfficeTemplateDetailMapper;
import com.example.api.mapper.template.OtherTemplateDetailMapper;
import com.example.api.mapper.template.TemplateMapper;
import com.example.api.service.dto.template.TemplateDto;
import com.example.api.service.validator.ScheduleValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemplateService {
    
    private final TemplateMapper templateMapper;
    private final LessonTemplateDetailMapper lessonTemplateDetailMapper;
    private final OfficeTemplateDetailMapper officeTemplateDetailMapper;
    private final OtherTemplateDetailMapper otherTemplateDetailMapper;

    public List<TemplateDto> findAll(UUID tutorId) {
        return templateMapper.selectAll(tutorId).stream()
            .map(TemplateEntity::toDto)
            .toList();
    }

    public TemplateDto find(UUID templateId) {
        return templateMapper.select(templateId)
            .orElseThrow(() -> new ResourceNotFoundException("テンプレートが見つかりません"))
            .toDto();
    }

    @Transactional
    public TemplateDto register(TemplateDto dto) {

        ScheduleValidator.checkInput(dto);

        TemplateEntity templateEntity = new TemplateEntity();
        templateEntity.setTutorId(dto.getTutorId());
        templateEntity.setTitle(dto.getTitle());
        templateEntity.setClassroomId(dto.getClassroomId());
        templateEntity.setTransportationFee(dto.getTransportationFee());
        templateMapper.insert(templateEntity);

        LessonTemplateDetailEntity lessonTemplateDetailEntity = new LessonTemplateDetailEntity();
        lessonTemplateDetailEntity.setTemplateId(templateEntity.getId());
        lessonTemplateDetailEntity.applyDto(dto.getLessonTemplateDetailDto());
        lessonTemplateDetailMapper.insert(lessonTemplateDetailEntity);

        OfficeTemplateDetailEntity officeTemplateDetailEntity = new OfficeTemplateDetailEntity();
        officeTemplateDetailEntity.setTemplateId(templateEntity.getId());
        officeTemplateDetailEntity.setStartTime(dto.getOfficeTemplateDetailDto().getStartTime());
        officeTemplateDetailEntity.setEndTime(dto.getOfficeTemplateDetailDto().getEndTime());
        officeTemplateDetailMapper.insert(officeTemplateDetailEntity);

        OtherTemplateDetailEntity otherTemplateDetailEntity = new OtherTemplateDetailEntity();
        otherTemplateDetailEntity.setTemplateId(templateEntity.getId());
        otherTemplateDetailEntity.setStartTime(dto.getOtherTemplateDetailDto().getStartTime());
        otherTemplateDetailEntity.setEndTime(dto.getOtherTemplateDetailDto().getEndTime());
        otherTemplateDetailEntity.setBreakMinutes(dto.getOtherTemplateDetailDto().getBreakMinutes());
        otherTemplateDetailEntity.setDescription(dto.getOtherTemplateDetailDto().getDescription());
        otherTemplateDetailMapper.insert(otherTemplateDetailEntity);

        return templateMapper.select(templateEntity.getId())
            .orElseThrow(() -> new ResourceNotFoundException("テンプレートが見つかりません"))
            .toDto();

    }

    @Transactional
    public TemplateDto edit(TemplateDto dto) {

        ScheduleValidator.checkInput(dto);

        TemplateEntity templateEntity = TemplateEntity.fromDto(dto);

        LessonTemplateDetailEntity lessonDetailEntity = templateEntity.getLessonTemplateDetailEntity();
        lessonDetailEntity.setTemplateId(templateEntity.getId());
        OfficeTemplateDetailEntity officeDetailEntity = templateEntity.getOfficeTemplateDetailEntity();
        officeDetailEntity.setTemplateId(templateEntity.getId());
        OtherTemplateDetailEntity otherDetailEntity = templateEntity.getOtherTemplateDetailEntity();
        otherDetailEntity.setTemplateId(templateEntity.getId());

        templateMapper.update(templateEntity);
        lessonTemplateDetailMapper.update(lessonDetailEntity);
        officeTemplateDetailMapper.update(officeDetailEntity);
        otherTemplateDetailMapper.update(otherDetailEntity);

        return templateMapper.select(templateEntity.getId())
            .orElseThrow(() -> new ResourceNotFoundException("テンプレートが見つかりません"))
            .toDto();

    }

    @Transactional
    public void delete(UUID templateId) {
        templateMapper.delete(templateId);
    }

}
