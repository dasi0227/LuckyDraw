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
    private Long awardId;

    /** 规则树ID */
    private String treeId;

    /** 抽奖奖品标题 */
    private String awardTitle;

    /** 奖品库存总量 */
    private Integer awardAllocate;

    /** 奖品库存余量 */
    private Integer awardSurplus;

    /** 奖品中奖概率 */
    private BigDecimal awardRate;

    /** 奖品排列序号 */
    private Integer awardIndex;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
