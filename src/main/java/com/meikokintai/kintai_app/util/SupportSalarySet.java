package com.meikokintai.kintai_app.util;

import com.meikokintai.kintai_app.model.Work;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import com.meikokintai.kintai_app.model.Salary;
import com.meikokintai.kintai_app.service.SalaryService;

@Component
public class SupportSalarySet {

    private final SalaryService salaryService;

    public SupportSalarySet(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    // 雇用開始年度によってパターンを識別
    private int getPattern(LocalDate date) {
        if (date.isBefore(LocalDate.of(2022, 4, 1))) {
            return 0;
        } else if (date.isBefore(LocalDate.of(2024, 8, 1))) {
            return 1;
        } else {
            return 2;
        }
    }

    // パターン、コマ数、年月日を引数に日時手当を計算する
    private int getResult(int pattern, int count, LocalDate date) {
        int result;
        int salary1;
        int salary2;
        if (date.isBefore(LocalDate.of(2025, 10, 1))) {
            salary1 = 390;
            salary2 = 195;
        } else {
            salary1 = 410;
            salary2 = 205;
        }
        if (pattern == 0) {
            if (count == 0) {
                result = 0;
            } else if (count < 4) {
                result = salary1;
            } else {
                result = 100 * count;
            }
        } else if (pattern == 1) {
            if (count == 0) {
                result = 0;
            } else {
                result = salary1;
            }
        } else if (pattern == 2) {
            if (count == 0) {
                result = 0;
            } else if (count == 1) {
                result = salary2;
            } else {
                result = salary1;
            }
        } else {
            result = salary1;
        }
        return result;
    }

    public int getSupportSalary(Work work) {
        Salary salary = salaryService.getFirstSalary(work.getUserId());
        LocalDate startDate = LocalDate.parse(salary.getDateFrom(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate targetDate = LocalDate.parse(work.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        boolean classList[] = {work.getClassM(), work.getClassK(), work.getClassS(), work.getClassA(), work.getClassB(), work.getClassC(), work.getClassD()};
        int classCount = 0;
        for (boolean c : classList) {
            if (c) {
                classCount++;
            }
        }
        int salaryPattern = getPattern(startDate);
        int salaryResult = getResult(salaryPattern, classCount, targetDate);
        return salaryResult;
    }
}
