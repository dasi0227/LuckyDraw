package com.dasi.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityEntity {

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

}
