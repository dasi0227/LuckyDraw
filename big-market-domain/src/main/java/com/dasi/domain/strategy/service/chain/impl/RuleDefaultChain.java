package com.dasi.domain.strategy.service.chain.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.io.RuleCheckResult;
import com.dasi.domain.strategy.model.type.RuleCheckOutcome;
import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.domain.strategy.service.lottery.IStrategyLottery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@RuleConfig(ruleModel = RuleModel.RULE_DEFAULT)
public class RuleDefaultChain extends AbstractStrategyChain {

    @Resource
    private IStrategyLottery strategyLottery;

    @Override
    public RuleCheckResult logic(String userId, Long strategyId) {
        Long awardId = strategyLottery.getLotteryAward(strategyId);
        log.info("【检查】RULE_DEFAULT 拦截：awardId={}", awardId);
        return RuleCheckResult.builder()
                .awardId(awardId)
                .ruleModel(RuleModel.RULE_DEFAULT)
                .ruleCheckOutcome(RuleCheckOutcome.PERMIT)
                .build();
    }

}
