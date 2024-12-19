package com.meikokintai.kintai_app.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class ClassSet {
    
    public static String classStart[] = {"09:10", "10:50", "13:10", "14:50", "16:30", "18:10", "19:50"}; 
    public static String classEnd[] = {"10:40", "12:20", "14:40", "16:20", "18:00", "19:40", "21:20"};
    
    public static boolean check1(int start, int end, String startTime, String endTime, String workDate) {
        LocalDateTime dateTime[] = new LocalDateTime[4];
        Duration durationStart;
        Duration durationEnd;
        try {
            dateTime[0] = LocalDateTime.parse(workDate+"-"+classStart[start], DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
            dateTime[1] = LocalDateTime.parse(workDate+"-"+classStart[end], DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
            dateTime[2] = LocalDateTime.parse(workDate+"-"+startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
            dateTime[3] = LocalDateTime.parse(workDate+"-"+endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
            durationStart = Duration.between(dateTime[2], dateTime[0]);
            durationEnd = Duration.between(dateTime[1], dateTime[3]);
        } catch (Exception e) {
            return false;
        }
        if (durationStart.toMinutes() >= 0 && durationEnd.toMinutes() >= 0) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean check2(int start, int end, int classCount, int breakTime, String workDate) {
        LocalDateTime dateTimeStart;
        LocalDateTime dateTimeEnd;
        Duration duration;
        try {
            dateTimeStart = LocalDateTime.parse(workDate+"-"+classStart[start], DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
            dateTimeEnd = LocalDateTime.parse(workDate+"-"+classEnd[end], DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
            duration = Duration.between(dateTimeStart, dateTimeEnd);
            if (((int)duration.toMinutes() - classCount * 100 - breakTime + 10) >= 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    public static LocalDateTime[] getStartAndEndTime(String workDate, int start, int end, int classCount) {
        LocalDateTime startTime;
        if (classCount == 1) {
            startTime = LocalDateTime.parse(workDate+"-"+classStart[start], DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm")).minusMinutes(10);
        } else {
            startTime = LocalDateTime.parse(workDate+"-"+classStart[start], DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm")).minusMinutes(20);
        }
        LocalDateTime endTime = LocalDateTime.parse(workDate+"-"+classEnd[end], DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm")).plusMinutes(10);
        LocalDateTime result[] = {startTime, endTime};
        return result;
    }
    
    public static LocalDateTime getStart(String workDate, int start) { 
        return LocalDateTime.parse(workDate+"-"+classStart[start], DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
    }
    
    public static LocalDateTime getEnd(String workDate, int end) { 
        return LocalDateTime.parse(workDate+"-"+classEnd[end], DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"));
    }
    
}
