package com.example.api.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.exception.ResourceNotFoundException;
import com.example.api.mapper.ClassroomMapper;
import com.example.api.mapper.ManagerMapper;
import com.example.api.mapper.entity.AccountEntity;
import com.example.api.mapper.entity.ManagerEntity;
import com.example.api.service.dto.ManagerDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerMapper managerMapper;
    private final ClassroomMapper classroomMapper;
    private final AccountService accountService;

    public ManagerDto find(UUID managerId) {
        return managerMapper.select(managerId)
            .orElseThrow(() -> new ResourceNotFoundException("管理者が見つかりません"))
            .toDto();
    }

    @Transactional
    public ManagerDto register(ManagerDto dto) {

        AccountEntity accountEntity = accountService.register(dto.getLoginId(), dto.getPassword());

        ManagerEntity managerEntity = new ManagerEntity();
        managerEntity.setAccountId(accountEntity.getId());
        managerEntity.setClassroomId(dto.getClassroomId());
        managerEntity.setFirstName(dto.getFirstName());
        managerEntity.setLastName(dto.getLastName());
        managerMapper.insert(managerEntity);

        return managerMapper.select(managerEntity.getId())
            .orElseThrow(() -> new ResourceNotFoundException("管理者が見つかりません"))
            .toDto();
    }

    @Transactional
    public ManagerDto edit(ManagerDto dto) {

        ManagerEntity managerEntity = managerMapper.select(dto.getId())
            .orElseThrow(() -> new ResourceNotFoundException("管理者が見つかりません"));

        classroomMapper.select(dto.getClassroomId())
            .orElseThrow(() -> new ResourceNotFoundException("教室が見つかりません"));

        accountService.editLoginId(managerEntity.getAccountId(), dto.getLoginId());

        managerEntity.setClassroomId(dto.getClassroomId());
        managerEntity.setFirstName(dto.getFirstName());
        managerEntity.setLastName(dto.getLastName());
        managerMapper.update(managerEntity);

        return managerMapper.select(dto.getId())
            .orElseThrow(() -> new ResourceNotFoundException("管理者が見つかりません"))
            .toDto();

    }

    @Transactional
    public void changePassword(UUID managerId, String currentPassword, String newPassword) {
        ManagerEntity managerEntity = managerMapper.select(managerId)
            .orElseThrow(() -> new ResourceNotFoundException("管理者が見つかりません"));
        accountService.changePassword(managerEntity.getAccountId(), currentPassword, newPassword);
    }

    @Transactional
    public void delete(UUID managerId) {
        ManagerEntity entity = managerMapper.select(managerId)
            .orElseThrow(() -> new ResourceNotFoundException("管理者が見つかりません"));

        accountService.delete(entity.getAccountId());
    }

}
