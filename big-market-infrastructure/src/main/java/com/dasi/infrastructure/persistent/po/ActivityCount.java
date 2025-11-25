package com.dasi.infrastructure.persistent.po;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityCount {

    private Long id;
    private Long activityCountId;
    private Integer totalCount;
    private Integer dayCount;
    private Integer monthCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
