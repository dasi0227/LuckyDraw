package com.dasi.domain.strategy.service.rule.tree.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.check.RuleCheckResponse;
import com.dasi.domain.strategy.model.check.RuleCheckResult;
import com.dasi.domain.strategy.model.check.RuleCheckModel;
import com.dasi.domain.strategy.service.rule.tree.IRuleTree;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RuleConfig(ruleModel = RuleCheckModel.RULE_LOCK)
public class RuleLockTree implements IRuleTree {

    private Long userRaffleCount = 10L;

    @Override
    public RuleCheckResponse logic(String userId, Long strategyId, Integer awardId, String ruleValue) {
        long limitRaffleCount = Long.parseLong(ruleValue);

        if (userRaffleCount >= limitRaffleCount) {
            log.info("【规则树 - rule_lock】放行：userRaffleCount = {}, limitRaffleCount = {}", userRaffleCount, limitRaffleCount);
            return RuleCheckResponse.builder()
                    .awardId(awardId)
                    .ruleCheckResult(RuleCheckResult.PERMIT)
                    .ruleCheckModel(RuleCheckModel.RULE_LOCK)
                    .build();
        } else {
            log.info("【规则树 - rule_lock】接管：userRaffleCount = {}, limitRaffleCount = {}", userRaffleCount, limitRaffleCount);
            return RuleCheckResponse.builder()
                    .awardId(null)
                    .ruleCheckResult(RuleCheckResult.CAPTURE)
                    .ruleCheckModel(RuleCheckModel.RULE_LOCK)
                    .build();
        }
    }

}
