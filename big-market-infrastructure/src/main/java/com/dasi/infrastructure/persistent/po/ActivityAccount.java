package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityAccount {

    /** 自增id */
    private Long id;

    /** 用户id */
    private String userId;

    /** 活动id */
    private Long activityId;

    /** 总分配 */
    private Integer totalAllocate;

    /** 总余额 */
    private Integer totalSurplus;

    /** 天上限 */
    private Integer dayLimit;

    /** 月上限 */
    private Integer monthLimit;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
