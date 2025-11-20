package com.dasi.domain.strategy.repository;


import com.dasi.domain.strategy.model.entity.AwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;
import com.dasi.domain.strategy.model.entity.StrategyEntity;
import com.dasi.domain.strategy.model.entity.StrategyRuleEntity;
import com.dasi.domain.strategy.model.vo.RuleModelsVO;

import java.util.List;
import java.util.Map;

public interface IStrategyRepository {
    List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId);

    void storeStrategyAwardRate(String key, Integer rateRange, Map<String, String> strategyAwardMap);

    int getRateRange(String key);

    Integer getStrategyAwardAssemble(String key, int randomNum);

    StrategyEntity queryStrategyEntityByStrategyId(Long strategyId);

    StrategyRuleEntity queryStrategyRuleByRuleModel(Long strategyId, String ruleModel);

    String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel);

    AwardEntity queryAwardEntityByAwardId(Integer awardId);

    RuleModelsVO queryStrategyAwardRuleModels(Long strategyId, Integer awardId);

    RuleModelsVO queryStrategyRuleModels(Long strategyId);
}
