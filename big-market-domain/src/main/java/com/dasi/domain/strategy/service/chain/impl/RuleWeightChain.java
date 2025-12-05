package com.dasi.domain.strategy.service.chain.impl;

import com.dasi.domain.strategy.annotation.RuleConfig;
import com.dasi.domain.strategy.model.io.RuleCheckResult;
import com.dasi.domain.strategy.model.type.RuleCheckOutcome;
import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.lottery.IStrategyLottery;
import com.dasi.types.constant.Delimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Component
@RuleConfig(ruleModel = RuleModel.RULE_WEIGHT)
public class RuleWeightChain extends AbstractStrategyChain {

    @Resource
    private IStrategyRepository strategyRepository;

    @Resource
    private IStrategyLottery strategyLottery;


    @Override
    public RuleCheckResult logic(String userId, Long strategyId) {
        // 1. 获取规则值
        String ruleValue = strategyRepository.queryStrategyRuleValue(strategyId, RuleModel.RULE_WEIGHT.name());
        if (StringUtils.isBlank(ruleValue)) {
            return next().logic(userId, strategyId);
        }

        // 2. 解析得到积分阈值和对应的奖品列表
        Map<Integer, String> weightMap = new HashMap<>();
        for (String group : ruleValue.split(Delimiter.SPACE)) {
            if (StringUtils.isBlank(group)) continue;
            String[] parts = group.split(Delimiter.COLON);
            if (parts.length != 2) throw new IllegalArgumentException("权重规则格式非法：" + group);
            weightMap.put(Integer.parseInt(parts[0]), parts[1]);
        }
        if (weightMap.isEmpty()) {
            return next().logic(userId, strategyId);
        }

        // 3. 按积分阈值从高到低排序
        List<Integer> thresholds = new ArrayList<>(weightMap.keySet());
        thresholds.sort(Comparator.reverseOrder());

        // 4. 根据用户积分判断能够到达的积分阈值
        int userScore = strategyRepository.queryUserScoreByStrategyId(userId, strategyId);
        Integer matchedThreshold = thresholds.stream()
                .filter(key -> userScore >= key)
                .findFirst()
                .orElse(null);

        // 5. 如果匹配上积分阈值，则在当前积分阈值下抽奖
        if (matchedThreshold != null) {
            Long awardId = strategyLottery.getLotteryAward(strategyId, String.valueOf(matchedThreshold));
            log.info("【检查】RULE_WEIGHT 拦截：userScore={}, weight={}，awardId={}", userScore, matchedThreshold, awardId);
            return RuleCheckResult.builder()
                    .awardId(awardId)
                    .ruleModel(RuleModel.RULE_WEIGHT)
                    .ruleCheckOutcome(RuleCheckOutcome.CAPTURE)
                    .build();
        }

        // 6. 放行走下一条规则
        log.info("【检查】RULE_WEIGHT 放行：userScore={}", userScore);
        return next().logic(userId, strategyId);
    }

}
