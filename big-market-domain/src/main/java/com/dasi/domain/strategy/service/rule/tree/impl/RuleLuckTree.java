package com.dasi.domain.strategy.service.rule.tree.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.check.RuleCheckModel;
import com.dasi.domain.strategy.model.check.RuleCheckResponse;
import com.dasi.domain.strategy.model.check.RuleCheckResult;
import com.dasi.domain.strategy.service.rule.tree.IRuleTree;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RuleConfig(ruleModel = RuleCheckModel.RULE_LUCK)
public class RuleLuckTree implements IRuleTree {

    @Override
    public RuleCheckResponse logic(String userId, Long strategyId, Integer awardId, String ruleValue) {
        Integer luckAwardId = Integer.valueOf(ruleValue);
        log.info("【规则树 - rule_luck】接管：awardId = {}", luckAwardId);
        return RuleCheckResponse.builder()
                .awardId(luckAwardId)
                .ruleCheckResult(RuleCheckResult.CAPTURE)
                .ruleCheckModel(RuleCheckModel.RULE_LUCK)
                .build();
    }

}
