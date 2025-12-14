package com.dasi.domain.strategy.service.chain.impl;

import com.dasi.domain.strategy.annotation.RuleModelConfig;
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
import java.util.stream.Collectors;

@Slf4j
@Component
@RuleModelConfig(ruleModel = RuleModel.RULE_LUCK)
public class RuleLuckChain extends AbstractStrategyChain {

    @Resource
    private IStrategyRepository strategyRepository;

    @Resource
    private IStrategyLottery strategyLottery;

    @Override
    public RuleCheckResult logic(String userId, Long strategyId) {
        // 1. 获取规则值
        String ruleValue = strategyRepository.queryStrategyRuleValue(strategyId, RuleModel.RULE_LUCK.name());
        if (StringUtils.isBlank(ruleValue)) {
            return next().logic(userId, strategyId);
        }

        // 2. 解析得到幸运值阈值和对应的奖品列表
        Map<Integer, String> luckMap = new HashMap<>();
        for (String group : ruleValue.split(Delimiter.SPACE)) {
            if (StringUtils.isBlank(group)) continue;
            String[] parts = group.split(Delimiter.COLON);
            if (parts.length != 2) throw new IllegalArgumentException("幸运值规则格式非法：" + group);
            luckMap.put(Integer.parseInt(parts[0]), parts[1]);
        }
        if (luckMap.isEmpty()) {
            return next().logic(userId, strategyId);
        }

        // 3. 按幸运值阈值从高到低排序
        List<Integer> thresholds = new ArrayList<>(luckMap.keySet());
        thresholds.sort(Comparator.reverseOrder());

        // 4. 获取用户幸运值
        Long activityId = strategyRepository.queryActivityIdByStrategyId(strategyId);
        int accountLuck = strategyRepository.queryActivityAccountLuck(userId, activityId);
        Integer matchedThreshold = thresholds.stream()
                .filter(key -> accountLuck >= key)
                .findFirst()
                .orElse(null);

        // 5. 如果匹配上幸运值阈值，则在当前幸运值阈值下抽奖
        if (matchedThreshold != null) {

            // 5.1 查看是第几个档位
            thresholds.sort(Comparator.naturalOrder());
            int retryCount = thresholds.indexOf(matchedThreshold);

            // 5.2 解析奖品列表，拿到前两个奖品
            String awardIdsStr = luckMap.get(matchedThreshold);
            Set<Long> firstTwo = Arrays.stream(awardIdsStr.split(Delimiter.COMMA))
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .map(Long::parseLong)
                    .limit(2)
                    .collect(Collectors.toSet());

            // 5.3 根据幸运值抽若干次
            Long awardId = strategyLottery.getLotteryAward(strategyId, String.valueOf(matchedThreshold));
            for (int used = 0; used < retryCount && firstTwo.contains(awardId); used++) {
                awardId = strategyLottery.getLotteryAward(strategyId, String.valueOf(matchedThreshold));
            }

            // 5.4 返回结果
            log.info("【抽奖】RULE_LUCK 拦截：accountLuck={}, luckThreshold={}，awardId={}", accountLuck, matchedThreshold, awardId);
            return RuleCheckResult.builder()
                    .awardId(awardId)
                    .ruleModel(RuleModel.RULE_LUCK)
                    .ruleCheckOutcome(RuleCheckOutcome.CAPTURE)
                    .build();
        }

        // 6. 放行走下一条规则
        log.info("【抽奖】RULE_LUCK 放行：accountLuck={}", accountLuck);
        return next().logic(userId, strategyId);
    }

}
