package com.dasi.domain.strategy.service.rule.chain;


// 过滤器的执行责任链
public interface IRuleChain {

    Integer logic(String userId, Long strategyId);

    // 获取链的下一个节点
    IRuleChain next();

    // 将节点组合成链
    IRuleChain appendNext(IRuleChain next);

}
