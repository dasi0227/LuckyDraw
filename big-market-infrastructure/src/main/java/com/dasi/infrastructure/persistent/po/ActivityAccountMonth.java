package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityAccountMonth {

    /** 自增id */
    private Long id;

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

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
