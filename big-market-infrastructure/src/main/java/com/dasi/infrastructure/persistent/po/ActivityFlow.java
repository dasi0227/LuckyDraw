package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityFlow {
    private Long id;
    private String userId;
    private Long activityId;
    private Integer totalCount;
    private Integer dayCount;
    private Integer monthCount;
    private String flowId;
    private String flowChannel;
    private String bizId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
