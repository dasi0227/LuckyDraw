package com.dasi.domain.strategy.service.rule.tree;


import com.dasi.domain.strategy.model.check.RuleCheckResponse;

public interface IRuleTree {

    RuleCheckResponse logic(String userId, Long strategyId, Integer awardId);

}
