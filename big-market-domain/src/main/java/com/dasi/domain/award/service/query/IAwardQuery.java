package com.dasi.domain.award.service.query;

import com.dasi.domain.strategy.model.entity.AwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;

import java.util.List;
import java.util.Map;

public interface IAwardQuery {

    List<StrategyAwardEntity> queryStrategyAwardListByActivityId(Long activityId);

    Map<String, AwardEntity> queryAwardMapByActivityId(List<StrategyAwardEntity> strategyAwardEntityList, Long activityId);

    Map<String, Integer> queryRuleNodeLockCountMapByActivityId(List<StrategyAwardEntity> strategyAwardEntityList, Long activityId);

    Integer queryUserLotteryCount(String userId, Long activityId);

}
