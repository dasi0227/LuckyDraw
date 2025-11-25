package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityOrder {
    private Long id;
    private String userId;
    private Long activityId;
    private String activityName;
    private Long strategyId;
    private String orderId;
    private LocalDateTime orderTime;
    private String state;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}