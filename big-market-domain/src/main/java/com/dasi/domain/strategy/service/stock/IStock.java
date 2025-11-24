package com.dasi.domain.strategy.service.stock;

import com.dasi.domain.strategy.model.dto.StockUpdateRequest;

public interface IStock {

    long subStrategyAwardCount(Long strategyId, Integer awardId);

    StockUpdateRequest getQueueValue() throws InterruptedException;

    void updateStrategyAwardStock(StockUpdateRequest stockUpdateRequest);

}
