package com.dasi.domain.strategy.service.rule.tree.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.domain.strategy.model.dto.RuleCheckResult;
import com.dasi.domain.strategy.model.type.RuleCheckOutcome;
import com.dasi.domain.strategy.service.rule.tree.IRuleTree;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RuleConfig(ruleModel = RuleModel.RULE_LUCK)
public class RuleLuckTree implements IRuleTree {

    @Override
    public RuleCheckResult logic(String userId, Long strategyId, Long awardId, String ruleValue) {
        Long luckAwardId = Long.valueOf(ruleValue);
        log.info("【策略规则树】rule_luck 接管：awardId={}", luckAwardId);
        return RuleCheckResult.builder()
                .awardId(luckAwardId)
                .ruleCheckOutcome(RuleCheckOutcome.CAPTURE)
                .ruleModel(RuleModel.RULE_LUCK)
                .build();
    }

}
