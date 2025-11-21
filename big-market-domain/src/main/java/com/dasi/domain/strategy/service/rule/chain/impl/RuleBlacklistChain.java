package com.dasi.domain.strategy.service.rule.chain.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.enumeration.RuleModel;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.rule.chain.AbstractRuleChain;
import com.dasi.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;

@Slf4j
@Component
@RuleConfig(ruleModel = RuleModel.RULE_BLACKLIST)
public class RuleBlacklistChain extends AbstractRuleChain {

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public Integer logic(String userId, Long strategyId) {
        // 1. 获取规则值
        String ruleValue = strategyRepository.queryStrategyRuleValue(strategyId, myRuleModel());
        if (StringUtils.isBlank(ruleValue)) {
            return next().logic(userId, strategyId);
        }

        // 2. 解析得到固定奖品和对应的黑名单列表
        String[] values = ruleValue.split(Constants.COLON);
        Integer awardId = Integer.valueOf(values[0]);
        String[] blackIds = values[1].split(Constants.COMMA);

        // 3. 判断是否位于黑名单之中
        if (Arrays.asList(blackIds).contains(userId)) {
            log.info("【抽奖责任链 - rule_weight】接管：黑名单用户={}", userId);
            return awardId;
        }

        // 4. 放行走下一条规则
        log.info("【抽奖责任链 - rule_blacklist】放行");
        return next().logic(userId, strategyId);
    }

    @Override
    protected String myRuleModel() {
        return RuleModel.RULE_BLACKLIST.getName();
    }

}
