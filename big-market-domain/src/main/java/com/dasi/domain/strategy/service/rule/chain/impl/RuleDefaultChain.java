package com.dasi.domain.strategy.service.rule.chain.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.rule.RuleCheckOutcome;
import com.dasi.domain.strategy.model.rule.RuleModel;
import com.dasi.domain.strategy.model.dto.RuleCheckResult;
import com.dasi.domain.strategy.service.lottery.ILottery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@RuleConfig(ruleModel = RuleModel.RULE_DEFAULT)
public class RuleDefaultChain extends AbstractRuleChain {

    @Resource
    private ILottery lottery;

    @Override
    public RuleCheckResult logic(String userId, Long strategyId) {
        Integer awardId = lottery.doLottery(strategyId);
        log.info("【责任链 - rule_default】接管：awardId={}", awardId);
        return RuleCheckResult.builder()
                .awardId(awardId)
                .ruleModel(RuleModel.RULE_DEFAULT)
                .ruleCheckOutcome(RuleCheckOutcome.PERMIT)
                .build();
    }

}
