package com.meikokintai.kintai_app.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meikokintai.kintai_app.model.WorkTemplate;
import com.meikokintai.kintai_app.repository.WorkTemplateRepository;

@Service
public class WorkTemplateService {
    
    private final WorkTemplateRepository workTemplateRepository;
    
    public WorkTemplateService(WorkTemplateRepository workTemplateRepository) {
        this.workTemplateRepository = workTemplateRepository;
    }
    
    public WorkTemplate findTemplateById(UUID templateId) {
        return this.workTemplateRepository.findTemplateById(templateId);
    }
    
    public List<WorkTemplate> findByUserId(UUID userId) {
        return this.workTemplateRepository.findByUserId(userId);
    }
    
    public void add(WorkTemplate workTemplate) {
        workTemplateRepository.save(workTemplate);
    }
    
    @Transactional
    public void update(WorkTemplate template) {
        this.workTemplateRepository.update(template.getId(), template.getTitle(), template.getTimeStart(), template.getTimeEnd(), template.getClassM(), template.getClassK(), template.getClassS(), template.getClassA(), template.getClassB(), template.getClassC(), template.getClassD(), template.getBreakTime(), template.getOfficeTimeStart(), template.getOfficeTimeEnd(), template.getOtherWork(), template.getOtherTimeStart(), template.getOtherTimeEnd(), template.getOtherBreakTime(), template.getCarfare(), template.getHelpArea());
    }
    
    @Transactional
    public void deleteById(UUID deleteId) {
        this.workTemplateRepository.deleteById(deleteId);
    }
    
}
