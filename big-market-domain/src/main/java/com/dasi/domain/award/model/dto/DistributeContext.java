package com.dasi.domain.award.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributeContext {

    private String userId;

    private Long activityId;

    private Long awardId;

    private String awardName;

    private Long strategyId;

    private String orderId;

}
