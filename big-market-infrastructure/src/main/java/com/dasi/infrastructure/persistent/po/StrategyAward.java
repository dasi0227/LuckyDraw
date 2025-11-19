package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StrategyAward {

    /** 自增ID */
    private Long id;

    /** 抽奖策略ID */
    private Long strategyId;

    /** 抽奖奖品ID */
    private Integer awardId;

    /** 抽奖奖品标题 */
    private String awardTitle;

    /** 抽奖奖品副标题 */
    private String awardSubtitle;

    /** 奖品库存总量 */
    private Integer awardCount;

    /** 奖品库存余量 */
    private Integer awardCountSurplus;

    /** 奖品中奖概率 */
    private BigDecimal awardRate;

    /** 奖品对应的规则模型集合（逗号分隔） */
    private String ruleModels;

    /** 排序 */
    private Integer sort;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
