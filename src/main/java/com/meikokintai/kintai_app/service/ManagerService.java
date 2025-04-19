package com.meikokintai.kintai_app.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.meikokintai.kintai_app.model.Manager;
import com.meikokintai.kintai_app.repository.ManagerRepository;

import jakarta.transaction.Transactional;

@Service
public class ManagerService {
    
    private final ManagerRepository managerRepository;
    
    public ManagerService(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }
    
    public List<Manager> findAll() {
        return this.managerRepository.findAll();
    }
    
    public Manager getByManagerId(UUID managerId) {
        return this.managerRepository.getByManagerId(managerId);
    }
    
    public void add(String loginId, String password, String classArea, int classCode) {
        Manager manager = new Manager();
        manager.setLoginId(loginId);
        manager.setPassword(password);
        manager.setClassArea(classArea);
        manager.setClassCode(classCode);
        managerRepository.save(manager);
    }
    
    public Manager getByLoginId(String loginId) {
        return this.managerRepository.getByLoginId(loginId);
    }

    @Transactional
    public void update(Manager manager) {
        managerRepository.update(manager.getId(), manager.getLoginId(), manager.getPassword(), manager.getClassArea(), manager.getClassCode());
    }

    public void deleteById(Manager manager) {
        managerRepository.deleteById(manager.getId());
    }

}
