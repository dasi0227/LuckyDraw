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

/*
CREATE TABLE `strategy_rule` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `strategy_id` int(8) NOT NULL COMMENT '抽奖策略ID',
  `award_id` int(8) DEFAULT NULL COMMENT '抽奖奖品ID【规则类型为策略，则不需要奖品ID】',
  `rule_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '规则类型；1-策略规则、2-奖品规则',
  `rule_model` varchar(16) NOT NULL COMMENT '规则模型；如随机、锁定、兜底等',
  `rule_value` varchar(64) NOT NULL COMMENT '规则比值（区间/次数/配置值）',
  `rule_desc` varchar(128) NOT NULL COMMENT '规则描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_strategy_id_award_id` (`strategy_id`,`award_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖策略规则表：定义抽奖策略及奖品规则逻辑';
 */