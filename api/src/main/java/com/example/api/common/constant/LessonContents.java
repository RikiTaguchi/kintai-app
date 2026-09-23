package com.example.api.common.constant;

import java.time.LocalTime;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LessonContents {

    public static final Integer LESSON_MINUTES = 100;

    public static final Integer LESSON_PREPARING_MINUTES = 20;

    public static final LocalTime LESSON_M_START = LocalTime.of(9, 10);
    public static final LocalTime LESSON_M_END = LocalTime.of(10, 40);

    public static final LocalTime LESSON_K_START = LocalTime.of(10, 50);
    public static final LocalTime LESSON_K_END = LocalTime.of(12, 20);

    public static final LocalTime LESSON_S_START = LocalTime.of(13, 10);
    public static final LocalTime LESSON_S_END = LocalTime.of(14, 40);

    public static final LocalTime LESSON_A_START = LocalTime.of(14, 50);
    public static final LocalTime LESSON_A_END = LocalTime.of(16, 20);

    public static final LocalTime LESSON_B_START = LocalTime.of(16, 30);
    public static final LocalTime LESSON_B_END = LocalTime.of(18, 0);

    public static final LocalTime LESSON_C_START = LocalTime.of(18, 10);
    public static final LocalTime LESSON_C_END = LocalTime.of(19, 40);

    public static final LocalTime LESSON_D_START = LocalTime.of(19, 50);
    public static final LocalTime LESSON_D_END = LocalTime.of(21, 20);

    public static final Integer LESSON_OUT_SIDE_HOURS_BORDER = 3;
    
}
