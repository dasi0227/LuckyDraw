package com.dasi.domain.strategy.service.lottery.impl;

import com.dasi.domain.strategy.model.dto.RuleCheckContext;
import com.dasi.domain.strategy.model.dto.RuleCheckResult;
import com.dasi.domain.strategy.model.vo.RuleTreeVO;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.rule.chain.IRuleChain;
import com.dasi.domain.strategy.service.rule.chain.RuleChainFactory;
import com.dasi.domain.strategy.service.rule.tree.IRuleTreeEngine;
import com.dasi.domain.strategy.service.rule.tree.RuleTreeFactory;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.SecureRandom;

@Slf4j
@Service
public class DefaultStrategyLottery extends AbstractStrategyLottery {

    @Resource
    private IStrategyRepository strategyRepository;

    @Resource
    @Lazy
    private RuleChainFactory ruleChainFactory;

    @Resource
    private RuleTreeFactory ruleTreeFactory;

    protected DefaultStrategyLottery(IStrategyRepository strategyRepository) {
        super(strategyRepository);
    }

    @Override
    protected RuleCheckResult beforeCheck(RuleCheckContext ruleCheckContext) {
        IRuleChain firstRuleChain = ruleChainFactory.getRuleModelChain(ruleCheckContext.getStrategyId());
        return firstRuleChain.logic(ruleCheckContext.getUserId(), ruleCheckContext.getStrategyId());
    }

    @Override
    protected RuleCheckResult afterCheck(RuleCheckContext ruleCheckContext) {
        String treeId = strategyRepository.queryStrategyAwardTreeIdByStrategyIdAndAwardId(ruleCheckContext.getStrategyId(), ruleCheckContext.getAwardId());
        if (treeId == null || treeId.isEmpty()) {
            return RuleCheckResult.builder()
                    .awardId(ruleCheckContext.getAwardId())
                    .build();
        }

        RuleTreeVO ruleTreeVO = strategyRepository.queryRuleTreeVOByTreeId(treeId);
        if (ruleTreeVO == null) {
            throw new AppException("规则树配置错误");
        }
        IRuleTreeEngine ruleTreeEngine = ruleTreeFactory.getTreeEngine(ruleTreeVO);
        return ruleTreeEngine.process(ruleCheckContext.getUserId(), ruleCheckContext.getStrategyId(), ruleCheckContext.getAwardId());
    }

    @Override
    public Long getLotteryAward(Long strategyId) {
        String key = String.valueOf(strategyId);
        return getLotteryAward(key);
    }

    @Override
    public Long getLotteryAward(Long strategyId, String ruleWeight) {
        String key = String.valueOf(strategyId).concat(Delimiter.UNDERSCORE).concat(ruleWeight);
        return getLotteryAward(key);
    }

    private Long getLotteryAward(String key) {
        // 1. 获取概率长度
        int rateRange = strategyRepository.getRateRange(key);
        // 2. 生成随机数，找到对应的概率奖品
        return strategyRepository.getRandomStrategyAward(key, new SecureRandom().nextInt(rateRange));
    }

}
