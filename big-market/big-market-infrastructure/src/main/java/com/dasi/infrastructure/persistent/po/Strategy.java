package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Strategy {

    /** 自增ID */
    private Long id;

    /** 抽奖策略ID */
    private Long strategyId;

    /** 抽奖策略描述 */
    private String strategyDesc;

    /** 抽奖策略模型 */
    private String ruleModels;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}