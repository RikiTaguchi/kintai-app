package com.meikokintai.kintai_app.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.meikokintai.kintai_app.model.Work;

//タイプ一覧
//0 : エラー
//1 : 授業業務 事務業務 研修/自習室 が全てある
//2 : 授業業務 事務業務 研修/自習室 が全てある(研修/自習室 は 空きコマ の間)
//3 : 授業業務 事務業務 研修/自習室 が全てある(事務業務 は 空きコマ の間)
//4 : 授業業務 事務業務 研修/自習室 が全てある(事務業務 研修/自習室 ともに 空きコマ の間)
//5 : 授業業務後 or 授業業務前 に 事務業務 がある
//6 : 空きコマ の間に 事務業務 がある
//7 : 授業業務後 or 授業業務前 に 研修/自習室 がある
//8 : 空きコマ の間に 研修/自習室 がある
//9 : 授業業務 のみ
//10 : 事務業務 と 研修/自習室 のみ
//11 : 事務業務 のみ
//12 : 研修/自習室 のみ

public abstract class TimeSet {
    
    public static int getType (Work work, int classStart, int classEnd) {
        LocalDateTime classTimeStart;
        LocalDateTime classTimeEnd;
        LocalDateTime officeTimeStart;
        LocalDateTime officeTimeEnd;
        LocalDateTime otherTimeStart;
        LocalDateTime otherTimeEnd;
        
        if (work.getClassCount() > 0 && work.getClassCount() < 3) {
            classTimeStart = ClassSet.getStartAndEndTime(work.getDate(), classStart, classEnd, work.getClassCount())[0];
            classTimeEnd = ClassSet.getStartAndEndTime(work.getDate(), classStart, classEnd, work.getClassCount())[1];
        } else if (work.getClassCount() > 2) {
            classTimeStart = LocalDateTime.parse((work.getDate() + "-" + work.getTimeStart()), DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
            classTimeEnd = LocalDateTime.parse((work.getDate() + "-" + work.getTimeEnd()), DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
        } else {
            classTimeStart = null;
            classTimeEnd = null;
        }
        
        if (!work.getOfficeTimeStart().isEmpty() && !work.getOfficeTimeEnd().isEmpty()) {
            officeTimeStart = LocalDateTime.parse((work.getDate() + "-" + work.getOfficeTimeStart()), DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
            officeTimeEnd = LocalDateTime.parse((work.getDate() + "-" + work.getOfficeTimeEnd()), DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
        } else {
            officeTimeStart = null;
            officeTimeEnd = null;
        }
        
        if (!work.getOtherTimeStart().isEmpty() && !work.getOtherTimeEnd().isEmpty() && !work.getOtherWork().isEmpty()) {
            otherTimeStart = LocalDateTime.parse((work.getDate() + "-" + work.getOtherTimeStart()), DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
            otherTimeEnd = LocalDateTime.parse((work.getDate() + "-" + work.getOtherTimeEnd()), DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
        } else {
            otherTimeStart = null;
            otherTimeEnd = null;
        }
        
        if (classTimeStart != null && officeTimeStart != null && otherTimeStart != null) {
            boolean check[] = new boolean[5];
            check[0] = Duration.between(officeTimeEnd, classTimeStart).toMinutes() >= 0 || Duration.between(classTimeEnd, officeTimeStart).toMinutes() >= 0;
            check[1] = Duration.between(classTimeStart, officeTimeStart).toMinutes() >= 0 && Duration.between(officeTimeEnd, classTimeEnd).toMinutes() >= 0 && Duration.between(classTimeStart, classTimeEnd).toMinutes() - Duration.between(officeTimeStart, officeTimeEnd).toMinutes() - work.getBreakTime() >= 0;
            check[2] = Duration.between(otherTimeEnd, classTimeStart).toMinutes() >= 0 || Duration.between(classTimeEnd, otherTimeStart).toMinutes() >= 0;
            check[3] = Duration.between(classTimeStart, otherTimeStart).toMinutes() >= 0 && Duration.between(otherTimeEnd, classTimeEnd).toMinutes() >= 0 && Duration.between(classTimeStart, classTimeEnd).toMinutes() - Duration.between(otherTimeStart, otherTimeEnd).toMinutes() - work.getBreakTime() >= 0;
            check[4] = Duration.between(otherTimeEnd, officeTimeStart).toMinutes() >= 0 || Duration.between(officeTimeEnd, otherTimeStart).toMinutes() >= 0;
            if ((!check[0] && !check[1]) || (!check[2] && !check[3]) || !check[4]) {
                return 0;
            } else if (check[0] && check[2]) {
                return 1;
            } else if (check[0] && check[3]) {
                return 2;
            } else if (check[1] && check[2]) {
                return 3;
            } else if (check[1] && check[3]) {
                return 4;
            } else {
                return 0;
            }
        } else if (classTimeStart != null && officeTimeStart != null && otherTimeStart == null) {
            if (Duration.between(officeTimeEnd, classTimeStart).toMinutes() >= 0 || Duration.between(classTimeEnd, officeTimeStart).toMinutes() >= 0) {
                return 5;
            } else if (Duration.between(classTimeStart, officeTimeStart).toMinutes() >= 0 && Duration.between(officeTimeEnd, classTimeEnd).toMinutes() >= 0 && Duration.between(classTimeStart, classTimeEnd).toMinutes() - Duration.between(officeTimeStart, officeTimeEnd).toMinutes() - work.getBreakTime() >= 0) {
                return 6;
            } else {
                return 0;
            }
        } else if (classTimeStart != null && officeTimeStart == null && otherTimeStart != null) {
            if (Duration.between(otherTimeEnd, classTimeStart).toMinutes() >= 0 || Duration.between(classTimeEnd, otherTimeStart).toMinutes() >= 0) {
                return 7;
            } else if (Duration.between(classTimeStart, otherTimeStart).toMinutes() >= 0 && Duration.between(otherTimeEnd, classTimeEnd).toMinutes() >= 0 && Duration.between(classTimeStart, classTimeEnd).toMinutes() - Duration.between(otherTimeStart, otherTimeEnd).toMinutes() - work.getBreakTime() >= 0) {
                return 8;
            } else {
                return 0;
            }
        } else if (classTimeStart != null && officeTimeStart == null && otherTimeStart == null) {
            return 9;
        } else if (classTimeStart == null && officeTimeStart != null && otherTimeStart != null) {
            if (Duration.between(otherTimeEnd, officeTimeStart).toMinutes() >= 0 || Duration.between(officeTimeEnd, otherTimeStart).toMinutes() >= 0) {
                return 10;
            } else {
                return 0;
            }
        } else if (classTimeStart == null && officeTimeStart != null && otherTimeStart == null) {
            return 11;
        } else if (classTimeStart == null && officeTimeStart == null && otherTimeStart != null) {
            return 12;
        } else {
            return 0;
        }
    }
    
}
