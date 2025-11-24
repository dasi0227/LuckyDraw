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

    /** 抽奖奖品副标题 */
    private String awardSubtitle;

    /** 抽奖奖品排序 */
    private Integer sort;

    /** 奖品库存总量 */
    private Integer awardCount;

    /** 奖品库存余量 */
    private Integer awardCountSurplus;

    /** 奖品中奖概率 */
    private BigDecimal awardRate;

}
