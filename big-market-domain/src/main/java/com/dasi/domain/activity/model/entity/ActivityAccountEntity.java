package com.dasi.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityAccountEntity {

    /** 用户id */
    private String userId;

    /** 活动id */
    private Long activityId;

    /** 总分配 */
    private Integer totalAllocate;

    /** 总余额 */
    private Integer totalSurplus;

    /** 天分配 */
    private Integer dayAllocate;

    /** 天余额 */
    private Integer daySurplus;

    /** 月分配 */
    private Integer monthAllocate;

    /** 月余额 */
    private Integer monthSurplus;

}
