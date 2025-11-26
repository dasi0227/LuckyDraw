package com.dasi.domain.strategy.service.rule.tree;

import com.dasi.domain.strategy.model.dto.RuleCheckResult;

public interface IRuleTreeEngine {

    RuleCheckResult process(String userId, Long strategyId, Integer awardId);

}
