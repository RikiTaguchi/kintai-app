package com.meikokintai.kintai_app.service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meikokintai.kintai_app.model.Salary;
import com.meikokintai.kintai_app.model.Work;
import com.meikokintai.kintai_app.repository.WorkRepository;
import com.meikokintai.kintai_app.util.ClassSet;
import com.meikokintai.kintai_app.util.TimeSet;

@Service
public class WorkService {
    
    private final WorkRepository workRepository;
    
    public WorkService(WorkRepository workRepository) {
        this.workRepository = workRepository;
    }
    
    public List<Work> findByUserId(UUID userId, Date dateFrom, Date dateTo) {
        try {
            SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
            return this.workRepository.findByUserId(userId, sdFormat.format(dateFrom), sdFormat.format(dateTo));
        } catch (Exception e) {
            return this.workRepository.findByUserId(userId, "", "");
        }
    }

    public List<Work> findAllByUserId(UUID userId) {
        return workRepository.findAllByUserId(userId);
    }
    
    public Work findWorkById(UUID id) {
        return this.workRepository.findWorkById(id);
    }
    
    public void add(Work work) {
        workRepository.save(work);
    }
    
    @Transactional
    public void update(Work work) {
        workRepository.update(work.getId(), work.getUserId(), work.getDate(), work.getDayOfWeek(), work.getTimeStart(), work.getTimeEnd(), work.getClassM(), work.getClassK(), work.getClassS(), work.getClassA(), work.getClassB(), work.getClassC(), work.getClassD(), work.getClassCount(), work.getHelpArea(), work.getBreakTime(), work.getOfficeTimeStart(), work.getOfficeTimeEnd(), work.getOfficeTime(), work.getOtherWork(), work.getOtherTimeStart(), work.getOtherTimeEnd(), work.getOtherBreakTime(), work.getOtherTime(), work.getCarfare(), work.getOutOfTime(), work.getOverTime(), work.getNightTime(), work.getSupportSalary());
    }
    
    @Transactional
    public void deleteById(UUID id) {
        workRepository.deleteById(id);
    }
    
    @Transactional
    public int[] calcSumSalary(UUID userId, Date dateFrom, Date dateTo, int classSalary, int officeSalary, int supportSalary) throws Exception {
        int result[] = new int[17];
        try {
            SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
            result[0] = workRepository.sumClassCount(userId, sdFormat.format(dateFrom), sdFormat.format(dateTo));
            result[1] = classSalary * result[0];
            result[2] = workRepository.sumWorkDay(userId, sdFormat.format(dateFrom), sdFormat.format(dateTo));
            result[3] = workRepository.sumOutOfTime(userId, sdFormat.format(dateFrom), sdFormat.format(dateTo));
            result[4] = (int)Math.ceil((double)(officeSalary*result[3])/(double)60);
            result[5] = workRepository.sumOfficeTime(userId, sdFormat.format(dateFrom), sdFormat.format(dateTo));
            result[6] = (int)Math.ceil((double)(officeSalary*result[5])/(double)60);
            result[7] = workRepository.sumOtherTime(userId, sdFormat.format(dateFrom), sdFormat.format(dateTo));
            result[8] = (int)Math.ceil((double)(officeSalary*result[7])/(double)60);
            result[9] = supportSalary * workRepository.sumSupportSalary(userId, sdFormat.format(dateFrom), sdFormat.format(dateTo));
            result[10] = 0;
            result[11] = workRepository.sumCarfare(userId, sdFormat.format(dateFrom), sdFormat.format(dateTo));
            result[12] = workRepository.sumOverTime(userId, sdFormat.format(dateFrom), sdFormat.format(dateTo));
            result[13] = (int)Math.ceil((double)(officeSalary*result[12])/(double)240);
            result[14] = workRepository.sumNightTime(userId, sdFormat.format(dateFrom), sdFormat.format(dateTo));
            result[15] = (int)Math.ceil((double)(officeSalary*result[14])/(double)240);
            result[16] = result[1] + result[4] + result[6] + result[8] + result[9] + result[11] + result[13] + result[15];
            return result;
        } catch (Exception e) {
            throw new Exception();
        }
    }
    
