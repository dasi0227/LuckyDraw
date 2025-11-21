package com.dasi.domain.strategy.service.rule.chain.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.enumeration.RuleModel;
import com.dasi.domain.strategy.service.armory.IStrategyLottery;
import com.dasi.domain.strategy.service.rule.chain.AbstractRuleChain;
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
    public Integer logic(String userId, Long strategyId) {
        Integer awardId = strategyLottery.doLottery(strategyId);
        log.info("【抽奖责任链 - rule_default】接管：awardId={}", awardId);
        return awardId;
    }

    @Override
    protected String myRuleModel() {
        return RuleModel.RULE_DEFAULT.getName();
    }
}
