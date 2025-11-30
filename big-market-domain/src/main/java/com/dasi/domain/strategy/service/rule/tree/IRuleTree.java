package com.dasi.domain.strategy.service.rule.tree;


import com.dasi.domain.strategy.model.dto.RuleCheckResult;

public interface IRuleTree {

    RuleCheckResult logic(String userId, Long strategyId, Long awardId, String ruleValue);

}
