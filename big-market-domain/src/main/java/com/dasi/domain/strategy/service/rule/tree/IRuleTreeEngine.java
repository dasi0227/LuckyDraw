package com.dasi.domain.strategy.service.rule.tree;

import com.dasi.domain.strategy.model.tree.TreeResult;

public interface IRuleTreeEngine {

    TreeResult process(String userId, Long strategyId, Integer awardId);

}
