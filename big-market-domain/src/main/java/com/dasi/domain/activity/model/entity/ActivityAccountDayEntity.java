package com.dasi.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityAccountDayEntity {

    /** 活动id */
    private Long activityId;

    /** 用户id */
    private String userId;

    /** yyyy-mm-dd */
    private String day;

    /** 日次数 */
    private Integer dayAllocate;

    /** 日次数-剩余 */
    private Integer daySurplus;

}
