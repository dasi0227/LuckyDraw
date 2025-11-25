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
public class ActivityOrderEntity {

    private String orderId;
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

}
