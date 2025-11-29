package com.dasi.domain.strategy.service.stock;

import com.dasi.domain.strategy.model.entity.StrategyAwardStockEntity;

public interface IStrategyStock {

    long subStrategyAwardCount(Long strategyId, Integer awardId);

    StrategyAwardStockEntity getQueueValue() throws InterruptedException;

    void updateStrategyAwardStock(StrategyAwardStockEntity stockUpdateRequest);

}
