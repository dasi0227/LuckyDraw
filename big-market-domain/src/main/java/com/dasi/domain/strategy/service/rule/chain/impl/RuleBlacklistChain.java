package com.dasi.domain.strategy.service.rule.chain.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.check.RuleCheckModel;
import com.dasi.domain.strategy.model.check.RuleCheckResponse;
import com.dasi.domain.strategy.model.check.RuleCheckResult;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;

@Slf4j
@Component
@RuleConfig(ruleModel = RuleCheckModel.RULE_BLACKLIST)
public class RuleBlacklistChain extends AbstractRuleChain {

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public RuleCheckResponse logic(String userId, Long strategyId) {
        // 1. 获取规则值
        String ruleValue = strategyRepository.queryStrategyRuleValue(strategyId, RuleCheckModel.RULE_BLACKLIST.getName());
        if (StringUtils.isBlank(ruleValue)) {
            return next().logic(userId, strategyId);
        }

        // 2. 解析得到固定奖品和对应的黑名单列表
        String[] values = ruleValue.split(Constants.COLON);
        Integer awardId = Integer.valueOf(values[0]);
        String[] blackIds = values[1].split(Constants.COMMA);

        // 3. 判断是否位于黑名单之中
        if (Arrays.asList(blackIds).contains(userId)) {
            log.info("【责任链 - rule_weight】接管：黑名单用户={}", userId);
            return RuleCheckResponse.builder()
                    .awardId(awardId)
                    .ruleCheckModel(RuleCheckModel.RULE_BLACKLIST)
                    .ruleCheckResult(RuleCheckResult.CAPTURE)
                    .build();
        }

        // 4. 放行走下一条规则
        log.info("【责任链 - rule_blacklist】放行");
        return next().logic(userId, strategyId);
    }

}
