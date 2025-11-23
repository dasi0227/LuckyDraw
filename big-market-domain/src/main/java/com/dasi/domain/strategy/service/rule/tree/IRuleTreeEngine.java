package com.dasi.domain.strategy.service.rule.tree;

import com.dasi.domain.strategy.model.check.RuleCheckResponse;

public interface IRuleTreeEngine {

    RuleCheckResponse process(String userId, Long strategyId, Integer awardId);

}
