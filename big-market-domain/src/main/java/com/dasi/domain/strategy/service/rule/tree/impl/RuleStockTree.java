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
@RuleConfig(ruleModel = RuleCheckModel.RULE_STOCK)
public class RuleStockTree implements IRuleTree {

    @Override
    public RuleCheckResponse logic(String userId, Long strategyId, Integer awardId) {
        return RuleCheckResponse.builder()
                .ruleCheckResult(RuleCheckResult.CAPTURE)
                .ruleCheckModel(RuleCheckModel.RULE_STOCK)
                .build();
    }

}
