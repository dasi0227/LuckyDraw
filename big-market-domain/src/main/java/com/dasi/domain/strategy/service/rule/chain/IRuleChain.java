package com.dasi.domain.strategy.service.rule.chain;

import com.dasi.domain.strategy.model.dto.RuleCheckResult;

// 过滤器的执行责任链
public interface IRuleChain {

    // 执行当前责任链节点
    RuleCheckResult logic(String userId, Long strategyId);

    // 获取链的下一个节点
    IRuleChain next();

    // 将节点组合成链
    IRuleChain appendNext(IRuleChain next);

}
