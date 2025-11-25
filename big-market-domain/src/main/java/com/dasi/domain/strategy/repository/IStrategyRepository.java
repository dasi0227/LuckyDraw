package com.dasi.domain.strategy.repository;


import com.dasi.domain.strategy.model.message.StockUpdateMessage;
import com.dasi.domain.strategy.model.entity.AwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyEntity;
import com.dasi.domain.strategy.model.entity.StrategyRuleEntity;
import com.dasi.domain.strategy.model.tree.RuleTreeVO;

import java.util.List;
import java.util.Map;

public interface IStrategyRepository {
    List<StrategyAwardEntity> queryStrategyAwardListByStrategyId(Long strategyId);

    void cacheStrategyAwardRate(String cacheKey, Integer rateRange, Map<String, String> strategyAwardMap);

    int getRateRange(String cacheKey);

    Integer getStrategyAwardAssemble(String cacheKey, int randomNum);

    StrategyEntity queryStrategyEntityByStrategyId(Long strategyId);

    StrategyRuleEntity queryStrategyRuleByStrategyIDAndRuleModel(Long strategyId, String ruleModel);

    String queryStrategyRuleValue(Long strategyId, String ruleModel);

    AwardEntity queryAwardEntityByAwardId(Integer awardId);

    RuleTreeVO queryRuleTreeVOByTreeId(String treeId);

    String queryStrategyAwardTreeIdByStrategyIdAndAwardId(Long strategyId, Integer awardId);

    void cacheStrategyAwardStock(String cacheKey, Integer stock);

    long subStrategyAwardStock(String cacheKey);

    void sendStockConsumeToQueue(StockUpdateMessage stockUpdateMessage);

    StockUpdateMessage getQueueValue();

    void updateStrategyAwardStock(Long strategyId, Integer awardId);
}
