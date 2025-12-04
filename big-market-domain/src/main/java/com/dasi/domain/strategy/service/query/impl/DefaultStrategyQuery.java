package com.dasi.domain.strategy.service.query.impl;

import com.dasi.domain.strategy.model.entity.AwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;
import com.dasi.domain.strategy.model.io.ActivityAwardDetail;
import com.dasi.domain.strategy.repository.IStrategyRepository;
import com.dasi.domain.strategy.service.query.IStrategyQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DefaultStrategyQuery implements IStrategyQuery {

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public List<ActivityAwardDetail> queryActivityAward(String userId, Long activityId) {

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
                .map(strategyAwardEntity -> {
                    Long awardId = strategyAwardEntity.getAwardId();
                    String key = String.valueOf(awardId);
                    return ActivityAwardDetail.builder()
                            .awardId(awardId)
                            .awardTitle(strategyAwardEntity.getAwardTitle())
                            .awardRate(strategyAwardEntity.getAwardRate())
                            .awardIndex(strategyAwardEntity.getAwardIndex())
                            .awardName(awardEntityMap.get(key).getAwardName())
                            .awardConfig(awardEntityMap.get(key).getAwardConfig())
                            .awardDesc(awardEntityMap.get(key).getAwardDesc())
                            .limitLotteryCount(limitLotteryCountMap.get(key))
                            .needLotteryCount(needLotteryCountMap.get(key))
                            .isLock(needLotteryCountMap.get(key) > 0)
                            .build();
                })
                .collect(Collectors.toList());
    }

}
