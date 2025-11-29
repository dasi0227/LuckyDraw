package com.dasi.domain.strategy.service.rule.tree.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.type.RuleCheckOutcome;
import com.dasi.domain.strategy.model.dto.RuleCheckResult;
import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.rule.tree.IRuleTree;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@RuleConfig(ruleModel = RuleModel.RULE_LOCK)
public class RuleLockTree implements IRuleTree {

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public RuleCheckResult logic(String userId, Long strategyId, Integer awardId, String ruleValue) {
        long limitLotteryCount = Long.parseLong(ruleValue);
        long userLotteryCount = strategyRepository.queryUserLotteryCount(userId, strategyId);
        if (userLotteryCount >= limitLotteryCount) {
            log.info("【策略规则树 - rule_lock】放行：userLotteryCount = {}, limitLotteryCount = {}", userLotteryCount, limitLotteryCount);
            return RuleCheckResult.builder()
                    .awardId(awardId)
                    .ruleCheckOutcome(RuleCheckOutcome.PERMIT)
                    .ruleModel(RuleModel.RULE_LOCK)
                    .build();
        } else {
            log.info("【策略规则树 - rule_lock】接管：userLotteryCount = {}, limitLotteryCount = {}", userLotteryCount, limitLotteryCount);
            return RuleCheckResult.builder()
                    .awardId(null)
                    .ruleCheckOutcome(RuleCheckOutcome.CAPTURE)
                    .ruleModel(RuleModel.RULE_LOCK)
                    .build();
        }
    }

}
