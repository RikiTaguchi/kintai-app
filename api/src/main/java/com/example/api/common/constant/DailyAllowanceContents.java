package com.example.api.common.constant;

import java.time.LocalDate;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DailyAllowanceContents {
    
    public static final LocalDate WORK_DATE_REVISION_2025_10 = LocalDate.of(2025, 10, 1);
    
    public static final LocalDate SALARY_EFFECTIVE_DATE_2022_04 = LocalDate.of(2022, 4, 1);
    public static final LocalDate SALARY_EFFECTIVE_DATE_2024_08 = LocalDate.of(2024, 8, 1);

    public static final int NO_ALLOWANCE = 0;

    public static final int DAILY_ALLOWANCE_FULL_LEGACY = 390;
    public static final int DAILY_ALLOWANCE_HALF_LEGACY = 195;

    public static final int DAILY_ALLOWANCE_FULL = 410;
    public static final int DAILY_ALLOWANCE_HALF = 205;

    public static final int ALLOWANCE_RATE_PER_PERIOD = 100;

    public static final int SINGLE_PERIOD_COUNT = 1;

    public static final int PERIOD_COUNT_THRESHOLD_LEGACY = 4;
    public static final int PERIOD_COUNT_THRESHOLD = 5;

}