    @Transactional
    public double[] calcSumSalary(Work work, Salary salary) {
        double result[] = new double[16];
        result[0] = (double)work.getClassCount();
        result[1] = (double)salary.getClassSalary() * result[0];
        result[2] = (double)1;
        result[3] = (double)work.getOutOfTime();
        result[4] = (double)(salary.getOfficeSalary()*result[3])/(double)60;
        result[5] = (double)work.getOfficeTime();
        result[6] = (double)(salary.getOfficeSalary()*result[5])/(double)60;
        result[7] = (double)work.getOtherTime();
        result[8] = (double)(salary.getOfficeSalary()*result[7])/(double)60;
        if (work.getSupportSalary().equals("true")) {
            result[9] = (double)salary.getSupportSalary();
        } else {
            result[9] = (double)0;
        }
        result[10] = (double)0;
        result[11] = (double)work.getCarfare();
        result[12] = (double)work.getOverTime();
        result[13] = (double)(salary.getOfficeSalary()*work.getOverTime())/(double)240;
        result[14] = (double)work.getNightTime();
        result[15] = (double)(salary.getOfficeSalary()*work.getNightTime())/(double)240;
        return result;
    }
    
    @Transactional
    public Work calcTimeAndSalary(Work work) throws Exception {
        Boolean classes[] = {work.getClassM(), work.getClassK(), work.getClassS(), work.getClassA(), work.getClassB(), work.getClassC(), work.getClassD()};
        int classCount = 0;
        int classStart = 0;
        int classEnd = 0;
        int nightTime = 0;
        int overTime = 0;
        int totalTime = 0;
        for (int i = 0; i < 7; i++) {
            if (classes[i]) {
                classCount += 1;
            }
        }
        for (int i = 0; i < 7; i++) {
            if (classes[i]) {
                classStart = i;
                classEnd = i;
                break;
            }
        }
        for (int i = classStart + 1; i < 7; i++) {
            if (classes[i]) {
                classEnd = i;
            }
        }
        work.setClassCount(classCount);
        if (!work.getTimeStart().isEmpty() && !work.getTimeEnd().isEmpty() && work.getClassCount() > 2 && ClassSet.check1(classStart, classEnd, work.getTimeStart(), work.getTimeEnd(), work.getDate())) {
            LocalDateTime classTimeStart = LocalDateTime.parse((work.getDate() + "-" + work.getTimeStart()), DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
            LocalDateTime classTimeEnd = LocalDateTime.parse((work.getDate() + "-" + work.getTimeEnd()), DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
            LocalDateTime nightTimeBorder = LocalDateTime.parse(work.getDate() + "-22:00", DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
            Duration durationClass = Duration.between(classTimeStart, classTimeEnd);
            Duration durationNight = Duration.between(nightTimeBorder, classTimeEnd);
            if ((durationClass.toMinutes() - work.getBreakTime()) - 100 * work.getClassCount() - 20 >= 0) {
                int outOfTime = (int)(durationClass.toMinutes() - work.getBreakTime() - 100 * work.getClassCount() - 20);
                if (outOfTime < 0) {
                    work.setOutOfTime(0);
                } else {
                    work.setOutOfTime(outOfTime);
                }
            } else if ((durationClass.toMinutes() - work.getBreakTime()) >= 90 * work.getClassCount()) {
                work.setOutOfTime(0);
            } else {
                throw new Exception();
            }
            if (durationNight.toMinutes() > 0) {
                nightTime += durationNight.toMinutes();
            }
            totalTime += durationClass.toMinutes() - work.getBreakTime();
        } else if (work.getClassCount() > 0 && work.getClassCount() < 3) {
            if (ClassSet.check2(classStart, classEnd, classCount, work.getBreakTime(), work.getDate())) {
                work.setOutOfTime(0);
                work.setTimeStart("");
                work.setTimeEnd("");
            } else {
                throw new Exception();
            }
            Duration durationClass = Duration.between(ClassSet.getStartAndEndTime(work.getDate(), classStart, classEnd, classCount)[0], ClassSet.getStartAndEndTime(work.getDate(), classStart, classEnd, classCount)[1]);
            totalTime += (int)(durationClass.toMinutes() - work.getBreakTime());
        } else if (work.getClassCount() > 0) {
            throw new Exception();
        } else {
            work.setSupportSalary("false");
            work.setBreakTime(0);
            work.setOutOfTime(0);
            work.setTimeStart("");
            work.setTimeEnd("");
        }
        if (!work.getOfficeTimeStart().isEmpty() && !work.getOfficeTimeEnd().isEmpty()) {
            LocalDateTime officeTimeStart = LocalDateTime.parse((work.getDate() + "-" + work.getOfficeTimeStart()), DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
            LocalDateTime officeTimeEnd = LocalDateTime.parse((work.getDate() + "-" + work.getOfficeTimeEnd()), DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
            LocalDateTime nightTimeBorder = LocalDateTime.parse(work.getDate() + "-22:00", DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
            Duration durationOffice = Duration.between(officeTimeStart, officeTimeEnd);
            Duration durationNight1 = Duration.between(nightTimeBorder, officeTimeEnd);
            Duration durationNight2 = Duration.between(nightTimeBorder, officeTimeStart);
            int officeTime = (int)durationOffice.toMinutes();
            if (officeTime > 0) {
                work.setOfficeTime(officeTime);
            } else {
                throw new Exception();
            }
            if (durationNight2.toMinutes() > 0) {
                nightTime += durationNight1.toMinutes() - durationNight2.toMinutes();
            } else if (durationNight1.toMinutes() > 0) {
                nightTime += durationNight1.toMinutes();
            }
            totalTime += work.getOfficeTime();
        } else {
            work.setOfficeTime(0);
            work.setOfficeTimeStart("");
            work.setOfficeTimeEnd("");
        }
        if (!work.getOtherTimeStart().isEmpty() && !work.getOtherTimeEnd().isEmpty() && !work.getOtherWork().isEmpty()) {
            LocalDateTime otherTimeStart = LocalDateTime.parse((work.getDate() + "-" + work.getOtherTimeStart()), DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
            LocalDateTime otherTimeEnd = LocalDateTime.parse((work.getDate() + "-" + work.getOtherTimeEnd()), DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
            LocalDateTime nightTimeBorder = LocalDateTime.parse(work.getDate() + "-22:00", DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
            Duration durationOther = Duration.between(otherTimeStart, otherTimeEnd);
            Duration durationNight1 = Duration.between(nightTimeBorder, otherTimeEnd);
            Duration durationNight2 = Duration.between(nightTimeBorder, otherTimeStart);
            int otherTime = (int)(durationOther.toMinutes() - work.getOtherBreakTime());
            if (durationOther.toMinutes() > 0 && otherTime > 0) {
                work.setOtherTime(otherTime);
            } else {
                throw new Exception();
            }
            if (durationNight2.toMinutes() > 0) {
                nightTime += durationNight1.toMinutes() - durationNight2.toMinutes();
            } else if (durationNight1.toMinutes() > 0) {
                nightTime += durationNight1.toMinutes();
            }
            
            totalTime += (int)(durationOther.toMinutes() - work.getOtherBreakTime());
            
        } else {
            work.setOtherWork("");
            work.setOtherBreakTime(0);
            work.setOtherTime(0);
            work.setOtherTimeStart("");
            work.setOtherTimeEnd("");
        }
        work.setOverTime(0);
        work.setNightTime(nightTime);
        int timeType = TimeSet.getType(work, classStart, classEnd);
        if (timeType == 0) {
            throw new Exception();
        } else if (timeType == 2 || timeType == 8) {
            if (classCount > 2) {
                work.setOutOfTime(work.getOutOfTime() - work.getOtherTime());
                if (work.getOutOfTime() < 0) {
                    throw new Exception();
                }
            }
            totalTime -= work.getOtherTime();
        } else if (timeType == 3 || timeType == 6) {
            if (classCount > 2) {
                work.setOutOfTime(work.getOutOfTime() - work.getOfficeTime());
                if (work.getOutOfTime() < 0) {
                    throw new Exception();
                }
            }
            totalTime -= work.getOfficeTime();
        } else if (timeType == 4) {
            if (classCount > 2) {
                work.setOutOfTime(work.getOutOfTime() - work.getOtherTime() - work.getOfficeTime());
                if (work.getOutOfTime() < 0) {
                    throw new Exception();
                }
            }
            totalTime -= work.getOtherTime() + work.getOfficeTime();
        }
        if (totalTime > 600) {
            overTime = totalTime - 600;
            if (overTime > 0) {
                work.setOverTime(overTime);
            } else {
                work.setOverTime(0);
            }
        }
        return work;
    }
}
