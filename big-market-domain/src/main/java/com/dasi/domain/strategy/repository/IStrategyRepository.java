package com.dasi.domain.strategy.repository;


import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;

import java.util.List;
import java.util.Map;

public interface IStrategyRepository {
    List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId);

    void storeStrategyAwardRate(Long strategyId, Integer rateRange, Map<String, String> strategyAwardMap);

    int getRateRange(Long strategyId);

    Integer getStrategyAwardAssemble(Long strategyId, int randomNum);
}
