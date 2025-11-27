package com.dasi.infrastructure.persistent.po;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityDay {

    /** 自增id */
    private Long id;

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

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
