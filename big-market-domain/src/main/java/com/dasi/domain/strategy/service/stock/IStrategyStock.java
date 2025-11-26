package com.dasi.domain.strategy.service.stock;

import com.dasi.domain.strategy.model.dto.StrategyAwardStock;

public interface IStrategyStock {

    long subStrategyAwardCount(Long strategyId, Integer awardId);

    StrategyAwardStock getQueueValue() throws InterruptedException;

    void updateStrategyAwardStock(StrategyAwardStock stockUpdateRequest);

}
