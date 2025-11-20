package com.dasi.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleContextEntity {

    /** 用户 ID */
    private String userId;

    /** 策略 ID */
    private Long strategyId;

    /** 奖品 ID */
    private Integer awardId;

    /** 抽奖规则模型 */
    private String ruleModel;

    public static RuleContextEntity buildRuleContext(RaffleRequestEntity request, String ruleModel) {
        return RuleContextEntity.builder()
                .userId(request.getUserId())
                .strategyId(request.getStrategyId())
                .awardId(request.getAwardId())
                .ruleModel(ruleModel)
                .build();
    }

}
