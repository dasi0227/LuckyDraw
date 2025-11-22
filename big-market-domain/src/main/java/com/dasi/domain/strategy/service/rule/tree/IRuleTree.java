package com.dasi.domain.strategy.service.rule.tree;


import com.dasi.domain.strategy.model.tree.TreeResult;

public interface IRuleTree {

    TreeResult logic(String userId, Long strategyId, Integer awardId);

}
