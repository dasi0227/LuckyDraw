package com.dasi.domain.strategy.service.tree;


import com.dasi.domain.strategy.model.io.RuleCheckResult;

public interface IStrategyTree {

    RuleCheckResult logic(String userId, Long strategyId, Long awardId, String ruleValue);

}
