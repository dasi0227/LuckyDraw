package com.dasi.domain.strategy.repository;


import com.dasi.domain.strategy.model.entity.StrategyAwardStockEntity;
import com.dasi.domain.strategy.model.entity.AwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyEntity;
import com.dasi.domain.strategy.model.entity.StrategyRuleEntity;
import com.dasi.domain.strategy.model.vo.RuleTreeVO;

import java.util.List;
import java.util.Map;

public interface IStrategyRepository {

    List<StrategyAwardEntity> queryStrategyAwardListByStrategyId(Long strategyId);

    void cacheStrategyAwardRate(String cacheKey, Integer rateRange, Map<String, String> strategyAwardMap);

    int getRateRange(String cacheKey);

    Integer getStrategyAwardAssemble(String cacheKey, int randomNum);

    Long queryStrategyIdByActivityId(Long activityId);

    long queryUserLotteryCount(String userId, Long strategyId);

    StrategyEntity queryStrategyEntityByStrategyId(Long strategyId);

    StrategyRuleEntity queryStrategyRuleByStrategyIDAndRuleModel(Long strategyId, String ruleModel);

    String queryStrategyRuleValue(Long strategyId, String ruleModel);

    AwardEntity queryAwardByAwardId(Integer awardId);

    RuleTreeVO queryRuleTreeVOByTreeId(String treeId);

    String queryStrategyAwardTreeIdByStrategyIdAndAwardId(Long strategyId, Integer awardId);

    void cacheStrategyAwardStock(String cacheKey, Integer stock);

    long subStrategyAwardStock(String cacheKey);

    void sendStrategyAwardStockConsumeToMQ(StrategyAwardStockEntity strategyAwardStockEntity);

    StrategyAwardStockEntity getQueueValue();

    void updateStrategyAwardStock(Long strategyId, Integer awardId);

}
