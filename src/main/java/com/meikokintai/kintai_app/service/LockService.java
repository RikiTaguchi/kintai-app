package com.meikokintai.kintai_app.service;

import java.util.UUID;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meikokintai.kintai_app.model.Lock;
import com.meikokintai.kintai_app.repository.LockRepository;

@Service
public class LockService {
    
    private final LockRepository lockRepository;
    
    public LockService(LockRepository lockRepository) {
        this.lockRepository = lockRepository;
    }

    public List<Lock> findByUserId(UUID userId) {
        return this.lockRepository.findByUserId(userId);
    }
    
    public void add(Lock lock) {
        lockRepository.save(lock);
    }

    public Lock getByTarget(UUID classAreaId, UUID userId, int year, int month) {
        return lockRepository.getByTarget(classAreaId, userId, year, month);
    }

    public void deleteById(Lock lock) {
        lockRepository.deleteById(lock.getId());
    }

    @Transactional
    public void update(Lock lock) {
        lockRepository.update(lock.getClassAreaId(), lock.getUserId(), lock.getYear(), lock.getMonth(), lock.getStatus());
    }

}
