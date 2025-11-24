package com.dasi.domain.strategy.service.stock;

import com.dasi.domain.strategy.model.message.StockUpdateMessage;

public interface IStock {

    long subStrategyAwardCount(Long strategyId, Integer awardId);

    StockUpdateMessage getQueueValue() throws InterruptedException;

    void updateStrategyAwardStock(StockUpdateMessage stockUpdateRequest);

}
