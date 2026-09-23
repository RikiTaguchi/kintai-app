package com.example.api.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.common.constant.DailyAllowanceContents;
import com.example.api.common.constant.DateContents;
import com.example.api.controller.model.work.WorkDateRange;
import com.example.api.exception.AlreadyExistsException;
import com.example.api.exception.BusinessException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.mapper.SalaryMapper;
import com.example.api.mapper.entity.work.LessonWorkDetailEntity;
import com.example.api.mapper.entity.work.OfficeWorkDetailEntity;
import com.example.api.mapper.entity.work.OtherWorkDetailEntity;
import com.example.api.mapper.entity.work.WorkEntity;
import com.example.api.mapper.work.LessonWorkDetailMapper;
import com.example.api.mapper.work.OfficeWorkDetailMapper;
import com.example.api.mapper.work.OtherWorkDetailMapper;
import com.example.api.mapper.work.WorkMapper;
import com.example.api.service.dto.work.WorkDto;
import com.example.api.service.validator.ScheduleValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkService {

    private final WorkMapper workMapper;
    private final LessonWorkDetailMapper lessonWorkDetailMapper;
    private final OfficeWorkDetailMapper officeWorkDetailMapper;
    private final OtherWorkDetailMapper otherWorkDetailMapper;
    private final SalaryMapper salaryMapper;

    public List<WorkDto> findAll(UUID tutorId, Integer year, Integer month) {

        WorkDateRange workDateRange = calculateDateRange(year, month);

        List<WorkEntity> workEntities = workMapper.selectAll(tutorId, workDateRange.getDateFrom(), workDateRange.getDateTo());

        if (workEntities.isEmpty()) {
            return List.of();
        }

        LocalDate salaryEffectiveDate = calculateSalaryEffectiveDate(tutorId);

        return workEntities.stream()
            .map((entity) -> calculateDailyAllowance(entity.toDto(), salaryEffectiveDate))
            .toList();
    }

    public WorkDto find(UUID workId) {
        
        WorkDto work = workMapper.select(workId)
            .orElseThrow(() -> new ResourceNotFoundException("勤務情報が見つかりません"))
            .toDto();
        
        LocalDate salaryEffectiveDate = calculateSalaryEffectiveDate(work.getTutorId());
        
        return calculateDailyAllowance(work, salaryEffectiveDate);
        
    }

    @Transactional
    public WorkDto register(WorkDto dto) {

        LocalDate salaryEffectiveDate = calculateSalaryEffectiveDate(dto.getTutorId());

        if (dto.getWorkingDate().isBefore(salaryEffectiveDate)) {
            throw new BusinessException("勤務日が給与情報の適用開始日より前です");
        }

        validateNoDuplicateWorkingDate(dto.getTutorId(), dto.getWorkingDate(), null);

        ScheduleValidator.checkInput(dto);

        WorkEntity workEntity = new WorkEntity();
        workEntity.setTutorId(dto.getTutorId());
        workEntity.setClassroomId(dto.getClassroomId());
        workEntity.setWorkingDate(dto.getWorkingDate());
        workEntity.setTransportationFee(dto.getTransportationFee());
        workMapper.insert(workEntity);

        LessonWorkDetailEntity lessonWorkDetailEntity = new LessonWorkDetailEntity();
        lessonWorkDetailEntity.setWorkId(workEntity.getId());
        lessonWorkDetailEntity.applyDto(dto.getLessonWorkDetailDto());
        lessonWorkDetailMapper.insert(lessonWorkDetailEntity);

        OfficeWorkDetailEntity officeWorkDetailEntity = new OfficeWorkDetailEntity();
        officeWorkDetailEntity.setWorkId(workEntity.getId());
        officeWorkDetailEntity.setStartTime(dto.getOfficeWorkDetailDto().getStartTime());
        officeWorkDetailEntity.setEndTime(dto.getOfficeWorkDetailDto().getEndTime());
        officeWorkDetailMapper.insert(officeWorkDetailEntity);

        OtherWorkDetailEntity otherWorkDetailEntity = new OtherWorkDetailEntity();
        otherWorkDetailEntity.setWorkId(workEntity.getId());
        otherWorkDetailEntity.setStartTime(dto.getOtherWorkDetailDto().getStartTime());
        otherWorkDetailEntity.setEndTime(dto.getOtherWorkDetailDto().getEndTime());
        otherWorkDetailEntity.setBreakMinutes(dto.getOtherWorkDetailDto().getBreakMinutes());
        otherWorkDetailEntity.setDescription(dto.getOtherWorkDetailDto().getDescription());
        otherWorkDetailMapper.insert(otherWorkDetailEntity);

        return calculateDailyAllowance(
            workMapper.select(workEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("勤務情報が見つかりません"))
                .toDto(),
            salaryEffectiveDate
        );
        
    }

    @Transactional
    public WorkDto edit(WorkDto dto) {

        LocalDate salaryEffectiveDate = calculateSalaryEffectiveDate(dto.getTutorId());

        if (dto.getWorkingDate().isBefore(salaryEffectiveDate)) {
            throw new BusinessException("勤務日が給与情報の適用開始日より前です");
        }

        validateNoDuplicateWorkingDate(dto.getTutorId(), dto.getWorkingDate(), dto.getId());

        ScheduleValidator.checkInput(dto);

        WorkEntity workEntity = WorkEntity.fromDto(dto);

        LessonWorkDetailEntity lessonDetailEntity = workEntity.getLessonWorkDetailEntity();
        lessonDetailEntity.setWorkId(workEntity.getId());
        OfficeWorkDetailEntity officeDetailEntity = workEntity.getOfficeWorkDetailEntity();
        officeDetailEntity.setWorkId(workEntity.getId());
        OtherWorkDetailEntity otherDetailEntity = workEntity.getOtherWorkDetailEntity();
        otherDetailEntity.setWorkId(workEntity.getId());

        workMapper.update(workEntity);
        lessonWorkDetailMapper.update(lessonDetailEntity);
        officeWorkDetailMapper.update(officeDetailEntity);
        otherWorkDetailMapper.update(otherDetailEntity);

        return calculateDailyAllowance(
            workMapper.select(workEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("勤務情報が見つかりません"))
                .toDto(),
            salaryEffectiveDate
        );
    }

    @Transactional
    public void delete(UUID workId) {
        workMapper.delete(workId);
    }

    private void validateNoDuplicateWorkingDate(UUID tutorId, LocalDate workingDate, UUID excludeId) {
        if (workMapper.existsByTutorIdAndWorkingDate(tutorId, workingDate, excludeId)) {
            throw new AlreadyExistsException("この日付の勤務情報は既に登録されています");
        }
    }

    private WorkDateRange calculateDateRange(Integer year, Integer month) {

        if (month == 1) {
            return new WorkDateRange(
                LocalDate.of(year -1, 12, DateContents.DAY_RANGE_FROM),
                LocalDate.of(year, month, DateContents.DAY_RANGE_TO)
            );
        } else {
            return new WorkDateRange(
                LocalDate.of(year, month - 1, DateContents.DAY_RANGE_FROM),
                LocalDate.of(year, month, DateContents.DAY_RANGE_TO)
            );
        }

    }

    private LocalDate calculateSalaryEffectiveDate(UUID tutorId) {
        return salaryMapper.selectEarliestEffectiveDate(tutorId)
            .orElseThrow(() -> new ResourceNotFoundException("給与情報が見つかりません"));
    }

    private WorkDto calculateDailyAllowance(WorkDto dto, LocalDate salaryEffectiveDate) {

        WorkDto newDto = new WorkDto();

        newDto.setId(dto.getId());
        newDto.setTutorId(dto.getTutorId());
        newDto.setClassroomId(dto.getClassroomId());
        newDto.setClassroomName(dto.getClassroomName());
        newDto.setClassroomNumber(dto.getClassroomNumber());
        newDto.setWorkingDate(dto.getWorkingDate());
        newDto.setTransportationFee(dto.getTransportationFee());
        newDto.setLessonWorkDetailDto(dto.getLessonWorkDetailDto());
        newDto.setOfficeWorkDetailDto(dto.getOfficeWorkDetailDto());
        newDto.setOtherWorkDetailDto(dto.getOtherWorkDetailDto());

        Integer fullAllowance;
        Integer halfAllowance;
        Integer periodThreshold;

        if (dto.getWorkingDate().isBefore(DailyAllowanceContents.WORK_DATE_REVISION_2025_10)) {
            fullAllowance = DailyAllowanceContents.DAILY_ALLOWANCE_FULL_LEGACY;
            halfAllowance = DailyAllowanceContents.DAILY_ALLOWANCE_HALF_LEGACY;
            periodThreshold = DailyAllowanceContents.PERIOD_COUNT_THRESHOLD_LEGACY;
        } else {
            fullAllowance = DailyAllowanceContents.DAILY_ALLOWANCE_FULL;
            halfAllowance = DailyAllowanceContents.DAILY_ALLOWANCE_HALF;
            periodThreshold = DailyAllowanceContents.PERIOD_COUNT_THRESHOLD;
        }

        Integer periodCount = dto.getLessonWorkDetailDto().getPeriodCodes() != null
            ? dto.getLessonWorkDetailDto().getPeriodCodes().size()
            : 0;
        Integer calculatedAllowance;

        if (salaryEffectiveDate.isBefore(DailyAllowanceContents.SALARY_EFFECTIVE_DATE_2022_04)) {
            if (periodCount == 0) {
                calculatedAllowance = DailyAllowanceContents.NO_ALLOWANCE;
            } else if (periodCount < periodThreshold) {
                calculatedAllowance = fullAllowance;
            } else {
                calculatedAllowance = DailyAllowanceContents.ALLOWANCE_RATE_PER_PERIOD * periodCount;
            }
        } else if (salaryEffectiveDate.isBefore(DailyAllowanceContents.SALARY_EFFECTIVE_DATE_2024_08)) {
            if (periodCount == 0) {
                calculatedAllowance = DailyAllowanceContents.NO_ALLOWANCE;
            } else {
                calculatedAllowance = fullAllowance;
            }
        } else {
            if (periodCount == 0) {
                calculatedAllowance = DailyAllowanceContents.NO_ALLOWANCE;
            } else if (periodCount == DailyAllowanceContents.SINGLE_PERIOD_COUNT) {
                calculatedAllowance = halfAllowance;
            } else {
                calculatedAllowance = fullAllowance;
            }
        }

        newDto.setDailyAllowance(calculatedAllowance);

        return newDto;

    }
    
}
