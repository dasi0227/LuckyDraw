package com.dasi.domain.strategy.service.chain;

import com.dasi.domain.strategy.model.io.RuleCheckResult;

// 过滤器的执行责任链
public interface IStrategyChain {

    // 执行当前责任链节点
    RuleCheckResult logic(String userId, Long strategyId);

    // 获取链的下一个节点
    IStrategyChain next();

    // 将节点组合成链
    IStrategyChain appendNext(IStrategyChain next);

    // 克隆当前节点
    IStrategyChain clone();

}
