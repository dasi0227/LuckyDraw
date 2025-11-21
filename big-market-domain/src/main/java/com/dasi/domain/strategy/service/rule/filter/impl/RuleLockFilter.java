package com.dasi.domain.strategy.service.rule.filter.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.io.FilterRequest;
import com.dasi.domain.strategy.model.io.FilterResponse;
import com.dasi.domain.strategy.model.enumeration.RuleModel;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.rule.filter.IRuleFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Slf4j
@Component
@RuleConfig(ruleModel = RuleModel.RULE_LOCK)
public class RuleLockFilter implements IRuleFilter<FilterResponse.FilterDuringEntity> {

    @Resource
    private IStrategyRepository repository;

    @SuppressWarnings("all")
    private Long userRaffleCount = 0L;

    @Override
    public FilterResponse<FilterResponse.FilterDuringEntity> filter(FilterRequest filterRequest) {
        log.info("【抽奖过滤器 - rule_lock】context = {}", filterRequest);

        // 1. 找到规则对应的值
        String ruleValue = repository.queryStrategyRuleValue(filterRequest.getStrategyId(), filterRequest.getAwardId(), filterRequest.getRuleModel());
        if (StringUtils.isBlank(ruleValue)) {
            return FilterResponse.allow();
        }
        Long raffleCount = Long.parseLong(ruleValue);

        // 2. 判断用户的抽奖次数
        if (userRaffleCount >= raffleCount) {
            return FilterResponse.allow();
        }

        // 3. 命中，接管
        return FilterResponse.takeOver(RuleModel.RULE_LOCK.getName());
    }
}
