package com.dasi.domain.strategy.service.rule.chain.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.domain.strategy.model.dto.RuleCheckResult;
import com.dasi.domain.strategy.model.type.RuleCheckOutcome;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.types.constant.Delimiter;
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
    public RuleCheckResult logic(String userId, Long strategyId) {
        // 1. 获取规则值
        String ruleValue = strategyRepository.queryStrategyRuleValue(strategyId, RuleModel.RULE_BLACKLIST.getCode());
        if (StringUtils.isBlank(ruleValue)) {
            return next().logic(userId, strategyId);
        }

        // 2. 解析得到固定奖品和对应的黑名单列表
        String[] values = ruleValue.split(Delimiter.COLON);
        Long awardId = Long.valueOf(values[0]);
        String[] blackIds = values[1].split(Delimiter.COMMA);

        // 3. 判断是否位于黑名单之中
        if (Arrays.asList(blackIds).contains(userId)) {
            log.info("【策略责任链】rule_blacklist 接管：userId={}, awardId={}", userId, awardId);
            return RuleCheckResult.builder()
                    .awardId(awardId)
                    .ruleModel(RuleModel.RULE_BLACKLIST)
                    .ruleCheckOutcome(RuleCheckOutcome.CAPTURE)
                    .build();
        }

        // 4. 放行走下一条规则
        log.info("【策略责任链】rule_blacklist 放行");
        return next().logic(userId, strategyId);
    }

}
