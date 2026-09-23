package com.example.api.common.constant;

import java.time.LocalDate;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DateContents {
    
    public static final LocalDate MIN_LOCAL_DATE_FROM = LocalDate.of(1900, 1, 1);
    public static final LocalDate MAX_LOCAL_DATE_TO = LocalDate.of(2100, 12, 31);

    public static final Integer DAY_RANGE_FROM = 26;
    public static final Integer DAY_RANGE_TO = 25;

}
