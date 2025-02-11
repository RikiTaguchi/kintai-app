package com.meikokintai.kintai_app.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public abstract class DateSet {
    
    public static String getYear (int year, int month, int day) {
        Date date;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        String result;
        try {
            date = sdFormat.parse(String.format("%04d", year) + "-" + String.format("%02d", month) + "-" + String.format("%02d", day));
            calendar.setTime(date);
            if (calendar.get(Calendar.DAY_OF_MONTH) > 25) {
                calendar.add(Calendar.MONTH, 1);
            }
            result = String.format("%04d", calendar.get(Calendar.YEAR));
        } catch (Exception e) {
            result = "";
        }
        return result;
    }
    
    public static String getYear (String form) {
        Date date;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        String result;
        try {
            date = sdFormat.parse(form);
            calendar.setTime(date);
            if (calendar.get(Calendar.DAY_OF_MONTH) > 25) {
                calendar.add(Calendar.MONTH, 1);
            }
            result = String.format("%04d", calendar.get(Calendar.YEAR));
        } catch (Exception e) {
            result = "";
        }
        return result;
    }
    
    public static String getMonth (int year, int month, int day) {
        Date date;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        String result;
        try {
            date = sdFormat.parse(String.format("%04d", year) + "-" + String.format("%02d", month) + "-" + String.format("%02d", day));
            calendar.setTime(date);
            if (calendar.get(Calendar.DAY_OF_MONTH) > 25) {
                calendar.add(Calendar.MONTH, 1);
            }
            result = String.format("%02d", calendar.get(Calendar.MONTH)+1);
        } catch (Exception e) {
            result = "";
        }
        return result;
    }
    
    public static String getMonth (String form) {
        Date date;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        String result;
        try {
            date = sdFormat.parse(form);
            calendar.setTime(date);
            if (calendar.get(Calendar.DAY_OF_MONTH) > 25) {
                calendar.add(Calendar.MONTH, 1);
            }
            result = String.format("%02d", calendar.get(Calendar.MONTH)+1);
        } catch (Exception e) {
            result = "";
        }
        return result;
    }
    
    public static String getDayOfWeek (String form) {
        Date date;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dayOfWeek;
        try {
            date = sdFormat.parse(form);
            calendar.setTime(date);
            switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                case 1:
                    dayOfWeek = "日";
                    break;
                case 2:
                    dayOfWeek = "月";
                    break;
                case 3:
                    dayOfWeek = "火";
                    break;
                case 4:
                    dayOfWeek = "水";
                    break;
                case 5:
                    dayOfWeek = "木";
                    break;
                case 6:
                    dayOfWeek = "金";
                    break;
                case 7:
                    dayOfWeek = "土";
                    break;
                default:
                    dayOfWeek = "";
            }
        } catch (Exception e) {
            dayOfWeek = "";
        }
        return dayOfWeek;
    }
    
    public static Date[] getDatePeriod (String year, String month) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date result[] = new Date[2];
        try {
            result[1] = sdFormat.parse(year + "-" + month + "-25");
            calendar.setTime(result[1]);
            calendar.add(Calendar.MONTH, -1);
            result[0] = sdFormat.parse(String.format("%04d", calendar.get(Calendar.YEAR)) + "-" + String.format("%02d", calendar.get(Calendar.MONTH)+1) + "-26");
        } catch (Exception e) {
            result[0] = new Date();
            result[1] = new Date();
        }
        return result;
    }
    
    public static String[] getDateBefore (String year, String month) {
        String result[] = new String[2];
        switch (month) {
            case "01":
                result[0] = String.format("%04d", Integer.valueOf(year)-1);
                result[1] = "12";
                break;
            default:
                result[0] = year;
                result[1] = String.format("%02d", Integer.valueOf(month)-1);
                break;
        }
        return result;
    }
    
    public static String[] getDateNext (String year, String month) {
        String result[] = new String[2];
        switch (month) {
            case "12":
                result[0] = String.format("%04d", Integer.valueOf(year)+1);
                result[1] = "01";
                break;
            default:
                result[0] = year;
                result[1] = String.format("%02d", Integer.valueOf(month)+1);
                break;
        }
        return result;
    }

    public static String getTerm (String start) {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String end = currentDate.format(formatter);
        Period period = Period.between(LocalDate.parse(start, formatter), LocalDate.parse(end, formatter));
        String result;
        if (period.getYears() < 0 || period.getMonths() < 0 || period.getDays() < 0) {
            result = "-";
        } else if (period.getYears() == 0) {
            result = period.getMonths() + "ヶ月";
        } else {
            result = period.getYears() + "年" + period.getMonths() + "ヶ月";
        }
        return result;
    }

    public static String getTerm (String start, String end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Period period = Period.between(LocalDate.parse(start, formatter), LocalDate.parse(end, formatter));
        String result;
        if (period.getYears() < 0 || period.getMonths() < 0 || period.getDays() < 0) {
            result = "-";
        } else if (period.getYears() == 0) {
            result = period.getMonths() + "ヶ月";
        } else {
            result = period.getYears() + "年" + period.getMonths() + "ヶ月";
        }
        return result;
    }
    
}
