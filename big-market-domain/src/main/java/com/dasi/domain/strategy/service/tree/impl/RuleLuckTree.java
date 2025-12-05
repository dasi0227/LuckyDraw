package com.dasi.domain.strategy.service.tree.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.domain.strategy.model.io.RuleCheckResult;
import com.dasi.domain.strategy.model.type.RuleCheckOutcome;
import com.dasi.domain.strategy.service.tree.IStrategyTree;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RuleConfig(ruleModel = RuleModel.RULE_LUCK)
public class RuleLuckTree implements IStrategyTree {

    @Override
    public RuleCheckResult logic(String userId, Long strategyId, Long awardId, String ruleValue) {
        Long luckAwardId = Long.valueOf(ruleValue);
        log.info("【检查】RULE_LUCK 拦截：awardId={}", luckAwardId);
        return RuleCheckResult.builder()
                .awardId(luckAwardId)
                .ruleCheckOutcome(RuleCheckOutcome.CAPTURE)
                .ruleModel(RuleModel.RULE_LUCK)
                .build();
    }

}
