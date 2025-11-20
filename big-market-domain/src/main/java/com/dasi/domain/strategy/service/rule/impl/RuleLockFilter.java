package com.dasi.domain.strategy.service.rule.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.entity.RuleContextEntity;
import com.dasi.domain.strategy.model.entity.RuleResultEntity;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.rule.IRuleFilter;
import com.dasi.domain.strategy.service.rule.factory.RuleFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@RuleConfig(ruleModel = RuleFactory.RuleModel.RULE_LOCK)
public class RuleLockFilter implements IRuleFilter<RuleResultEntity.RuleDuringEntity> {

    @Resource
    private IStrategyRepository repository;

    private Long userRaffleCount = 0L;

    @Override
    public RuleResultEntity<RuleResultEntity.RuleDuringEntity> filter(RuleContextEntity ruleContextEntity) {
        // 1. 找到规则对应的值
        String ruleValue = repository.queryStrategyRuleValue(ruleContextEntity.getStrategyId(), ruleContextEntity.getAwardId(), ruleContextEntity.getRuleModel());
        if (StringUtils.isBlank(ruleValue)) {
            return RuleResultEntity.allow();
        }
        Long raffleCount = Long.parseLong(ruleValue);

        // 2. 判断用户的抽奖次数
        if (userRaffleCount >= raffleCount) {
            return RuleResultEntity.allow();
        }

        // 3. 命中，接管
        return RuleResultEntity.takeOver(RuleFactory.RuleModel.RULE_LOCK.getName());
    }
}
