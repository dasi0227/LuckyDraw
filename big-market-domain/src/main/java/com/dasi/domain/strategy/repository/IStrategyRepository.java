package com.dasi.domain.strategy.repository;


import com.dasi.domain.strategy.model.entity.*;
import com.dasi.domain.strategy.model.vo.RuleTreeVO;

import java.util.List;
import java.util.Map;

public interface IStrategyRepository {

    List<StrategyAwardEntity> queryStrategyAwardListByStrategyId(Long strategyId);

    Long queryStrategyIdByActivityId(Long activityId);

    Long queryActivityIdByStrategyId(Long strategyId);

    int queryUserLotteryCount(String userId, Long strategyId);

    StrategyEntity queryStrategyEntityByStrategyId(Long strategyId);

    StrategyRuleEntity queryStrategyRuleByStrategyIDAndRuleModel(Long strategyId, String ruleModel);

    String queryStrategyRuleValue(Long strategyId, String ruleModel);

    AwardEntity queryAwardByAwardId(Integer awardId);

    RuleTreeVO queryRuleTreeVOByTreeId(String treeId);

    String queryStrategyAwardTreeIdByStrategyIdAndAwardId(Long strategyId, Integer awardId);

    void cacheStrategyAwardStock(String cacheKey, Integer stock);

    void cacheStrategyAwardRate(String cacheKey, Integer rateRange, Map<String, String> strategyAwardMap);

    int getRateRange(String cacheKey);

    Integer getRandomStrategyAward(String cacheKey, int randomNum);

    long subStrategyAwardStock(String cacheKey);

    void sendStrategyAwardStockConsumeToMQ(StrategyAwardStockEntity strategyAwardStockEntity);

    StrategyAwardStockEntity getQueueValue();

    void updateStrategyAwardStock(Long strategyId, Integer awardId);

}
