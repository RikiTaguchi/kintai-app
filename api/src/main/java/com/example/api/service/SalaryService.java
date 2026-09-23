package com.example.api.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.common.constant.DateContents;
import com.example.api.exception.AlreadyExistsException;
import com.example.api.exception.BusinessException;
import com.example.api.exception.InvalidInputException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.mapper.SalaryMapper;
import com.example.api.mapper.TutorMapper;
import com.example.api.mapper.entity.SalaryEntity;
import com.example.api.mapper.entity.TutorEntity;
import com.example.api.mapper.entity.work.WorkEntity;
import com.example.api.mapper.work.WorkMapper;
import com.example.api.service.dto.SalaryDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SalaryService {

    private final SalaryMapper salaryMapper;
    private final TutorMapper tutorMapper;
    private final WorkMapper workMapper;

    public List<SalaryDto> findAll(UUID tutorId) {
        return salaryMapper.selectAll(tutorId).stream()
            .map(SalaryEntity::toDto)
            .toList();
    }

    public SalaryDto find(UUID salaryId) {
        return salaryMapper.select(salaryId)
            .orElseThrow(() -> new ResourceNotFoundException("給与情報が見つかりません"))
            .toDto();
    }

    @Transactional
    public SalaryDto register(SalaryDto dto) {

        validateEffectiveDate(dto);
        validateNoDuplicateEffectiveDate(dto.getTutorId(), dto.getEffectiveDate(), null);

        SalaryEntity entity = SalaryEntity.fromDto(dto);
        salaryMapper.insert(entity);

        return salaryMapper.select(entity.getId())
            .orElseThrow(() -> new ResourceNotFoundException("給与情報が見つかりません"))
            .toDto();
    }

    @Transactional
    public SalaryDto edit(SalaryDto dto) {

        validateEffectiveDate(dto);
        validateNoDuplicateEffectiveDate(dto.getTutorId(), dto.getEffectiveDate(), dto.getId());

        salaryMapper.update(SalaryEntity.fromDto(dto));

        return salaryMapper.select(dto.getId())
            .orElseThrow(() -> new ResourceNotFoundException("給与情報が見つかりません"))
            .toDto();

    }

    @Transactional
    public void delete(UUID salaryId) {
        
        SalaryEntity salaryEntity = salaryMapper.select(salaryId)
            .orElseThrow(() -> new ResourceNotFoundException("給与情報が見つかりません"));
        
        TutorEntity tutorEntity = tutorMapper.select(salaryEntity.getTutorId())
            .orElseThrow(() -> new ResourceNotFoundException("講師が見つかりません"));

        List<SalaryEntity> filteredSalaryEntities = salaryMapper.selectAll(tutorEntity.getId()).stream()
            .filter((entity) -> !entity.getId().equals(salaryEntity.getId()))
            .toList();

        LocalDate dateFrom = DateContents.MIN_LOCAL_DATE_FROM;
        LocalDate dateTo = filteredSalaryEntities.stream()
            .map(SalaryEntity::getEffectiveDate)
            .min(Comparator.naturalOrder())
            .map((date) -> date.minusDays(1))
            .orElse(DateContents.MAX_LOCAL_DATE_TO);

        List<WorkEntity> workEntities = workMapper.selectAll(tutorEntity.getId(), dateFrom, dateTo);

        if (!workEntities.isEmpty()) {
            throw new BusinessException("この給与情報は勤務情報で参照されているため削除できません");
        }

        salaryMapper.delete(salaryId);
    }

    private void validateNoDuplicateEffectiveDate(UUID tutorId, LocalDate effectiveDate, UUID excludeId) {
        if (salaryMapper.existsByTutorIdAndEffectiveDate(tutorId, effectiveDate, excludeId)) {
            throw new AlreadyExistsException("同じ適用開始日の給与情報が既に存在します");
        }
    }

    private void validateEffectiveDate(SalaryDto dto) {

        if (
            dto.getEffectiveDate().isBefore(DateContents.MIN_LOCAL_DATE_FROM) ||
            dto.getEffectiveDate().isAfter(DateContents.MAX_LOCAL_DATE_TO)
        ) {
            throw new InvalidInputException(
                String.format("適用開始日は %s から %s の間で入力してください",
                    DateContents.MIN_LOCAL_DATE_FROM,
                    DateContents.MAX_LOCAL_DATE_TO)
            );
        }
        
    }
    
}
