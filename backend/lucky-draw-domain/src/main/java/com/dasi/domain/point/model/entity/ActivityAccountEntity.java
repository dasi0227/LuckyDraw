package com.dasi.domain.point.model.entity;

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

    /** 用户积分 */
    private Integer accountPoint;

    /** 用户幸运值 */
    private Integer accountLuck;

    /** 总分配 */
    private Integer totalAllocate;

    /** 总抽奖次数 */
    private Integer totalSurplus;

    /** 天上限 */
    private Integer dayLimit;

    /** 月上限 */
    private Integer monthLimit;

}
