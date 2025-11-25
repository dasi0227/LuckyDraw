package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Activity {

    private Long id;
    private Long activityId;
    private String activityName;
    private String activityDesc;
    private LocalDateTime beginDateTime;
    private LocalDateTime endDateTime;
    private Integer stockCount;
    private Integer stockCountSurplus;
    private Long activityCountId;
    private Long strategyId;
    private String state;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
