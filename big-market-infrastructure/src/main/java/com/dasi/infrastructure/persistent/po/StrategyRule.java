package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StrategyRule {

    /** 自增ID */
    private Long id;

    /** 抽奖策略ID */
    private Long strategyId;

    /** 奖品ID（规则类型为策略级时可为空） */
    private Integer awardId;

    /** 规则类型；1-策略规则、2-奖品规则 */
    private Integer ruleType;

    /** 规则模型 */
    private String ruleModel;

    /** 规则比值 */
    private String ruleValue;

    /** 规则描述 */
    private String ruleDesc;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}