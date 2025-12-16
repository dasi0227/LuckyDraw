package com.dasi.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DASH_DAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");
    private static final DateTimeFormatter DASH_MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss ");
    private static final DateTimeFormatter DASH_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取当天日期字符串。
     *
     * @param isDash 是否使用破折号格式：
     *               true  返回 yyyy-MM-dd
     *               false 返回 yyyyMMdd
     * @return 当天日期字符串
     */
    public static String thisDay(boolean isDash) {
        return isDash
                ? LocalDate.now().format(DASH_DAY_FORMAT)
                : LocalDate.now().format(DAY_FORMAT);
    }

    /**
     * 获取当月日期字符串。
     *
     * @param isDash 是否使用破折号格式：
     *               true  返回 yyyy-MM
     *               false 返回 yyyyMM
     * @return 当月字符串
     */
    public static String thisMonth(boolean isDash) {
        return isDash
                ? LocalDate.now().format(DASH_MONTH_FORMAT)
                : LocalDate.now().format(MONTH_FORMAT);
    }

    /**
     * 获取当前时间字符串（含时分秒）。
     *
     * @param isDash 是否使用破折号格式：
     *               true  返回 yyyy-MM-dd HH:mm:ss
     *               false 返回 yyyyMMddHHmmss
     * @return 当前时间字符串
     */
    public static String thisMoment(boolean isDash) {
        return isDash
                ? LocalDateTime.now().format(DASH_TIME_FORMAT)
                : LocalDateTime.now().format(TIME_FORMAT);
    }


    public static String currentTimeMillis() {
        return String.valueOf(System.currentTimeMillis());
    }

}
