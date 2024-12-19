package com.meikokintai.kintai_app.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meikokintai.kintai_app.model.Salary;
import com.meikokintai.kintai_app.repository.SalaryRepository;

@Service
public class SalaryService {
    
    private final SalaryRepository salaryRepository;
    
    public SalaryService(SalaryRepository salaryRepository) {
        this.salaryRepository = salaryRepository;
    }
    
    public List<Salary> findAll() {
        return this.salaryRepository.findAll();
    }
    
    public List<Salary> findByUserId(UUID userId) {
        return this.salaryRepository.findByUserId(userId);
    }
    
    public void add(Salary salary) {
        salaryRepository.save(salary);
    }
    
    @Transactional
    public void update(Salary salary) {
        salaryRepository.update(salary.getId(), salary.getUserId(), salary.getDateFrom(), salary.getClassSalary(), salary.getOfficeSalary(), salary.getSupportSalary(), salary.getCarfare());
    }

    @Transactional
    public void delete(Salary salary) {
        salaryRepository.delete(salary);
    }
    
    public Salary getBySalaryId(UUID salaryId) {
        return this.salaryRepository.getBySalaryId(salaryId);
    }
    
    public Salary getByDate(UUID userId, String date) {
        List<Salary> salaryList = salaryRepository.findByUserId(userId);
        Salary setSalary = new Salary();
        LocalDateTime localDateTimeTarget;
        LocalDateTime localDateTimeNow = LocalDateTime.parse("2000-01-01-00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
        LocalDateTime localDateTimeSet = LocalDateTime.parse(date+"-00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
        for (Salary s : salaryList) {
            localDateTimeTarget = LocalDateTime.parse(s.getDateFrom()+"-00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
            if (Duration.between(localDateTimeNow, localDateTimeTarget).toMinutes() >= 0 && Duration.between(localDateTimeTarget, localDateTimeSet).toMinutes() >= 0) {
                localDateTimeNow = localDateTimeTarget;
                setSalary = s;
            }
        }
        return setSalary;
    }
    
}
