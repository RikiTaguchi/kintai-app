package com.example.api.common.constant;

import java.time.LocalTime;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PremiumPayContents {

    public static final Double PREMIUM_PAY_RATE = 0.25;

    public static final LocalTime NIGHT_START_TIME = LocalTime.of(22, 0);

    public static final Integer OVER_TIME_BORDER_LINE = 60 * 8;

}
