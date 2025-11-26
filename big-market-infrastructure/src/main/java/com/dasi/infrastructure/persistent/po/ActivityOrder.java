package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityOrder {
    private Long id;
    private String orderId;
    private String bizId;
    private String userId;
    private Long sku;
    private Long strategyId;
    private Long activityId;
    private String activityName;
    private Integer totalCount;
    private Integer monthCount;
    private Integer dayCount;
    private String state;
    private LocalDateTime orderTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}