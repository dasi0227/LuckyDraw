package com.dasi.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaffleRequestEntity {

    /** 用户 ID */
    private String userId;

    /** 策略 ID */
    private Long strategyId;

    /** 奖品 ID */
    private Integer awardId;


}
