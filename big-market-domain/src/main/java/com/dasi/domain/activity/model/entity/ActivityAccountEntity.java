package com.dasi.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityAccountEntity {

    private String userId;

    private Long activityId;

    private Integer totalAmount;

    private Integer totalSurplus;

    private Integer dayAmount;

    private Integer daySurplus;

    private Integer monthAmount;

    private Integer monthSurplus;

}
