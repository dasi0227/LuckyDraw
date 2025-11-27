package com.dasi.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityMonthEntity {

    /** 活动id */
    private Long activityId;

    /** 用户id */
    private String userId;

    /** yyyy-mm */
    private String month;

    /** 月次数 */
    private Integer monthAllocate;

    /** 月次数-剩余 */
    private Integer monthSurplus;

}
