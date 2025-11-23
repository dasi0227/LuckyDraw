package com.dasi.domain.strategy.service.rule.chain.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.check.RuleCheckModel;
import com.dasi.domain.strategy.model.check.RuleCheckResponse;
import com.dasi.domain.strategy.model.check.RuleCheckResult;
import com.dasi.domain.strategy.service.lottery.ILottery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@RuleConfig(ruleModel = RuleCheckModel.RULE_DEFAULT)
public class RuleDefaultChain extends AbstractRuleChain {

    @Resource
    private ILottery lottery;

    @Override
    public RuleCheckResponse logic(String userId, Long strategyId) {
        Integer awardId = lottery.doLottery(strategyId);
        log.info("【责任链 - rule_default】接管：awardId={}", awardId);
        return RuleCheckResponse.builder()
                .awardId(awardId)
                .ruleCheckModel(RuleCheckModel.RULE_DEFAULT)
                .ruleCheckResult(RuleCheckResult.PERMIT)
                .build();
    }

}
