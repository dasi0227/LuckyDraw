package com.dasi.domain.strategy.service.rule.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.entity.RuleContextEntity;
import com.dasi.domain.strategy.model.entity.RuleResultEntity;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.rule.IRuleFilter;
import com.dasi.domain.strategy.service.rule.factory.RuleFactory;
import com.dasi.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Component
@RuleConfig(ruleModel = RuleFactory.RuleModel.RULE_WEIGHT)
public class RuleWeightFilter implements IRuleFilter<RuleResultEntity.RuleBeforeEntity> {

    @Resource
    private IStrategyRepository repository;

    // 目前写死用户积分
    public Long userScore = 4500L;

    // 示例：4000:101,102,103 5000:101,102,103,104 6000:101,102,103,104,105
    @Override
    public RuleResultEntity<RuleResultEntity.RuleBeforeEntity> filter(RuleContextEntity ruleContextEntity) {
        // 1. 找到规则对应的值
        String ruleValue = repository.queryStrategyRuleValue(ruleContextEntity.getStrategyId(), ruleContextEntity.getAwardId(), ruleContextEntity.getRuleModel());
        if (StringUtils.isBlank(ruleValue)) {
            return RuleResultEntity.allow();
        }

        // 2. 解析得到积分阈值和对应的奖品列表
        Map<Long, String> weightMap = new HashMap<>();
        for (String group : ruleValue.split(Constants.SPACE)) {
            if (StringUtils.isBlank(group)) continue;
            String[] parts = group.split(Constants.COLON);
            if (parts.length != 2) throw new IllegalArgumentException("权重规则格式非法：" + group);
            weightMap.put(Long.parseLong(parts[0]), parts[1]);
        }
        if (weightMap.isEmpty()) {
            return RuleResultEntity.allow();
        }

        // 3. 按积分阈值从高到低排序
        List<Long> thresholds = new ArrayList<>(weightMap.keySet());
        thresholds.sort(Comparator.reverseOrder());

        // 4. 根据用户积分判断能够到达的积分阈值
        Long matchedThreshold = thresholds.stream()
                .filter(key -> userScore >= key)
                .findFirst()
                .orElse(null);
        if (matchedThreshold == null) {
            return RuleResultEntity.allow();
        }

        // 5. 得到最终匹配的积分
        RuleResultEntity.RuleBeforeEntity result = RuleResultEntity.RuleBeforeEntity.builder()
                .strategyId(ruleContextEntity.getStrategyId())
                .ruleWeight(String.valueOf(matchedThreshold))
                .build();
        return RuleResultEntity.takeOver(RuleFactory.RuleModel.RULE_WEIGHT.getName(), result);
    }
}
