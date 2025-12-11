package com.dasi.domain.strategy.service.tree.impl;

import com.dasi.domain.strategy.annotation.RuleModelConfig;
import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.domain.strategy.model.io.RuleCheckResult;
import com.dasi.domain.strategy.model.type.RuleCheckOutcome;
import com.dasi.domain.strategy.service.tree.IStrategyTree;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RuleModelConfig(ruleModel = RuleModel.RULE_FALLBACK)
public class RuleFallbackTree implements IStrategyTree {

    @Override
    public RuleCheckResult logic(String userId, Long strategyId, Long awardId, String ruleValue) {
        Long luckAwardId = Long.valueOf(ruleValue);
        log.info("【抽奖】RULE_FALLBACK 拦截：awardId={}", luckAwardId);
        return RuleCheckResult.builder()
                .awardId(luckAwardId)
                .ruleCheckOutcome(RuleCheckOutcome.CAPTURE)
                .ruleModel(RuleModel.RULE_FALLBACK)
                .build();
    }

}
