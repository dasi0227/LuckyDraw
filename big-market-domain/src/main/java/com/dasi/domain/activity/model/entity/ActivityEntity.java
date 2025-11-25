package com.dasi.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityEntity {

    private Long activityId;
    private String activityName;
    private String activityDesc;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Long activityCountId;
    private Long strategyId;
    private String state;

}
