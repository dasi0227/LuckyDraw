package com.dasi.domain.strategy.service.rule.chain.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.dto.RuleCheckResult;
import com.dasi.domain.strategy.model.type.RuleCheckOutcome;
import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.domain.strategy.service.lottery.IStrategyLottery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@RuleConfig(ruleModel = RuleModel.RULE_DEFAULT)
public class RuleDefaultChain extends AbstractRuleChain {

    @Resource
    private IStrategyLottery strategyLottery;

    @Override
    public RuleCheckResult logic(String userId, Long strategyId) {
        Integer awardId = strategyLottery.getLotteryAward(strategyId);
        log.info("【策略责任链 - rule_default】接管：awardId={}", awardId);
        return RuleCheckResult.builder()
                .awardId(awardId)
                .ruleModel(RuleModel.RULE_DEFAULT)
                .ruleCheckOutcome(RuleCheckOutcome.PERMIT)
                .build();
    }

}
