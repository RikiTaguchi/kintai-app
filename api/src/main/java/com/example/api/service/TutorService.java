package com.example.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.exception.InvalidInputException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.mapper.TutorMapper;
import com.example.api.mapper.entity.AccountEntity;
import com.example.api.mapper.entity.TutorEntity;
import com.example.api.service.dto.TutorDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TutorService {

    private final TutorMapper tutorMapper;
    private final AccountService accountService;

    public List<TutorDto> findAll(UUID classroomId) {
        return tutorMapper.selectAll(classroomId)
            .stream()
            .map(TutorEntity::toDto)
            .toList();
    }

    public TutorDto find(UUID tutorId) {
        return tutorMapper.select(tutorId)
            .orElseThrow(() -> new ResourceNotFoundException("講師が見つかりません"))
            .toDto();
    }

    @Transactional
    public TutorDto register(TutorDto dto) {

        AccountEntity accountEntity = accountService.register(dto.getLoginId(), dto.getPassword());

        TutorEntity tutorEntity = new TutorEntity();
        tutorEntity.setAccountId(accountEntity.getId());
        tutorEntity.setClassroomId(dto.getClassroomId());
        tutorEntity.setTutorNumber(dto.getTutorNumber());
        tutorEntity.setFirstName(dto.getFirstName());
        tutorEntity.setLastName(dto.getLastName());
        tutorMapper.insert(tutorEntity);

        return tutorMapper.select(tutorEntity.getId())
            .orElseThrow(() -> new ResourceNotFoundException("講師が見つかりません"))
            .toDto();

    }

    @Transactional
    public TutorDto edit(TutorDto dto) {

        if (Boolean.TRUE.equals(dto.getTerminated()) && dto.getTerminationDate() == null) {
            throw new InvalidInputException("退職日を入力してください");
        }

        TutorEntity entity = new TutorEntity();
        entity.setId(dto.getId());
        entity.setTutorNumber(dto.getTutorNumber());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setTerminated(dto.getTerminated());
        entity.setTerminationDate(Boolean.TRUE.equals(dto.getTerminated()) ? dto.getTerminationDate() : null);

        tutorMapper.update(entity);

        return tutorMapper.select(dto.getId())
            .orElseThrow(() -> new ResourceNotFoundException("講師が見つかりません"))
            .toDto();

    }

    @Transactional
    public void resetPassword(UUID tutorId, String newPassword) {
        TutorEntity entity = tutorMapper.select(tutorId)
            .orElseThrow(() -> new ResourceNotFoundException("講師が見つかりません"));
        accountService.resetPassword(entity.getAccountId(), newPassword);
    }

    @Transactional
    public void changePassword(UUID tutorId, String currentPassword, String newPassword) {
        TutorEntity entity = tutorMapper.select(tutorId)
            .orElseThrow(() -> new ResourceNotFoundException("講師が見つかりません"));
        accountService.changePassword(entity.getAccountId(), currentPassword, newPassword);
    }

    @Transactional
    public void delete(UUID tutorId) {

        TutorEntity entity = tutorMapper.select(tutorId)
            .orElseThrow(() -> new ResourceNotFoundException("講師が見つかりません"));

        accountService.delete(entity.getAccountId());

    }

}
