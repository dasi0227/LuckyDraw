package com.dasi.domain.strategy.service.query;

import com.dasi.domain.strategy.model.entity.AwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;
import com.dasi.domain.strategy.model.io.QueryActivityAwardContext;
import com.dasi.domain.strategy.model.io.QueryActivityAwardResult;
import com.dasi.domain.strategy.model.io.QueryActivityLuckContext;
import com.dasi.domain.strategy.model.io.QueryActivityLuckResult;
import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StrategyQuery implements IStrategyQuery {

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public List<QueryActivityAwardResult> queryActivityAward(QueryActivityAwardContext queryActivityAwardContext) {

        String userId = queryActivityAwardContext.getUserId();
        Long activityId = queryActivityAwardContext.getActivityId();

        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (activityId == null) throw new AppException("缺少参数 activityId");

        // 1. 先拿到当前活动对应的策略的所有奖品
        List<StrategyAwardEntity> strategyAwardEntityList = strategyRepository.queryStrategyAwardListByActivityId(activityId);

        // 2. 查询奖品的详细信息
        Map<String, AwardEntity> awardEntityMap = strategyRepository.queryAwardMapByActivityId(strategyAwardEntityList, activityId);

        // 3. 查询策略奖品的详细信息
        Map<String, Integer> limitLotteryCountMap = strategyRepository.queryRuleLockLimitMapByActivityId(strategyAwardEntityList, activityId);

        // 4. 查询用户的详细信息
        Integer userLotteryCount = strategyRepository.queryUserLotteryCountByActivityId(userId, activityId);
        Map<String, Integer> needLotteryCountMap = limitLotteryCountMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> Math.max(entry.getValue() - userLotteryCount, 0)));

        return strategyAwardEntityList.stream()
                .sorted(Comparator.comparingInt(StrategyAwardEntity::getAwardIndex))
                .map(strategyAwardEntity -> {
                    Long awardId = strategyAwardEntity.getAwardId();
                    String key = String.valueOf(awardId);
                    return QueryActivityAwardResult.builder()
                            .awardId(awardId)
                            .awardRate(strategyAwardEntity.getAwardRate())
                            .awardIndex(strategyAwardEntity.getAwardIndex())
                            .awardName(awardEntityMap.get(key).getAwardName())
                            .needLotteryCount(needLotteryCountMap.get(key))
                            .isLock(needLotteryCountMap.get(key) > 0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public QueryActivityLuckResult queryActivityLuck(QueryActivityLuckContext queryActivityLuckContext) {

        String userId = queryActivityLuckContext.getUserId();
        Long activityId = queryActivityLuckContext.getActivityId();
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (activityId == null) throw new AppException("缺少参数 activityId");

        Long strategyId = strategyRepository.queryStrategyIdByActivityId(activityId);
        int accountLuck = strategyRepository.queryActivityAccountLuck(userId, activityId);
        String ruleValue = strategyRepository.queryStrategyRuleValue(strategyId, RuleModel.RULE_LUCK.name());
        List<StrategyAwardEntity> strategyAwardEntityList = strategyRepository.queryStrategyAwardListByActivityId(activityId);
        Map<String, AwardEntity> awardEntityMap = strategyRepository.queryAwardMapByActivityId(strategyAwardEntityList, activityId);

        Map<String, List<String>> luckThreshold = new HashMap<>();
        for (String group : ruleValue.split(Delimiter.SPACE)) {
            String[] parts = group.split(Delimiter.COLON);
            if (parts.length != 2) throw new IllegalArgumentException("幸运值规则格式非法：" + group);
            String luck = parts[0];
            String[] awardIds = parts[1].split(Delimiter.COMMA);
            List<String> awardNames = Arrays.stream(awardIds)
                    .map(awardId -> awardEntityMap.get(awardId).getAwardName())
                    .collect(Collectors.toList());
            luckThreshold.put(luck, awardNames);
        }

        return QueryActivityLuckResult.builder()
                .accountLuck(accountLuck)
                .luckThreshold(luckThreshold)
                .build();
    }

}
