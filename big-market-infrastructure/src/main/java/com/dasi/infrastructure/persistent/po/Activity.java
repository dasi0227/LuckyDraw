package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Activity {

    /** 自增id */
    private Long id;

    /** 活动id */
    private Long activityId;

    /** 策略id */
    private Long strategyId;

    /** 活动名称 */
    private String activityName;

    /** 活动描述 */
    private String activityDesc;

    /** 活动状态 */
    private String activityState;

    /** 活动开始时间 */
    private LocalDateTime activityBeginTime;

    /** 活动结束时间 */
    private LocalDateTime activityEndTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
