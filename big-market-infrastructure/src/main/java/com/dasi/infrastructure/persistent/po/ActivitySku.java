package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivitySku {

    private Long id;
    private Long sku;
    private Long activityId;
    private Long activityCountId;
    private Integer stockAmount;
    private Integer stockSurplus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
