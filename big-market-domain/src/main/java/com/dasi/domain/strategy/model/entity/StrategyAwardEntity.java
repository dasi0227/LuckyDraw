package com.dasi.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyAwardEntity {

    /** 抽奖策略ID */
    private Long strategyId;

    /** 抽奖奖品ID */
    private Integer awardId;

    /** 抽奖奖品标题 */
    private String awardTitle;

    /** 奖品库存总量 */
    private Integer awardTotal;

    /** 奖品库存余量 */
    private Integer awardSurplus;

    /** 奖品中奖概率 */
    private BigDecimal awardRate;

}
