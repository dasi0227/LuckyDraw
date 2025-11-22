package com.dasi.domain.strategy.service.rule.tree.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.enumeration.RuleCheckResult;
import com.dasi.domain.strategy.model.enumeration.RuleModel;
import com.dasi.domain.strategy.model.tree.TreeResult;
import com.dasi.domain.strategy.service.rule.tree.IRuleTree;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RuleConfig(ruleModel = RuleModel.RULE_STOCK)
public class RuleStockTree implements IRuleTree {

    @Override
    public TreeResult logic(String userId, Long strategyId, Integer awardId) {
        return TreeResult.builder()
                .ruleCheckResult(RuleCheckResult.CAPTURE)
                .build();
    }

}
